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

public class RangeBarFeedTest implements IStrategy, IFeedListener {

    //on the left-hand side choosing particular IFeedDescriptor implementation enforces the feed type - in this case - range bars
    @Configurable("range bar feed")
    public RangeBarFeedDescriptor rangeBarFeedDescriptor = new RangeBarFeedDescriptor(Instrument.EURUSD, PriceRange.TWO_PIPS, OfferSide.ASK);
    private IConsole console;

    @Override
    public void onStart(IContext context) throws JFException {
        console = context.getConsole();
        context.setSubscribedInstruments(java.util.Collections.singleton(rangeBarFeedDescriptor.getInstrument()), true);
        context.subscribeToFeed(rangeBarFeedDescriptor, this);
    }

    @Override
    public void onFeedData(IFeedDescriptor feedDescriptor, ITimedData feedData) {
        IRangeBar rangeBar = (IRangeBar)feedData;
        console.getOut().println("Completed range bar's close price: " + rangeBar.getClose());
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {    }
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {    }
    public void onMessage(IMessage message) throws JFException {    }
    public void onAccount(IAccount account) throws JFException {    }
    public void onStop() throws JFException {    }
}

