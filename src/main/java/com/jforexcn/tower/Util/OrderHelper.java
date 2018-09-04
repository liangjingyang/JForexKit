package com.jforexcn.tower.Util;

import com.dukascopy.api.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by simple on 2018/8/18
 */

public class OrderHelper {
    /*
     * To ensure only one request per order
     */
    private String strategyTag;
    private IContext context;
    private HashMap<String, OrderProcessing> orderProcessingHashMap = new HashMap<>();
    private HashMap<String, Long> submitTimeMap = new HashMap<>();
    private HashMap<String, Long> filledTimeMap = new HashMap<>();
    private HashMap<Object, Integer> theCounter = new HashMap<>();
    private long orderSubmitInterval = 60 * 1000;
    
    public OrderHelper(String strategyTag, IContext context) {
        this.strategyTag = strategyTag;
        this.context = context;
    }

    public void onMessage(IMessage message) throws JFException {
        IOrder order = message.getOrder();
        if (order != null) {
            removeOrderProcessing(order.getId());
        }
        if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {
        } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {
        } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {
            addCounter(message.getType());
            checkAndResetCounter(message.getType(), 5);
        } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {
        } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {
            addCounter(message.getType());
            checkAndResetCounter(message.getType(), 5);
        } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {
        } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {
            addCounter(message.getType());
            checkAndResetCounter(message.getType(), 5);
        } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {
        }
    }

    public void addCounter(Object obj) {
        if (theCounter.containsKey(obj)) {
            theCounter.put(obj, theCounter.get(obj) + 1);
        } else {
            theCounter.put(obj, 1);
        }
    }

    public void checkAndResetCounter(Object obj, int waterMark) {
        if (theCounter.containsKey(obj)) {
            int count = theCounter.get(obj);
            if (count >= waterMark) {
                BaseHelper.sendMail(strategyTag + " " + obj.toString() + " reaches " + waterMark, "");
                theCounter.put(obj, 0);
            }
        }
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
        if (time - lastSubmitTime > orderSubmitInterval) {
            context.getEngine().submitOrder(label, instrument, orderCommand, amount, price, slippage);
            submitTimeMap.put(instrument + "#" + orderCommand, time);
        }
    }

    /**
     * allow only one order per cInstrument one minute.
     */
    public void submitOrder(String label, Instrument instrument, IEngine.OrderCommand orderCommand,
                            double amount, double price, double slippage, double stopLossPrice,
                            double takeProfitPrice, long time) throws JFException {
        Long lastSubmitTime = submitTimeMap.get(instrument + "#" + orderCommand);
        if (lastSubmitTime == null) {
            lastSubmitTime = 0L;
        }
        if (time - lastSubmitTime > orderSubmitInterval) {
            context.getEngine().submitOrder(label, instrument, orderCommand, amount, price, slippage, stopLossPrice, takeProfitPrice);
            submitTimeMap.put(instrument + "#" + orderCommand, time);
        }
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
                            double amount, double price, double slippage, double stopLossPrice,
                            double takeProfitPrice, long goodTillTime, long time) throws JFException {
        Long lastSubmitTime = submitTimeMap.get(instrument + "#" + orderCommand);
        if (lastSubmitTime == null) {
            lastSubmitTime = 0L;
        }
        if (time - lastSubmitTime > orderSubmitInterval) {
            context.getEngine().submitOrder(label, instrument, orderCommand, amount, price, slippage, stopLossPrice, takeProfitPrice, goodTillTime);
            submitTimeMap.put(instrument + "#" + orderCommand, time);
        }
    }
    public void setStopLossPrice(IOrder order, double price) throws JFException {
        price = BaseHelper.scalePrice(price, order.getInstrument().getPipScale());
        if (order.getStopLossPrice() == price) {
            return;
        }
        long now = context.getHistory().getTimeOfLastTick(order.getInstrument());
        String processingKey = getOrderProcessingKey(order.getId(), "setStopLossPrice");
        if (canOrderProcessing(processingKey, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), now);
            insertOrderProcessing(processingKey, orderProcessing);
            order.setStopLossPrice(price);
        }
    }

    public void setStopLossPrice(IOrder order, double price, OfferSide side) throws JFException {
        price = BaseHelper.scalePrice(price, order.getInstrument().getPipScale());
        if (order.getStopLossPrice() == price) {
            return;
        }
        long now = context.getHistory().getTimeOfLastTick(order.getInstrument());
        String processingKey = getOrderProcessingKey(order.getId(), "setStopLossPrice");
        if (canOrderProcessing(processingKey, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), now);
            insertOrderProcessing(processingKey, orderProcessing);
            order.setStopLossPrice(price, side);
        }
    }

    public void setTakeProfitPrice(IOrder order, double price) throws JFException {
        price = BaseHelper.scalePrice(price, order.getInstrument().getPipScale());
        if (order.getTakeProfitPrice() == price) {
            return;
        }
        long now = context.getHistory().getTimeOfLastTick(order.getInstrument());
        String processingKey = getOrderProcessingKey(order.getId(), "setTakeProfitPrice");
        if (canOrderProcessing(processingKey, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), now);
            insertOrderProcessing(processingKey, orderProcessing);
            order.setTakeProfitPrice(price);
        }
    }


    public void close(IOrder order, double amount, double price, double slippage) throws JFException {
        price = BaseHelper.scalePrice(price, order.getInstrument().getPipScale());
        if (!(order.getState().equals(IOrder.State.FILLED) || order.getState().equals(IOrder.State.OPENED))) {
            return;
        }
        long now = context.getHistory().getTimeOfLastTick(order.getInstrument());
        String processingKey = getOrderProcessingKey(order.getId(), "close");
        if (canOrderProcessing(processingKey, now)) {
            OrderProcessing orderProcessing = new OrderProcessing(order.getId(), now);
            insertOrderProcessing(processingKey, orderProcessing);
            order.close(amount, price, slippage);
        }
    }

    public void close(IOrder order, double amount, double price) throws JFException {
        price = BaseHelper.scalePrice(price, order.getInstrument().getPipScale());
        if (!(order.getState().equals(IOrder.State.FILLED) || order.getState().equals(IOrder.State.OPENED))) {
            return;
        }
        long now = context.getHistory().getTimeOfLastTick(order.getInstrument());
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
        long now = context.getHistory().getTimeOfLastTick(order.getInstrument());
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
        long now = context.getHistory().getTimeOfLastTick(order.getInstrument());
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

    public List<IOrder> getStrategyOrdersByInstrument(Instrument instrument) throws JFException {
        List<IOrder> orders = new ArrayList<>();
        for (IOrder order : context.getEngine().getOrders()) {
            if (order.getLabel().startsWith(strategyTag)) {
                if (order.getInstrument().equals(instrument)) {
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    public IOrder getStrategyLastOrderByInstrument(Instrument instrument) throws JFException {
        IOrder lastOrder = null;
        for (IOrder order : context.getEngine().getOrders()) {
            if (order.getLabel().startsWith(strategyTag)) {
                if (order.getInstrument().equals(instrument)) {
                    if (lastOrder == null) {
                        lastOrder = order;
                    } else {
                        if (order.getCreationTime() > lastOrder.getCreationTime()) {
                            lastOrder = order;
                        }
                    }
                }
            }
        }
        return lastOrder;
    }
}
