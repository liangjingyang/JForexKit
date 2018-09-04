package com.jforexcn.inbox.strategy;

/**
 * Created by simple on 31/12/16.
 *
 * request by MARCO
 *
 * 1 candle=1 min
 * 2 货币(cInstrument),为可输入变量，默认为EUR/USD
 * 3 交易数量（amount),为可输入变量，默认为10000
 * 4 计算X周期的振幅=高低差，其中X为可输入变量,示例中X=60
 * 5 计算X周期的振幅的平均数+标准差(msX)
 * 6 计算Y周期的振幅的平均数（mY)，其中Y为可输入变量，示例中Y=15
 * 7 最小跨度（minKD),为可输入变量，默认为0.0005
 * 8 case 1: mY<msX 并且 close>上一bar的high，并且最新close-上一bar的close>=minKD
 * 如有反向单，则先平仓反向单，然后开默认amount 空单
 * 如无反向单，则直接以默认amount开新空单
 * 如已经持有同向单，则继续持有，无需开新单。
 * 所有交易都以市价单操作
 * case 2: mY<msX 并且 close<上一bar的low,并且最新close-上一bar的close>=minKD
 * 如有反向单，则先平仓反向单，然后开默认amount 多单
 * 如无反向单，则直接以默认amount开新多单
 * 如已经持有同向单，则继续持有，无需开新单。
 * 所有交易都以市价单操作
 * case 3: mY>=msX 并且 close>上一bar的high，并且最新close-上一bar的close>=minKD
 * 如有反向单，则先平仓反向单，然后开默认amount 多单
 * 如无反向单，则直接以默认amount开新多单
 * 如已经持有同向单，则继续持有，无需开新单。
 * 所有交易都以市价单操作
 * case 4: mY>=msX 并且 close<上一bar的low,并且最新close-上一bar的close>=minKD
 * 如有反向单，则先平仓反向单，然后开默认amount 空单
 * 如无反向单，则直接以默认amount开空单
 * 如已经持有同向单，则继续持有，无需开新单。
 * 所有交易都以市价单操作
 * case 5: 以上皆不是，不作为
 * 9 每次交易后，须把交易时间，预期交易价格，实际成交价格以及账户余额记录在一txt文档中
 * (未实现) 10  每次程序中断/关闭前，须把本次运行参数记录于另一txt文档中,以备继续运行。
 * (未实现) 11  每次运行程序，须读取上次保存的运行参数，如参数缺省，则采用默认参数
 * 12  设置周末自动平所有仓位选项
 * 13  打印1分钟bar和所有参数计算结果, 方便核对
 */

import com.dukascopy.api.Configurable;
import com.dukascopy.api.Filter;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;


import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

public class JForexCN2Macro implements IStrategy {

    /**
     * Created by simple(simple.continue@gmail.com) on 31/12/16.
     */

    @Configurable("Instrument")
    public Instrument cInstrument = Instrument.EURUSD;
    @Configurable("Amount (million)")
    public double cAmount = 0.01;
    @Configurable("Time cPeriod X")
    public int cTimePeriodForX = 60;
    @Configurable("Time cPeriod Y")
    public int cTimePeriodForY = 15;
    @Configurable("Min KD")
    public double cMinKD = 0.0005;
    @Configurable("Hour for closing all positions")
    public int cCloseAllHour = 23;
    @Configurable("Day of week for closing all positions (1-7)")
    public int cCloseAllDayOfWeek = 5;
    @Configurable("Should close all positions at weekend?")
    public boolean cShouldCloseAtWeekend = false;
    @Configurable("Should print test CSV?")
    public boolean cIsPrintTestCSV = false;


    private IEngine engine;
    private IConsole console;
    private IHistory history;
    private IAccount account;

    private HashSet<Instrument> subscribedInstruments = new HashSet<>();
    private List<IBar> barList;
    private IBar lastBar;
    private static String STRATEGY_NAME = "StrategyMacro";

    private SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
    private SimpleDateFormat gtmDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    private PrintWriter testPrintWriter;
    private PrintWriter logPrintWriter;

    private double msX = 0;
    private double mY = 0;

    private double lastCloseAskPriceForLog = 0;
    private double lastCloseBidPriceForLog = 0;
    private HashMap<String, Double> tryOpenPriceHashMap = new HashMap<>();


    @Override
    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.history = context.getHistory();
        this.account = context.getAccount();

        // 验证参数合法性
        boolean argsAssert = (cAmount > 0.000001 && cTimePeriodForX > 0 && cTimePeriodForY > 0 && cMinKD > 0 &&
                (cCloseAllHour >= 0 && cCloseAllHour <= 23) && cTimePeriodForX >= cTimePeriodForY) &&
                (cCloseAllDayOfWeek >= 1 && cCloseAllDayOfWeek <= 7);

        if (!argsAssert) {
            throw new JFException("args error!");
        }

        // 我们周一 ~ 周日是 1~7, 实际系统是 周日 ~ 周六 是1~7, 这里换算一下
        if (cCloseAllDayOfWeek == 7) {
            cCloseAllDayOfWeek = 1;
        } else {
            cCloseAllDayOfWeek = cCloseAllDayOfWeek + 1;
        }

        try {
            // 记录参数
            localDateFormat.setTimeZone(TimeZone.getTimeZone("CTT"));
            logPrintWriter = new PrintWriter(STRATEGY_NAME + "_" + localDateFormat.format(new Date()) + ".csv", "UTF-8");
            StringBuilder headBuilder = new StringBuilder();
            headBuilder.append("cAInstrument");
            headBuilder.append(",");
            headBuilder.append("cAmount");
            headBuilder.append(",");
            headBuilder.append("cBarPeriod");
            headBuilder.append(",");
            headBuilder.append("cTimePeriodForY");
            headBuilder.append(",");
            headBuilder.append("cMinKD");
            headBuilder.append(",");
            headBuilder.append("cPauseHour");
            headBuilder.append("\n");

            headBuilder.append(cInstrument);
            headBuilder.append(",");
            headBuilder.append(cAmount);
            headBuilder.append(",");
            headBuilder.append(cTimePeriodForX);
            headBuilder.append(",");
            headBuilder.append(cTimePeriodForY);
            headBuilder.append(",");
            headBuilder.append(cMinKD);
            headBuilder.append(",");
            headBuilder.append(cCloseAllHour);
            headBuilder.append("\n");

            // 交易记录的标题
            headBuilder.append("open_time");
            headBuilder.append(",");
            headBuilder.append("close_time");
            headBuilder.append(",");
            headBuilder.append("try_open_price");
            headBuilder.append(",");
            headBuilder.append("open_price");
            headBuilder.append(",");
            headBuilder.append("close_price");
            headBuilder.append(",");
            headBuilder.append("command");
            headBuilder.append(",");
            headBuilder.append("amount");
            headBuilder.append(",");
            headBuilder.append("account_equity");
            logPrintWriter.println(headBuilder);
            logPrintWriter.flush();
            if (cIsPrintTestCSV) {
                testPrintWriter = new PrintWriter("Test_" + STRATEGY_NAME + "_" + localDateFormat.format(new Date()) + ".csv", "UTF-8");
            }
        } catch (Exception e) {
            throw new JFException(e.getMessage());
        }

        subscribedInstruments.add(cInstrument);

        context.setSubscribedInstruments(subscribedInstruments, true);

        // 初始化前60个Bar
        long prevBarTime = history.getPreviousBarStart(Period.ONE_MIN, history.getLastTick(cInstrument).getTime());
        lastBar = history.getBar(cInstrument, Period.ONE_MIN, OfferSide.ASK, 1);
        barList = history.getBars(cInstrument, Period.ONE_MIN, OfferSide.ASK, Filter.WEEKENDS, cTimePeriodForX - 1, prevBarTime, 1);

        // 记录测试数据
        if (cIsPrintTestCSV) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("date_time");
            stringBuilder.append(",");
            stringBuilder.append("open");
            stringBuilder.append(",");
            stringBuilder.append("high");
            stringBuilder.append(",");
            stringBuilder.append("low");
            stringBuilder.append(",");
            stringBuilder.append("close");
            stringBuilder.append(",");
            stringBuilder.append("range");
            stringBuilder.append(",");
            stringBuilder.append("msX");
            stringBuilder.append(",");
            stringBuilder.append("mY");
            stringBuilder.append(",");
            stringBuilder.append("operation");
            stringBuilder.append("\n");
            for (IBar bar : barList) {
                stringBuilder.append(gtmDateFormat.format(new Date(bar.getTime())));
                stringBuilder.append(",");
                stringBuilder.append(bar.getOpen());
                stringBuilder.append(",");
                stringBuilder.append(bar.getHigh());
                stringBuilder.append(",");
                stringBuilder.append(bar.getLow());
                stringBuilder.append(",");
                stringBuilder.append(bar.getClose());
                stringBuilder.append(",");
                stringBuilder.append(bar.getHigh() - bar.getLow());
                stringBuilder.append(",");
                stringBuilder.append(",");
                stringBuilder.append(",");
                stringBuilder.append("\n");
            }
            testPrintWriter.print(stringBuilder);
            testPrintWriter.flush();
        }
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder order = message.getOrder();
        if (order == null || !isThisStrategyOrder(order)) {
            // 非订单相关message, 以及不是本策略相关订单的信息, 忽略
            return;
        }
        if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {
        } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {
        } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {
        } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {
        } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {
        } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {
        } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {
            // 如果订单平仓被拒绝, 继续平仓.
            puts("WARN! order close rejected! Reason: " + message.getContent());
            order.close();
        } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {
            // 订单关闭成功, 记录交易记录
            logOrderClosed(order);
        }
    }

    @Override
    public void onStop() throws JFException {
        // 退出时清理文件描述符
        if (cIsPrintTestCSV) {
            testPrintWriter.close();
        }
        logPrintWriter.close();
        puts(STRATEGY_NAME + " stop!");
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (isThisInstrument(instrument)) {
            if (Period.ONE_MIN.equals(period)) {
                String logCommand = "";
                boolean isOpen;
                lastCloseAskPriceForLog = askBar.getClose();
                lastCloseBidPriceForLog = bidBar.getClose();

                // 重新计算各个参数
                recalculate(askBar);

                Calendar today = Calendar.getInstance(TimeZone.getTimeZone("CTT"));
                today.setTimeInMillis(askBar.getTime());
                int hour = today.get(Calendar.HOUR_OF_DAY);
                int day = today.get(Calendar.DAY_OF_WEEK);

                if (cShouldCloseAtWeekend && (day == cCloseAllDayOfWeek && hour >= cCloseAllHour || day > cCloseAllDayOfWeek)) {
                    // 周末平仓
                    closeOrdersByCommand(IEngine.OrderCommand.BUY);
                    closeOrdersByCommand(IEngine.OrderCommand.SELL);
                    puts("Close all this strategy order at weekend!");
                    logCommand = "close all this strategy order";
                } else if (mY < msX && askBar.getClose() > lastBar.getHigh() && Math.abs(askBar.getClose() - lastBar.getClose()) >= cMinKD) {
                    // open sell, close buy
                    closeOrdersByCommand(IEngine.OrderCommand.BUY);
                    isOpen = openOrderByCommand(IEngine.OrderCommand.SELL);
                    logCommand = "condition 1: open sell & close buy; should open?: " + isOpen;
                } else if (mY < msX && askBar.getClose() < lastBar.getLow() && Math.abs(askBar.getClose() - lastBar.getClose()) >= cMinKD) {
                    // open buy, close sell
                    closeOrdersByCommand(IEngine.OrderCommand.SELL);
                    isOpen = openOrderByCommand(IEngine.OrderCommand.BUY);
                    logCommand = "condition 2: open buy & close sell; should open?: " + isOpen;
                } else if (mY >= msX && askBar.getClose() > lastBar.getHigh() && Math.abs(askBar.getClose() - lastBar.getClose()) >= cMinKD) {
                    // open buy, close sell
                    closeOrdersByCommand(IEngine.OrderCommand.SELL);
                    isOpen = openOrderByCommand(IEngine.OrderCommand.BUY);
                    logCommand = "condition 3: open buy & close sell; should open?: " + isOpen;
                } else if (mY >= msX && askBar.getClose() < lastBar.getLow() && Math.abs(askBar.getClose() - lastBar.getClose()) >= cMinKD) {
                    // open sell, close buy
                    closeOrdersByCommand(IEngine.OrderCommand.BUY);
                    isOpen = openOrderByCommand(IEngine.OrderCommand.SELL);
                    logCommand = "condition 4: open sell & close buy; should open?: " + isOpen;
                }
                lastBar = askBar;

                // 记录测试数据
                if (cIsPrintTestCSV) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(gtmDateFormat.format(new Date(askBar.getTime())));
                    stringBuilder.append(",");
                    stringBuilder.append(askBar.getOpen());
                    stringBuilder.append(",");
                    stringBuilder.append(askBar.getHigh());
                    stringBuilder.append(",");
                    stringBuilder.append(askBar.getLow());
                    stringBuilder.append(",");
                    stringBuilder.append(askBar.getClose());
                    stringBuilder.append(",");
                    stringBuilder.append(askBar.getHigh() - askBar.getLow());
                    stringBuilder.append(",");
                    stringBuilder.append(msX);
                    stringBuilder.append(",");
                    stringBuilder.append(mY);
                    stringBuilder.append(",");
                    stringBuilder.append(logCommand);
                    testPrintWriter.println(stringBuilder);

                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append(gtmDateFormat.format(new Date(bidBar.getTime())));
                    stringBuilder2.append(",");
                    stringBuilder2.append(bidBar.getOpen());
                    stringBuilder2.append(",");
                    stringBuilder2.append(bidBar.getHigh());
                    stringBuilder2.append(",");
                    stringBuilder2.append(bidBar.getLow());
                    stringBuilder2.append(",");
                    stringBuilder2.append(bidBar.getClose());
                    stringBuilder2.append(",");
                    stringBuilder2.append(bidBar.getHigh() - bidBar.getLow());
                    stringBuilder2.append(",");
                    stringBuilder2.append(msX);
                    stringBuilder2.append(",");
                    stringBuilder2.append(mY);
                    stringBuilder2.append(",");
                    stringBuilder2.append(logCommand);
                    testPrintWriter.println(stringBuilder2);

                    testPrintWriter.flush();
                }
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private void closeOrdersByCommand(IEngine.OrderCommand command) throws JFException {
        for (IOrder o : getStrategyOrders()) {
            if (command.equals(o.getOrderCommand())) {
                o.close();
            }
        }
    }

    private boolean openOrderByCommand(IEngine.OrderCommand command) throws JFException {
        if (hasSameCommandOrder(command)) {
            return false;
        }

        String orderLabel = getOrderLabel(cInstrument, command);
        if (IEngine.OrderCommand.BUY.equals(command)) {
            tryOpenPriceHashMap.put(orderLabel, lastCloseAskPriceForLog);
        } else {
            tryOpenPriceHashMap.put(orderLabel, lastCloseBidPriceForLog);
        }

        engine.submitOrder(orderLabel, cInstrument, command, cAmount);
        return true;
    }

    private boolean hasSameCommandOrder(IEngine.OrderCommand command) throws JFException {
        for (IOrder o : getStrategyOrders()) {
            if (command.equals(o.getOrderCommand())) {
                return true;
            }
        }
        return false;
    }

    private void logOrderClosed(IOrder order) {
        Double tryOpenPrice = 0.0;
        if (tryOpenPriceHashMap.containsKey(order.getLabel())) {
            tryOpenPrice = tryOpenPriceHashMap.remove(order.getLabel());
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(gtmDateFormat.format(order.getCreationTime()));
        stringBuilder.append(",");
        stringBuilder.append(gtmDateFormat.format(order.getCloseTime()));
        stringBuilder.append(",");
        stringBuilder.append(tryOpenPrice);
        stringBuilder.append(",");
        stringBuilder.append(order.getOpenPrice());
        stringBuilder.append(",");
        stringBuilder.append(order.getClosePrice());
        stringBuilder.append(",");
        stringBuilder.append(order.getOrderCommand());
        stringBuilder.append(",");
        stringBuilder.append(order.getAmount());
        stringBuilder.append(",");
        stringBuilder.append(account.getBaseEquity());
        logPrintWriter.println(stringBuilder);
        logPrintWriter.flush();
    }

    private void recalculate(IBar askBar) {
        barList.remove(0);
        barList.add(askBar);

        // calc msX mY
        ArrayList<Double> rangeListX = new ArrayList<>();
        ArrayList<Double> rangeListY = new ArrayList<>();

        int yStartPos = barList.size() - cTimePeriodForY;
        for (int i = 0; i < barList.size(); i++) {
            double r = barList.get(i).getHigh() - barList.get(i).getLow();
            rangeListX.add(r);
            if (i >= yStartPos) {
                rangeListY.add(r);
            }
        }

        double averageX = getAverage(rangeListX);
        double sdX = getStdDev(rangeListX);
        msX = averageX + sdX;
        mY = getAverage(rangeListY);

    }

    private double getAverage(ArrayList<Double> data)
    {
        double sum = 0;
        for(double a : data)
            sum += a;
        return sum / data.size();
    }

    private double getVariance(ArrayList<Double> data)
    {
        double avg = getAverage(data);
        double temp = 0;
        for(double a : data) {
            double b = (a - avg);
            temp += b * b;
        }
        return temp / (data.size() - 1);
    }

    private double getStdDev(ArrayList<Double> data)
    {
        return Math.sqrt(getVariance(data));
    }

    private List<IOrder> getStrategyOrders() throws JFException {
        ArrayList<IOrder> orders = new ArrayList<>();
        for (IOrder o : engine.getOrders()) {
            if (isThisStrategyOrder(o)) {
                orders.add(o);
            }
        }
        return orders;
    }

    private boolean isThisInstrument(Instrument instrument) {
        return subscribedInstruments.contains(instrument);
    }

    private boolean isThisStrategyOrder(IOrder order) {
        return order.getLabel().startsWith(STRATEGY_NAME);
    }

    private String getOrderLabel(Instrument instrument, IEngine.OrderCommand orderCommand) {
        return STRATEGY_NAME + "_" + instrument.toString().replace('/', '_') + "_" + orderCommand + "_" +
                System.currentTimeMillis();
    }

    private void puts(String str) {
        console.getInfo().println(str);
    }

}