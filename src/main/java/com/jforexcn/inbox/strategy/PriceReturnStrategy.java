package com.jforexcn.inbox.strategy;

/**
 * Created by simple on 26/9/16.
 */

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforexcn.shared.lib.MailService;
import com.jforexcn.shared.client.StrategyRunner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class PriceReturnStrategy implements IStrategy {


    private IEngine mEngine;
    private IConsole mConsole;
    private IIndicators mIndicators;
    private IHistory mHistory;

    private Set<Instrument> mInstrumentSet = new HashSet<Instrument>();

    private HashMap<Instrument, PriceReturnParams> priceReturnParamsHashMap = new HashMap<>();
    private HashMap<Instrument, Integer> pipsMap = new HashMap<>();
    private Set<Instrument> processingHashMap = new HashSet<>();

    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    public static String STRATEGY_TAG = "Strategy_PriceReturn";

    public static int START_HOUR = 21;
    public static int HOLD_HOURS = 5;

    private boolean init = false;

    private boolean todayOrdered = false;

    @Override
    public void onStart(IContext context) throws JFException {
        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mIndicators = context.getIndicators();
        this.mHistory = context.getHistory();

        // subscribe instruments
        pipsMap.put(Instrument.EURUSD, 12);
        for (Map.Entry<Instrument, Integer> entry : pipsMap.entrySet()) {
            mInstrumentSet.add(entry.getKey());
        }
        context.setSubscribedInstruments(mInstrumentSet, true);

        // date format
        mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));

    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        try {
            IOrder order = message.getOrder();
            if (order != null && isStrategyInstrument(order.getInstrument())) {
                if (IOrder.State.CANCELED.equals(order.getState())) {
                    removeProcessing(order.getInstrument());
                } else if (IOrder.State.FILLED.equals(order.getState())) {
                    todayOrdered = true;
                    removeProcessing(order.getInstrument());
                } else {
                    puts("onMessage other type: " + message.getType().toString() +
                            ", content: " + message.getContent());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendEmail("CRASH! " + getClass().getSimpleName(), "OnMessage exception: " + e.getMessage());
        }
    }

    @Override
    public void onStop() throws JFException {
        puts(STRATEGY_TAG + "OnStop!");
        sendEmail(STRATEGY_TAG + "OnStop!", "");
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        if (!init) {
            for (Instrument strategyInstrument : mInstrumentSet) {
                priceReturnParamsHashMap.put(strategyInstrument, initPriceReturnParams(instrument, tick.getTime()));
            }
            List<IOrder> orders = mHistory.getOrdersHistory(instrument, tick.getTime() - 24 * 3600 * 1000, tick.getTime());
            for (IOrder o : orders) {
                if (isStrategyOrder(o)) {
                    todayOrdered = true;
                    break;
                }
            }
            init = true;
        }
        if (isStrategyInstrument(instrument) && !isProcessing(instrument)) {
            IOrder order = getStrategyOrder(instrument);
            if (order != null) {

                if (order.getStopLossPrice() == 0) {
                    if (IEngine.OrderCommand.BUY.equals(order.getOrderCommand())) {
                        order.setStopLossPrice(order.getOpenPrice() - pipsMap.get(instrument) * instrument.getPipValue());
                        addProcessing(instrument);
                    } else if (IEngine.OrderCommand.SELL.equals(order.getOrderCommand())) {
                        order.setStopLossPrice(order.getOpenPrice() + pipsMap.get(instrument) * instrument.getPipValue());
                        addProcessing(instrument);
                    }
                }
                if (order.getTakeProfitPrice() == 0) {
                    if (IEngine.OrderCommand.BUY.equals(order.getOrderCommand())) {
                        order.setTakeProfitPrice(order.getOpenPrice() + (pipsMap.get(instrument) + 2) * instrument.getPipValue());
                        addProcessing(instrument);
                    } else if (IEngine.OrderCommand.SELL.equals(order.getOrderCommand())) {
                        order.setTakeProfitPrice(order.getOpenPrice() - (pipsMap.get(instrument) + 2) * instrument.getPipValue());
                        addProcessing(instrument);
                    }
                }
                if (order.getProfitLossInPips() > 5) {
                    if (IEngine.OrderCommand.BUY.equals(order.getOrderCommand())) {
                        order.setStopLossPrice(order.getOpenPrice() + 1 * instrument.getPipValue());
                        addProcessing(instrument);
                    } else if (IEngine.OrderCommand.SELL.equals(order.getOrderCommand())) {
                        order.setStopLossPrice(order.getOpenPrice() - 1 * instrument.getPipValue());
                        addProcessing(instrument);
                    }
                }
            }
            if (order == null && !isTodayOrdered() && duringOpenOrderTime(instrument, tick)) {
                PriceReturnParams priceReturnParams = priceReturnParamsHashMap.get(instrument);
                IEngine.OrderCommand orderCommand = priceReturnParams.getOrderCommand(tick, pipsMap.get(instrument), instrument);
                if (IEngine.OrderCommand.BUY.equals(orderCommand)) {

                    mEngine.submitOrder(getOrderLabel(instrument, orderCommand, tick.getTime()),
                            instrument, IEngine.OrderCommand.BUY, 0.03, tick.getAsk(), 1);
                    addProcessing(instrument);
                } else if (IEngine.OrderCommand.SELL.equals(orderCommand)) {

                    mEngine.submitOrder(getOrderLabel(instrument, orderCommand, tick.getTime()),
                            instrument, IEngine.OrderCommand.SELL, 0.03, tick.getBid(), 1);
                    addProcessing(instrument);
                }
            }
        }
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (Period.ONE_HOUR.equals(period)) {
            priceReturnParamsHashMap.put(instrument, initPriceReturnParams(instrument, askBar.getTime() + 3600 * 1000));
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private boolean isStrategyInstrument(Instrument instrument) {
        return mInstrumentSet.contains(instrument);
    }

    private boolean isProcessing(Instrument instrument) {
        return processingHashMap.contains(instrument);
    }

    private void addProcessing(Instrument instrument) {
        if (!isProcessing(instrument)) {
            processingHashMap.add(instrument);
        }
    }

    private void removeProcessing(Instrument instrument) {
        if (isProcessing(instrument)) {
            processingHashMap.remove(instrument);
        }
    }
    private String getOrderLabel(Instrument instrument, IEngine.OrderCommand orderCommand, long time) {
        return STRATEGY_TAG + "_" + instrument.toString().replace('/', '_') + "_" + orderCommand +
                "_" + mSimpleDateFormat.format(new Date(time)) + "_" + time;
    }

    private void puts(String str) {
//        mConsole.getInfo().println(str);
        StrategyRunner.LOGGER.info(str);
    }

    private void sendEmail(String subject, String body) {
        MailService.sendMail(subject, body);
    }

    private IOrder getStrategyOrder(Instrument instrument) throws JFException {
        for (IOrder order : mEngine.getOrders()) {
            if (order != null &&
                    mInstrumentSet.contains(instrument) &&
                    instrument.equals(order.getInstrument()) &&
                    isStrategyOrder(order))
            {
                return order;
            }
        }
        return null;
    }

    private boolean isStrategyOrder(IOrder order) {
        return order.getLabel().startsWith(STRATEGY_TAG);
    }

    private boolean isTodayOrdered() {
        return todayOrdered;
    }

    private boolean duringOpenOrderTime(Instrument instrument, ITick tick) {
        if (priceReturnParamsHashMap.containsKey(instrument)) {
            return priceReturnParamsHashMap.get(instrument).duringOpenOrderTime(tick);
        }
        todayOrdered = false;
        return false;
    }

    private PriceReturnParams initPriceReturnParams(Instrument instrument, long time) throws JFException {
        Calendar today = Calendar.getInstance(TimeZone.getTimeZone("GTM"));
        today.setTimeInMillis(time);
//        if (today.get(Calendar.HOUR_OF_DAY) == START_HOUR) {
//            todayOrdered = false;
//        }
        today.set(Calendar.HOUR_OF_DAY, START_HOUR);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        long start = today.getTimeInMillis();
        long end = start + HOLD_HOURS * 3600 * 1000;

        puts("start: " + start + ", end: " + end + ", dayofweek: " + today.get(Calendar.DAY_OF_WEEK));
        return new PriceReturnParams(start, end, today.get(Calendar.DAY_OF_WEEK));
    }

    public class PriceReturnParams {
        final private long startTime;
        final private long endTime;
        final private int day;

        public PriceReturnParams(long startTime, long endTime, int day) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.day = day;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public int getDay() {
            return day;
        }

        public boolean duringOpenOrderTime(ITick tick) {
            return getDay() <= Calendar.THURSDAY &&
                    tick.getTime() >= getStartTime() &&
                    tick.getTime() < getEndTime();
        }

        public IEngine.OrderCommand getOrderCommand(ITick tick, int pips, Instrument instrument) throws JFException {
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone("GTM"));
            today.setTimeInMillis(tick.getTime());
            int hour = today.get(Calendar.HOUR_OF_DAY);
            int shift = hour - START_HOUR;
            if (shift < 0) {
                shift = shift + 24;
            }
            IBar bar = mHistory.getBar(instrument, Period.ONE_HOUR, OfferSide.ASK, shift);
            if (tick.getBid() > bar.getOpen() + pips * instrument.getPipValue()) {
                puts("tick: " + tick.getBid() + ", open: " + bar.getOpen() + ", shift: " + shift + ", now: " + tick.getTime());
                return IEngine.OrderCommand.SELL;
            } else if (tick.getAsk() < bar.getOpen() - pips * instrument.getPipValue()) {
                puts("tick: " + tick.getBid() + ", open: " + bar.getOpen() + ", shift: " + shift + ", now: " + tick.getTime());
                return IEngine.OrderCommand.BUY;
            }
            return null;
        }
    }

}