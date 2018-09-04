package com.jforexcn.inbox.strategy;

/**
 * Created by simple on 2/7/16.
 */

import com.dukascopy.api.*;
import com.dukascopy.api.IEngine.OrderCommand;

import java.text.SimpleDateFormat;
import java.util.*;

public class HourStrategy implements IStrategy {


    @Configurable("Amount")
    public double cAmount = 0.1;
    @Configurable("Stop loss")
    public int cSlPips = 50;
    @Configurable("Moving stop loss")
    public int cMoveSlPips = 30;
    @Configurable("Take profit")
    public int cTpMaxPips = 100;
    @Configurable("Back Bars count for support and resistance")
    public int cBackBarsHourly = 4;
    @Configurable("Back Bars count for support and resistance")
    public int cBackBarsDaily = 5;
    @Configurable("Instrument")
    public Instrument cInstrument = Instrument.EURUSD;
    @Configurable("Period for checking")
    public Period cCheckPeriod = Period.ONE_MIN;
    @Configurable("Period for order")
    public Period cOrderPeriod = Period.ONE_HOUR;
    @Configurable("Break pips")
    public int cBreakPips = 0;
    @Configurable("Slippage pips")
    public int cSlippagePips = 5;
    @Configurable("Held order millisecond")
    public int cHoldOrderMillis = 14400000;
    @Configurable("Trading from 1")
    public int cTradingFrom1 = 0;
    @Configurable("Trading to 2")
    public int cTradingTo1 = 0;
    @Configurable("Trading from 1")
    public int cTradingFrom2 = 13;
    @Configurable("Trading to 2")
    public int cTradingTo2 = 15;
    @Configurable("Max daily order count")
    public int cMaxDailyOrderCount = 2;

    private IEngine mEngine;
    private IHistory mHistory;
    private IConsole mConsole;
    private IOrder mOrder;

    private String mLastOrderLabel;
    List<IBar> mOrderHistoryAskBars = new ArrayList<IBar>();
    List<IBar> mDailyHistoryAskBars = new ArrayList<IBar>();
    private double mOrderPriceHigh;
    private double mOrderPriceLow;
    private boolean mIsInit = false;
    private boolean mIsChanging = false;
    private double mDailyVolatilityInPips = 0.0;
    private int mSlPips;
    private int mTpMaxPips;
    private int mDailyOrderCount;
    private long mTradingFromMills1;
    private long mTradingToMills1;
    private long mTradingFromMills2;
    private long mTradingToMills2;

    private double mMaxLossInPips = 0.0;
    private double mProfitLossInPips = 0.0;

    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");


    @Override
    public void onStart(IContext context) throws JFException {
        this.mEngine = context.getEngine();
        this.mHistory = context.getHistory();
        this.mConsole = context.getConsole();

        mSlPips = cSlPips;
        mTpMaxPips = cTpMaxPips;
        mDailyOrderCount = 0;

        mTradingFromMills1 = cTradingFrom1 * Period.ONE_HOUR.getInterval();
        mTradingToMills1 = cTradingTo1 * Period.ONE_HOUR.getInterval();
        mTradingFromMills2 = cTradingFrom2 * Period.ONE_HOUR.getInterval();
        mTradingToMills2 = cTradingTo2 * Period.ONE_HOUR.getInterval();

        context.setSubscribedInstruments(java.util.Collections.singleton(cInstrument), true);

        mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
//        puts("OnMessage, type: " + message.getType() + ", reason: " + message.getReasons());
        IOrder order = message.getOrder();
        if (order != null && order.equals(mOrder)) {
            if (order.getState().equals(IOrder.State.FILLED)) {

                if (mLastOrderLabel == null || !mLastOrderLabel.equals(order.getLabel())) {
                    puts("submitOrder, order command:  " + order.getOrderCommand() + ", orderLabel: " + order.getLabel());
                    mLastOrderLabel = order.getLabel();
                    mDailyOrderCount = mDailyOrderCount + 1;
                }
                if (message.getType().equals(IMessage.Type.ORDER_CHANGED_OK) ||
                        message.getType().equals(IMessage.Type.ORDER_CHANGED_REJECTED)) {
                    mIsChanging = false;
                }
            } else if (order.getState().equals(IOrder.State.CLOSED)) {
                mProfitLossInPips = mProfitLossInPips + mOrder.getProfitLossInPips();
                if (mProfitLossInPips < mMaxLossInPips) {
                    mMaxLossInPips = mProfitLossInPips;
                }
                puts("Close order, label: " + mOrder.getLabel() +
                        ", profitLoss: " + mOrder.getProfitLossInPips() +
                        ", TotalProfitLoss: " + mProfitLossInPips +
                        ", MaxLoss: " + mMaxLossInPips +
                        ", mDailyOrderCount: " + mDailyOrderCount +
                        ", getFillTime: " + mSimpleDateFormat.format(mOrder.getFillTime()) +
                        ", getCloseTime: " + mSimpleDateFormat.format(mOrder.getCloseTime()));
            } else {
//                puts("OnMessage, other order: " + order.getLabel());
            }
        }
    }

    @Override
    public void onStop() throws JFException {
        puts("OnStop");
        // Do nothing, leave the orders.
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        if (!mIsInit) {
            init(tick.getTime());
            mIsInit = true;
            puts("init: " + instrument);
        }
        String orderLabel = getTodayOrderLabel(tick.getTime());
        if (canOpenOrderToday(orderLabel, tick.getTime())) {
            if (tick.getAsk() < mOrderPriceLow - cBreakPips * cInstrument.getPipValue()) {
                submitOrder(OrderCommand.SELL, orderLabel);
            } else if (tick.getBid() > mOrderPriceHigh + cBreakPips * cInstrument.getPipValue()) {
                submitOrder(OrderCommand.BUY, orderLabel);
            }
        }
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (instrument.equals(Instrument.EURUSD)) {
            if (period.equals(cOrderPeriod)) {
                updateHistoryBars(mOrderHistoryAskBars, askBar, cBackBarsHourly);
                updateOrderPriceHighLow();
                checkCloseOrder(askBar.getTime());
                puts(mSimpleDateFormat.format(askBar.getTime()) + " I'm living ~~~ ");
            }
//            if (cPeriod.equals(cCheckPeriod)) {
//                String orderLabel = getTodayOrderLabel(askBar.getTime());
//                if (canOpenOrderToday(orderLabel, askBar.getTime())) {
//                    if (askBar.getClose() < mOrderPriceLow - cBreakPips * cAInstrument.getPipValue()) {
//                        submitOrder(OrderCommand.SELL, orderLabel);
//                    } else if (askBar.getClose() > mOrderPriceHigh + cBreakPips * cAInstrument.getPipValue()) {
//                        submitOrder(OrderCommand.BUY, orderLabel);
//                    }
//                }
//            }
            if (period.equals(Period.DAILY)) {
                updateHistoryBars(mDailyHistoryAskBars, askBar, cBackBarsDaily);
                updateDailyVolatility();
                mDailyOrderCount = 0;
//            } else if (cPeriod.equals(Period.ONE_MIN)) {
//                updateOrderStopLoss();
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private void init(long currentTimeMillis) throws JFException {
        initHistoryBarsAndPriceSection(currentTimeMillis);
        initLastOrderAndLabel(currentTimeMillis);
        runTest();
    }

    public void initHistoryBarsAndPriceSection(long currentTimeMillis) throws JFException {
        long toTime = currentTimeMillis - (currentTimeMillis % cOrderPeriod.getInterval());
        long fromTime = toTime - cOrderPeriod.getInterval() * (cBackBarsHourly - 1);
        mOrderHistoryAskBars = mHistory.getBars(Instrument.EURUSD, cOrderPeriod, OfferSide.ASK, fromTime,
                toTime);
        updateOrderPriceHighLow();

        toTime = currentTimeMillis - (currentTimeMillis % Period.DAILY.getInterval());
        fromTime = toTime - Period.DAILY.getInterval() * (cBackBarsDaily - 1);
        mDailyHistoryAskBars = mHistory.getBars(Instrument.EURUSD, Period.DAILY, OfferSide.ASK, fromTime,
                toTime);
        updateDailyVolatility();
    }

    private void initLastOrderAndLabel(long currentTimeMillis) throws JFException {
        String todayLabel = getTodayOrderLabel(currentTimeMillis);
        mOrder = mEngine.getOrder(todayLabel);
        if (mOrder != null) {
            mLastOrderLabel = todayLabel;
        } else {
            List<IOrder> orders = mHistory.getOrdersHistory(Instrument.EURUSD,
                    currentTimeMillis - Period.DAILY.getInterval(), currentTimeMillis);
            if (orders != null) {
                for (IOrder order : orders) {
                    String label = getTodayOrderLabel(currentTimeMillis);
                    if (order.getLabel().equals(label)) {
                        mLastOrderLabel = label;
                        break;
                    }
                }
            }
        }
    }

    private boolean canOpenOrderToday(String orderLabel, long currentTimeMillis) {
        long intervalMillis = currentTimeMillis % Period.DAILY.getInterval();
        boolean res =
                (mOrder == null ||
                        mOrder.getState().equals(IOrder.State.CANCELED) ||
                        mOrder.getState().equals(IOrder.State.CLOSED)) &&
                        mDailyOrderCount < cMaxDailyOrderCount &&
//                        !orderLabel.equals(mLastOrderLabel) &&
                        ((intervalMillis >= mTradingFromMills1 && intervalMillis <= mTradingToMills1) ||
                                (intervalMillis >= mTradingFromMills2 && intervalMillis <= mTradingToMills2));
//        boolean state = (mOrder == null ||
//                mOrder.getState().equals(IOrder.State.CANCELED) ||
//                mOrder.getState().equals(IOrder.State.CLOSED));
//        if (!state) {
//            puts("canOpenOrderToday, order.state: " + mOrder.getState());
//        }

//        puts("canOpenOrderToday: " + res + ", " +
//                "orderLabel: " + orderLabel +
//                ", mLastOrderLabel: " + mLastOrderLabel +
//                ", intervalMillis: " + intervalMillis +
//                ", cTradingFrom: " + cTradingFrom * Period.ONE_HOUR.getInterval() +
//                ", cTradingTo: " + cTradingTo * Period.ONE_HOUR.getInterval() +
//                ", orderState: " + state);
        return res;
    }


    private void submitOrder(OrderCommand orderCommand, String orderLabel) throws JFException {
        ITick lastTick = mHistory.getLastTick(cInstrument);
        if (orderCommand.equals(OrderCommand.BUY)) {
            mOrder = mEngine.submitOrder(orderLabel, cInstrument, orderCommand, cAmount, 0, cSlippagePips,
                    getStopLossPrice(orderCommand, lastTick.getAsk()),
                    getTakeProfitPrice(orderCommand, lastTick.getBid()));
//            puts("submitOrder BUY, sl: " + getStopLossPrice(orderCommand, lastTick.getAsk()) +
//                    ", tp: " + getTakeProfitPrice(orderCommand, lastTick.getBid()));
        } else if (orderCommand.equals(OrderCommand.SELL)) {
            mOrder = mEngine.submitOrder(orderLabel, cInstrument, orderCommand, cAmount, 0, cSlippagePips,
                    getStopLossPrice(orderCommand, lastTick.getBid()),
                    getTakeProfitPrice(orderCommand, lastTick.getAsk()));
//            puts("submitOrder SELL, sl: " + getStopLossPrice(orderCommand, lastTick.getAsk()) +
//                    ", tp: " + getTakeProfitPrice(orderCommand, lastTick.getBid()));
        }
    }

    private void updateOrderStopLoss() throws JFException {
        if (!mIsChanging && mOrder != null && mOrder.getState().equals(IOrder.State.FILLED)) {
            OrderCommand orderCommand = mOrder.getOrderCommand();
            if (mOrder.getProfitLossInPips() > cMoveSlPips + 2) {
                if (orderCommand.equals(OrderCommand.BUY) && mOrder.getStopLossPrice() < mOrder.getOpenPrice()) {
                    mOrder.setStopLossPrice(mOrder.getOpenPrice() + 2 * cInstrument.getPipValue());
                    mIsChanging = true;
                } else if (orderCommand.equals(OrderCommand.SELL) && mOrder.getStopLossPrice() > mOrder.getOpenPrice()) {
                    mOrder.setStopLossPrice(mOrder.getOpenPrice() - 2 * cInstrument.getPipValue());
                    mIsChanging = true;
                }
            }
        }
    }

    private void checkCloseOrder(long currentTimeMillis) throws JFException {
        if (mOrder != null && mOrder.getState().equals(IOrder.State.FILLED)) {
            if (mOrder.getFillTime() + cHoldOrderMillis <= currentTimeMillis) {
                mOrder.close();
            }
        }
    }

    private String getTodayOrderLabel(long time) {
        return "StrategyHour_" + mSimpleDateFormat.format(new Date(time));
    }

    private double getStopLossPrice(OrderCommand orderCmd, double price) {
        if (orderCmd.equals(OrderCommand.BUY)) {
            return price - cSlPips * cInstrument.getPipValue();
        } else {
            return price + cSlPips * cInstrument.getPipValue();
        }
    }

    private double getTakeProfitPrice(OrderCommand orderCmd, double price) {
        if (orderCmd.equals(OrderCommand.BUY)) {
            return price + cTpMaxPips * cInstrument.getPipValue();
        } else {
            return price - cTpMaxPips * cInstrument.getPipValue();
        }
    }

    private void updateHistoryBars(List<IBar> historyBars, IBar bar, int backBarsCount) {
        historyBars.add(bar);
        Collections.sort(historyBars, new Comparator<IBar>() {
            @Override
            public int compare(IBar o1, IBar o2) {
                return o1.getTime() < o2.getTime() ? 1 : -1;
            }
        });
        if (historyBars.size() > backBarsCount) {
            historyBars.remove(historyBars.size() - 1);
        }
    }

    private void updateOrderPriceHighLow() {
        double low = 0.0;
        double high = 0.0;
        for (IBar bar : mOrderHistoryAskBars) {
            if (bar.getLow() < low || low <= 0.0) {
                low = bar.getLow();
            }
            if (bar.getHigh() > high) {
                high = bar.getHigh();
            }
        }

        mOrderPriceLow = low;
        mOrderPriceHigh = high + 2 * cInstrument.getPipValue();
//        puts("updateOrderPriceHighLow, mOrderPriceLow: " + mOrderPriceLow + ", mOrderPriceHigh: " + mOrderPriceHigh);
    }

    private void updateDailyVolatility() {
        double dailyVolatility = 0.0;
        for (IBar bar : mDailyHistoryAskBars) {
            dailyVolatility = dailyVolatility + (bar.getHigh() - bar.getLow());
        }
        mDailyVolatilityInPips = dailyVolatility / mDailyHistoryAskBars.size() / cInstrument.getPipValue();
        mSlPips = (int) (mDailyVolatilityInPips + 0.5);
        if (mSlPips > cSlPips) {
            mSlPips = cSlPips;
        }
        mTpMaxPips = (int) (mDailyVolatilityInPips * 2 + 0.5);
        if (mTpMaxPips > cTpMaxPips) {
            mTpMaxPips = cTpMaxPips;
        }
        puts("updateDailyVolatility, cSlPips: " + mSlPips + ", cTpMaxPips: " + mTpMaxPips);
    }

    private void puts(String str) {
        mConsole.getOut().println(str);
    }

    private void runTest() throws JFException {
    }

}