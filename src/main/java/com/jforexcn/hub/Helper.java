package com.jforexcn.hub;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.JFUtils;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.TickBarSize;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by simple(simple.continue@gmail.com) on 26/04/2018.
 */

public class Helper {

    /**
     * common methods
     */
    private Set<Instrument> instrumentSet = new HashSet<Instrument>();
    private HashMap<String, Long> submitTimeMap = new HashMap<>();
    private HashMap<String, Long> filledTimeMap = new HashMap<>();

    String strategyTag;
    IContext mContext;
    IHistory mHistory;
    IAccount mAccount;
    IEngine mEngine;
    IConsole mConsole;
    JFUtils mUtils;

    boolean debug = true;

    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    public void init(IContext context, String strategyTag) {
        this.strategyTag = strategyTag;
        this.mContext = context;
        this.mHistory = context.getHistory();
        this.mAccount = context.getAccount();
        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mUtils = context.getUtils();
        // date format
        mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
    }

    public void addInstrument(Instrument instrument) {
        instrumentSet.add(instrument);
    }

    public void addInstruments(Set<Instrument> instruments) {
        instrumentSet.addAll(instruments);
    }

    public Set<Instrument> getInstrumentSet() {
        return instrumentSet;
    }

    public String getStrategyTag() {
        return strategyTag;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public long getLastTickTime(Instrument instrument) throws JFException {
        long lastTickTime = mHistory.getTimeOfLastTick(instrument);
        if (lastTickTime <= 0) {
            lastTickTime = System.currentTimeMillis();
        }
        return lastTickTime;
    }

    public double pipToCurrency(Instrument instrument) throws JFException {
        return mUtils.convertPipToCurrency(instrument, mAccount.getAccountCurrency());
    }

    public double clearAmount(Instrument instrument, double amount) {
        switch (instrument.toString()){
            case "XAU/USD" : return amount >= 1 ? amount : 0;
            case "XAG/USD" : return amount >= 50 ? amount : 0;
            default : return amount >= 1000 ? amount : 0;
        }
    }

    public double getMinMil(Instrument instrument){
        switch (instrument.toString()){
            case "XAU/USD" : return 0.000001;
            case "XAG/USD" : return 0.00005;
            default : return 0.001;
        }
    }

    public int getMinAmount(Instrument instrument){
        switch (instrument.toString()){
            case "XAU/USD" : return 1;
            case "XAG/USD" : return 50;
            default : return 1000;
        }
    }

    public int getMilScale(Instrument instrument){
        switch (instrument.toString()){
            case "XAU/USD" : return 6;
            case "XAG/USD" : return 5;
            default : return 3;
        }
    }

    public double amountToMil(Instrument instrument, double amount) {
        BigDecimal mil = new BigDecimal(amount);
        BigDecimal divisor = new BigDecimal(1000000);
        mil = mil.divide(divisor, getMilScale(instrument), RoundingMode.DOWN);
        return mil.doubleValue();
    }

    public double milToAmount(double mil) {
        BigDecimal amount = new BigDecimal(mil);
        BigDecimal multiplicand = new BigDecimal(1000000);
        amount = amount.multiply(multiplicand);
        amount = amount.setScale(0, RoundingMode.DOWN);
        return amount.doubleValue();
    }

    public double scalePrice(double price, int pipScale) {
        // pips: 0.0001, price: 0.00001
        return scaleDouble(price, pipScale + 1, RoundingMode.HALF_UP);
    }

    public double scaleDouble(double value, int scale, RoundingMode roundingMode) {
        if (scale < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(scale, roundingMode);
        return bd.doubleValue();
    }

    public void logInfo(String str) {
        mConsole.getInfo().println(str);
//            StrategyRunner.LOGGER.info(str);
    }

    public void logError(String str) {
        mConsole.getErr().println(str);
    }

    public void logDebug(String str) {
        if (debug) {
            mConsole.getInfo().println("== DEBUG == " + str);
        }
    }

    public void sendEmail(String subject, String body) {
    }

    public boolean isStrategyInstrument(Instrument instrument) {
        return instrumentSet.contains(instrument);
    }

    public boolean isStrategyOrder(IOrder order) {
        return order.getLabel().startsWith(getStrategyTag());
    }

    public void orderFilled(IOrder order) {
        String key = order.getInstrument() + "#" + order.getOrderCommand();
        filledTimeMap.put(key, order.getFillTime());
    }

    public long getLastFilledTime(Instrument instrument, IEngine.OrderCommand command) {
        Long time = filledTimeMap.get(instrument + "#" + command);
        if (time != null) {
            return time;
        }
        return 0L;
    }

    /**
     * allow only one order per cInstrument one minute.
     */
    public void submitOrder(String label, Instrument instrument, IEngine.OrderCommand orderCommand,
                            double amount, double price, double slippage, long time) throws JFException {
        Long lastSubmitTime = submitTimeMap.get(instrument + "#" + orderCommand);
        if (lastSubmitTime == null) {
            lastSubmitTime = 0L;
        }
        if (time - lastSubmitTime > 60 * 1000) {
            mEngine.submitOrder(label, instrument, orderCommand, amount, price, slippage);
            submitTimeMap.put(instrument + "#" + orderCommand, time);
        }
    }


    /**
     * To ensure only one request per order
     */
    private HashMap<String, OrderProcessing> orderProcessingHashMap = new HashMap<>();

    public void setStopLossPrice(IOrder order, double price) throws JFException {
        price = scalePrice(price, order.getInstrument().getPipScale());
        if (order.getStopLossPrice() == price) {
            return;
        }
        long now = mHistory.getTimeOfLastTick(order.getInstrument());
        String processingKey = getOrderProcessingKey(order.getId(), "setStopLossPrice");
        if (canOrderProcessing(processingKey, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), now);
            insertOrderProcessing(processingKey, orderProcessing);
            order.setStopLossPrice(price);
        }
    }

    public void setStopLossPrice(IOrder order, double price, OfferSide side) throws JFException {
        price = scalePrice(price, order.getInstrument().getPipScale());
        if (order.getStopLossPrice() == price) {
            return;
        }
        long now = mHistory.getTimeOfLastTick(order.getInstrument());
        String processingKey = getOrderProcessingKey(order.getId(), "setStopLossPrice");
        if (canOrderProcessing(processingKey, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), now);
            insertOrderProcessing(processingKey, orderProcessing);
            order.setStopLossPrice(price, side);
        }
    }

    public void setTakeProfitPrice(IOrder order, double price) throws JFException {
        price = scalePrice(price, order.getInstrument().getPipScale());
        if (order.getTakeProfitPrice() == price) {
            return;
        }
        long now = mHistory.getTimeOfLastTick(order.getInstrument());
        String processingKey = getOrderProcessingKey(order.getId(), "setTakeProfitPrice");
        if (canOrderProcessing(processingKey, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), now);
            insertOrderProcessing(processingKey, orderProcessing);
            order.setTakeProfitPrice(price);
        }
    }


    public void close(IOrder order, double amount, double price, double slippage) throws JFException {
        price = scalePrice(price, order.getInstrument().getPipScale());
        if (!(order.getState().equals(IOrder.State.FILLED) || order.getState().equals(IOrder.State.OPENED))) {
            return;
        }
        long now = mHistory.getTimeOfLastTick(order.getInstrument());
        String processingKey = getOrderProcessingKey(order.getId(), "close");
        if (canOrderProcessing(processingKey, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), now);
            insertOrderProcessing(processingKey, orderProcessing);
            order.close(amount, price, slippage);
        }
    }

    public void close(IOrder order, double amount, double price) throws JFException {
        price = scalePrice(price, order.getInstrument().getPipScale());
        if (!(order.getState().equals(IOrder.State.FILLED) || order.getState().equals(IOrder.State.OPENED))) {
            return;
        }
        long now = mHistory.getTimeOfLastTick(order.getInstrument());
        String processingKey = getOrderProcessingKey(order.getId(), "close");
        if (canOrderProcessing(processingKey, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), now);
            insertOrderProcessing(processingKey, orderProcessing);
            order.close(amount, price);
        }
    }

    public void close(IOrder order, double amount) throws JFException {
        if (!(order.getState().equals(IOrder.State.FILLED) || order.getState().equals(IOrder.State.OPENED))) {
            return;
        }
        long now = mHistory.getTimeOfLastTick(order.getInstrument());
        String processingKey = getOrderProcessingKey(order.getId(), "close");
        if (canOrderProcessing(processingKey, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), now);
            insertOrderProcessing(processingKey, orderProcessing);
            order.close(amount);
        }
    }

    public void close(IOrder order) throws JFException {
        if (!(order.getState().equals(IOrder.State.FILLED) || order.getState().equals(IOrder.State.OPENED))) {
            return;
        }
        long now = mHistory.getTimeOfLastTick(order.getInstrument());
        String processingKey = getOrderProcessingKey(order.getId(), "close");
        if (canOrderProcessing(processingKey, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), now);
            insertOrderProcessing(processingKey, orderProcessing);
            order.close();
        }
    }

    private boolean canOrderProcessing(String processingKey, long time) {
        if (orderProcessingHashMap.containsKey(processingKey)) {
            if (orderProcessingHashMap.get(processingKey).isExpired(time)) {
                removeOrderProcessing(processingKey);
            } else {
                return false;
            }
        }
        return true;
    }

    private void insertOrderProcessing(String processingKey, OrderProcessing orderProcessing) {
        orderProcessingHashMap.put(processingKey, orderProcessing);
    }

    public void removeOrderProcessing(String processingKey) {
        orderProcessingHashMap.remove(processingKey);
    }

    private String getOrderProcessingKey(String orderId, String action) {
        return orderId + "@" + action;
    }

    private class OrderProcessing {
        private final String orderId;
        private final long time;
        private final int expireTime = 3000;

        OrderProcessing(String orderId, long time) {
            this.orderId = orderId;
            this.time = time;
        }

        boolean isExpired(long time) {
            if (time - this.time > expireTime) {
                return true;
            }
            return false;
        }
    }
}
