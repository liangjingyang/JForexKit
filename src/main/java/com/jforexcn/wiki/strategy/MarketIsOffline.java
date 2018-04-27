package com.jforexcn.wiki.strategy;

/**
 * https://www.dukascopy.com/wiki/en/development/strategy-api/instruments/market-hours
 * https://www.jforexcn.com/development/strategy-api/instruments/market-hours.html
 */

import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.TimeZone;

import com.dukascopy.api.*;

/**
 * The strategy checks within 14 day cPeriod, which days
 * the market was/will be offline.
 *
 */
public class MarketIsOffline implements IStrategy {

    private IDataService dataService;
    private IHistory history;
    private IConsole console;

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public void onStart(IContext context) throws JFException {
        dataService = context.getDataService();
        history = context.getHistory();
        console = context.getConsole();
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        long lastTickTime = history.getLastTick(Instrument.EURUSD).getTime();
        //check from 7 days ago till 7 days in future
        for(int i = -7; i < 7; i++){
            long time = lastTickTime + Period.DAILY.getInterval() * i;
            console.getOut().println(sdf.format(time) + " offline=" + isOffline(time));
        }
    }

    private boolean isOffline(long time) throws JFException{
        Set<ITimeDomain> offlines = dataService.getOfflineTimeDomains(time - Period.WEEKLY.getInterval(), time + Period.WEEKLY.getInterval());
        for(ITimeDomain offline : offlines){
            if( time > offline.getStart() &&  time < offline.getEnd()){
                return true;
            }
        }
        return false;
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
