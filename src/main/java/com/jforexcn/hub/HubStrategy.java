package com.jforexcn.hub;

import com.dukascopy.api.Configurable;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Library;
import com.dukascopy.api.Period;
import com.dukascopy.api.RequiresFullAccess;
import com.dukascopy.api.feed.ITailoredFeedDescriptor;
import com.dukascopy.api.feed.ITailoredFeedListener;
import com.dukascopy.api.feed.ITickBar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by simple(simple.continue@gmail.com) on 26/04/2018.
 */

@RequiresFullAccess
@Library("/data/github/JForexKit/build/libs/JForexKit-3.0.jar")
public class HubStrategy implements IStrategy, ITailoredFeedListener<ITickBar> {

    @Configurable("")
    public Set<Instrument> instruments = new HashSet<Instrument>();

    @Configurable("")
    public Set<SubStrategyName> subStrategyNames = new HashSet<SubStrategyName>();

    private final HashMap<String, SubStrategy> supportedSubStrategies = new HashMap<>();
    private final Set<SubStrategy> subStrategies = new HashSet<>();

    public HubStrategy() {
        supportedSubStrategies.put(StopLossOne.class.getSimpleName(), new StopLossOne());
        supportedSubStrategies.put(myOfflineTrades.class.getSimpleName(), new myOfflineTrades());
    }

    @Override
    public void onStart(IContext context) throws JFException {
        HubConfiguration.load(context);
        if (instruments.size() == 0) {
            String configKey = HubConfiguration.getConfigKey(
                    this.getClass().getSimpleName(),
                    HubConfiguration.SINGLE_SCOPE,
                    Instrument.class.getSimpleName(),
                    "instruments");
            Object instrumentList = HubConfiguration.getConfig(configKey);
            if (instrumentList instanceof List) {
                instruments.addAll((List<Instrument>) instrumentList);
            }
        }
        context.getConsole().getInfo().println("=== HubStrategy Instruments Start ===");
        for (Instrument instrument : instruments) {
            context.getConsole().getInfo().println(instrument);
        }
        context.getConsole().getInfo().println("=== HubStrategy Instruments End ===");

        if (subStrategyNames.size() == 0) {
            String configKey = HubConfiguration.getConfigKey(
                    this.getClass().getSimpleName(),
                    HubConfiguration.SINGLE_SCOPE,
                    SubStrategyName.class.getSimpleName(),
                    "strategyNames");
            Object strategyNameList = HubConfiguration.getConfig(configKey);
            if (strategyNameList instanceof List) {
                subStrategyNames.addAll((List<SubStrategyName>) strategyNameList);
            }
        }
        context.getConsole().getInfo().println("=== HubStrategy Strategies Start ===");
        for (SubStrategyName subStrategyName : subStrategyNames) {
            context.getConsole().getInfo().println(subStrategyName);
        }
        context.getConsole().getInfo().println("=== HubStrategy Strategies End ===");


        for (SubStrategyName subStrategyName : subStrategyNames) {
            String name = subStrategyName.toString();
            if (supportedSubStrategies.containsKey(name)) {
                SubStrategy subStrategy = supportedSubStrategies.get(name);
                subStrategy.addInstruments(instruments);
                subStrategies.add(subStrategy);
            }
        }

        context.setSubscribedInstruments(instruments);
        Set<ITailoredFeedDescriptor<ITickBar>> descriptors = FeedDescriptors.generateDescriptorsByInstruments(instruments);
        for (ITailoredFeedDescriptor<ITickBar> descriptor : descriptors) {
            context.subscribeToFeed(descriptor, this);
        }

        for (SubStrategy subStrategy: subStrategies) {
            subStrategy.onStart(context);
        }
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        for (SubStrategy subStrategy: subStrategies) {
            subStrategy.onTick(instrument, tick);
        }
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        for (SubStrategy subStrategy: subStrategies) {
            subStrategy.onBar(instrument, period, askBar, bidBar);
        }
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        for (SubStrategy subStrategy: subStrategies) {
            subStrategy.onMessage(message);
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
        for (SubStrategy subStrategy: subStrategies) {
            subStrategy.onAccount(account);
        }
    }

    @Override
    public void onStop() throws JFException {
        for (SubStrategy subStrategy: subStrategies) {
            subStrategy.onStop();
        }
    }

    @Override
    public void onFeedData(ITailoredFeedDescriptor<ITickBar> feedDescriptor, ITickBar feedData) {
        for (SubStrategy subStrategy: subStrategies) {
            subStrategy.onFeedData(feedDescriptor, feedData);
        }
    }

    public static class SubStrategyName {

        public static final SubStrategyName STOP_LOSS_ONE = createSubStrategyName(StopLossOne.class.getSimpleName());
        public static final SubStrategyName MY_OFFLINE_TRADES = createSubStrategyName(myOfflineTrades.class.getSimpleName());

        public static final HashMap<String, SubStrategyName> INSTANCES = new HashMap<>();
        public final String name;

        public SubStrategyName(String name){
            this.name = name;
        }

        @Override
        public String toString(){
            return name;
        }

        public static SubStrategyName createSubStrategyName(String name) {
            SubStrategyName subStrategyName = new SubStrategyName(name);
            INSTANCES.put(name, subStrategyName);
            return subStrategyName;
        }

        public static SubStrategyName valueOf(String name) {
            return INSTANCES.get(name);
        }
    }
}
