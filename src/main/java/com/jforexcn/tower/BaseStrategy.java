package com.jforexcn.tower;

import com.dukascopy.api.*;
import com.jforexcn.tower.Util.OrderHelper;

/**
 * Created by simple on 2018/8/18
 */

public abstract class BaseStrategy implements IStrategy {

    @Configurable("Strategy Tag")
    public String strategyTag = "MyStrategy";
    @Configurable("Instrument")
    public Instrument instrument = Instrument.EURUSD;
    @Configurable(value = "spreads", stepSize = 1)
    public int spreads = 3;
    @Configurable(value = "isDebug")
    public boolean debug = false;

    protected OrderHelper orderHelper;

    protected IContext context;
    protected IHistory history;
    protected IAccount account;
    protected IEngine engine;
    protected IConsole console;
    protected JFUtils utils;
    protected IIndicators indicators;

    @Override
    public void onStart(IContext context) throws JFException {
        orderHelper = new OrderHelper(getStrategyTag(), context);
        this.context = context;
        if (this.context != null) {
            history = this.context.getHistory();
            account = this.context.getAccount();
            engine = this.context.getEngine();
            console = this.context.getConsole();
            utils = this.context.getUtils();
            indicators = this.context.getIndicators();
        }
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {

    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {

    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        orderHelper.onMessage(message);
    }

    @Override
    public void onAccount(IAccount account) throws JFException {

    }

    @Override
    public void onStop() throws JFException {

    }

    public boolean isStrategyInstrument(Instrument instrument) {
        return this.instrument != null && this.instrument.equals(instrument);
    }

    public boolean isStrategyOrder(IOrder order) {
        return order.getLabel().startsWith(getStrategyTag());
    }

    public String getStrategyTag() {
        return strategyTag;
    }

    public void logDebug(String msg) {
        if (debug) {
            this.console.getOut().println("=== [" + getStrategyTag() + "][D] === " + msg);
        }
    }

    public void logInfo(String msg) {
        this.console.getInfo().println("=== [" + getStrategyTag() + "][I] === " + msg);
    }

    public void logErr(String msg) {
        this.console.getErr().println("=== [" + getStrategyTag() + "][E] === " + msg);
    }
}

