package com.jforexcn.wiki.strategy;

/**
 * Created by simple(simple.continue@gmail.com) on 15/12/2017.
 *
 * https://www.dukascopy.com/wiki/en/development/strategy-api/instruments/feeds
 * https://www.jforexcn.com/development/strategy-api/instruments/feeds.html
 */

import com.dukascopy.api.*;
import com.dukascopy.api.feed.*;
import com.dukascopy.api.feed.util.*;

public class SingleSimpleFeedTest implements IStrategy, IFeedListener {

    @Configurable("")
    public IFeedDescriptor feedDescriptor = new RangeBarFeedDescriptor(Instrument.EURUSD, PriceRange.TWO_PIPS, OfferSide.ASK);
    private IConsole console;

    @Override
    public void onStart(IContext context) throws JFException {
        console = context.getConsole();
        context.setSubscribedInstruments(java.util.Collections.singleton(feedDescriptor.getInstrument()), true);
        context.subscribeToFeed(feedDescriptor, this);
    }

    @Override
    public void onFeedData(IFeedDescriptor feedDescriptor, ITimedData feedData) {
        console.getOut().println("range bar completed: " + feedData + " of feed: " + feedDescriptor);
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {    }
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {    }
    public void onMessage(IMessage message) throws JFException {    }
    public void onAccount(IAccount account) throws JFException {    }
    public void onStop() throws JFException {    }
}

