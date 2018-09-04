package com.jforexcn.inbox.strategy;

/**
 * Created by simple on 26/9/16.
 */

import com.dukascopy.api.Filter;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class EMA20EMA60CrossStrategy implements IStrategy {


    private IEngine mEngine;
    private IConsole mConsole;
    private IIndicators mIndicators;
    private IHistory mHistory;

    private Set<Instrument> mInstrumentSet = new HashSet<Instrument>();

    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    public static String STRATEGY_TAG = "EMA20EMA60";

    @Override
    public void onStart(IContext context) throws JFException {
        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mIndicators = context.getIndicators();
        this.mHistory = context.getHistory();

        // subscribe instruments
//        mInstrumentSet.add(Instrument.EURUSD);
//        mInstrumentSet.add(Instrument.USDJPY);
//        mInstrumentSet.add(Instrument.XAUUSD);
//        mInstrumentSet.add(Instrument.GBPUSD);
//        mInstrumentSet.add(Instrument.AUDUSD);
//        mInstrumentSet.add(Instrument.USDCAD);
//        mInstrumentSet.add(Instrument.USDCHF);
//        mInstrumentSet.add(Instrument.NZDUSD);
        mInstrumentSet.add(Instrument.GBPAUD);
        context.setSubscribedInstruments(mInstrumentSet, true);

        // date format
        mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
    }

    @Override
    public void onStop() throws JFException {
        puts(STRATEGY_TAG + "OnStop!");
        sendEmail(STRATEGY_TAG + "OnStop!", "");
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (period.equals(Period.DAILY)) {
            double[] ema2 = mIndicators.ema(instrument, period, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE, 60, Filter.WEEKENDS, 2, askBar.getTime(), 0);
            double[] ema1 = mIndicators.ema(instrument, period, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE, 20, Filter.WEEKENDS, 2, askBar.getTime(), 0);

            if (ema1[0] <= ema2[0] && ema1[1] > ema2[1]) {
                for (IOrder order : getSellOrders(instrument)) {
                    order.close();
                }
                String orderLabel = getOrderLabel(instrument, IEngine.OrderCommand.BUY, askBar.getTime());
                mEngine.submitOrder(orderLabel, instrument, IEngine.OrderCommand.BUY, 0.02, bidBar.getClose(), 3);
            } else if (ema1[0] >= ema2[0] && ema1[1] < ema2[1]) {
                for (IOrder order : getBuyOrders(instrument)) {
                    order.close();
                }
                String orderLabel = getOrderLabel(instrument, IEngine.OrderCommand.SELL, askBar.getTime());
                mEngine.submitOrder(orderLabel, instrument, IEngine.OrderCommand.SELL, 0.02, bidBar.getClose(), 3);
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private String getOrderLabel(Instrument instrument, IEngine.OrderCommand orderCommand, long time) {
        return STRATEGY_TAG + "_" + instrument.toString().replace('/', '_') + "_" + orderCommand +
                "_" + mSimpleDateFormat.format(new Date(time)) + "_" + time;
    }

    private void puts(String str) {
        mConsole.getInfo().println(str);
//        StrategyRunner.LOGGER.info(str);
    }

    private void sendEmail(String subject, String body) {
//        MailService.sendMail(subject, body);
    }

    private ArrayList<IOrder> getBuyOrders(Instrument instrument) throws JFException {
        ArrayList<IOrder> orders = new ArrayList<>();
        for (IOrder order : mEngine.getOrders()) {
            if (order != null &&
                    mInstrumentSet.contains(instrument) &&
                    instrument.equals(order.getInstrument()) &&
                    IEngine.OrderCommand.BUY.equals(order.getOrderCommand())
            ) {
                orders.add(order);
            }
        }
        return orders;
    }

    private ArrayList<IOrder> getSellOrders(Instrument instrument) throws JFException {
        ArrayList<IOrder> orders = new ArrayList<>();
        for (IOrder order : mEngine.getOrders()) {
            if (order != null &&
                    mInstrumentSet.contains(instrument) &&
                    instrument.equals(order.getInstrument()) &&
                    IEngine.OrderCommand.SELL.equals(order.getOrderCommand())
                    ) {
                orders.add(order);
            }
        }
        return orders;
    }

}