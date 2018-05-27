package com.jforexcn.hub.strategy;

import com.dukascopy.api.Configurable;
import com.dukascopy.api.DataType;
import com.dukascopy.api.Filter;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Library;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.RequiresFullAccess;
import com.dukascopy.api.feed.FeedDescriptor;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.feed.ITailoredFeedDescriptor;
import com.dukascopy.api.feed.ITickBar;
import com.jforexcn.hub.indicator.OneBarHammer;
import com.jforexcn.hub.lib.FeedDescriptors;

/**
 * Created by simple(simple.continue@gmail.com) on 06/05/2018.
 */

@RequiresFullAccess
@Library("/data/github/JForexKit/build/libs/JForexKit-3.0.jar")
public class OpenOrderOne extends StopLossTwo implements IStrategy {

    private static String STRATEGY_TAG = OpenOrderOne.class.getSimpleName();

    @Configurable(value = "period")
    public Period cPeriod = Period.ONE_HOUR;
    @Configurable("amountInMil")
    public double cAmountInMil = 0.01;
    @Configurable("slippage")
    public int cSlippage = 3;
    @Configurable("maxStopLossPips")
    public int cmaxStopLossPips = 30;
    @Configurable(value = "bodyPercent", stepSize = 0.01)
    public double cBodyPercent = 0.35;
    @Configurable(value = "shortShadowPercent", stepSize = 0.01)
    public double cShortShadowPercent = 0.15;
    @Configurable(value = "longShadowPercent", stepSize = 0.01)
    public double cLongShadowPercent = 0.55;
    @Configurable(value = "barMinPips", stepSize = 1)
    public int cBarMinPips = 10;
    @Configurable(value = "breakPips", stepSize = 1)
    public int cBreakPips = 5;
    @Configurable(value = "lookback", stepSize = 1)
    public int cLookback = 2;
    @Configurable(value = "shortEMATimePeriod")
    public int cShortEMATimePeriod = 20;
    @Configurable(value = "longEMATimePeriod")
    public int cLongEMATimePeriod = 60;

    private String indicatorName;
    private IFeedDescriptor feedDescriptor;

    private Period getPeriod() throws JFException {
        if (!isFromHub()) {
            return cPeriod;
        }
        return getConfig(cInstrument.toString(), "period", Period.class);
    }

    private double getAmountInMil() throws JFException {
        if (!isFromHub()) {
            return cAmountInMil;
        }
        return getConfig("amountInMil", Double.class);
    }

    private int getSlippage() throws JFException {
        if (!isFromHub()) {
            return cSlippage;
        }
        return getConfig(cInstrument.toString(), "slippage", Integer.class);
    }


    private int getBarMinPips() throws JFException {
        if (!isFromHub()) {
            return cBarMinPips;
        }
        return getConfig(cInstrument.toString(), "barMinPips", Integer.class);
    }

    private int getBreakPips() throws JFException {
        if (!isFromHub()) {
            return cBreakPips;
        }
        return getConfig(cInstrument.toString(), "breakPips", Integer.class);
    }

    private int getLookback() throws JFException {
        if (!isFromHub()) {
            return cLookback;
        }
        return getConfig(cInstrument.toString(), "lookback", Integer.class);
    }

    private double getBodyPercent() throws JFException {
        if (!isFromHub()) {
            return cBodyPercent;
        }
        return getConfig("bodyPercent", Double.class);
    }

    private double getShortShadowPercent() throws JFException {
        if (!isFromHub()) {
            return cShortShadowPercent;
        }
        return getConfig("shortShadowPercent", Double.class);
    }

    private double getLongShadowPercent() throws JFException {
        if (!isFromHub()) {
            return cLongShadowPercent;
        }
        return getConfig("longShadowPercent", Double.class);
    }

    private int getShortEMATimePeriod() throws JFException {
        if (!isFromHub()) {
            return cShortEMATimePeriod;
        }
        return getConfig(cInstrument.toString(), "shortEMATimePeriod", Integer.class);
    }

    private int getLongEMATimePeriod() throws JFException {
        if (!isFromHub()) {
            return cLongEMATimePeriod;
        }
        return getConfig(cInstrument.toString(), "longEMATimePeriod", Integer.class);
    }

    @Override
    public void onStart(IContext context) throws JFException {
        super.onStart(context);
        init(context, STRATEGY_TAG);
        helper.logDebug(STRATEGY_TAG + " start!");

        indicatorName = mIndicators.registerCustomIndicator(OneBarHammer.class);
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        super.onTick(instrument, tick);
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        super.onBar(instrument, period, askBar, bidBar);
        if (helper.isStrategyInstrument(instrument)) {
            if (period.equals(getPeriod())) {
                tryCreateOrder(instrument, askBar.getTime());
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
        super.onAccount(account);
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        super.onMessage(message);
        if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {

        }
    }

    @Override
    public void onStop() throws JFException {
        super.onStop();
        helper.logDebug(STRATEGY_TAG + " OnStop!");
    }

    private IOrder getLastOrder() throws JFException {
        IOrder lastOrder = null;
        for (IOrder order : mEngine.getOrders()) {
            if (lastOrder == null) {
                lastOrder = order;
            } else {
                if (order.getInstrument().equals(cInstrument) &&
                        order.getCreationTime() > lastOrder.getCreationTime()) {
                    lastOrder = order;
                }
            }
        }
        return lastOrder;
    }

    private boolean canCreateOrder(long time) throws JFException {
        IOrder lastOrder = getLastOrder();
        if (lastOrder != null) {
            if (lastOrder.getFillTime() + getPeriod().getInterval() > time) {
                return false;
            }
        }
        return true;
    }

    private void tryCreateOrder(Instrument instrument, long time) throws JFException {
        if (canCreateOrder(time)) {
            IBar lastAskBar = mHistory.getBar(instrument, getPeriod(), OfferSide.ASK, 1);
            IBar lastBidBar = mHistory.getBar(instrument, getPeriod(), OfferSide.BID, 1);
            ITick lastTick = mHistory.getLastTick(instrument);
            Hammer askHammer = getHammer(instrument, OfferSide.ASK, lastAskBar);
            Hammer bidHammer = getHammer(instrument, OfferSide.BID, lastBidBar);
            String label = null;
            long goodTillTime = getPeriod().getInterval() * 2;
            double price, stopLoss, takeProfit;
            if (Hammer.TO_BUY.equals(askHammer) &&
                    Hammer.TO_BUY.equals(bidHammer)) {
                helper.logDebug("====== TO_BUY");
                price = lastAskBar.getHigh() + 2 * instrument.getPipValue();
                if (price >= lastTick.getAsk()) {
                    label = getLabel(instrument, time);
                    stopLoss = lastAskBar.getLow() - 2 * instrument.getPipValue();
                    takeProfit = 0;
                    helper.submitOrder(label, instrument, IEngine.OrderCommand.BUYLIMIT, getAmountInMil(), price, getSlippage(), stopLoss, takeProfit, goodTillTime, time);
                }
            } else if (Hammer.TO_SELL.equals(askHammer) &&
                    Hammer.TO_SELL.equals(bidHammer)) {
                helper.logDebug("====== TO_SELL");
                price = lastBidBar.getLow() - 2 * instrument.getPipValue();
                if (price <= lastTick.getBid()) {
                    label = getLabel(instrument, time);
                    stopLoss = lastAskBar.getHigh() + 2 * instrument.getPipValue();
                    takeProfit = 0;
                    helper.submitOrder(label, instrument, IEngine.OrderCommand.SELLSTOP, getAmountInMil(), price, getSlippage(), stopLoss, takeProfit, goodTillTime, time);
                }
            }
        }
    }

    private String getLabel(Instrument instrument, long time) {
        return helper.getStrategyTag() + "_" +
                instrument.name() + "_" +
                time;
    }

    enum Hammer {
        NONE,
        TO_BUY,
        TO_SELL
    }

    private Hammer getHammer(Instrument instrument, OfferSide offerSide, IBar lastBar) throws JFException {
        OfferSide[] offerSides = new OfferSide[] {
               offerSide
        };
//        IIndicators.AppliedPrice[] inputTypes = new IIndicators.AppliedPrice[] {
//                IIndicators.AppliedPrice.CLOSE
//        };
        Object[] optParams = new Object[] {
                getBodyPercent(),
                getShortShadowPercent(),
                getLongShadowPercent(),
                getBarMinPips(),
                getLookback(),
                getBreakPips()
        };

        long time = lastBar.getTime();
        feedDescriptor = new FeedDescriptor();
        feedDescriptor.setDataType(DataType.TIME_PERIOD_AGGREGATION);
        feedDescriptor.setFilter(Filter.ALL_FLATS);
        feedDescriptor.setInstrument(instrument);
        feedDescriptor.setPeriod(getPeriod());
        Object[] result = mIndicators.calculateIndicator(feedDescriptor, offerSides, indicatorName,
                null, optParams, 1, time, 0);
        int toBuyHammer = ((int[]) result[0])[0];
        int toSellHammer = ((int[]) result[1])[0];

        double[] shortEMAs = mIndicators.ema(instrument, getPeriod(), offerSide, IIndicators.AppliedPrice.CLOSE, getShortEMATimePeriod(), Filter.ALL_FLATS, 1, time, 0);
        double[] longEMAs = mIndicators.ema(instrument, getPeriod(), offerSide, IIndicators.AppliedPrice.CLOSE, getLongEMATimePeriod(), Filter.ALL_FLATS, 1, time, 0);

        if (toBuyHammer == 1 && toSellHammer == 0 && shortEMAs[0] > longEMAs[0]) {
            return Hammer.TO_BUY;
        } else if (toBuyHammer == 0 && toSellHammer == 1 && shortEMAs[0] < longEMAs[0]) {
            return Hammer.TO_SELL;
        } else {
            return Hammer.NONE;
        }
    }
}

