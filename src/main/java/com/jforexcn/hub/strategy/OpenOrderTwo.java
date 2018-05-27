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
import com.dukascopy.charts.data.datacache.CandleData;
import com.dukascopy.charts.data.datacache.tickbar.TickBarData;
import com.dukascopy.dds2.greed.agent.strategy.objects.Order;
import com.jforexcn.hub.indicator.OneBarHammer;
import com.jforexcn.hub.lib.Candle;
import com.jforexcn.hub.lib.HubConfiguration;

import java.util.List;

/**
 * Created by simple(simple.continue@gmail.com) on 25/05/2018.
 */

@RequiresFullAccess
@Library("/data/github/JForexKit/build/libs/JForexKit-3.0.jar")
public class OpenOrderTwo extends SubStrategy {

    private static String STRATEGY_TAG = OpenOrderTwo.class.getSimpleName();

    @Configurable(value = "period")
    public Period cPeriod = Period.ONE_HOUR;
    @Configurable("amountInMil")
    public double cAmountInMil = 0.01;
    @Configurable("slippage")
    public int cSlippage = 3;
    @Configurable(value = "maxReversePips", stepSize = 1)
    public int cMaxReversePips = 30;
    @Configurable(value = "minReversePips", stepSize = 1)
    public int cMinReversePips = 15;
    @Configurable(value = "lookback", stepSize = 1)
    public int cLookback = 3;

    @Configurable(value = "High Rectangle High Line", stepSize = 0.01)
    public double cHighRectHighLine = 0;
    @Configurable(value = "High Rectangle Low Line", stepSize = 0.01)
    public double cHighRectLowLine = 0;
    @Configurable(value = "Low Rectangle High Line", stepSize = 0.01)
    public double cLowRectHighLine = 0;
    @Configurable(value = "Low Rectangle Low Line", stepSize = 0.01)
    public double cLowRectLowLine = 0;

    private Instrument getInstrument() throws JFException {
        return cInstrument;
    }

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
        return getConfig(cInstrument.toString(), "amountInMil", Double.class);
    }

    private int getSlippage() throws JFException {
        if (!isFromHub()) {
            return cSlippage;
        }
        return getConfig(cInstrument.toString(), "slippage", Integer.class);
    }

    private int getMaxReversePips() throws JFException {
        if (!isFromHub()) {
            return cMaxReversePips;
        }
        return getConfig(cInstrument.toString(), "maxReversePips", Integer.class);
    }

    private int getMinReversePips() throws JFException {
        if (!isFromHub()) {
            return cMinReversePips;
        }
        return getConfig(cInstrument.toString(), "minReversePips", Integer.class);
    }

    private int getLookback() throws JFException {
        if (!isFromHub()) {
            return cLookback;
        }
        return getConfig(cInstrument.toString(), "lookback", Integer.class);
    }

    private double getHighRectHighLine() throws JFException {
        if (!isFromHub()) {
            return cHighRectHighLine;
        }
        return getConfig(cInstrument.toString(),"highRectHighLine", Double.class);
    }

    private double getHighRectLowLine() throws JFException {
        if (!isFromHub()) {
            return cHighRectLowLine;
        }
        return getConfig(cInstrument.toString(), "highRectLowLine", Double.class);
    }


    private double getLowRectHighLine() throws JFException {
        if (!isFromHub()) {
            return cLowRectHighLine;
        }
        return getConfig(cInstrument.toString(), "lowRectHighLine", Double.class);
    }


    private double getLowRectLowLine() throws JFException {
        if (!isFromHub()) {
            return cLowRectLowLine;
        }
        return getConfig(cInstrument.toString(), "lowRectLowLine", Double.class);
    }

    @Override
    public void onStart(IContext context) throws JFException {
        super.onStart(context);
        init(context, STRATEGY_TAG);
        helper.logDebug(STRATEGY_TAG + " start!");
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
                HubConfiguration.printConfig(mContext, this.getClass().getSimpleName());

                long prevBarTime = helper.mHistory.getPreviousBarStart(period, helper.mHistory.getLastTick(instrument).getTime());
                List<IBar> bars = helper.mHistory.getBars(Instrument.EURUSD, Period.ONE_HOUR, OfferSide.BID, Filter.ALL_FLATS, getLookback(), prevBarTime, 0);
                IBar mergedBar = mergeBars(bars);
                // High Rect
                if (mergedBar.getHigh() > getHighRectLowLine() && mergedBar.getClose() < getHighRectLowLine()) { // High Rect
                    if (mergedBar.getHigh() - mergedBar.getClose() >= getMinReversePips() * getInstrument().getPipValue() &&
                            mergedBar.getHigh() - mergedBar.getClose() <= getMaxReversePips() * getInstrument().getPipValue()) {
                        // Open Sell
                        long time = helper.mHistory.getLastTick(instrument).getTime();
                        String label = getLabel(instrument, time);
                        double stopLoss = mergedBar.getHigh() + getSlippage() * getInstrument().getPipValue();
                        helper.submitOrder(label, instrument, IEngine.OrderCommand.SELL, getAmountInMil(), 0, getSlippage(), stopLoss, 0, time);
                    }
                } else if (mergedBar.getLow() < getLowRectHighLine() && mergedBar.getClose() > getLowRectHighLine()) { // Low Rect
                    if (mergedBar.getClose() - mergedBar.getLow() >= getMinReversePips() * getInstrument().getPipValue() &&
                            mergedBar.getClose() - mergedBar.getLow() <= getMaxReversePips() * getInstrument().getPipValue()) {
                        // Open Buy
                        long time = helper.mHistory.getLastTick(instrument).getTime();
                        String label = getLabel(instrument, time);
                        double stopLoss = mergedBar.getLow() - getSlippage() * getInstrument().getPipValue();
                        helper.submitOrder(label, instrument, IEngine.OrderCommand.BUY, getAmountInMil(), 0, getSlippage(), stopLoss, 0, time);
                    }
                }
            }
        }
    }

    private IBar mergeBars(List<IBar> bars) {
        Candle candle = new Candle();
        candle.mergeBars(bars);
        return candle;
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

    private String getLabel(Instrument instrument, long time) {
        return helper.getStrategyTag() + "_" +
                instrument.name() + "_" +
                time;
    }


}

