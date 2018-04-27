package com.jforexcn.wiki.strategy;

/**
 * https://www.dukascopy.com/wiki/en/development/strategy-api/instruments/market-hours
 * https://www.jforexcn.com/development/strategy-api/instruments/market-hours.html
 */

import java.util.Set;

import com.dukascopy.api.*;

public class MarketOfflineHours implements IStrategy {

    private IDataService dataService;
    private IHistory history;

    @Override
    public void onStart(IContext context) throws JFException {
        dataService = context.getDataService();
        history = context.getHistory();
        long time = history.getLastTick(Instrument.EURUSD).getTime();
        Set<ITimeDomain> prevOfflines = dataService.getOfflineTimeDomains(
                time - Period.WEEKLY.getInterval() * 3,
                time
        );
        Set<ITimeDomain> nextOffline = dataService.getOfflineTimeDomains(
                time,
                time + Period.WEEKLY.getInterval()
        );

        context.getConsole().getOut().println("Offlines last 3 weeks " + prevOfflines + "\n Offlines in the following week: " + nextOffline);
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

