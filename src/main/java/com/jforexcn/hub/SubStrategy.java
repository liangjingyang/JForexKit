package com.jforexcn.hub;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFUtils;
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
}
