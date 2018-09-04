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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class HelperStrategy implements IStrategy {


    private IEngine engine;
    private IConsole console;
    private IIndicators indicators;
    private IHistory history;

    private String STRATEGY_TAG = "HelperStrategy";

    private Helper h = new Helper();

    @Override
    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.indicators = context.getIndicators();
        this.history = context.getHistory();

        // subscribe instruments
        h.instrumentSet.add(Instrument.EURUSD);
        h.instrumentSet.add(Instrument.USDJPY);
        h.instrumentSet.add(Instrument.XAUUSD);
        h.instrumentSet.add(Instrument.GBPUSD);
        h.instrumentSet.add(Instrument.AUDUSD);
        h.instrumentSet.add(Instrument.USDCAD);
        h.instrumentSet.add(Instrument.USDCHF);
        h.instrumentSet.add(Instrument.NZDUSD);
        h.instrumentSet.add(Instrument.GBPAUD);
        context.setSubscribedInstruments(h.instrumentSet, true);

        // date format
        h.simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {

    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder order = message.getOrder();
        if (order != null) {
            h.removeOrderProcessing(order.getId());
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
        h.puts(STRATEGY_TAG + " OnStop!");
        h.sendEmail(STRATEGY_TAG + " OnStop!", "");
    }




    /**
     * Helper Methods begin ========================================================================
     */

    private class Helper {

        /**
         * common methods
         */
        private Set<Instrument> instrumentSet = new HashSet<Instrument>();
        private HashMap<Instrument, Integer> stopLossPipsMap = new HashMap<>();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

        private double HALF_HAND = 0.01;
        private double ONE_HAND = 0.02;
        private int LITTLE_GOAL = 5;

        private int MAX_STOP_LOSS_PIPS = 100;

        public Helper() {

        }


        private void puts(String str) {
//        console.getInfo().println(str);
            StrategyRunner.LOGGER.info(str);
        }

        private void sendEmail(String subject, String body) {
            MailService.sendMail(subject, body);
        }


        /**
         * To ensure only one request per order
         */
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


        /**
         * Label and Group Management of the filled orders
         */

        private int LABEL_STRATEGY_TAG_INDEX = 0;
        private int LABEL_ROOT_ID_INDEX = 1;
        private int LABEL_ROOT_TAG_INDEX = 2;
        private int LABEL_TIME_MILLIS = 3;
        private String LABEL_NULL = "Null";
        private String LABEL_ROOT_TAG = "Root";
        private String LABEL_MEMBER_TAG = "Member";
        private String LABEL_SINGLE_TAG = "Single";
        private HashMap<String, OrderGroup> groupOrderMap = new HashMap<>();
        private List<IOrder> singleOrderList = new ArrayList<>();

        private List<IOrder> getFilledOrders() throws JFException {
            List<IOrder> filledOrders = new ArrayList<>();
            List<IOrder> allOrders = engine.getOrders();
            for (IOrder order : allOrders) {
                if (order != null && IOrder.State.FILLED.equals(order.getState())) {
                    filledOrders.add(order);
                }
            }
            return filledOrders;
        }

        private String createRootLabel(String orderId) {
            return STRATEGY_TAG + "_" + orderId + "_" + LABEL_ROOT_TAG + "_" + System.currentTimeMillis();
        }

        private String createMemberLabel(String orderId) {
            return STRATEGY_TAG + "_" + orderId + "_" + LABEL_MEMBER_TAG + "_" + System.currentTimeMillis();
        }

        private String createSingleLabel() {
            return STRATEGY_TAG + "_" + LABEL_NULL + "_" + LABEL_SINGLE_TAG + "_" + System.currentTimeMillis();
        }

        private String getLabelKeywordBy(IOrder order, int index) {
            String label = order.getLabel();
            if (label == null) {
                return LABEL_NULL;
            } else {
                String[] splitedLabel = label.split("_");
                if (splitedLabel.length == 4) {
                    return splitedLabel[index];
                } else {
                    return LABEL_NULL;
                }
            }
        }

        private void resetOrderGroupMap() throws JFException {
            groupOrderMap.clear();
            List<IOrder> filledOrders = getFilledOrders();
            filledOrders.sort(new Comparator<IOrder>() {
                @Override
                public int compare(IOrder o1, IOrder o2) {
                    return o1.getFillTime() == o2.getFillTime() ? 0 :
                            (o1.getFillTime() > o2.getFillTime() ? 1 : -1);
                }

            });
            for (IOrder order : filledOrders) {
                String rootId = getLabelKeywordBy(order, LABEL_ROOT_ID_INDEX);
                String groupId;
                if (LABEL_NULL.equals(rootId)) {
                    singleOrderList.add(order);
                    rootId = order.getId();
                    groupId = getGroupId(order.getId(), order.getInstrument(), order.getOrderCommand());
                } else {
                    groupId = getGroupId(rootId, order.getInstrument(), order.getOrderCommand());
                }

                if (!groupOrderMap.containsKey(groupId)) {
                    OrderGroup orderGroup = new OrderGroup(rootId, order.getInstrument(), groupId);
                    orderGroup.getOrders().add(order);
                    groupOrderMap.put(orderGroup.getGroupId(), orderGroup);
                } else if (groupOrderMap.containsKey(groupId)) {
                    groupOrderMap.get(groupId).getOrders().add(order);
                }
            }
        }

        private String getGroupId(String rootId, Instrument instrument, IEngine.OrderCommand command) {
            return instrument.toString() + "_" + rootId + "_" + command.toString();
        }

        private class OrderGroup {
            private final Instrument instrument;
            private final List<IOrder> orders = new ArrayList<>();
            private final String rootId;
            private final String groupId;

            public OrderGroup(String rootId, Instrument instrument, String groupId) {
                this.instrument = instrument;
                this.rootId = rootId;
                this.groupId = groupId;
            }


            public List<IOrder> getOrders() {
                return orders;
            }

            public Instrument getInstrument() {
                return instrument;
            }

            public String getRootId() {
                return rootId;
            }

            public String getGroupId() {
                return groupId;
            }
        }
    }


    /**
     * Helper Methods END ==========================================================================
     */



}