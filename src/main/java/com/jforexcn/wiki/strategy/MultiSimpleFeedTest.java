package com.jforexcn.wiki.strategy;

/**
 * Created by simple(simple.continue@gmail.com) on 15/12/2017.
 *
 * https://www.dukascopy.com/wiki/en/development/strategy-api/instruments/feeds
 * https://www.jforexcn.com/development/strategy-api/instruments/feeds.html
 */

import java.util.Arrays;
import java.util.HashSet;

import com.dukascopy.api.Configurable;
import com.dukascopy.api.Filter;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.ITimedData;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.PriceRange;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.feed.IFeedListener;
import com.dukascopy.api.feed.util.RangeBarFeedDescriptor;
import com.dukascopy.api.feed.util.TimePeriodAggregationFeedDescriptor;

public class MultiSimpleFeedTest implements IStrategy, IFeedListener {

    private IConsole console;

    //on the left-hand side choosing particular IFeedDescriptor implementation enforces the feed type - in this case - range bars
    @Configurable("range bar feed")
    public RangeBarFeedDescriptor rangeBarFeedDescriptor = new RangeBarFeedDescriptor(Instrument.EURUSD, PriceRange.TWO_PIPS, OfferSide.ASK);
    @Configurable("any feed")
    public IFeedDescriptor anyFeedDescriptor = new TimePeriodAggregationFeedDescriptor(Instrument.EURUSD, Period.TEN_SECS, OfferSide.ASK, Filter.NO_FILTER);

    @Override
    public void onStart(IContext context) throws JFException {
        console = context.getConsole();
        context.setSubscribedInstruments(new HashSet<Instrument>(Arrays.asList(rangeBarFeedDescriptor.getInstrument(), anyFeedDescriptor.getInstrument())), true);
        context.subscribeToFeed(rangeBarFeedDescriptor, this);
        context.subscribeToFeed(anyFeedDescriptor, this);
    }

    @Override
    public void onFeedData(IFeedDescriptor feedDescriptor, ITimedData feedData) {
        if (feedDescriptor.equals(this.rangeBarFeedDescriptor)) {
            console.getOut().format("RANGE BAR completed of the feed: %s descriptor: %s", feedData, feedDescriptor).println();
        } else if (feedDescriptor.equals(this.anyFeedDescriptor)) {
            console.getInfo().format("%s element completed of the feed: %s descriptor: %s",feedDescriptor.getDataType(), feedData, feedDescriptor).println();
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
