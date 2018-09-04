package com.jforexcn.inbox.strategy;

/**
 * Created by simple on 26/9/16.
 */

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
import com.jforexcn.shared.lib.MailService;
import com.jforexcn.shared.client.StrategyRunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class StopLossWatcher implements IStrategy {


    private IEngine mEngine;
    private IConsole mConsole;
    private IIndicators mIndicators;
    private IHistory mHistory;

    private Set<Instrument> mInstrumentSet = new HashSet<Instrument>();

    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
    private static String STRATEGY_TAG = "StopLossWatcher";

    private static double MAX_STOP_LOSS_PIPS = 100;
    private static double HALF_HAND = 0.01;
    private static double ONE_HAND = 0.02;
    private static int LITTLE_GOAL = 5;

    @Override
    public void onStart(IContext context) throws JFException {
        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mIndicators = context.getIndicators();
        this.mHistory = context.getHistory();

        // subscribe instruments
        mInstrumentSet.add(Instrument.EURUSD);
        mInstrumentSet.add(Instrument.USDJPY);
        mInstrumentSet.add(Instrument.XAUUSD);
        mInstrumentSet.add(Instrument.GBPUSD);
        mInstrumentSet.add(Instrument.AUDUSD);
        mInstrumentSet.add(Instrument.USDCAD);
        mInstrumentSet.add(Instrument.USDCHF);
        mInstrumentSet.add(Instrument.NZDUSD);
        mInstrumentSet.add(Instrument.GBPAUD);
        context.setSubscribedInstruments(mInstrumentSet, true);

        // date format
        mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (period.equals(Period.TEN_SECS)) {
            // 没有止损的订单必须设置止损,
            for (IOrder order : getFilledOrdersWithoutStopLossByInstrument(instrument)) {
                if (IEngine.OrderCommand.BUY.equals(order.getOrderCommand())) {
                    setStopLossPrice(order, bidBar.getClose() - MAX_STOP_LOSS_PIPS * instrument.getPipValue());
                    puts("Set MAX stop loss BUY id:" + order.getId() + ", cInstrument:" + instrument);
                } else if (IEngine.OrderCommand.SELL.equals(order.getOrderCommand())) {
                    setStopLossPrice(order, askBar.getClose() + MAX_STOP_LOSS_PIPS * instrument.getPipValue());
                    puts("Set MAX stop loss SELL id:" + order.getId() + ", cInstrument:" + instrument);
                }
            }

            // 大于最小标准手数的仓位:
            // 1. 盈利达到止损相同的点数, 就把止损价格设置到开仓的价格
            // 2. 盈利到达LITTLE_GOAL, 一定的点数就平仓一部分, 留最小标准手数的一半
            double lossPrice;
            for (IOrder order : getFilledOrdersWithStopLossByInstrument(instrument)) {
                if (order.getAmount() > HALF_HAND) {
                    if (IEngine.OrderCommand.BUY.equals(order.getOrderCommand())) {
                        lossPrice = order.getOpenPrice() - order.getStopLossPrice();
                        if (lossPrice > 0) {
                            if (askBar.getClose() > order.getOpenPrice() + lossPrice) {
                                setStopLossPrice(order, order.getOpenPrice());
                                puts("Set Stop Loss to Open Price BUY id:" + order.getId() + ", cInstrument:" + instrument);
                            }
                        }
                        if (order.getProfitLossInPips() > LITTLE_GOAL && lossPrice <= 0) {
                            close(order, order.getAmount() - HALF_HAND);
                            puts("Get Little Goal close BUY id:" + order.getId() + ", amount: " + (order.getAmount() - HALF_HAND) + ", cInstrument:" + instrument);
                        }
                    } else if (IEngine.OrderCommand.SELL.equals(order.getOrderCommand())) {
                        lossPrice = (order.getStopLossPrice() - order.getOpenPrice());
                        if (lossPrice > 0) {
                            if (bidBar.getClose() < order.getOpenPrice() - lossPrice) {
                                setStopLossPrice(order, order.getOpenPrice());
                                puts("Set Stop Loss to Open Price SELL id:" + order.getId() + ", cInstrument:" + instrument);
                            }
                        }
                        if (order.getProfitLossInPips() > LITTLE_GOAL && lossPrice <= 0) {
                            close(order, order.getAmount() - HALF_HAND);
                            puts("Get Little Goal close BUY id:" + order.getId() + ", amount: " + (order.getAmount() - HALF_HAND) + ", cInstrument:" + instrument);
                        }
                    }
                }
            }
        } else if (period.equals(Period.FIVE_MINS)) {
            // 有止损价格的, 并且止损价高于开仓价格的, 移动止损: 2/3个10日ATR
            double half_atr;
            double stopLossPrice;
            for (IOrder order : getFilledOrdersWithStopLossByInstrument(instrument)) {
                if (IEngine.OrderCommand.BUY.equals(order.getOrderCommand()) && order.getOpenPrice() <= order.getStopLossPrice()) {
                    half_atr = ((int) (mIndicators.atr(instrument, Period.DAILY, OfferSide.BID, 10, 0) / instrument.getPipValue())) * instrument.getPipValue() / 3 * 2;
                    if (askBar.getClose() > order.getStopLossPrice() + half_atr) {
                        stopLossPrice = ((int) ((askBar.getClose() - half_atr) / instrument.getPipValue())) * instrument.getPipValue();
                        puts("FIVE_MINS BUY cInstrument: " + instrument + ", order.label: " + order.getLabel() + ", half_atr: " + half_atr + ", stopLossPrice: " + stopLossPrice);
                        setStopLossPrice(order, stopLossPrice);
                    }
                } else if (IEngine.OrderCommand.SELL.equals(order.getOrderCommand()) && order.getOpenPrice() >= order.getStopLossPrice()) {
                    half_atr = ((int) (mIndicators.atr(instrument, Period.DAILY, OfferSide.ASK, 10, 0) / instrument.getPipValue())) * instrument.getPipValue() / 3 * 2;
                    if (bidBar.getClose() < order.getStopLossPrice() - half_atr) {
                        stopLossPrice = ((int) ((bidBar.getClose() + half_atr) / instrument.getPipValue())) * instrument.getPipValue();
                        puts("FIVE_MINS SELL cInstrument: " + instrument + ", order.label: " + order.getLabel() + ", half_atr: " + half_atr + ", stopLossPrice: " + stopLossPrice);
                        setStopLossPrice(order, stopLossPrice);
                    }
                }
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

    private void sendEmail(String subject, String body) {
        MailService.sendMail(subject, body);
    }

    private ArrayList<IOrder> getFilledOrdersWithStopLossByInstrument(Instrument instrument) throws JFException {
        ArrayList<IOrder> orders = new ArrayList<>();
        for (IOrder order : mEngine.getOrders()) {
            if (order != null &&
                    mInstrumentSet.contains(instrument) &&
                    IOrder.State.FILLED.equals(order.getState()) &&
                    order.getStopLossPrice() > 0 &&
                    instrument.equals(order.getInstrument())
                    ) {
                orders.add(order);
            }
        }
        return orders;
    }

    private ArrayList<IOrder> getFilledOrdersWithoutStopLossByInstrument(Instrument instrument) throws JFException {
        ArrayList<IOrder> orders = new ArrayList<>();
        for (IOrder order : mEngine.getOrders()) {
            if (order != null &&
                    mInstrumentSet.contains(instrument) &&
                    IOrder.State.FILLED.equals(order.getState()) &&
                    order.getStopLossPrice() <= 0 &&
                    instrument.equals(order.getInstrument())
                    ) {
                orders.add(order);
            }
        }
        return orders;
    }

    // common methods

    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder order = message.getOrder();
        if (order != null) {
            removeOrderProcessing(order.getId());
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
        puts(STRATEGY_TAG + " OnStop!");
        sendEmail(STRATEGY_TAG + " OnStop!", "");
    }

    private HashMap<String, OrderProcessing> orderProcessingHashMap = new HashMap<>();
    private void setStopLossPrice(IOrder order, double price) throws JFException {
        long now = System.currentTimeMillis();
        if (canOrderProcessing(order, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), "setStopLossPrice", now);
            insertOrderProcessing(order.getId(), orderProcessing);
            order.setStopLossPrice(price);
        }
    }

    private void setStopLossPrice(IOrder order, double price, OfferSide side) throws JFException {
        long now = System.currentTimeMillis();
        if (canOrderProcessing(order, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), "setStopLossPrice", now);
            insertOrderProcessing(order.getId(), orderProcessing);
            order.setStopLossPrice(price, side);
        }
    }

    private void close(IOrder order, double amount, double price, double slippage) throws JFException {
        long now = System.currentTimeMillis();
        if (canOrderProcessing(order, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), "close", now);
            insertOrderProcessing(order.getId(), orderProcessing);
            order.close(amount, price, slippage);
        }
    }

    private void close(IOrder order, double amount, double price) throws JFException {
        long now = System.currentTimeMillis();
        if (canOrderProcessing(order, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), "close", now);
            insertOrderProcessing(order.getId(), orderProcessing);
            order.close(amount, price);
        }
    }

    private void close(IOrder order, double amount) throws JFException {
        long now = System.currentTimeMillis();
        if (canOrderProcessing(order, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), "close", now);
            insertOrderProcessing(order.getId(), orderProcessing);
            order.close(amount);
        }
    }

    private void close(IOrder order) throws JFException {
        long now = System.currentTimeMillis();
        if (canOrderProcessing(order, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), "close", now);
            insertOrderProcessing(order.getId(), orderProcessing);
            order.close();
        }
    }

    private boolean canOrderProcessing(IOrder order, long time) {
        if (orderProcessingHashMap.containsKey(order.getId())) {
            if (orderProcessingHashMap.get(order.getId()).isExpired(time)) {
                removeOrderProcessing(order.getId());
            } else {
                return false;
            }
        }
        return true;
    }

    private void insertOrderProcessing(String orderId, OrderProcessing orderProcessing) {
        orderProcessingHashMap.put(orderId, orderProcessing);
    }

    private void removeOrderProcessing(String orderId) {
        orderProcessingHashMap.remove(orderId);
    }

    private class OrderProcessing {
        private final String orderId;
        private final long time;
        private final String action;
        private final int expireTime = 30000;

        public OrderProcessing(String orderId, String action, long time) {
            this.orderId = orderId;
            this.action = action;
            this.time = time;
        }

        public boolean isExpired(long time) {
            if (time - this.time > expireTime) {
                return true;
            }
            return false;
        }
    }

}