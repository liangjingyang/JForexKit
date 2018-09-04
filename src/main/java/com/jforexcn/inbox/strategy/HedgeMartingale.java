package com.jforexcn.inbox.strategy;

/**
 * Created by simple on 8/5/17.
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class HedgeMartingale implements IStrategy {

    private class MTParams {

         final int tpPips;
         final double startAmount;
         final double multiple;
         final int positionLimit;
         final int intervalPips;

         MTParams(int tpPips, double startAmount, double multiple, int positionLimit, int intervalPips) {
            this.tpPips = tpPips;
            this.startAmount = startAmount;
            this.multiple = multiple;
            this.positionLimit = positionLimit;
            this.intervalPips = intervalPips;
        }
    }

    private class MTGroup {
        MTPair parent;
        MTGroup brother;
        IEngine.OrderCommand command;
        ArrayList<IOrder> orders = new ArrayList<IOrder>();
        Instrument instrument;

        MTGroup(MTPair parent, IEngine.OrderCommand command, Instrument instrument) {
            this.parent = parent;
            this.command = command;
            this.instrument = instrument;
        }

        IOrder getLastOrder() {
            if (orders.size() > 0) {
                return orders.get(orders.size() - 1);
            }
            return null;
        }

        double getLastOrderAmount() {
            if (orders.size() > 0) {
                return orders.get(orders.size() - 1).getAmount();
            }
            return 0.0;
        }

        public void onTick(ITick tick) throws JFException {
            if (isHavePosition()) {
                double totalPips = getTotalProfitLossInPips(tick);
                MTParams params = mtParams.get(instrument);
                if (totalPips > params.tpPips) {
                    closePositions();
                } else {
                    addPosition(tick);
                }
            } else {
                newPosition(tick);
            }
        }

        public void onBar(Period period, IBar askBar, IBar bidBar) throws JFException {

        }

        private boolean isHavePosition() {
            return orders.size() > 0;
        }

        private void newPosition(ITick tick) throws JFException {
            if (checkCloseHigher(instrument, command)) {
                MTParams params = mtParams.get(instrument);

                double price = tick.getAsk();
                if (command.equals(IEngine.OrderCommand.BUY)) {
                    price = tick.getBid();
                }

                double amount = params.startAmount;
                if (brother != null && brother.getLastOrderAmount() > amount) {
                    amount = brother.getLastOrderAmount();
                }
                MTLabel orderLabel = new MTLabel(STRATEGY_TAG, instrument, command, tick.getTime());

                h.submitOrder(orderLabel.toString(), instrument, command, amount, price, 3, tick.getTime());
            }
        }

        private void addPosition(ITick tick) throws JFException {
            IOrder lastOrder = getLastOrder();
            int orderCount = orders.size();
            MTParams params = mtParams.get(instrument);
            if (orderCount < params.positionLimit &&
                    checkCloseHigher(instrument, command)) {
                double addAmount = params.startAmount * Math.pow(params.multiple, orderCount);
                double addPrice;
                if (command.equals(IEngine.OrderCommand.BUY)) {
                    addPrice = lastOrder.getOpenPrice() - params.intervalPips * instrument.getPipValue();
//                h.puts("tryAddposition, buy, addprice: " + addPrice + ", addAmount: " + addAmount + ", bid: " + tick.getBid());
                    if (tick.getBid() < addPrice) {
                        MTLabel orderLabel = new MTLabel(STRATEGY_TAG, instrument, command, tick.getTime());
                        h.submitOrder(orderLabel.toString(), instrument, command, addAmount, tick.getBid(), 3, tick.getTime());
                    }
                } else if (command.equals(IEngine.OrderCommand.SELL)) {
                    addPrice = lastOrder.getOpenPrice() + params.intervalPips * instrument.getPipValue();
//                h.puts("tryAddposition, sell, addprice: " + addPrice + ", addAmount: " + addAmount + ", ask: " + tick.getAsk());
                    if (tick.getAsk() > addPrice) {
                        MTLabel orderLabel = new MTLabel(STRATEGY_TAG, instrument, command, tick.getTime());
                        h.submitOrder(orderLabel.toString(), instrument, command, addAmount, tick.getAsk(), 3, tick.getTime());
                    }
                }
            }
        }

        private void closePositions() throws JFException {
            for (IOrder o : orders) {
                h.close(o);
            }
        }

        private double getTotalProfitLossInPips(ITick tick) throws JFException {
            double pips = 0;
            MTParams params = mtParams.get(instrument);
            for (IOrder o : orders) {
                pips = pips + o.getProfitLossInPips() * (o.getAmount() / params.startAmount);
            }
//            if (orders.size() >= params.positionLimit) {
//                IOrder lastOrder = orders.get(orders.size() - 1);
//                List<IOrder> historyOrders = history.getOrdersHistory(cInstrument, lastOrder.getFillTime(), tick.getTime());
//                for(IOrder ho : historyOrders) {
//                    if (IOrder.State.CLOSED.equals(ho.getState()) && ho.getCloseTime() >= lastOrder.getFillTime()) {
//                        pips = pips + ho.getProfitLossInPips() * (ho.getAmount() / params.startAmount);
//                    }
//                }
//            }
            return pips;
        }
    }

    private class MTPair {
        MTGroup buyGroup;
        MTGroup sellGroup;
        Instrument instrument;

        MTPair(Instrument instrument) {
            this.instrument = instrument;
        }

        public void onTick(ITick tick) throws JFException {
            if (buyGroup != null) {
                buyGroup.onTick(tick);
            }
            if (sellGroup != null) {
                sellGroup.onTick(tick);
            }
        }

        public void onBar(Period period, IBar askBar, IBar bidBar) throws JFException {
            if (buyGroup != null) {
                buyGroup.onBar(period, askBar, bidBar);
            }
            if (sellGroup != null) {
                sellGroup.onBar(period, askBar, bidBar);
            }
        }
    }

    private static class MTLabel {
        private final String strategyTag;
        private final Instrument instrument;
        private final IEngine.OrderCommand command;
        private final long time;
        MTLabel(String strategyTag, Instrument instrument, IEngine.OrderCommand command, long time) {
            this.strategyTag = strategyTag;
            this.instrument = instrument;
            this.command = command;
            this.time = time;
        }

        public String toString() {
            String instrumentStr = this.instrument.toString().replace('/', '_');
            return this.strategyTag + "_" +
                    instrumentStr + "_" +
                    this.command + "_" +
                    this.time;
        }

        static MTLabel fromString(String label) {
            String[] splited = label.split("_");
            if (splited.length == 5) {
                Instrument instrument = Instrument.fromString(splited[1] + "/" + splited[2]);
                IEngine.OrderCommand command = IEngine.OrderCommand.valueOf(splited[3]);
                return new MTLabel(splited[0],
                        instrument,
                        command,
                        Long.parseLong(splited[4]));
            }
            return null;
        }
    }

    private class MTPositionManager {
        HashMap<Instrument, MTPair> pairs;

        void refresh(IOrder order) throws JFException {
            if (order == null) {
                return;
            }
            refresh(order.getInstrument());
        }

        void refresh(Instrument instrument) throws JFException {
            if (instrument == null) {
                return;
            }
            pairs = new HashMap<>();
            MTPair pair = new MTPair(instrument);
            MTGroup buyGroup = new MTGroup(pair, IEngine.OrderCommand.BUY, instrument);
            MTGroup sellGroup = new MTGroup(pair, IEngine.OrderCommand.SELL, instrument);
            pair.buyGroup = buyGroup;
            pair.sellGroup = sellGroup;
            buyGroup.brother = sellGroup;
            sellGroup.brother = buyGroup;
            pairs.put(instrument, pair);
            List<IOrder> orders = h.getOrdersByInstrument(instrument);
            for (IOrder order : orders) {
                MTLabel mtLabel = MTLabel.fromString(order.getLabel());
                if (mtLabel != null) {
                    if (buyGroup.command.equals(order.getOrderCommand())) {
                        buyGroup.orders.add(order);
                    } else if (sellGroup.command.equals(order.getOrderCommand())) {
                        sellGroup.orders.add(order);
                    }
                }

            }
            h.puts("cInstrument: " + instrument + ", buy group order size: " + buyGroup.orders.size() + ", sell group order size: " + sellGroup.orders.size());
        }

        public void onTick(Instrument instrument, ITick tick) throws JFException {
            MTPair pair = pairs.get(instrument);
            if (pair != null) {
                pair.onTick(tick);
            }
        }

        public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
            MTPair pair = pairs.get(instrument);
            if (pair != null) {
                pair.onBar(period, askBar, bidBar);
            }
        }
    }


    private IEngine engine;
    private IConsole console;
    private IIndicators indicators;
    private IHistory history;

    private static String STRATEGY_TAG = "HedgeMartingale";
    private final Helper h = new Helper();
    private HashMap<Instrument, MTParams> mtParams = new HashMap<Instrument, MTParams>();
    private final MTPositionManager m = new MTPositionManager();
    private HashMap<Instrument, Boolean> isCloseHigher = new HashMap<Instrument, Boolean>();
    private boolean isInit = false;

    @Override
    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.indicators = context.getIndicators();
        this.history = context.getHistory();

        // date format
        h.simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));

        // subscribe instruments
        h.instrumentSet.add(Instrument.GBPAUD);
        context.setSubscribedInstruments(h.instrumentSet, true);

        MTParams GBPAUDMartingaleParams = new MTParams(15, 0.002, 1.5, 4, 30);
        mtParams.put(Instrument.GBPAUD, GBPAUDMartingaleParams);

        h.puts("onStart");
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        try {
            if (!isInit) {
                for (Instrument strategyInstrument : h.instrumentSet) {
                    updateIsCloseHigher(strategyInstrument, history.getBarStart(Period.FIVE_MINS, tick.getTime()));
                    m.refresh(strategyInstrument);
                }
                isInit = true;
            }
            if (h.isStrategyInstrument(instrument)) {
                m.onTick(instrument, tick);
            }
        } catch (JFException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            h.sendEmail("CRASH! " + getClass().getSimpleName(), "onTick exception: " + e.getMessage());
        }
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        try {
            if (h.isStrategyInstrument(instrument)) {
                if (period.equals(Period.FIVE_MINS)) {
                    updateIsCloseHigher(instrument, askBar.getTime());
                }
                m.onBar(instrument, period, askBar, bidBar);
            }
        } catch (JFException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            h.sendEmail("CRASH! " + getClass().getSimpleName(), "onBar exception: " + e.getMessage());
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }


    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder order = message.getOrder();
        h.puts("onMessage order: " + order.getLabel() + ", command: " + order.getOrderCommand() + ", message: " + message.getType());
        if (order != null) {
            h.removeOrderProcessing(order.getId());
        }
        if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {
            m.refresh(order);
        } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {
            m.refresh(order);
        } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {
            m.refresh(order);
            h.orderFilled(order);
        } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {
            m.refresh(order);
        } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {
            h.close(order);
        } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {
            m.refresh(order);
        }
    }

    @Override
    public void onStop() throws JFException {
        h.puts(STRATEGY_TAG + " OnStop!");
        h.sendEmail(STRATEGY_TAG + " OnStop!", "");
    }

    private void updateIsCloseHigher(Instrument instrument, long time) throws JFException {
        // before : now : after = 0 : n : n+m
        List<IBar> barList = history.getBars(instrument, Period.FIVE_MINS, OfferSide.ASK, Filter.WEEKENDS, 2, time, 0);
        if (barList.size() >= 2) {
//            puts("close1: " + barList.get(1).getClose() + ", close0: " + barList.get(0).getClose());
            if (barList.get(1).getClose() >= barList.get(0).getClose()) {
                isCloseHigher.put(instrument, true);
            } else {
                isCloseHigher.put(instrument, false);
            }
        } else {
            isCloseHigher.put(instrument, null);
        }
    }

    private boolean checkCloseHigher(Instrument instrument, IEngine.OrderCommand orderCommand) {
        Boolean higher = isCloseHigher.get(instrument);
        if (higher == null) {
            return false;
        }
        if (orderCommand.equals(IEngine.OrderCommand.BUY) && higher) {
            return true;
        } else if (orderCommand.equals(IEngine.OrderCommand.SELL) && !higher) {
            return true;
        }
        return false;
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
        private HashMap<String, Long> submitTimeMap = new HashMap<>();
        private HashMap<String, Long> filledTimeMap = new HashMap<>();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

        public Helper() {

        }


        private void puts(String str) {
            console.getInfo().println(str);
//            StrategyRunner.LOGGER.info(str);
        }

        private void sendEmail(String subject, String body) {
//            MailService.sendMail(subject, body);
        }

        private boolean isStrategyInstrument(Instrument instrument) {
            return instrumentSet.contains(instrument);
        }

        private void orderFilled(IOrder order) {
            String key = order.getInstrument() + "#" + order.getOrderCommand();
            filledTimeMap.put(key, order.getFillTime());
        }

        private long getLastFilledTime(Instrument instrument, IEngine.OrderCommand command) {
            Long time = filledTimeMap.get(instrument + "#" + command);
            if (time != null) {
                return time;
            }
            return 0L;
        }

        /**
         * allow only one order per cInstrument one minute.
         */
        private void submitOrder(String label, Instrument instrument, IEngine.OrderCommand orderCommand,
                                 double amount, double price, double slippage, long time) throws JFException {
            Long lastSubmitTime = submitTimeMap.get(instrument + "#" + orderCommand);
            if (lastSubmitTime == null) {
                lastSubmitTime = 0L;
            }
            if (time - lastSubmitTime > 60 * 1000) {
                engine.submitOrder(label, instrument, orderCommand, amount, price, slippage);
                submitTimeMap.put(instrument + "#" + orderCommand, time);
                h.puts("submit order label: " + label + ", cInstrument: " + instrument + ", command: " + orderCommand + ", amount: " + amount + ", price: " + price);
            }
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
         * Label and Group Management of the orders
         */
        private List<IOrder> getOrders() throws JFException {
            List<IOrder> filledOrders = new ArrayList<>();
            List<IOrder> allOrders = engine.getOrders();
            for (IOrder order : allOrders) {
                if (order != null) {
                    filledOrders.add(order);
                }
            }
            filledOrders.sort(new Comparator<IOrder>() {
                @Override
                public int compare(IOrder o1, IOrder o2) {
                    return o1.getCreationTime() == o2.getCreationTime() ? 0 :
                            (o1.getCreationTime() > o2.getCreationTime() ? 1 : -1);
                }

            });
            return filledOrders;
        }

        private List<IOrder> getOrdersByInstrument(Instrument instrument) throws JFException {
            List<IOrder> orders = new ArrayList<>();
            for (IOrder order : engine.getOrders()) {
                if (order != null && order.getLabel().startsWith(STRATEGY_TAG) && instrument.equals(order.getInstrument())) {
                    orders.add(order);
                }
            }
            orders.sort(new Comparator<IOrder>() {
                @Override
                public int compare(IOrder o1, IOrder o2) {
                    return o1.getCreationTime() == o2.getCreationTime() ? 0 :
                            (o1.getCreationTime() > o2.getCreationTime() ? 1 : -1);
                }

            });
            return orders;
        }

        private IOrder getLastOrderByInstrument(Instrument instrument) throws JFException {
            List<IOrder> allOrders = engine.getOrders();
            IOrder lastOrder = null;
            for (IOrder order : allOrders) {
                if (order != null && instrument.equals(order.getInstrument())) {
                    if (lastOrder == null || order.getCreationTime() > lastOrder.getCreationTime()) {
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
                if (order != null) {
                    if (lastOrder == null || order.getCreationTime() > lastOrder.getCreationTime()) {
                        lastOrder = order;
                    }
                }
            }
            return lastOrder;
        }
    }


    /**
     * Helper Methods END ==========================================================================
     */
}