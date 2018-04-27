package com.jforexcn.wiki.strategy;

import com.dukascopy.api.*;

/**
 * The strategy logs prices differences both in absolute values and in pips.
 *
 */
public class PrintPriceDifference implements IStrategy {

    private Instrument instrument = Instrument.EURUSD;
    public IHistory history;
    public IConsole console;

    public void onStart(IContext context) throws JFException {
        this.history = context.getHistory();
        this.console = context.getConsole();

        ITick tick0 = history.getLastTick(instrument);
        ITick tick10 = history.getTick(instrument, 10);
        double priceDiff = tick0.getBid() - tick10.getBid();
        double spread0 = tick0.getAsk() - tick0.getBid();
        double priceDiffPips = priceDiff / instrument.getPipValue();
        double spreadPips = priceDiff / instrument.getPipValue();
        console.getOut().format(
                "0th tick=%s, 10th tick=%s \n price difference between last and 10th tick=%.5f (%.1f pips) \n last spread=%.5f (%.1f pips)"
                ,tick0, tick10, priceDiff, priceDiffPips, spread0, spreadPips).println();

    }

    public void onAccount(IAccount account) throws JFException {}
    public void onMessage(IMessage message) throws JFException {}
    public void onStop() throws JFException {    }
    public void onTick(Instrument instrument, final ITick tick) throws JFException {}
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {}

}
