package com.jforexcn.wiki.strategy;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.dukascopy.api.*;
import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.feed.*;
import com.dukascopy.api.feed.util.*;

/**
 * Created by simple(simple.continue@gmail.com) on 15/12/2017.
 *
 * https://www.dukascopy.com/wiki/en/development/strategy-api/instruments/feeds
 * https://www.jforexcn.com/development/strategy-api/instruments/feeds.html
 */

/**
 * The example strategy shows how to work with an arbitrary feed:
 * - subscribe and print latest completed feed data
 * - retrieve history by feed descriptor
 * - calculate an indicator by feed descriptor
 * - open a chart of the feed
 *
 */

public class FeedHistIndFromList implements IStrategy, IFeedListener {

    private IConsole console;
    private IHistory history;
    private IIndicators indicators;

    @Configurable(value = "feed type", description = "choose any type of feed (except ticks) in the strategy parameters dialog")
    public IFeedDescriptor feedDescriptor = new RangeBarFeedDescriptor(Instrument.EURUSD, PriceRange.TWO_PIPS, OfferSide.ASK);
    @Configurable("open chart")
    public boolean openChart = false;

    final int dataCount = 3;

    @Override
    public void onStart(IContext context) throws JFException {

        history = context.getHistory();
        console = context.getConsole();
        indicators = context.getIndicators();

        if(feedDescriptor.getDataType() == DataType.TICKS){
            console.getErr().println("IFeedListener does not work with ticks yet!");
            context.stop();
        }

        context.setSubscribedInstruments(new HashSet<Instrument>(Arrays.asList(new Instrument[] { feedDescriptor.getInstrument() })), true);

        if(openChart){
            IChart chart = context.openChart(feedDescriptor);
            chart.add(indicators.getIndicator("EMA"), new Object[] { 12 });
        }

        console.getOut().println("subscribe to feed=" + feedDescriptor);
        context.subscribeToFeed(feedDescriptor, this);

    }

    @Override
    public void onFeedData(IFeedDescriptor feedDescriptor, ITimedData feedData) {
        console.getOut().println(feedData + " of feed: " + feedDescriptor);
        try {
            ITimedData lastFeedData = history.getFeedData(feedDescriptor, 0); //currently forming feed element
            List<ITimedData> feedDataList = history.getFeedData(feedDescriptor, dataCount, feedData.getTime(), 0);

            double[] ema =indicators.ema(feedDescriptor, AppliedPrice.CLOSE, feedDescriptor.getOfferSide(), 12).calculate(dataCount, feedData.getTime(), 0);

            String feedDataName = feedData.getClass().getInterfaces()[0].getSimpleName();
            console.getOut().format("%s last=%s, previous=%s,\n previous 3 elements=%s \n ema for last 3=%s", feedDataName, lastFeedData, feedData,
                    feedDataList, Arrays.toString(ema)).println();
        } catch (JFException e) {
            console.getErr().println(e);
            e.printStackTrace();
        }
    }


    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {    }

    @Override
    public void onMessage(IMessage message) throws JFException {    }

    @Override
    public void onAccount(IAccount account) throws JFException {    }

    @Override
    public void onStop() throws JFException {    }

}

