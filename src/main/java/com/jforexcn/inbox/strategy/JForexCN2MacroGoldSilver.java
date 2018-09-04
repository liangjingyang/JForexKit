package com.jforexcn.inbox.strategy;

/**
 * Created by simple on 03/03/17.
 *
 * request by MARCO
 *
**/

import com.dukascopy.api.Configurable;
import com.dukascopy.api.Filter;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IDataService;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.ITimeDomain;
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

public class JForexCN2MacroGoldSilver implements IStrategy {

    /**
     * Created by simple(simple.continue@gmail.com) on  03/03/17.
     */

    @Configurable("A Instrument")
    public Instrument cAInstrument = Instrument.XAUUSD;
    @Configurable("B Instrument")
    public Instrument cBInstrument = Instrument.XAGUSD;
    @Configurable("Bar Period")
    public Period cBarPeriod = Period.FIFTEEN_MINS;
    @Configurable("Count cPeriod")
    public int cCountPeriod = 30;
    @Configurable("Trade Unit of A Instrument")
    public double cTradeUnit = 1;
    @Configurable("Multiplier")
    public double cMultiplier = 1;
    @Configurable("Delta")
    public double cDelta = 0.2;
    @Configurable("Min Delta")
    public double cMinDelta = 0.2;

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
    private IDataService dataService;

    private HashSet<Instrument> subscribedInstruments = new HashSet<>();
    private List<Double> aCloseList = new ArrayList<>();
    private List<Double> bCloseList = new ArrayList<>();
    private ITick aLastCloseTick;
    private ITick bLastCloseTick;
    private static String STRATEGY_NAME = "MacroGoldSilver";

    private SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
    private SimpleDateFormat gtmDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    private PrintWriter testPrintWriter;
    private PrintWriter logPrintWriter;

    private HashMap<String, Double> tryOpenPriceHashMap = new HashMap<>();

    private double abLastCloseRatio = 0.0;
    private double abRatioMA = 0.0;
    private double abRatioMax = 0.0;
    private double abRatioMin = 0.0;

    @Override
    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.history = context.getHistory();
        this.account = context.getAccount();
        this.dataService = context.getDataService();

        ITimeDomain domain = dataService.getOfflineTimeDomain();
        puts("start: " + domain.getStart() + ", end: " + domain.getEnd());
        puts("now: " + System.currentTimeMillis() + ", isWeedend: " + dataService.isOfflineTime(System.currentTimeMillis()));


        // 验证参数合法性
        boolean argsAssert = (cTradeUnit > 0.000001 && cCountPeriod > 0 &&
                (cCloseAllHour >= 0 && cCloseAllHour <= 23) &&
                (cCloseAllDayOfWeek >= 1 && cCloseAllDayOfWeek <= 7));

        if (!argsAssert) {
            throw new JFException("args error!");
        }

        cTradeUnit = cTradeUnit * cMultiplier / 1000000;

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
            headBuilder.append("cBInstrument");
            headBuilder.append(",");
            headBuilder.append("cCountPeriod");
            headBuilder.append(",");
            headBuilder.append("cBarPeriod");
            headBuilder.append(",");
            headBuilder.append("cTradeUnit");
            headBuilder.append(",");
            headBuilder.append("cMultiplier");
            headBuilder.append(",");
            headBuilder.append("cMinDelta");
            headBuilder.append(",");
            headBuilder.append("cPauseHour");
            headBuilder.append(",");
            headBuilder.append("cPauseDayOfWeek");
            headBuilder.append(",");
            headBuilder.append("cShouldCloseAtWeekend");
            headBuilder.append(",");
            headBuilder.append("cIsPrintTestCSV");
            headBuilder.append("\n");

            headBuilder.append(cAInstrument);
            headBuilder.append(",");
            headBuilder.append(cBInstrument);
            headBuilder.append(",");
            headBuilder.append(cCountPeriod);
            headBuilder.append(",");
            headBuilder.append(cBarPeriod);
            headBuilder.append(",");
            headBuilder.append(cTradeUnit);
            headBuilder.append(",");
            headBuilder.append(cMultiplier);
            headBuilder.append(",");
            headBuilder.append(cMinDelta);
            headBuilder.append(",");
            headBuilder.append(cCloseAllHour);
            headBuilder.append(",");
            headBuilder.append(cCloseAllDayOfWeek);
            headBuilder.append(",");
            headBuilder.append(cShouldCloseAtWeekend);
            headBuilder.append(",");
            headBuilder.append(cIsPrintTestCSV);
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

        subscribedInstruments.add(cAInstrument);
        subscribedInstruments.add(cBInstrument);

        context.setSubscribedInstruments(subscribedInstruments, true);

        // 初始化
        long prevBarTime = history.getPreviousBarStart(cBarPeriod, history.getLastTick(cAInstrument).getTime());
        aLastCloseTick = history.getLastTick(cAInstrument);
        bLastCloseTick = history.getLastTick(cBInstrument);
        List<IBar> aBarList = history.getBars(cAInstrument, cBarPeriod, OfferSide.ASK, Filter.WEEKENDS, cCountPeriod - 1, prevBarTime, 1);
        List<IBar> bBarList  = history.getBars(cBInstrument, cBarPeriod, OfferSide.ASK, Filter.WEEKENDS, cCountPeriod - 1, prevBarTime, 1);

        List<IBar> aBarList2 = history.getBars(cAInstrument, cBarPeriod, OfferSide.BID, Filter.WEEKENDS, cCountPeriod - 1, prevBarTime, 1);
        List<IBar> bBarList2  = history.getBars(cBInstrument, cBarPeriod, OfferSide.BID, Filter.WEEKENDS, cCountPeriod - 1, prevBarTime, 1);

        for (int i = 0; i < aBarList.size(); i++) {
            IBar aBar = aBarList.get(i);
            IBar bBar = bBarList.get(i);
            aCloseList.add(aBar.getClose());
            bCloseList.add(bBar.getClose());
        }

        // 记录测试数据
        if (cIsPrintTestCSV) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("offer_side");
            stringBuilder.append(",");
            stringBuilder.append("date_time");
            stringBuilder.append(",");
            stringBuilder.append("a close");
            stringBuilder.append(",");
            stringBuilder.append("b close");
            stringBuilder.append(",");
            stringBuilder.append("abRatio");
            stringBuilder.append(",");
            stringBuilder.append("abRatioMax");
            stringBuilder.append(",");
            stringBuilder.append("abRatioMin");
            stringBuilder.append(",");
            stringBuilder.append("abRatioMA");
            stringBuilder.append(",");
            stringBuilder.append("abRatioLastTrade");
            stringBuilder.append(",");
            stringBuilder.append("cDelta");
            stringBuilder.append(",");
            stringBuilder.append("cMinDelta");
            stringBuilder.append(",");
            stringBuilder.append("operation");
            stringBuilder.append("\n");
            for (int i = 0; i < aBarList.size(); i++) {
                IBar aBar = aBarList.get(i);
                IBar bBar = bBarList.get(i);

                stringBuilder.append("ask");
                stringBuilder.append(",");
                stringBuilder.append(gtmDateFormat.format(new Date(aBar.getTime())));
                stringBuilder.append(",");
                stringBuilder.append(aBar.getClose());
                stringBuilder.append(",");
                stringBuilder.append(bBar.getClose());
                stringBuilder.append(",");
                stringBuilder.append(aBar.getClose() / bBar.getClose());
                stringBuilder.append(",");
                stringBuilder.append(",");
                stringBuilder.append(",");
                stringBuilder.append(",");
                stringBuilder.append(",");
                stringBuilder.append(",");
                stringBuilder.append(",");
                stringBuilder.append("\n");

                IBar aBar2 = aBarList2.get(i);
                IBar bBar2 = bBarList2.get(i);
                stringBuilder.append("bid");
                stringBuilder.append(",");
                stringBuilder.append(gtmDateFormat.format(new Date(aBar2.getTime())));
                stringBuilder.append(",");
                stringBuilder.append(aBar2.getClose());
                stringBuilder.append(",");
                stringBuilder.append(bBar2.getClose());
                stringBuilder.append(",");
                stringBuilder.append(aBar2.getClose() / bBar2.getClose());
                stringBuilder.append(",");
                stringBuilder.append(",");
                stringBuilder.append(",");
                stringBuilder.append(",");
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
            puts("====== =======");
            puts("====== order filled, time: " + order.getFillTime());
            puts("====== order filled, price: " + order.getOpenPrice());
            puts("====== order filled, cInstrument: " + order.getInstrument());
            puts("====== order filled, command: " + order.getOrderCommand());
            puts("====== order filled, amount: " + order.getAmount());
            puts("====== =======");
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
            if (cBarPeriod.equals(period)) {

                if (!isOffline(askBar.getTime())) {

                    // 重新计算各个参数
                    recalculate();

                    String logCommand = "";
                    boolean isOpen;

                    Calendar today = Calendar.getInstance(TimeZone.getTimeZone("CTT"));
                    today.setTimeInMillis(askBar.getTime());
                    int hour = today.get(Calendar.HOUR_OF_DAY);
                    int day = today.get(Calendar.DAY_OF_WEEK);

                    double abRatioLastTrade = 0;

                    if (cShouldCloseAtWeekend && (day == cCloseAllDayOfWeek && hour >= cCloseAllHour || day > cCloseAllDayOfWeek)) {
                        // 周末平仓
                        closeAllStrategyOrders();
//                    puts("Close all this strategy order at weekend!");
                        logCommand = "close all this strategy order";
                    } else if (getStrategyOrders().size() <= 0) {
                        // 空仓
                        if (abLastCloseRatio >= abRatioMA + cDelta) {
                            // sell A, buy B
                            openOrderByCommand(IEngine.OrderCommand.SELL);
                            logCommand = "1: no position sell A buy B";
                        } else if (abLastCloseRatio <= abRatioMA - cDelta) {
                            // buy A, sell B
                            openOrderByCommand(IEngine.OrderCommand.BUY);
                            logCommand = "2: no position buy A sell B";
                        }
                    } else {
                        // 持仓
                        abRatioLastTrade = getABRatioLastTrade();
                        if (abRatioLastTrade > 0 &&
                                abLastCloseRatio >= abRatioMax &&
                                (abLastCloseRatio >= abRatioLastTrade + cMinDelta || abLastCloseRatio <= abRatioLastTrade - cMinDelta)) {
                            // sell A, buy B
                            closeOrdersByCommand(IEngine.OrderCommand.BUY);
                            isOpen = openOrderByCommand(IEngine.OrderCommand.SELL);
                            logCommand = "3: sell A buy B should open?: " + isOpen;
                        } else if (abRatioLastTrade > 0 &&
                                abLastCloseRatio <= abRatioMin &&
                                (abLastCloseRatio >= abRatioLastTrade + cMinDelta || abLastCloseRatio <= abRatioLastTrade - cMinDelta)) {
                            // buy A, sell B
                            closeOrdersByCommand(IEngine.OrderCommand.SELL);
                            isOpen = openOrderByCommand(IEngine.OrderCommand.BUY);
                            logCommand = "4: buy A sell B should open?: " + isOpen;
                        }
                    }

                    // 记录测试数据
                    if (cIsPrintTestCSV) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("ask");
                        stringBuilder.append(",");
                        stringBuilder.append(gtmDateFormat.format(new Date(askBar.getTime())));
                        stringBuilder.append(",");
                        stringBuilder.append(aLastCloseTick.getAsk());
                        stringBuilder.append(",");
                        stringBuilder.append(bLastCloseTick.getAsk());
                        stringBuilder.append(",");
                        stringBuilder.append(abLastCloseRatio);
                        stringBuilder.append(",");
                        stringBuilder.append(abRatioMax);
                        stringBuilder.append(",");
                        stringBuilder.append(abRatioMin);
                        stringBuilder.append(",");
                        stringBuilder.append(abRatioMA);
                        stringBuilder.append(",");
                        stringBuilder.append(abRatioLastTrade);
                        stringBuilder.append(",");
                        stringBuilder.append(cDelta);
                        stringBuilder.append(",");
                        stringBuilder.append(cMinDelta);
                        stringBuilder.append(",");
                        stringBuilder.append(logCommand);
                        testPrintWriter.println(stringBuilder);

                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("bid");
                        stringBuilder2.append(",");
                        stringBuilder2.append(gtmDateFormat.format(new Date(bidBar.getTime())));
                        stringBuilder2.append(",");
                        stringBuilder2.append(aLastCloseTick.getBid());
                        stringBuilder2.append(",");
                        stringBuilder2.append(bLastCloseTick.getBid());
                        stringBuilder2.append(",");
                        stringBuilder2.append(aLastCloseTick.getBid() / bLastCloseTick.getBid());
                        stringBuilder2.append(",");
                        stringBuilder2.append(abRatioMax);
                        stringBuilder2.append(",");
                        stringBuilder2.append(abRatioMin);
                        stringBuilder2.append(",");
                        stringBuilder2.append(abRatioMA);
                        stringBuilder2.append(",");
                        stringBuilder2.append(abRatioLastTrade);
                        stringBuilder2.append(",");
                        stringBuilder2.append(cDelta);
                        stringBuilder2.append(",");
                        stringBuilder2.append(cMinDelta);
                        stringBuilder2.append(",");
                        stringBuilder2.append(logCommand);
                        testPrintWriter.println(stringBuilder2);

                        testPrintWriter.flush();
                    }
                }
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private void recalculate() throws JFException {
        aLastCloseTick = history.getLastTick(cAInstrument);
        bLastCloseTick = history.getLastTick(cBInstrument);

        aCloseList.remove(0);
        aCloseList.add(aLastCloseTick.getAsk());

        bCloseList.remove(0);
        bCloseList.add(bLastCloseTick.getAsk());

//        puts("======== aCloseList.size(): " + aCloseList.size());
//        puts("======== bCloseList.size(): " + bCloseList.size());
//        puts("======== aLastCloseTick.getAsk(): " + aLastCloseTick.getAsk());
//        puts("======== bLastCloseTick.getAsk(): " + bLastCloseTick.getAsk());

        abLastCloseRatio = aLastCloseTick.getAsk() / bLastCloseTick.getAsk();

        double abRatioTotal = 0;
        double ratio;
        double max = 0;
        double min = 0;
        for (int i = 0; i < aCloseList.size(); i++) {
            ratio = aCloseList.get(i) / bCloseList.get(i);
            abRatioTotal = abRatioTotal + ratio;
            if (max == 0) {
                max = ratio;
            } else if (ratio > max) {
                max = ratio;
            }
            if (min == 0) {
                min = ratio;
            } else if (ratio < min) {
                min = ratio;
            }
        }
        abRatioMax = max;
        abRatioMin = min;
        abRatioMA = abRatioTotal / aCloseList.size();
    }

    private void closeAllStrategyOrders() throws JFException {
        for (IOrder o : getStrategyOrders()) {
            o.close();
        }
    }

    private void closeOrdersByCommand(IEngine.OrderCommand command) throws JFException {
        IEngine.OrderCommand aCommand = command;
        IEngine.OrderCommand bCommand = IEngine.OrderCommand.BUY;
        if (IEngine.OrderCommand.BUY.equals(command)) {
            bCommand = IEngine.OrderCommand.SELL;
        }
        for (IOrder order : getStrategyOrders()) {
            if (cAInstrument.equals(order.getInstrument()) && aCommand.equals(order.getOrderCommand())) {
                order.close();
            } else if (cBInstrument.equals(order.getInstrument()) && bCommand.equals(order.getOrderCommand())) {
                order.close();
            }
        }
    }

    private double getABRatioLastTrade() throws JFException {
        IOrder aOrder = null;
        IOrder bOrder = null;
        for (IOrder order : getStrategyOrders()) {
            if (cAInstrument.equals(order.getInstrument())) {
                aOrder = order;
            } else if (cBInstrument.equals(order.getInstrument())) {
                bOrder = order;
            }
        }
        if (aOrder != null && bOrder != null) {
            return aOrder.getOpenPrice() / bOrder.getOpenPrice();
        }
        return 0;
    }

    private boolean openOrderByCommand(IEngine.OrderCommand command) throws JFException {
        if (hasSameCommandOrder(command)) {
            return false;
        }

        long pairTag = System.currentTimeMillis();

        String aOrderLabel = getOrderLabel(cAInstrument, pairTag);
        if (IEngine.OrderCommand.BUY.equals(command)) {
            tryOpenPriceHashMap.put(aOrderLabel, aLastCloseTick.getAsk());
        } else {
            tryOpenPriceHashMap.put(aOrderLabel, aLastCloseTick.getBid());
        }
        engine.submitOrder(aOrderLabel, cAInstrument, command, cTradeUnit);

        String bOrderLabel = getOrderLabel(cBInstrument, pairTag);
        IEngine.OrderCommand bCommand;
        if (IEngine.OrderCommand.BUY.equals(command)) {
            bCommand = IEngine.OrderCommand.SELL;
            tryOpenPriceHashMap.put(bOrderLabel, bLastCloseTick.getBid());
        } else {
            bCommand = IEngine.OrderCommand.BUY;
            tryOpenPriceHashMap.put(bOrderLabel, bLastCloseTick.getAsk());
        }
        engine.submitOrder(bOrderLabel, cBInstrument, bCommand, cTradeUnit * (aLastCloseTick.getAsk() / bLastCloseTick.getAsk()));

        return true;
    }

    private boolean hasSameCommandOrder(IEngine.OrderCommand command) throws JFException {
        for (IOrder aOrder : getStrategyOrders()) {
            if (command.equals(aOrder.getOrderCommand()) && cAInstrument.equals(aOrder.getInstrument())) {
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
        return cAInstrument.equals(instrument);
    }

    private boolean isThisStrategyOrder(IOrder order) {
        return order.getLabel().startsWith(STRATEGY_NAME);
    }

    private String getOrderLabel(Instrument instrument, long pairTag) {
        return STRATEGY_NAME + "_" + instrument.toString().replace('/', '_') + "_" + pairTag;
    }

    private String getOrderLabelPairTag(String label) {
        String[] tags = label.split("_");
        return tags[tags.length - 1];
    }

    private boolean isOffline(long time) throws JFException{
        return dataService.isOfflineTime(time);
    }


    private void puts(String str) {
        console.getInfo().println(str);
    }

}