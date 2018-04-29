package com.jforexcn.hub;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.JFUtils;
import com.dukascopy.api.Period;
import com.dukascopy.api.feed.ITailoredFeedDescriptor;
import com.dukascopy.api.feed.ITailoredFeedListener;
import com.dukascopy.api.feed.ITickBar;

import java.util.Set;

/**
 * Created by simple(simple.continue@gmail.com) on 27/04/2018.
 */

public abstract class SubStrategy implements IStrategy, ITailoredFeedListener<ITickBar> {

    public Helper helper = new Helper();

    IContext mContext;
    JFUtils mUtils;
    IEngine mEngine;
    IConsole mConsole;
    IIndicators mIndicators;
    IHistory mHistory;
    IAccount mAccount;


    void addInstruments(Set<Instrument> instruments) {
        helper.addInstruments(instruments);
    }

    void init(IContext context, String tag) {
        helper.init(context, tag);
        this.mContext = context;
        this.mHistory = context.getHistory();
        this.mAccount = context.getAccount();
        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mUtils = context.getUtils();
        this.mIndicators = context.getIndicators();
    }

    @Override
    public void onFeedData(ITailoredFeedDescriptor<ITickBar> feedDescriptor, ITickBar feedData) {

    }

    public <T> T getConfig(String fieldName, Class<T> cls) throws JFException {
        return getConfig(HubConfiguration.SINGLE_SCOPE, fieldName, cls);
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
        String key = HubConfiguration.getConfigKey(this.getClass().getSimpleName(), scope, typeName, fieldName);
        Object value = HubConfiguration.getConfig(key);
        if (value == null) {
            String key2 = HubConfiguration.getConfigKey(this.getClass().getSimpleName(), HubConfiguration.DEFAULT_SCOPE, typeName, fieldName);
            value = HubConfiguration.getConfig(key2);
        }
        if (value == null) {
            throw new JFException("Can not find params in config, key: " + key);
        }
        return value;
    }
}
