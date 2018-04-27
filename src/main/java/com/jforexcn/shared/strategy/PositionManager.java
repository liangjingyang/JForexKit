package com.jforexcn.shared.strategy;

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
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class PositionManager implements IStrategy {


    private IEngine engine;
    private IConsole console;
    private IIndicators indicators;
    private IHistory history;

    private static String STRATEGY_TAG = "PositionManager";
    private final Helper h = new Helper();

    @Override
    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.indicators = context.getIndicators();
        this.history = context.getHistory();

        // subscribe instruments
        h.instrumentSet.add(Instrument.EURUSD);
        h.instrumentSet.add(Instrument.USDCHF);
        h.instrumentSet.add(Instrument.GBPUSD);
        h.instrumentSet.add(Instrument.USDJPY);
        h.instrumentSet.add(Instrument.AUDUSD);
        h.instrumentSet.add(Instrument.NZDUSD);
        h.instrumentSet.add(Instrument.USDCAD);
        h.instrumentSet.add(Instrument.USDCNH);
        h.instrumentSet.add(Instrument.GBPAUD);
        h.instrumentSet.add(Instrument.GBPJPY);
        h.instrumentSet.add(Instrument.CADJPY);

        context.setSubscribedInstruments(h.instrumentSet, true);

        // stop loss pips
        h.stopLossPipsMap.put(Instrument.EURUSD, 30);
        h.stopLossPipsMap.put(Instrument.USDCHF, 30);
        h.stopLossPipsMap.put(Instrument.GBPUSD, 40);
        h.stopLossPipsMap.put(Instrument.USDJPY, 40);
        h.stopLossPipsMap.put(Instrument.AUDUSD, 30);
        h.stopLossPipsMap.put(Instrument.NZDUSD, 30);
        h.stopLossPipsMap.put(Instrument.USDCAD, 40);
        h.stopLossPipsMap.put(Instrument.USDCNH, 50);
        h.stopLossPipsMap.put(Instrument.GBPAUD, 50);
        h.stopLossPipsMap.put(Instrument.GBPJPY, 50);
        h.stopLossPipsMap.put(Instrument.CADJPY, 50);

        // date format
        h.simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
        h.resetOrderGroupMap();
        h.puts("onStart");
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (!h.isStrategyInstrument(instrument)) {
            return;
        }
        if (period.equals(Period.TEN_SECS)) {
            // 没有止损的独立仓位必须设置止损,
            for (IOrder order : getSingleOrderWithoutStopLoss(instrument)) {
                if (IEngine.OrderCommand.BUY.equals(order.getOrderCommand())) {
                    h.setStopLossPrice(order, bidBar.getClose() - h.MAX_STOP_LOSS_PIPS * instrument.getPipValue());
                    h.puts("Set MAX stop loss BUY id:" + order.getId() + ", cInstrument:" + instrument);
                } else if (IEngine.OrderCommand.SELL.equals(order.getOrderCommand())) {
                    h.setStopLossPrice(order, askBar.getClose() + h.MAX_STOP_LOSS_PIPS * instrument.getPipValue());
                    h.puts("Set MAX stop loss SELL id:" + order.getId() + ", cInstrument:" + instrument);
                }
            }

            // 大于最小标准手数的独立仓位:
            // 1. 盈利达到止损相同的点数, 就把止损价格设置到开仓的价格
            // 2. 盈利到达LITTLE_GOAL, 一定的点数就平仓一部分, 留最小标准手数的一半
            double lossPrice;
            for (IOrder order : getSingleOrderWithStopLoss(instrument)) {
                if (order.getAmount() > h.HALF_HAND) {
                    if (IEngine.OrderCommand.BUY.equals(order.getOrderCommand())) {
                        lossPrice = order.getOpenPrice() - order.getStopLossPrice();
                        if (lossPrice > 0) {
                            if (askBar.getClose() > order.getOpenPrice() + lossPrice) {
                                h.setStopLossPrice(order, order.getOpenPrice());
                                h.puts("Set Stop Loss to Open Price BUY id:" + order.getId() + ", cInstrument:" + instrument);
                            }
                        }
                        if (order.getProfitLossInPips() > h.LITTLE_GOAL && lossPrice <= 0) {
                            h.puts("Get Little Goal close BUY id:" + order.getId() + ", amount: " + (order.getAmount() - h.HALF_HAND) + ", cInstrument:" + instrument);
                            h.close(order, order.getAmount() - h.HALF_HAND);
                        }
                    } else if (IEngine.OrderCommand.SELL.equals(order.getOrderCommand())) {
                        lossPrice = (order.getStopLossPrice() - order.getOpenPrice());
                        if (lossPrice > 0) {
                            if (bidBar.getClose() < order.getOpenPrice() - lossPrice) {
                                h.setStopLossPrice(order, order.getOpenPrice());
                                h.puts("Set Stop Loss to Open Price SELL id:" + order.getId() + ", cInstrument:" + instrument);
                            }
                        }
                        if (order.getProfitLossInPips() > h.LITTLE_GOAL && lossPrice <= 0) {
                            h.puts("Get Little Goal close SELL id:" + order.getId() + ", amount: " + (order.getAmount() - h.HALF_HAND) + ", cInstrument:" + instrument);
                            h.close(order, order.getAmount() - h.HALF_HAND);
                        }
                    }
                }
            }

            // 判断所有订单, GroupOrderMap, 按照分组来设置止损
            double stopLossPrice;
            for (Map.Entry<String, Helper.OrderGroup> entry : h.groupOrderMap.entrySet()) {
                Helper.OrderGroup orderGroup = entry.getValue();
                if (instrument.equals(orderGroup.getInstrument()) && orderGroup.getOrders().size() >= 1) {
                    IOrder lastOrder = orderGroup.getOrders().get(orderGroup.getOrders().size() - 1);
                    IOrder firstOrder = orderGroup.getOrders().get(0);
                    if (lastOrder.isLong()) {
                        if (orderGroup.getOrders().size() > 1 && lastOrder.getStopLossPrice() <= 0) {
                            // 设置止损, 位于最大浮动获利的0.382回调
                            stopLossPrice = ((int) ((firstOrder.getOpenPrice() + (lastOrder.getOpenPrice() - firstOrder.getOpenPrice()) * 0.618) / instrument.getPipValue()))
                                    * instrument.getPipValue();
                            for (IOrder order : orderGroup.getOrders()) {
                                h.setStopLossPrice(order, stopLossPrice);
                            }
                        }
                    } else {
                        if (orderGroup.getOrders().size() > 1 && lastOrder.getStopLossPrice() <= 0) {
                            // 设置止损, 位于最大浮动获利的0.382回调
                            stopLossPrice = ((int) ((firstOrder.getOpenPrice() - (firstOrder.getOpenPrice() - lastOrder.getOpenPrice()) * 0.618) / instrument.getPipValue()))
                                    * instrument.getPipValue();
                            for (IOrder order : orderGroup.getOrders()) {
                                h.setStopLossPrice(order, stopLossPrice);
                            }
                        }
                    }
                }
            }
        } else if (period.equals(Period.FIVE_MINS)) {
//            // 有止损价格的独立仓位, 并且止损价高于开仓价格的, 移动止损: 2/3个10日ATR
//            double half_atr;
//            double stopLossPrice;
//            for (IOrder order : getSingleOrderWithStopLoss(cInstrument)) {
//                if (IEngine.OrderCommand.BUY.equals(order.getOrderCommand()) && order.getOpenPrice() <= order.getStopLossPrice()) {
//                    half_atr = ((int) (indicator.atr(cInstrument, Period.DAILY, OfferSide.BID, 10, 0) / cInstrument.getPipValue())) * cInstrument.getPipValue() / 3 * 2;
//                    if (askBar.getClose() > order.getStopLossPrice() + half_atr) {
//                        stopLossPrice = ((int) ((askBar.getClose() - half_atr) / cInstrument.getPipValue())) * cInstrument.getPipValue();
//                        h.puts("FIVE_MINS BUY cInstrument: " + cInstrument + ", order.label: " + order.getLabel() + ", half_atr: " + half_atr + ", stopLossPrice: " + stopLossPrice);
//                        h.setStopLossPrice(order, stopLossPrice);
//                    }
//                } else if (IEngine.OrderCommand.SELL.equals(order.getOrderCommand()) && order.getOpenPrice() >= order.getStopLossPrice()) {
//                    half_atr = ((int) (indicator.atr(cInstrument, Period.DAILY, OfferSide.ASK, 10, 0) / cInstrument.getPipValue())) * cInstrument.getPipValue() / 3 * 2;
//                    if (bidBar.getClose() < order.getStopLossPrice() - half_atr) {
//                        stopLossPrice = ((int) ((bidBar.getClose() + half_atr) / cInstrument.getPipValue())) * cInstrument.getPipValue();
//                        h.puts("FIVE_MINS SELL cInstrument: " + cInstrument + ", order.label: " + order.getLabel() + ", half_atr: " + half_atr + ", stopLossPrice: " + stopLossPrice);
//                        h.setStopLossPrice(order, stopLossPrice);
//                    }
//                }
//            }

            // 判断所有订单, GroupOrderMap, 按照分组判断是否加仓
            h.resetOrderGroupMap();
            for (Map.Entry<String, Helper.OrderGroup> entry : h.groupOrderMap.entrySet()) {
                Helper.OrderGroup orderGroup = entry.getValue();
                if (instrument.equals(orderGroup.getInstrument()) && orderGroup.getOrders().size() >= 1) {
                    IOrder lastOrder = orderGroup.getOrders().get(orderGroup.getOrders().size() - 1);
                    IOrder firstOrder = orderGroup.getOrders().get(0);
                    if (lastOrder.isLong()) {
                        // 加仓
                        if (orderGroup.getOrders().size() < 4 && lastOrder.getProfitLossInPips() >= 2 * h.stopLossPipsMap.get(lastOrder.getInstrument())) {
                            h.submitOrder(h.createMemberLabel(firstOrder.getId(), instrument), instrument, lastOrder.getOrderCommand(), h.HALF_HAND, askBar.getClose(), 3);
                        }
                    } else {
                        if (orderGroup.getOrders().size() < 4 && lastOrder.getProfitLossInPips() >= 2 * h.stopLossPipsMap.get(lastOrder.getInstrument())) {
                            h.submitOrder(h.createMemberLabel(firstOrder.getId(), instrument), instrument, lastOrder.getOrderCommand(), h.HALF_HAND, bidBar.getClose(), 3);
                        }
                    }
                }

            }
        }
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
            h.resetOrderGroupMap();
        } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {
            h.resetOrderGroupMap();
        }
    }

    @Override
    public void onStop() throws JFException {
        h.puts(STRATEGY_TAG + " OnStop!");
        h.sendEmail(STRATEGY_TAG + " OnStop!", "");
    }

    private ArrayList<IOrder> getSingleOrderWithStopLoss(Instrument instrument) throws JFException {
        ArrayList<IOrder> orders = new ArrayList<>();
        for (IOrder order : h.singleOrderList) {
            if (order != null &&
                    h.instrumentSet.contains(instrument) &&
                    IOrder.State.FILLED.equals(order.getState()) &&
                    order.getStopLossPrice() > 0 &&
                    instrument.equals(order.getInstrument())
                    ) {
                orders.add(order);
            }
        }
        return orders;
    }

    private ArrayList<IOrder> getSingleOrderWithoutStopLoss(Instrument instrument) throws JFException {
        ArrayList<IOrder> orders = new ArrayList<>();
        for (IOrder order : h.singleOrderList) {
            if (order != null &&
                    h.instrumentSet.contains(instrument) &&
                    IOrder.State.FILLED.equals(order.getState()) &&
                    order.getStopLossPrice() <= 0 &&
                    instrument.equals(order.getInstrument())
                    ) {
                orders.add(order);
            }
        }
        return orders;
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

        private boolean isStrategyInstrument(Instrument instrument) {
            return instrumentSet.contains(instrument);
        }

        /**
         * buy bar and sell bar
         */
        private boolean isBuyBar(IBar bar) {
            double bigRange = bar.getHigh() - bar.getLow();
            if (bar.getClose() > bar.getOpen()) {
                // 阳线的情况, 收盘价必须在bar的上1/3部分
                if (bar.getClose() > bar.getLow() + bigRange / 3 * 2) {
                    return true;
                }
            } else {
                // 阴线和十字星的情况, 收盘价必须在bar的上1/4部分
                if (bar.getClose() > bar.getLow() + bigRange / 4 * 3) {
                    return true;
                }
            }
            return false;
        }

        private boolean isSellBar(IBar bar) {
            double bigRange = bar.getHigh() - bar.getLow();
            if (bar.getClose() < bar.getOpen()) {
                // 阴线的情况, 收盘价必须在bar的下1/3部分
                if (bar.getClose() < bar.getLow() + bigRange / 3 * 1) {
                    return true;
                }
            } else {
                // 阳线和十字星的情况, 收盘价必须在bar的上1/4部分
                if (bar.getClose() < bar.getLow() + bigRange / 4 * 1) {
                    return true;
                }
            }
            return false;
        }

        /**
         * allow only one order per cInstrument one minute.
         */
        private void submitOrder(String label, Instrument instrument, IEngine.OrderCommand orderCommand,
                                 double amount, double price, double slippage) throws JFException {
            IOrder lastOrder = getLastOrderByInstrument(instrument);
            if (lastOrder != null && (history.getTimeOfLastTick(instrument) - lastOrder.getFillTime() < 60 * 1000)) {
                return;
            }
            engine.submitOrder(label, instrument, orderCommand, amount, price, slippage);
        }


        /**
         * To ensure only one request per order
         */
        private HashMap<String, OrderProcessing> orderProcessingHashMap = new HashMap<>();

        private void setStopLossPrice(IOrder order, double price) throws JFException {
            long now = history.getTimeOfLastTick(order.getInstrument());
            if (canOrderProcessing(order, now)) {
                OrderProcessing orderProcessing = new OrderProcessing(order.getId(), "setStopLossPrice", now);
                insertOrderProcessing(order.getId(), orderProcessing);
                order.setStopLossPrice(price);
            }
        }

        private void setStopLossPrice(IOrder order, double price, OfferSide side) throws JFException {
            long now = history.getTimeOfLastTick(order.getInstrument());
            if (canOrderProcessing(order, now)) {
                OrderProcessing orderProcessing = new OrderProcessing(order.getId(), "setStopLossPrice", now);
                insertOrderProcessing(order.getId(), orderProcessing);
                order.setStopLossPrice(price, side);
            }
        }

        private void close(IOrder order, double amount, double price, double slippage) throws JFException {
            long now = history.getTimeOfLastTick(order.getInstrument());
            if (canOrderProcessing(order, now)) {
                OrderProcessing orderProcessing = new OrderProcessing(order.getId(), "close", now);
                insertOrderProcessing(order.getId(), orderProcessing);
                order.close(amount, price, slippage);
            }
        }

        private void close(IOrder order, double amount, double price) throws JFException {
            long now = history.getTimeOfLastTick(order.getInstrument());
            if (canOrderProcessing(order, now)) {
                OrderProcessing orderProcessing = new OrderProcessing(order.getId(), "close", now);
                insertOrderProcessing(order.getId(), orderProcessing);
                order.close(amount, price);
            }
        }

        private void close(IOrder order, double amount) throws JFException {
            long now = history.getTimeOfLastTick(order.getInstrument());
            if (canOrderProcessing(order, now)) {
                OrderProcessing orderProcessing = new OrderProcessing(order.getId(), "close", now);
                insertOrderProcessing(order.getId(), orderProcessing);
                order.close(amount);
            }
        }

        private void close(IOrder order) throws JFException {
            long now = history.getTimeOfLastTick(order.getInstrument());
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
            filledOrders.sort(new Comparator<IOrder>() {
                @Override
                public int compare(IOrder o1, IOrder o2) {
                    return o1.getFillTime() == o2.getFillTime() ? 0 :
                            (o1.getFillTime() > o2.getFillTime() ? 1 : -1);
                }

            });
            return filledOrders;
        }

        private IOrder getLastOrderByInstrument(Instrument instrument) throws JFException {
            List<IOrder> allOrders = engine.getOrders();
            IOrder lastOrder = null;
            for (IOrder order : allOrders) {
                if (order != null && instrument.equals(order.getInstrument()) && IOrder.State.FILLED.equals(order.getState())) {
                    if (lastOrder == null || order.getFillTime() > lastOrder.getFillTime()) {
                        lastOrder = order;
                    }
                }
            }
            return lastOrder;
        }

        private IOrder getLastOrder() throws JFException {
            List<IOrder> allOrders = engine.getOrders();
            IOrder lastOrder = null;
            for (IOrder order : allOrders) {
                if (order != null && IOrder.State.FILLED.equals(order.getState())) {
                    if (lastOrder == null || order.getFillTime() > lastOrder.getFillTime()) {
                        lastOrder = order;
                    }
                }
            }
            return lastOrder;
        }

        private String createRootLabel(String orderId, Instrument instrument) throws JFException {
            return STRATEGY_TAG + "_" + orderId + "_" + LABEL_ROOT_TAG + "_" + history.getTimeOfLastTick(instrument);
        }

        private String createMemberLabel(String orderId, Instrument instrument) throws JFException {
            return STRATEGY_TAG + "_" + orderId + "_" + LABEL_MEMBER_TAG + "_" + history.getTimeOfLastTick(instrument);
        }

        private String createSingleLabel(Instrument instrument) throws JFException {
            return STRATEGY_TAG + "_" + LABEL_NULL + "_" + LABEL_SINGLE_TAG + "_" + history.getTimeOfLastTick(instrument);
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