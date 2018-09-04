package com.jforexcn.inbox.strategy;

/**
 * Created by simple on 2/7/16.
 */

import com.dukascopy.api.Filter;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforexcn.shared.lib.MailService;
import com.jforexcn.shared.client.StrategyRunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class Watching implements IStrategy {

    private IEngine mEngine;
    private IConsole mConsole;
    private IIndicators mIndicators;
    private HashMap<Instrument, ArrayList<WatchingIndicator>> mWatchingInstruments = new HashMap<Instrument, ArrayList<WatchingIndicator>>();

    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    @Override
    public void onStart(IContext context) throws JFException {
        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mIndicators = context.getIndicators();

        ArrayList<WatchingIndicator> EURUSDIndicators = new ArrayList<WatchingIndicator>();
        WatchingIndicator smaIndicator = new SMAIndicator();
        EURUSDIndicators.add(smaIndicator);
        RangeIndicator rangeIndicator = new RangeIndicator();
        rangeIndicator.setResistance(1.1700);
        rangeIndicator.setSupport(1.0850);
        EURUSDIndicators.add(rangeIndicator);

        ArrayList<WatchingIndicator> USDJPYIndicators = new ArrayList<WatchingIndicator>();
        WatchingIndicator smaIndicator2 = new SMAIndicator();
        USDJPYIndicators.add(smaIndicator2);
        RangeIndicator rangeIndicator2 = new RangeIndicator();
        rangeIndicator2.setResistance(108.65);
        rangeIndicator2.setSupport(108.25);
        USDJPYIndicators.add(rangeIndicator2);

//        mWatchingInstruments.put(Instrument.EURUSD, EURUSDIndicators);
        mWatchingInstruments.put(Instrument.USDJPY, USDJPYIndicators);

        context.setSubscribedInstruments(mWatchingInstruments.keySet(), true);

        mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
    }

    @Override
    public void onMessage(IMessage message) throws JFException {

    }

    @Override
    public void onStop() throws JFException {
        puts("OnStop, I'm " + this.getClass().getSimpleName());
        // Do nothing, leave the orders.
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (period.equals(Period.ONE_HOUR)) {
            puts("I'm living, I'm " + this.getClass().getSimpleName());
        }
        if (mWatchingInstruments.containsKey(instrument)) {
            for (WatchingIndicator indicator : mWatchingInstruments.get(instrument)) {
                indicator.checkIndicator(instrument, period, askBar, bidBar);
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private void puts(String str) {
//        mConsole.getInfo().println(str);
        StrategyRunner.LOGGER.info(str);
    }

    private void sendEmail(WatchingIndicator senderIndicator, String body) {
        puts(senderIndicator.getClass().getSimpleName() + ": " + body);
        MailService.sendMail(senderIndicator.getClass().getSimpleName(), body);
    }

    private interface WatchingIndicator {
        void initIndicator(Instrument instrument) throws JFException;

        void checkIndicator(Instrument instrument, Period barPeriod, IBar askBar, IBar bidBar) throws JFException;
    }

    private class RangeIndicator implements WatchingIndicator {
        private double resistance = 1.1500;
        private double support = 1.0500;
//        private double resistance = 1.1222;
//        private double support = 1.1215;
        private Period period = Period.ONE_MIN;

        public void setResistance(double resistance) {
            this.resistance = resistance;
        }

        public void setSupport(double support) {
            this.support = support;
        }

        @Override
        public void initIndicator(Instrument instrument) throws JFException {

        }

        @Override
        public void checkIndicator(Instrument instrument, Period barPeriod, IBar askBar, IBar bidBar) throws JFException {
            if (period.equals(barPeriod)) {
                if (askBar.getClose() < support) {
                    support = support - 20;
                    sendEmail(RangeIndicator.this, "BROKEN!!! The price " + askBar.getClose() + " broke the support " + support);
                } else if (bidBar.getClose() > resistance) {
                    resistance = resistance + 20;
                    sendEmail(RangeIndicator.this, "BROKEN!!! The price " + bidBar.getClose() + " broke the resistance " + resistance);
                }
            }
        }
    }

    private class SMAIndicator implements WatchingIndicator {

        private int shortTimePeriod = 10;
        private int middleTimePeriod = 30;
        private int longTimePeriod = 80;
        private Period period = Period.DAILY;

        public int getShortTimePeriod() {
            return shortTimePeriod;
        }

        public void setShortTimePeriod(int shortTimePeriod) {
            this.shortTimePeriod = shortTimePeriod;
        }

        public int getMiddleTimePeriod() {
            return middleTimePeriod;
        }

        public void setMiddleTimePeriod(int middleTimePeriod) {
            this.middleTimePeriod = middleTimePeriod;
        }

        public int getLongTimePeriod() {
            return longTimePeriod;
        }

        public void setLongTimePeriod(int longTimePeriod) {
            this.longTimePeriod = longTimePeriod;
        }

        public Period getPeriod() {
            return period;
        }

        public void setPeriod(Period period) {
            this.period = period;
        }

        @Override
        public void initIndicator(Instrument instrument) throws JFException {
        }

        @Override
        public void checkIndicator(Instrument instrument, Period barPeriod, IBar askBar, IBar bidBar) throws JFException {
            if (barPeriod.equals(period)) {
                double[] shortSMA = mIndicators.sma(instrument, period, OfferSide.ASK,
                        IIndicators.AppliedPrice.CLOSE, shortTimePeriod, Filter.WEEKENDS, 2, askBar.getTime(), 0);
                double[] middleSMA = mIndicators.sma(instrument, period, OfferSide.ASK,
                        IIndicators.AppliedPrice.CLOSE, middleTimePeriod, Filter.WEEKENDS, 2, askBar.getTime(), 0);
                double[] longSMA = mIndicators.sma(instrument, period, OfferSide.ASK,
                        IIndicators.AppliedPrice.CLOSE, longTimePeriod, Filter.WEEKENDS, 2, askBar.getTime(), 0);
                // 0:n before:now
                if (isInTrend(shortSMA[0], middleSMA[0], longSMA[0]) && !isInTrend(shortSMA[1], middleSMA[1], longSMA[1]) ||
                        !isInTrend(shortSMA[0], middleSMA[0], longSMA[0]) && isInTrend(shortSMA[1], middleSMA[1], longSMA[1])) {
                    sendEmail(SMAIndicator.this, "WARNING!!! " + instrument +
                            " trend is towards " + getTrend(shortSMA[1], middleSMA[1], longSMA[1]) +
                            "\nLast Bar close Ask: " + askBar.getClose() + ", Bid: " + bidBar.getClose() +
                            "\ntime: " + mSimpleDateFormat.format(new Date(askBar.getTime())) +
                            "\ns1: " + shortSMA[0] + ", m1: " + middleSMA[0] + ", l1: " + longSMA[0] +
                            "\ns2: " + shortSMA[1] + ", m2: " + middleSMA[1] + ", l2: " + longSMA[1]);
                }
            }
        }

        private boolean isInTrend(double shortSMA, double middleSMA, double longSMA) {
            return (shortSMA > middleSMA && middleSMA > longSMA) || (shortSMA < middleSMA && middleSMA < longSMA);
        }

        private String getTrend(double shortSMA, double middleSMA, double longSMA) {
            if (shortSMA > middleSMA && middleSMA > longSMA) {
                return "up";
            } else if (shortSMA < middleSMA && middleSMA < longSMA) {
                return "down";
            } else {
                return "wobble";
            }
        }


    }
}
