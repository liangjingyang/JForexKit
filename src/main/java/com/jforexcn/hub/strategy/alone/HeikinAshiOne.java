package com.jforexcn.hub.strategy.alone;

/**
 * Created by simple on 24/05/2018.
 */

import com.dukascopy.api.Configurable;
import com.dukascopy.api.Filter;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Library;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.RequiresFullAccess;
import com.jforexcn.hub.strategy.SubStrategy;

import java.util.ArrayList;
import java.util.List;

@RequiresFullAccess
@Library("/data/github/JForexKit/build/libs/JForexKit-3.0.jar")
public class HeikinAshiOne extends SubStrategy {
    private static String STRATEGY_TAG = "HeikinAshiOne";

    @Configurable("Period")
    public Period cPeriod = Period.ONE_HOUR;

    private Period getPeriod() throws JFException {
        if (!isFromHub()) {
            return cPeriod;
        }
        return getConfig(cInstrument.toString(), "period", Period.class);
    }

    @Override
    public void onStart(IContext context) throws JFException {
        init(context, STRATEGY_TAG);
        helper.logDebug(STRATEGY_TAG + " start!");
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        super.onMessage(message);
        IOrder order = message.getOrder();
        if (order != null && helper.isStrategyInstrument(order.getInstrument())) {
            if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {

            } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {

            } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {

            } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {

            } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {

            } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {

            } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {
                order.close();
            } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {

            }
        }
    }
    @Override
    public void onStop() throws JFException {
        helper.logDebug(STRATEGY_TAG + " stop!");
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (instrument.equals(cInstrument)) {
            if (period.equals(Period.ONE_HOUR)) {
                IEngine.OrderCommand askCommand = calculateHeikinAshi(instrument, OfferSide.ASK);
                if (IEngine.OrderCommand.BUY.equals(askCommand)) {
                    closeOrder(instrument, IEngine.OrderCommand.SELL);
                    openOrder(instrument, IEngine.OrderCommand.BUY);
                } else if (IEngine.OrderCommand.SELL.equals(askCommand)) {
                    closeOrder(instrument, IEngine.OrderCommand.BUY);
                    openOrder(instrument, IEngine.OrderCommand.SELL);
                }
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private void closeOrder(Instrument instrument, IEngine.OrderCommand command) throws JFException {
        for (IOrder order : helper.mEngine.getOrders()) {
            if (helper.isStrategyOrder(order) && helper.isStrategyInstrument(instrument)) {
                if (command.equals(order.getOrderCommand())) {
                    order.close();
                }
            }
        }
    }

    private List<IOrder> getOrders(Instrument instrument, IEngine.OrderCommand command) throws JFException {
        List<IOrder> orders = new ArrayList<>();
        for (IOrder order : helper.mEngine.getOrders()) {
            if (helper.isStrategyOrder(order) && helper.isStrategyInstrument(instrument)) {
                if (command.equals(order.getOrderCommand())) {
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    private IOrder getLastOrder(Instrument instrument, IEngine.OrderCommand command) throws JFException {
        IOrder lastOrder = null;
        for (IOrder order : helper.mEngine.getOrders()) {
            if (helper.isStrategyOrder(order) && helper.isStrategyInstrument(instrument)) {
                if (command.equals(order.getOrderCommand())) {
                    if (lastOrder == null) {
                        lastOrder = order;
                    } else {
                        if (order.getCreationTime() > lastOrder.getCreationTime()) {
                            lastOrder = order;
                        }
                    }
                }
            }
        }
        return lastOrder;
    }

    private void openOrder(Instrument instrument, IEngine.OrderCommand command) throws JFException {
        if (getOrders(instrument, command).size() < 4 ) {
            IOrder lastOrder = getLastOrder(instrument, command);
            ITick lastTick = helper.mHistory.getLastTick(instrument);
            long time = lastTick.getTime();
            if (lastOrder == null || time >= lastOrder.getCreationTime() + 3 * 3600 * 1000 - 3000) {
                String label = getLabel(cInstrument, time);
                helper.submitOrder(label, instrument, command, 0.01, 0, 3, time);
            }
        }
    }

    private IEngine.OrderCommand calculateHeikinAshi(Instrument calculateInstrument, OfferSide offerSide) throws JFException {
        OfferSide[] offerSides = new OfferSide[] {
                offerSide
        };
        IIndicators.AppliedPrice[] appliedPrices = null;
        Object[] optParams = new Object[] {};
        long time = helper.mHistory.getBar(calculateInstrument, getPeriod(), OfferSide.ASK, 1).getTime();
        // calculateIndicator(Instrument instrument, Period period, OfferSide[] offerSides, String functionName,
        //        AppliedPrice[] inputTypes, Object[] optParams, Filter filter, int numberOfCandlesBefore, long time, int numberOfCandlesAfter)
        Object[] result = helper.mIndicators.calculateIndicator(calculateInstrument, getPeriod(), offerSides, "HEIKINASHI", appliedPrices,
                optParams, Filter.ALL_FLATS, 5, time, 0);
        double[] lastPrices1 = (double[]) ((Object[]) result[0])[1];
        double[] lastPrices2 = (double[]) ((Object[]) result[0])[2];
        double[] lastPrices3 = (double[]) ((Object[]) result[0])[3];
        double[] lastPrices4 = (double[]) ((Object[]) result[0])[4];
        if (isLong(lastPrices3) && isShort(lastPrices4)) {
            return IEngine.OrderCommand.SELL;
        } else if (isShort(lastPrices3) && isLong(lastPrices4)) {
            return IEngine.OrderCommand.BUY;
        } else {
            if (isLong(lastPrices2) && isLong(lastPrices3) && isLong(lastPrices4)) {
                return IEngine.OrderCommand.BUY;
            } else if (isShort(lastPrices2) && isShort(lastPrices3) && isShort(lastPrices4)) {
                return IEngine.OrderCommand.SELL;
            }
        }
        return null;
    }

    private boolean isLong(double[] prices) {
        int open = 0;
        int close = 1;
        return prices[open] < prices[close];
    }

    private boolean isShort(double[] prices) {
        int open = 0;
        int close = 1;
        return prices[open] > prices[close];
    }

    private String getLabel(Instrument instrument, long time) {
        return helper.getStrategyTag() + "_" +
                instrument.name() + "_" +
                time;
    }
}