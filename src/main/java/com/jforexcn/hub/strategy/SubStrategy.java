package com.jforexcn.hub.strategy;

import com.dukascopy.api.Configurable;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.JFUtils;
import com.dukascopy.api.Period;
import com.dukascopy.api.feed.ITailoredFeedDescriptor;
import com.dukascopy.api.feed.ITailoredFeedListener;
import com.dukascopy.api.feed.ITickBar;
import com.jforexcn.hub.lib.FeedDescriptors;
import com.jforexcn.hub.lib.Helper;

import java.util.Set;

/**
 * Created by simple(simple.continue@gmail.com) on 27/04/2018.
 */

public class SubStrategy implements IStrategy, ITailoredFeedListener<ITickBar> {

    @Configurable("Instrument")
    public Instrument cInstrument = Instrument.EURUSD;

    public Helper helper = new Helper();
    private boolean fromHub = false;

    IContext mContext;
    JFUtils mUtils;
    IEngine mEngine;
    IConsole mConsole;
    IIndicators mIndicators;
    IHistory mHistory;
    IAccount mAccount;

    public boolean isFromHub() {
        return fromHub;
    }

    public void setFromHub(boolean fromHub) {
        this.fromHub = fromHub;
    }

    public void setInstrument(Instrument instrument) {
        cInstrument = instrument;
        helper.addInstrument(instrument);
    }

    public void addInstruments(Set<Instrument> instruments) {
        helper.addInstruments(instruments);
    }

    public void init(IContext context, String tag) throws JFException {
        helper.init(context, tag);
        this.mContext = context;
        this.mHistory = context.getHistory();
        this.mAccount = context.getAccount();
        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mUtils = context.getUtils();
        this.mIndicators = context.getIndicators();
        if (!isFromHub()) {
            helper.addInstrument(cInstrument);
            context.setSubscribedInstruments(helper.getInstrumentSet());
            Set<ITailoredFeedDescriptor<ITickBar>> descriptors = FeedDescriptors.generateDescriptorsByInstruments(helper.getInstrumentSet());
            for (ITailoredFeedDescriptor<ITickBar> descriptor : descriptors) {
                context.subscribeToFeed(descriptor, this);
            }
        }
    }


    public <T> T getConfig(String fieldName, Class<T> cls) throws JFException {
        return getConfig(com.jforexcn.hub.lib.HubConfiguration.SINGLE_SCOPE, fieldName, cls);
    }

    public <T> T getConfig(String scope, String fieldName, Class<T> cls) throws JFException {
        Object value = getConfig(scope, fieldName, cls.getSimpleName());
        try {
            return (T) value;
        } catch (Exception e) {
            throw new JFException("HubConfiguration " + fieldName + " of " + helper.strategyTag + " should be " + cls.getSimpleName());
        }
    }


    public Object getConfig(String scope, String fieldName, String typeName) throws JFException {
        String key = com.jforexcn.hub.lib.HubConfiguration.getConfigKey(this.getClass().getSimpleName(), scope, typeName, fieldName);
        Object value = com.jforexcn.hub.lib.HubConfiguration.getConfig(key);
        if (value == null) {
            String key2 = com.jforexcn.hub.lib.HubConfiguration.getConfigKey(this.getClass().getSimpleName(), com.jforexcn.hub.lib.HubConfiguration.DEFAULT_SCOPE, typeName, fieldName);
            value = com.jforexcn.hub.lib.HubConfiguration.getConfig(key2);
        }
        if (value == null) {
            throw new JFException("Can not find params in config, key: " + key);
        }
        return value;
    }

    @Override
    public void onStart(IContext context) throws JFException {

    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {

    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {

    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        helper.onMessage(message);
    }

    @Override
    public void onAccount(IAccount account) throws JFException {

    }

    @Override
    public void onStop() throws JFException {

    }

    @Override
    public void onFeedData(ITailoredFeedDescriptor<ITickBar> feedDescriptor, ITickBar feedData) {

    }
}
