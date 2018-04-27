package com.jforexcn.wiki.strategy;

import java.util.Arrays;
import java.util.List;

import com.dukascopy.api.*;
import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.IIndicators.MaType;
import com.dukascopy.api.util.DateUtils;

/**
 * The following strategy demonstrates how to log the most commonly used
 * values and arrays of values in in JForex API
 *
 */
public class LoggingValues implements IStrategy {

    private IConsole console;
    private IHistory history;
    private IIndicators indicators;

    @Override
    public void onStart(IContext context) throws JFException {
        console = context.getConsole();
        history = context.getHistory();
        indicators = context.getIndicators();

        IBar prevBar = history.getBar(Instrument.EURUSD, Period.TEN_SECS, OfferSide.BID, 1);
        long time = prevBar.getTime();

        double max = indicators.max(Instrument.EURUSD, Period.TEN_SECS, OfferSide.BID, AppliedPrice.CLOSE, 20, 1);
        //log single double value; time value; object (given that it has an overridden toString method)
        console.getOut().format("single-value max indicator result: %.5f previous bar time=%s previous bar=%s", max, DateUtils.format(time), prevBar).println();

        //log 2-dimensional array
        double[][] bbands = indicators.bbands(Instrument.EURUSD, Period.TEN_SECS, OfferSide.BID, AppliedPrice.CLOSE,
                20, 5, 4, MaType.SMA, Filter.NO_FILTER, 5, time, 0);
        print("2-dimensional bbands indicator result array: " + arrayToString(bbands));

        //log 1-dimensional array
        double[] maxArr = indicators.max(Instrument.EURUSD, Period.TEN_SECS, OfferSide.BID, AppliedPrice.CLOSE,
                20, Filter.NO_FILTER, 5, time, 0);
        print("1-dimensional max indicator result array: " + arrayToString(maxArr));

        List<IBar> bars = history.getBars(Instrument.EURUSD, Period.TEN_SECS, OfferSide.BID, Filter.NO_FILTER, 5, time, 0);
        //log indexed array of objects, unindexed array of objects, list of objects
        console.getOut().format("previous bars \n as indexed array: %s \n as unindexed array: %s \n as list: %s",
                arrayToString(bars.toArray()), Arrays.toString(bars.toArray()), bars).println();

        context.stop();
    }

    private void print(Object o){
        console.getOut().println(o);
    }

    public static String arrayToString(double[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < arr.length; r++) {
            sb.append(String.format("[%s] %.5f; ",r, arr[r]));
        }
        return sb.toString();
    }

    public static String arrayToString(Object[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < arr.length; r++) {
            sb.append(String.format("[%s] %s; ",r, arr[r]));
        }
        return sb.toString();
    }

    public static String arrayToString(double[][] arr) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < arr.length; r++) {
            for (int c = 0; c < arr[r].length; c++) {
                sb.append(String.format("[%s][%s] %.5f; ",r, c, arr[r][c]));
            }
            sb.append("; ");
        }
        return sb.toString();
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {}
    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {}
    @Override
    public void onMessage(IMessage message) throws JFException {}
    @Override
    public void onAccount(IAccount account) throws JFException {}
    @Override
    public void onStop() throws JFException {}

}
