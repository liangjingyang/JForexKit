package com.jforexcn.inbox.plugin;

/**
 * Created by simple(simple.continue@gmail.com) on 29/11/2017.
 */

import com.dukascopy.api.Configurable;
import com.dukascopy.api.IChart;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.ITimedData;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.TickBarSize;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.feed.IFeedListener;
import com.dukascopy.api.feed.ITailoredFeedDescriptor;
import com.dukascopy.api.feed.ITailoredFeedListener;
import com.dukascopy.api.feed.util.TickBarFeedDescriptor;
import com.dukascopy.api.plugins.IPluginContext;
import com.dukascopy.api.plugins.Plugin;

public class TickBar2000Plugin extends Plugin implements ITailoredFeedListener {

    private IConsole console;
    private ITailoredFeedDescriptor tickBar2000Descriptor;

    @Configurable("Tick Bar Size")
    public int cTickBarSize = 1000;

    @Override
    public void onStart(IPluginContext context) throws JFException {
        console = context.getConsole();
        console.getOut().println("Plugin started");
        tickBar2000Descriptor = new TickBarFeedDescriptor(
                Instrument.EURUSD,
                TickBarSize.valueOf(cTickBarSize),
                OfferSide.ASK
        );
        IChart chart = context.openChart(tickBar2000Descriptor);
        context.subscribeToFeed(tickBar2000Descriptor, this);
    }

    @Override
    public void onStop() throws JFException {
        console.getOut().println("Plugin stopped");
    }

    @Override
    public void onFeedData(ITailoredFeedDescriptor feedDescriptor, ITimedData feedData) {

    }
}
