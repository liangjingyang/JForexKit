package com.jforexcn.inbox.strategy;

/**
 * Created by simple on 1/15/16.
 */

import com.dukascopy.api.*;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.feed.IFeedListener;
import com.dukascopy.api.feed.util.TimePeriodAggregationFeedDescriptor;


public class TestStrategy implements IStrategy, IFeedListener {

    @Configurable("Feed")
    public IFeedDescriptor feedDescriptor =
            new TimePeriodAggregationFeedDescriptor(
                    Instrument.EURUSD,
                    Period.TEN_SECS,
                    OfferSide.ASK,
                    Filter.NO_FILTER
            );
    @Configurable("Amount")
    public double amount = 0.001;
    @Configurable("Stop loss")
    public int slPips = 10;
    @Configurable("Take profit")
    public int tpPips = 10;
    @Configurable(value = "", description = "close the existing order on creation of a new order if it has not been closed yet")
    public boolean closePreviousOrder = true;
    @Configurable("")
    public int smaTimePeriod = 2;

    private final static int LAST = 2;
    private final static int PREV = 1;
    private final static int SECOND_TO_LAST = 0;

    private IEngine engine;
    private IHistory history;
    private IConsole console;
    private IOrder order;
    private IIndicators indicators;

    @Override
    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.history = context.getHistory();
        this.console = context.getConsole();
        this.indicators = context.getIndicators();

        // subscribe the cInstrument that we are going to work with
        context.setSubscribedInstruments(java.util.Collections.singleton(feedDescriptor.getInstrument()), true);
        if (feedDescriptor.getDataType() == DataType.TICKS) {
            console.getWarn().println("The strategy can't trade according to the tick feed!");
            context.stop();
        }

        // subscribe to feed
        context.subscribeToFeed(feedDescriptor, this);
    }

    @Override
    public void onFeedData(IFeedDescriptor feedDescriptor, ITimedData feedData) {
        try {
            // we need 3 last indicator values to detect the trend change
            double[] sma = indicators.sma(feedDescriptor, AppliedPrice.CLOSE, feedDescriptor.getOfferSide(), smaTimePeriod).calculate(3, feedData.getTime(), 0);
            if (sma[PREV] > sma[SECOND_TO_LAST] && sma[LAST] < sma[PREV]) { // downtrend
                console.getOut().println("Encountered downtrend");
                submitOrder(false);
            } else if (sma[PREV] < sma[SECOND_TO_LAST] && sma[LAST] > sma[PREV]) { // uptrend
                console.getOut().println("Encountered uptrend");
                submitOrder(true);
            }
        } catch (Exception e) {
            console.getErr().println(e);
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
    }

    private void submitOrder(boolean isLong) throws JFException {
        double slPrice, tpPrice;
        Instrument instrument = feedDescriptor.getInstrument();
        ITick lastTick = history.getLastTick(instrument);
        OrderCommand orderCmd = isLong ? OrderCommand.BUY : OrderCommand.SELL;

        // calculating stop loss and take profit prices
        if (isLong) {
            slPrice = lastTick.getAsk() - slPips * instrument.getPipValue();
            tpPrice = lastTick.getAsk() + tpPips * instrument.getPipValue();
        } else {
            slPrice = lastTick.getBid() + slPips * instrument.getPipValue();
            tpPrice = lastTick.getBid() - tpPips * instrument.getPipValue();
        }

        // close previously opened order
        if (closePreviousOrder && order != null && order.getState() == IOrder.State.FILLED) {
            // we don't use order.waitForUpdate, since our next actions don't depend on the previous order anymore
            order.close();
        }

        // open new order
        console.getOut().println("Opening " + orderCmd + " order");
        order = engine.submitOrder("trend_" + orderCmd.toString() + System.currentTimeMillis(), instrument, orderCmd, amount, 0, 20, slPrice, tpPrice);
    }

    @Override
    public void onStop() throws JFException {
        if (order != null && (order.getState() == IOrder.State.FILLED || order.getState() == IOrder.State.OPENED)) {
            order.close();
        }
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }
}
