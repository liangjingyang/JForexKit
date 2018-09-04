package com.jforexcn.inbox.strategy;

import com.dukascopy.api.Configurable;
import com.dukascopy.api.Filter;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by simple(simple.continue@gmail.com) on 15/12/2017.
 */

public class HammerOrBreakSignal implements IStrategy {

    @Configurable(value = "Instrument", modifiable = false)
    public Instrument cInstrument = Instrument.EURUSD;
    @Configurable(value = "Amount in mil", modifiable = false)
    public double cAmount = 0.01;
    @Configurable(value = "Slippage", modifiable = false)
    public int cSlippage = 3;
    @Configurable(value = "Order close after how many bars", modifiable = false)
    public int cCloseBarCount = 3;
    @Configurable(value = "Stop loss pips", modifiable = false)
    public int cStopLossPips = 30;
    @Configurable(value = "Take profit pips", modifiable = false)
    public int cTakeProfitPips = 60;
    @Configurable(value = "Close by SlTp", modifiable = false)
    public boolean cCloseBySLTP = true;
    @Configurable(value = "Period", modifiable = false)
    public Period cPeriod = Period.FOUR_HOURS;
    @Configurable(value = "Close edge factor, smaller stronger", modifiable = false)
    public double cCloseEdgeFactor = 0.4;
    @Configurable(value = "Bar rect min pips", modifiable = false)
    public double cBarRectPips = 10;
    @Configurable(value = "Look back bars count for hammer ( 1 ~ 20 )", modifiable = false)
    public int cHammerBarCount = 5;
    @Configurable(value = "Shadow scale for hammer ( 0 ~ 1 )", modifiable = false)
    public double cHammerShadowScale = 0.55;
    @Configurable(value = "Look back bars count for break ( 4 ~ 20 )", modifiable = false)
    public int cBreakBarCount = 12;
    @Configurable(value = "Break range scale", modifiable = false)
    public double cBreakRangeScale = 1;

    private IEngine engine;
    private IHistory history;
    private IIndicators indicators;
    private IConsole console;

    private List<IBar> askBarList;
    private List<IBar> bidBarList;

    private static String TAG = "HammerOrBreakSignal";

    @Override
    public void onStart(IContext context) throws JFException {
        engine = context.getEngine();
        history = context.getHistory();
        indicators = context.getIndicators();
        console = context.getConsole();

        Set<Instrument> instrumentSet = new HashSet<Instrument>();
        instrumentSet.add(cInstrument);
        context.setSubscribedInstruments(instrumentSet, true);
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {

    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (instrument.equals(cInstrument) && period.equals(cPeriod)) {
            tryCloseOrder();
            prepareBars(instrument, period);
            IEngine.OrderCommand command = checkHammer();
            if (IEngine.OrderCommand.BUY.equals(command)) {
                double price = history.getLastTick(instrument).getAsk();
                if (cCloseBySLTP) {
                    double sl = price - cStopLossPips * instrument.getPipValue();
                    double tp = price + cTakeProfitPips * instrument.getPipValue();
                    engine.submitOrder(getLabel(), instrument, command, cAmount, price, cSlippage, sl, tp);
                } else {
                    engine.submitOrder(getLabel(), instrument, command, cAmount, price, cSlippage);
                }
            } else if (IEngine.OrderCommand.SELL.equals(command)) {
                double price = history.getLastTick(instrument).getBid();
                if (cCloseBySLTP) {
                    double sl = price + cStopLossPips * instrument.getPipValue();
                    double tp = price - cTakeProfitPips * instrument.getPipValue();
                    engine.submitOrder(getLabel(), instrument, command, cAmount, price, cSlippage, sl, tp);
                } else {
                    engine.submitOrder(getLabel(), instrument, command, cAmount, price, cSlippage);
                }
            }
        }
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder o = message.getOrder();
        if (o != null && isStrategyInstrument(o.getInstrument())) {
            if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {
                o.close();
            } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {

    }

    @Override
    public void onStop() throws JFException {

    }

    private String getLabel() throws JFException {
        return TAG + "_" + history.getLastTick(cInstrument).getTime();
    }

    private void tryCloseOrder() throws JFException {
        for (IOrder o : getOrders()) {
            if (shouldCloseOrderByBar(o)) {
                o.close();
            }
        }
    }

    private boolean shouldCloseOrderByBar(IOrder order) throws JFException {
        if (cCloseBySLTP) {
            return false;
        }
        long filledTime = order.getFillTime();
        long lastTickTime = history.getLastTick(order.getInstrument()).getTime();
        return lastTickTime - filledTime >= cPeriod.getInterval() * cCloseBarCount - 30000;
    }

    private List<IOrder> getOrders() throws JFException {
        List<IOrder> orders = new ArrayList<>();
        for (IOrder o : engine.getOrders()) {
            if (isStrategyInstrument(o.getInstrument())) {
                orders.add(o);
            }
        }
        return orders;
    }

    private boolean isStrategyInstrument(Instrument instrument) {
        return cInstrument.equals(instrument);
    }

    private void prepareBars(Instrument instrument, Period period) throws JFException {
        long prevPrevBarTime = history.getPreviousBarStart(period, history.getLastTick(instrument).getTime());
        int numberOfCandlesBefore = Math.max(cHammerBarCount, cBreakBarCount) + 5;
        askBarList = history.getBars(instrument, period, OfferSide.ASK, Filter.WEEKENDS, numberOfCandlesBefore, prevPrevBarTime, 1);
        bidBarList = history.getBars(instrument, period, OfferSide.BID, Filter.WEEKENDS, numberOfCandlesBefore, prevPrevBarTime, 1);
    }

    // three bar hammer
    private IEngine.OrderCommand checkHammer() throws JFException {
        int size = askBarList.size();
        IBar lastAskBar = askBarList.get(size - 1);
        IEngine.OrderCommand command = getCommandByLastBar(lastAskBar);
        console.getInfo().println("lastAskBar: OHLC: " + lastAskBar.getOpen() + ", " + lastAskBar.getHigh() + ", " + lastAskBar.getLow() + ", " + lastAskBar.getClose() + ", Command:" + command);
        if (command == null) {
            return null;
        }

        MergedBar mergedBar = new MergedBar();
//        for (int i = size - 1; i >= size - 1 - cHammerBarCount; i--) {
//            mergedBar.merge(askBarList.get(i));
//            console.getInfo().println("merged BarCount: " + mergedBar.getBarCount());
//            if (isHammer(mergedBar, command)) {
//                return command;
//            }
//        }
//
//        mergedBar = new MergedBar();
        int j = 0;
        for (int i = size - 2; i >= size - 1 - cBreakBarCount; i--) {
            mergedBar.merge(askBarList.get(i));
            j = j + 1;
            if (j >= 3 && isBreak(mergedBar, lastAskBar, command)) {
                return command;
            }
        }

        return null;
    }

    protected boolean isHammer(MergedBar mergedBar, IEngine.OrderCommand command) {
        double allRange = mergedBar.getHigh() - mergedBar.getLow();
        console.getInfo().println("allRange: " + allRange);
        console.getInfo().println("BUY, shadow: " + (Math.min(mergedBar.getOpen(), mergedBar.getClose()) - mergedBar.getLow()));
        console.getInfo().println("Sell, shadow: " + (mergedBar.getHigh() - Math.max(mergedBar.getOpen(), mergedBar.getClose())));
        return (((Math.min(mergedBar.getOpen(), mergedBar.getClose()) - mergedBar.getLow()) / allRange > cHammerShadowScale) && IEngine.OrderCommand.BUY.equals(command)) ||
                (((mergedBar.getHigh() - Math.max(mergedBar.getOpen(), mergedBar.getClose())) / allRange > cHammerShadowScale) && IEngine.OrderCommand.SELL.equals(command));
    }

    protected boolean isBreak(MergedBar mergedBar, IBar lastBar, IEngine.OrderCommand command) {
        double rect = mergedBar.getMergedRect();
        double maxRect = mergedBar.getMaxRect();
        double expectedRect = cBarRectPips * cInstrument.getPipValue();
        if (maxRect <= expectedRect || rect <= expectedRect) {
            console.getInfo().println("rect: " + rect);
            console.getInfo().println("lastBar.getClose(): " + lastBar.getClose() + ", mergedBar.getTop(): " + mergedBar.getTop() + ", mergedBar.getBottom(): " + mergedBar.getBottom());
            return (
                    IEngine.OrderCommand.BUY.equals(command) &&
                            lastBar.getClose() > mergedBar.getTop() &&
                            (lastBar.getClose() - mergedBar.getTop()) / rect > cBreakRangeScale
            ) || (
                    IEngine.OrderCommand.SELL.equals(command) &&
                            mergedBar.getBottom() > lastBar.getClose() &&
                            (mergedBar.getBottom() - lastBar.getClose()) / rect > cBreakRangeScale
            );
        }
        return false;
    }

    // Bar range more than a half of the bar, and bar range must larger than cBarRectPips
    private IEngine.OrderCommand getCommandByLastBar(IBar bar) {
        double rectRange = Math.abs(bar.getOpen() - bar.getClose());
        if (rectRange > cBarRectPips * cInstrument.getPipValue()) {
            double allRange = bar.getHigh() - bar.getLow();
            if (bar.getHigh() - bar.getClose() < allRange * cCloseEdgeFactor) {
                return IEngine.OrderCommand.BUY;
            } else if (bar.getClose() - bar.getLow() < allRange * cCloseEdgeFactor) {
                return IEngine.OrderCommand.SELL;
            }
        }
        return null;
    }

    public static class MergedBar implements IBar {

        private double open;
        private double close;
        private double low;
        private double high;
        private double volume;
        private double averageRect;
        private double maxRect;
        private double top;
        private double bottom;
        private int barCount;
        private long firstBarTime;
        private long lastBarTime;

        @Override
        public double getOpen() {
            return open;
        }

        @Override
        public double getClose() {
            return close;
        }

        @Override
        public double getLow() {
            return low;
        }

        @Override
        public double getHigh() {
            return high;
        }

        @Override
        public double getVolume() {
            return volume;
        }

        @Override
        public long getTime() {
            return firstBarTime;
        }

        public double getMergedRect() {
            return top - bottom;
        }

        public double getTop() {
            return top;
        }

        public double getBottom() {
            return bottom;
        }

        public double getBarCount() { return barCount; }

        public double getAverageRect() { return averageRect; }

        public double getMaxRect() { return maxRect; }

        public void merge(IBar bar) {

            if (bar.getOpen() > top || top == 0) {
                top = bar.getOpen();
            }
            if (bar.getOpen() < bottom || bottom == 0) {
                bottom = bar.getOpen();
            }
            if (bar.getClose() > top || top == 0) {
                top = bar.getClose();
            }
            if (bar.getClose() < bottom || bottom == 0) {
                bottom = bar.getClose();
            }
            if (bar.getHigh() > high || high == 0) {
                high = bar.getHigh();
            }
            if (bar.getLow() < low || low == 0) {
                low = bar.getLow();
            }
            if (firstBarTime == 0) {
                firstBarTime = bar.getTime();
                lastBarTime = bar.getTime();
                close = bar.getClose();
                open = bar.getOpen();
            } else {
                if (firstBarTime > bar.getTime()) {
                    firstBarTime = bar.getTime();
                    open = bar.getOpen();
                }
                if (lastBarTime < bar.getTime()) {
                    lastBarTime = bar.getTime();
                    close = bar.getClose();
                }

            }
            volume = volume + bar.getVolume();
            double rect = Math.abs(bar.getOpen() - bar.getClose());
            if (maxRect < rect) {
                maxRect = rect;
            }
            double additionRealRange = averageRect * barCount + rect;
            barCount = barCount + 1;
            averageRect = additionRealRange / barCount;
        }
    }
}
