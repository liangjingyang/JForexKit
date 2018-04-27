package com.jforexcn.wiki.strategy;

/**
 * Created by simple(simple.continue@gmail.com) on 27/12/2017.
 *
 * https://www.dukascopy.com/wiki/en/development/strategy-api/instruments/conversion-utils
 * https://www.jforexcn.com/development/strategy-api/instruments/convert-utils.html
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.dukascopy.api.*;

/**
 * The following strategy shows the use of JFUtils.convertPipToCurrency function.
 * The strategy works with:
 * - set of instruments
 * - set of currencies
 *
 * It finds pip value of each cInstrument in each currency.
 * It also prints rate of each cInstrument and the inverted rate.
 *
 */
public class PipConvertTest3 implements IStrategy {
    private IConsole console;
    private JFUtils utils;

    private final Set<ICurrency> currencies = new HashSet<ICurrency>(Arrays.asList(new ICurrency[] {
            Instrument.USDJPY.getSecondaryJFCurrency(),
            Instrument.USDCHF.getSecondaryJFCurrency(),
            Instrument.USDJPY.getPrimaryJFCurrency(),
            Instrument.EURUSD.getPrimaryJFCurrency()
    }));

    private final Set<Instrument> instruments = new HashSet<Instrument>(Arrays.asList(new Instrument[] {
            Instrument.CHFJPY,
            Instrument.EURJPY,
            Instrument.EURUSD,
            Instrument.USDJPY
    }));

    public void onStart(IContext context) throws JFException {
        this.console = context.getConsole();
        this.utils = context.getUtils();

        context.setSubscribedInstruments(instruments);

        // wait max 1 second for the instruments to get subscribed
        int i = 10;
        while (!context.getSubscribedInstruments().containsAll(instruments)) {
            try {
                console.getOut().println("Instruments not subscribed yet " + i);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                console.getOut().println(e.getMessage());
            }
            i--;
        }

        //multiple conversion example
        for (Instrument instrument : instruments) {
            for (ICurrency currency : currencies) {
                try {
                    double pipValue = utils.convertPipToCurrency(instrument, currency, null);
                    print("%s -> %s, pip value=%.8f (%s/%s rate: %.8f, inverted rate: %.8f)",
                            instrument, currency, pipValue,
                            instrument.getSecondaryJFCurrency(), currency,
                            pipValue / instrument.getPipValue(), (1/pipValue * instrument.getPipValue()));
                } catch (JFException e) {
                    console.getErr().println(String.format("%s->%s conversion failed: %s", instrument, currency, e));
                }
            }
        }

        context.stop();
    }

    private void print(String format, Object...args){
        print(String.format(format, args));
    }

    private void print(Object message) {
        console.getOut().println(message);
    }

    public void onAccount(IAccount account) throws JFException {    }
    public void onMessage(IMessage message) throws JFException {    }
    public void onStop() throws JFException {    }
    public void onTick(Instrument instrument, ITick tick) throws JFException {}
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {    }
}