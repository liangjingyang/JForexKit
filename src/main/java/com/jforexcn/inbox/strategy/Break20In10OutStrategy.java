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
import com.jforexcn.shared.client.StrategyRunner;
import com.jforexcn.shared.lib.StrategyManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class Break20In10OutStrategy implements IStrategy {


    private IEngine mEngine;
    private IConsole mConsole;
    private IIndicators mIndicators;
    private IHistory mHistory;
    private IAccount mAcount;

    private Set<Instrument> mInstrumentSet = new HashSet<Instrument>();

    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
    private static String STRATEGY_TAG = "Break20In10OutStrategy";

    @Override
    public void onStart(IContext context) throws JFException {
        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mIndicators = context.getIndicators();
        this.mHistory = context.getHistory();
        this.mAcount = context.getAccount();

        // subscribe instruments
        mInstrumentSet.add(Instrument.GBPJPY);
//        mInstrumentSet.add(Instrument.USDJPY);
//        mInstrumentSet.add(Instrument.XAUUSD);
//        mInstrumentSet.add(Instrument.GBPUSD);
//        mInstrumentSet.add(Instrument.AUDUSD);
//        mInstrumentSet.add(Instrument.USDCAD);
//        mInstrumentSet.add(Instrument.USDCHF);
//        mInstrumentSet.add(Instrument.NZDUSD);
//        mInstrumentSet.add(Instrument.GBPAUD);
        context.setSubscribedInstruments(mInstrumentSet, true);

        // date format
        mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (isStrategyInstrument(instrument)) {
            if (period.equals(Period.DAILY)) {
                puts("bar time: " + askBar.getTime());
                long prevPrevBarTime = mHistory.getPreviousBarStart(period, mHistory.getLastTick(instrument).getTime());
                double[][] minMax36 = mIndicators.minMax(instrument, Period.DAILY, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE, 20, Filter.WEEKENDS, 0, prevPrevBarTime, 1);
                double[][] minMax18 = mIndicators.minMax(instrument, Period.DAILY, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE, 10, Filter.WEEKENDS, 0, prevPrevBarTime, 1);
//                puts("minMax36, min: " + minMax36[0] + ", max: " + minMax36[1] + ", length: " + minMax36.length);
//                puts("minMax18, min: " + minMax18[0] + ", max: " + minMax18[1] + ", length: " + minMax18.length);
                List<IOrder> orders = getOrdersByInstrument(instrument);
                if (orders.size() > 0) {
                    IOrder lastOrder = orders.get(orders.size() - 1);
                    if (lastOrder.getOrderCommand().equals(IEngine.OrderCommand.BUY) && bidBar.getClose() < minMax18[0][0]) {
                        for (IOrder order : orders) {
                            order.close();
                        }
                    } else if (lastOrder.getOrderCommand().equals(IEngine.OrderCommand.SELL) && askBar.getClose() > minMax18[1][0]) {
                        for (IOrder order : orders) {
                            order.close();
                        }
                    }
                }
                if (orders.size() < 4) {
                    if (askBar.getClose() > minMax36[1][0]) {
                        String orderLabel = getOrderLabel(instrument, IEngine.OrderCommand.BUY, askBar.getTime());
                        mEngine.submitOrder(orderLabel, instrument, IEngine.OrderCommand.BUY, 0.02, bidBar.getClose(), 3);
                    } else if (bidBar.getClose() < minMax36[0][0]) {
                        String orderLabel = getOrderLabel(instrument, IEngine.OrderCommand.SELL, askBar.getTime());
                        mEngine.submitOrder(orderLabel, instrument, IEngine.OrderCommand.SELL, 0.02, askBar.getClose(), 3);
                    }
                }

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

    private IOrder getLastOrder(Instrument instrument) throws JFException {
        for (IOrder order : mEngine.getOrders()) {
            if (order != null && IOrder.State.FILLED.equals(order.getState()) && instrument.equals(order.getInstrument())) {
                return order;
            }
        }
        return null;
    }

    private List<IOrder> getOrdersByInstrument(Instrument instrument) throws JFException {
        List<IOrder> orderList = new ArrayList<IOrder>();
        for (IOrder order : mEngine.getOrders()) {
            if (order != null && IOrder.State.FILLED.equals(order.getState()) && instrument.equals(order.getInstrument())) {
                orderList.add(order);
            }
        }
        return orderList;
    }

    private void puts(String str) {
//        mConsole.getInfo().println(str);
        StrategyManager.LOGGER.info(str);
    }

    private void sendEmail(String subject, String body) {
//        MailService.sendMail(subject, body);
    }



    // common methods

    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder order = message.getOrder();
        if (order != null) {
        }
        if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {

        }
    }

    @Override
    public void onStop() throws JFException {
        puts("Equity: " + mAcount.getEquity());
        puts(STRATEGY_TAG + " OnStop!");
        sendEmail(STRATEGY_TAG + " OnStop!", "");
    }

    private boolean isStrategyInstrument(Instrument instrument) {
        return mInstrumentSet.contains(instrument);
    }

}