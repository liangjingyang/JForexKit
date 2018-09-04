package com.jforexcn.inbox.strategy;

/**
 * Created by simple on 22/09/17.
 *
 * request by xiexiaom
 *
**/

import com.dukascopy.api.Configurable;
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
import com.dukascopy.api.Period;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

public class CloseOrderByEquity implements IStrategy {

    @Configurable("Profit value in account currency")
    public double cProfitValue = 1.0;
    @Configurable("Should close UNFILLED order?")
    public boolean cIsCloseUnfilledOrder = false;
    @Configurable("Base Equity if can't read from save file")
    public double cBaseEquity = 1000000;


    @Configurable("Minute for pause this strategy")
    public int cPauseMinute = 0;
    @Configurable("Hour for pause this strategy")
    public int cPauseHour = 4;
    @Configurable("Day of week for pause this strategy (1-7)")
    public int cPauseDayOfWeek = 6;

    @Configurable("Minute for resume this strategy")
    public int cResumeMinute = 0;
    @Configurable("Hour for resume this strategy")
    public int cResumeHour = 6;
    @Configurable("Day of week for resume this strategy (1-7)")
    public int cResumeDayOfWeek = 1;



    private IEngine engine;
    private IConsole console;
    private IHistory history;
    private IAccount account;

    private HashSet<Instrument> subscribedInstruments = new HashSet<>();

    private static String STRATEGY_NAME = "StrategyCloseOrderByEquity";

    private SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    private boolean isPause = false;

    private HashMap<String, IOrder> closingOrders = new HashMap<>();

    private Properties properties = new Properties();

    private static String BASE_EQUITY = "baseEquity";


    @Override
    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.history = context.getHistory();
        this.account = context.getAccount();

        // 验证参数合法性
        boolean argsAssert = (cProfitValue >= 0.1 && cProfitValue <= 100) &&
                (cPauseHour >= 0 && cPauseHour <= 23) &&
                (cPauseDayOfWeek >= 1 && cPauseDayOfWeek <= 7) &&
                (cResumeHour >= 0 && cResumeHour <= 23) &&
                (cResumeDayOfWeek >= 1 && cResumeDayOfWeek <= 7) &&
                (cPauseDayOfWeek > cResumeDayOfWeek);

        if (!argsAssert) {
            throw new JFException("args error!");
        }

        // 我们周一 ~ 周日是 1~7, 实际系统是 周日 ~ 周六 是1~7, 这里换算一下
        if (cPauseDayOfWeek == 7) {
            cPauseDayOfWeek = 1;
        } else {
            cPauseDayOfWeek = cPauseDayOfWeek + 1;
        }
        if (cResumeDayOfWeek == 7) {
            cResumeDayOfWeek = 1;
        } else {
            cResumeDayOfWeek = cResumeDayOfWeek + 1;
        }

        localDateFormat.setTimeZone(TimeZone.getTimeZone("CTT"));

        tryLoadSaveFile();

        subscribedInstruments.add(Instrument.EURUSD);
        context.setSubscribedInstruments(subscribedInstruments, true);

        puts("Base Equity: " + properties.getProperty(BASE_EQUITY));
        puts("Profit Value in current Currency: " + cProfitValue);
        puts("Should close UNFILLED order: " + cIsCloseUnfilledOrder);
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder order = message.getOrder();
        if (order == null) {
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
            puts("WARN! order close rejected! Order Id: " + order.getId() + " Reason: " + message.getContent());
            if (closingOrders.containsKey(order.getId())) {
                order.close();
            }
        } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {
            // 订单关闭成功, 记录交易记录
            closingOrders.remove(order.getId());
        }
    }

    @Override
    public void onStop() throws JFException {
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (isThisInstrument(instrument)) {
            if (Period.TEN_SECS.equals(period)) {
                tryWriteEquity();
                tryPauseOrResume(askBar);
                if (!isPause) {
                    tryCloseAllOrders();
                }
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private void tryPauseOrResume(IBar bar) {
        Calendar pauseDate = Calendar.getInstance(TimeZone.getTimeZone("CTT"));
        pauseDate.set(Calendar.MINUTE, cPauseMinute);
        pauseDate.set(Calendar.HOUR_OF_DAY, cPauseHour);
        pauseDate.set(Calendar.DAY_OF_WEEK, cPauseDayOfWeek);
        Calendar resumeDate = Calendar.getInstance(TimeZone.getTimeZone("CTT"));
        resumeDate.set(Calendar.MINUTE, cResumeMinute);
        resumeDate.set(Calendar.HOUR_OF_DAY, cResumeHour);
        resumeDate.set(Calendar.DAY_OF_WEEK, cResumeDayOfWeek);

        if (isPause && bar.getTime() >= resumeDate.getTimeInMillis() && bar.getTime() <= pauseDate.getTimeInMillis()) {
            isPause = false;
        } else if (!isPause) {
            isPause = true;
        }
    }

    private void tryCloseAllOrders() throws JFException {
        if (!isPause) {
            Double baseEquity = Double.valueOf(properties.getProperty(BASE_EQUITY));
            if (account.getEquity() >= baseEquity + cProfitValue) {
                puts("Close all orders, base equity: " + baseEquity + ", profit value: " + cProfitValue + ", current equity: " + account.getEquity());
                for (IOrder order : engine.getOrders()) {
                    if (cIsCloseUnfilledOrder) {
                        closingOrders.put(order.getId(), order);
                        order.close();
                    } else if (order.getState().equals(IOrder.State.FILLED)) {
                        closingOrders.put(order.getId(), order);
                        order.close();
                    }
                }
            }
        }
    }

    private void tryStoreSaveFile() {
        String propertiesFileName = getSaveFileName();
        try {
            FileOutputStream outputStream = new FileOutputStream(propertiesFileName);
            properties.store(outputStream, localDateFormat.format(new Date()));
        } catch (Exception e) {
            puts("ERROR!!! Can't store save file. Error message: " + e.getMessage());
        }
    }

    private void tryLoadSaveFile() throws JFException {
        String propertiesFileName = getSaveFileName();
        try {
            File folder = new File(getFolderName());
            if (!folder.exists()) {
                folder.mkdir();
            }
            InputStream inputStream = new FileInputStream(propertiesFileName);
            properties.load(inputStream);
        } catch (Exception e) {
            Double baseEquity = cBaseEquity;
            properties.setProperty(BASE_EQUITY, baseEquity.toString());
            puts("WARN!!! Can't find base equity from save file or save file not exist. Run this strategy first time? Error message: " + e.getMessage());
        }

    }

    private String getSaveFileName() {
        return getFolderName() + "/save.properties";
    }

    private String getFolderName() {
        return account.getUserName() + "_" + STRATEGY_NAME;
    }

    private void tryWriteEquity() throws JFException {
        List<IOrder> filledOrders = getFilledOrders();
        if (filledOrders.size() == 0) {
            Double baseEquity = account.getBaseEquity();
            if (!baseEquity.toString().equals(properties.getProperty(BASE_EQUITY))) {
                properties.setProperty(BASE_EQUITY, baseEquity.toString());
                tryStoreSaveFile();
            }
        }
    }

    private List<IOrder> getFilledOrders() throws JFException {
        List<IOrder> filledOrders = new ArrayList<>();
        for (IOrder order : engine.getOrders()) {
            if (order.getState().equals(IOrder.State.FILLED)) {
                filledOrders.add(order);
            }
        }
        return filledOrders;
    }


    private boolean isThisInstrument(Instrument instrument) {
        return subscribedInstruments.contains(instrument);
    }


    private void puts(String str) {
        console.getInfo().println(STRATEGY_NAME + " === " + str);
    }

}