package com.jforexcn.wiki.strategy;

/**
 * Created by simple(simple.continue@gmail.com) on 05/12/2017.
 *
 * https://www.dukascopy.com/wiki/en/development/get-started-api/use-in-jforex/strategy-tutorial
 * https://www.jforexcn.com/development/getting-started/general/strategy-tutorial.html
 */


import java.util.*;
import com.dukascopy.api.IEngine.OrderCommand;

import com.dukascopy.api.*;

public class BarsAndTicks implements IStrategy {
    private IEngine engine;
    private IConsole console;
    private IHistory history;
    private IContext context;
    private IIndicators indicators;
    private IUserInterface userInterface;
    //add IBar variable and ITick
    private IBar previousBar;
    private ITick myLastTick;
    private String myString;


    @Configurable(value="Instrument value")
    public Instrument myInstrument = Instrument.EURGBP;
    @Configurable(value="Offer Side value", obligatory=true)
    public OfferSide myOfferSide;
    @Configurable(value="Period value")
    public Period myPeriod = Period.TEN_MINS;

    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.history = context.getHistory();
        this.context = context;
        this.indicators = context.getIndicators();
        this.userInterface = context.getUserInterface();

        //subscribe an instrument:
        Set<Instrument> instruments = new HashSet<Instrument>();
        instruments.add(myInstrument);
        context.setSubscribedInstruments(instruments, true);

        //initialize myIBar and myLastTick variables:
        previousBar = history.getBar(myInstrument, myPeriod, myOfferSide, 1);
        myLastTick = history.getLastTick(myInstrument);
        //print the results of previousBar and myLastTick
        console.getOut().println(previousBar);
        console.getOut().println(myLastTick);

        //make a trade
        OrderCommand myCommand = previousBar.getOpen() > previousBar.getClose() ? OrderCommand.SELL : OrderCommand.BUY;
        engine.submitOrder("MyStrategyOrder2", myInstrument, myCommand, 0.1);
    }

    public void onAccount(IAccount account) throws JFException {
    }

    public void onMessage(IMessage message) throws JFException {
    }

    public void onStop() throws JFException {
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {


    }
}
