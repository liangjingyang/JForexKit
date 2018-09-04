package com.jforexcn.inbox.strategy;

import com.dukascopy.api.Configurable;
import com.dukascopy.api.Filter;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IChart;
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
import com.dukascopy.api.JFUtils;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by simple(simple.continue@gmail.com) on 17/04/2018.
 */

public class ReverseDoubleStrategy implements IStrategy {

    private IEngine mEngine;
    private IConsole mConsole;
    private IIndicators mIndicators;
    private IHistory mHistory;
    private IChart mChart;
    private IAccount mAccount;
    private JFUtils mUtils;

    @Configurable("a0 Strategy Tag")
    public String cStrategyTag = "ReverseDouble";
    @Configurable("a1 Instrument")
    public Instrument cInstrument = Instrument.EURUSD;
    @Configurable("a2 Period")
    public Period cPeriod = Period.ONE_HOUR;
    @Configurable("a3 Start Amount")
    public int cStartAmount = 1000;
    @Configurable("a4 Stop Lost Pips")
    public double cStopLossPips = 40;
    @Configurable("a5 Take Profit Pips")
    public double cTakeProfitPips = 50;
    @Configurable("a6 Add Position Factor")
    public double cAddPositionFactor = 2;
    @Configurable("a7 Try Before Pause")
    public int cTryCount = 3;
    @Configurable("a8 Pause Days")
    public int cPauseDays = 2;


    @Configurable("b0 EMA Time Period")
    public int cEMAShortTimePeriod = 48;
    @Configurable("b1 EMA Time Period")
    public int cEMALongTimePeriod = 144;
    @Configurable("b1 RSI Time Period")
    public int cRSITimePeriod = 8;


    @Configurable("z0 Debug")
    public boolean cDebug = false;


    private boolean isPaused = false;
    private int pausedDays = 0;

    private Helper h = new Helper();
    private Statistics statistics;

    private IOrder lastOrder;

    @Override
    public void onStart(IContext context) throws JFException {
//TODO: check args

        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mIndicators = context.getIndicators();
        this.mHistory = context.getHistory();
        this.mChart = context.getChart(cInstrument);
        this.mAccount = context.getAccount();
        this.mUtils = context.getUtils();

        h.setDebug(cDebug);
        // subscribe instruments
        h.instrumentSet.add(cInstrument);
        Set<Instrument> instrumentSetToSubscribed = new HashSet<Instrument>();
        instrumentSetToSubscribed.add(cInstrument);
        context.setSubscribedInstruments(instrumentSetToSubscribed, true);

        statistics = new Statistics(this);

        cStartAmount = h.clearAmount(cInstrument, cStartAmount);

        h.logInfo(h.getStrategyTag() + " Start!");
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        // cancel order manual order.state: CANCEL; Here only care CLOSED
        List<IOrder> strategyOrders = getStrategyOrders();
        if (getStrategyOrders().size() == 0) {
            ITick lastTick = mHistory.getLastTick(cInstrument);
            long lastTickTime = lastTick.getTime();
            if (lastOrder == null) {
                pausedDays = cPauseDays;
                //Instrument instrument, Period period, OfferSide side, AppliedPrice appliedPrice, int timePeriod, Filter filter, int numberOfCandlesBefore, long time, int numberOfCandlesAfter
                double[] shortEMAs = mIndicators.ema(cInstrument, cPeriod, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE, cEMAShortTimePeriod, Filter.WEEKENDS, 1, lastTickTime, 0);
                double[] longEMAs = mIndicators.ema(cInstrument, cPeriod, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE, cEMALongTimePeriod, Filter.WEEKENDS, 1, lastTickTime, 0);
                String label = h.getStrategyTag() + "_1";
                double mil = h.amountToMil(cInstrument, cStartAmount);
                if (shortEMAs[0] > longEMAs[0]) {
                    h.submitOrder(label, cInstrument, IEngine.OrderCommand.BUY, mil, lastTick.getAsk(), 3, lastTickTime);
                } else {
                    h.submitOrder(label, cInstrument, IEngine.OrderCommand.SELL, mil, lastTick.getBid(), 3, lastTickTime);
                }
            } else {
                String lastLabel = lastOrder.getLabel();
                String[] splitedLabel = lastLabel.split("_");
                int tryCount = Integer.valueOf(splitedLabel[splitedLabel.length - 1]);
                IEngine.OrderCommand command;
                double mil;
                if (lastOrder.getProfitLossInPips() > 0) {
                    double[] rsis = mIndicators.rsi(cInstrument, cPeriod, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE, cRSITimePeriod, Filter.WEEKENDS, 4, lastTickTime, 0);
                    String label = h.getStrategyTag() + "_1";
                    mil = h.amountToMil(cInstrument, cStartAmount);
//                    if (Math.abs(rsis[2] - rsis[1]) > 5) {
//                        if (rsis[0] > rsis[1] && rsis[2] > rsis[1] && rsis[1] < 40) {
//                            h.logDebug("rsis: " + rsis[0] + ", " + rsis[1] + ", " + rsis[2]);
//                            h.submitOrder(label, cInstrument, IEngine.OrderCommand.BUY, mil, lastTick.getAsk(), 3, lastTickTime);
//                        } else if (rsis[0] < rsis[1] && rsis[2] < rsis[1] && rsis[1] > 60) {
//                            h.logDebug("rsis: " + rsis[0] + ", " + rsis[1] + ", " + rsis[2]);
//                            h.submitOrder(label, cInstrument, IEngine.OrderCommand.SELL, mil, lastTick.getBid(), 3, lastTickTime);
//                        }
//                    }
                    command = lastOrder.getOrderCommand();
                    h.submitOrder(label, cInstrument, command, mil, 0, 3, lastTickTime);
                    pausedDays = cPauseDays;
                } else {
                    if (tryCount == cTryCount && pausedDays != 0) {
                        isPaused = true;
                        return;
                    }
                    tryCount = tryCount + 1;
                    command = IEngine.OrderCommand.BUY;
                    if (command.equals(lastOrder.getOrderCommand())) {
                        command = IEngine.OrderCommand.SELL;
                    }
                    mil = h.amountToMil(cInstrument, cStartAmount * Math.pow(cAddPositionFactor, tryCount - 1));
                    String newLabel = splitedLabel[0] + "_" + String.valueOf(tryCount);
                    h.submitOrder(newLabel, cInstrument, command, mil, 0, 3, lastTickTime);
                }

            }
        } else {
            for (IOrder order : strategyOrders) {
                double pips = order.getProfitLossInPips();
                if (pips > cTakeProfitPips || pips < (0 - cStopLossPips)) {
                    order.close();
                }
            }
        }

    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (Period.DAILY.equals(period)) {
            if (isPaused) {
                pausedDays = pausedDays - 1;
                if (pausedDays == 0) {
                    isPaused = false;
                }
            }
        }

    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder order = message.getOrder();
        if (order != null &&
                h.isStrategyOrder(order)) {
            if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {
                h.orderFilled(order);
            } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {
                order.close();
            } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {
                lastOrder = order;
                if (order.getState().equals(IOrder.State.CLOSED)) {
                    statistics.onOrderClosed(order, mAccount.getBaseEquity());
                }
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {

    }

    @Override
    public void onStop() throws JFException {

    }

    public List<IOrder> getStrategyOrders() throws JFException {
        ArrayList<IOrder> orders = new ArrayList<IOrder>();
        for (IOrder order : mEngine.getOrders()) {
            if (h.isStrategyOrder(order)) {
                orders.add(order);
            }
        }
        return orders;
    }


    private class Helper {

        /**
         * common methods
         */
        private Set<Instrument> instrumentSet = new HashSet<Instrument>();
        private HashMap<String, Long> submitTimeMap = new HashMap<>();
        private HashMap<String, Long> filledTimeMap = new HashMap<>();

        private boolean debug = true;

        Helper() {
        }

        public String getStrategyTag() {
            return cStrategyTag +
                    cInstrument.getPrimaryJFCurrency() +
                    cInstrument.getSecondaryJFCurrency() +
                    cPeriod.getNumOfUnits() +
                    cPeriod.getUnit().getCompactDescription();
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

        private double pipToCurrency(Instrument instrument) throws JFException {
            return mUtils.convertPipToCurrency(instrument, mAccount.getAccountCurrency());
        }

        private int clearAmount(Instrument instrument, int amount) {
            switch (instrument.toString()){
                case "XAU/USD" : return amount >= 1 ? amount : 0;
                case "XAG/USD" : return amount >= 50 ? amount : 0;
                default : return amount >= 1000 ? amount : 0;
            }
        }

        private double getMinMil(Instrument instrument){
            switch (instrument.toString()){
                case "XAU/USD" : return 0.000001;
                case "XAG/USD" : return 0.00005;
                default : return 0.001;
            }
        }

        private int getMinAmount(Instrument instrument){
            switch (instrument.toString()){
                case "XAU/USD" : return 1;
                case "XAG/USD" : return 50;
                default : return 1000;
            }
        }

        private int getMilScale(Instrument instrument){
            switch (instrument.toString()){
                case "XAU/USD" : return 6;
                case "XAG/USD" : return 5;
                default : return 3;
            }
        }

        private double amountToMil(Instrument instrument, double amount) {
            BigDecimal mil = new BigDecimal(amount);
            BigDecimal divisor = new BigDecimal(1000000);
            mil = mil.divide(divisor, getMilScale(instrument), RoundingMode.DOWN);
            return mil.doubleValue();
        }

        private double milToAmount(double mil) {
            BigDecimal amount = new BigDecimal(mil);
            BigDecimal multiplicand = new BigDecimal(1000000);
            amount = amount.multiply(multiplicand);
            amount = amount.setScale(0, RoundingMode.DOWN);
            return amount.doubleValue();
        }

        private double scalePrice(double price, int pipScale) {
            // pips: 0.0001, price: 0.00001
            return scaleDouble(price, pipScale + 1, RoundingMode.HALF_UP);
        }

        private double scaleDouble(double value, int scale, RoundingMode roundingMode) {
            if (scale < 0) {
                throw new IllegalArgumentException();
            }
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(scale, roundingMode);
            return bd.doubleValue();
        }

        private void logInfo(String str) {
            mConsole.getInfo().println(str);
//            StrategyRunner.LOGGER.info(str);
        }

        private void logDebug(String str) {
            if (debug) {
                mConsole.getInfo().println("== DEBUG == " + str);
//            StrategyRunner.LOGGER.info(str);
            }
        }

        private void sendEmail(String subject, String body) {
//            MailService.sendMail(subject, body);
        }

        private boolean isStrategyInstrument(Instrument instrument) {
            return instrumentSet.contains(instrument);
        }

        private boolean isStrategyOrder(IOrder order) {
            return order.getLabel().startsWith(getStrategyTag()) || isStrategyInstrument(order.getInstrument());
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
                mEngine.submitOrder(label, instrument, orderCommand, amount, price, slippage);
                submitTimeMap.put(instrument + "#" + orderCommand, time);
            }
        }


        /**
         * To ensure only one request per order
         */
        private HashMap<String, Helper.OrderProcessing> orderProcessingHashMap = new HashMap<>();

        private void setStopLossPrice(IOrder order, double price) throws JFException {
            if (order.getStopLossPrice() == price) {
                return;
            }
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            String processingKey = getOrderProcessingKey(order.getId(), "setStopLossPrice");
            if (canOrderProcessing(processingKey, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), now);
                insertOrderProcessing(processingKey, orderProcessing);
                order.setStopLossPrice(price);
            }
        }

        private void setStopLossPrice(IOrder order, double price, OfferSide side) throws JFException {
            if (order.getStopLossPrice() == price) {
                return;
            }
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            String processingKey = getOrderProcessingKey(order.getId(), "setStopLossPrice");
            if (canOrderProcessing(processingKey, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), now);
                insertOrderProcessing(processingKey, orderProcessing);
                order.setStopLossPrice(price, side);
            }
        }

        private void setTakeProfitPrice(IOrder order, double price) throws JFException {
            if (order.getTakeProfitPrice() == price) {
                return;
            }
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            String processingKey = getOrderProcessingKey(order.getId(), "setTakeProfitPrice");
            if (canOrderProcessing(processingKey, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), now);
                insertOrderProcessing(processingKey, orderProcessing);
                order.setTakeProfitPrice(price);
            }
        }


        private void close(IOrder order, double amount, double price, double slippage) throws JFException {
            if (!(order.getState().equals(IOrder.State.FILLED) || order.getState().equals(IOrder.State.OPENED))) {
                return;
            }
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            String processingKey = getOrderProcessingKey(order.getId(), "close");
            if (canOrderProcessing(processingKey, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), now);
                insertOrderProcessing(processingKey, orderProcessing);
                order.close(amount, price, slippage);
            }
        }

        private void close(IOrder order, double amount, double price) throws JFException {
            if (!(order.getState().equals(IOrder.State.FILLED) || order.getState().equals(IOrder.State.OPENED))) {
                return;
            }
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            String processingKey = getOrderProcessingKey(order.getId(), "close");
            if (canOrderProcessing(processingKey, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), now);
                insertOrderProcessing(processingKey, orderProcessing);
                order.close(amount, price);
            }
        }

        private void close(IOrder order, double amount) throws JFException {
            if (!(order.getState().equals(IOrder.State.FILLED) || order.getState().equals(IOrder.State.OPENED))) {
                return;
            }
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            String processingKey = getOrderProcessingKey(order.getId(), "close");
            if (canOrderProcessing(processingKey, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), now);
                insertOrderProcessing(processingKey, orderProcessing);
                order.close(amount);
            }
        }

        private void close(IOrder order) throws JFException {
            if (!(order.getState().equals(IOrder.State.FILLED) || order.getState().equals(IOrder.State.OPENED))) {
                return;
            }
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            String processingKey = getOrderProcessingKey(order.getId(), "close");
            if (canOrderProcessing(processingKey, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), now);
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

        private void insertOrderProcessing(String processingKey, Helper.OrderProcessing orderProcessing) {
            orderProcessingHashMap.put(processingKey, orderProcessing);
        }

        private void removeOrderProcessing(String processingKey) {
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

    private class Statistics {
        private final double startEquity;
        private double endEquity = 0;
        private int longTradeCount = 0;
        private int shortTradeCount = 0;
        private int profitTradeCount = 0;
        private int lossTradeCount = 0;
        private double bestTradeProfit = 0;
        private double worstTradeLoss = 0;

        private int maxConsecutiveWinCount = 0;
        private double maxConsecutiveProfit = 0;
        private int maxConsecutiveLossCount = 0;
        private double maxConsecutiveLoss = 0;
        private double maxDrawdown = 0;
        private double maxDrawdownRate = 0;

        private int consecutiveWinCount = 0;
        private double consecutiveProfit = 0;
        private int consecutiveLossCount = 0;
        private double consecutiveLoss = 0;
        private double drawdown = 0;
        private double drawdownRate = 0;

        private IOrder order;
        private TreeMap<String, Object> extras = new TreeMap<>();

        Statistics(ReverseDoubleStrategy strategy) throws JFException {
            try {
                this.startEquity = strategy.mAccount.getBaseEquity();
                setEndEquity(this.startEquity);
                Field[] fields = strategy.getClass().getDeclaredFields();
                for(Field field: fields){
                    if(field.isAnnotationPresent(Configurable.class)) {
                        Object value = field.get(strategy);
                        extras.put(field.getDeclaredAnnotation(Configurable.class).value(), value);
                    }
                }
            } catch (Exception e) {
                throw new JFException(e.getMessage());
            }
        }

        void onOrderClosed(IOrder order, double currentEquity) {
            setEndEquity(currentEquity);
            setOrder(order);
            h.logDebug(getCsvValues());
        }

        public void putExtra(String key, Object value) {
            extras.put(key, value);
        }

        public void putExtras(Map<String, Object> map) {
            this.extras.putAll(map);
        }

        String getCsvHeads() {
            StringBuilder heads = new StringBuilder();
            heads.append("StartEquity");
            heads.append(",");
            heads.append("EndEquity");
            heads.append(",");
            heads.append("MaxDrawdown");
            heads.append(",");
            heads.append("maxDrawdownRate");
            heads.append(",");
            heads.append("LongTradeCount");
            heads.append(",");
            heads.append("ShortTradeCount");
            heads.append(",");
            heads.append("ProfitTradeCount");
            heads.append(",");
            heads.append("LossTradeCount");
            heads.append(",");
            heads.append("BestTradeProfit");
            heads.append(",");
            heads.append("WorstTradeLoss");
            heads.append(",");
            heads.append("MaxConsecutiveWinCount");
            heads.append(",");
            heads.append("MaxConsecutiveProfit");
            heads.append(",");
            heads.append("MaxConsecutiveLossCount");
            heads.append(",");
            heads.append("MaxConsecutiveLoss");
            for (Map.Entry<String, Object> entry : extras.entrySet()) {
                heads.append(",");
                heads.append(entry.getKey());
            }
            return heads.toString();
        }

        String getCsvValues() {
            StringBuilder values = new StringBuilder();
            values.append(startEquity);
            values.append(",");
            values.append(endEquity);
            values.append(",");
            values.append(maxDrawdown);
            values.append(",");
            values.append(maxDrawdownRate);
            values.append(",");
            values.append(longTradeCount);
            values.append(",");
            values.append(shortTradeCount);
            values.append(",");
            values.append(profitTradeCount);
            values.append(",");
            values.append(lossTradeCount);
            values.append(",");
            values.append(bestTradeProfit);
            values.append(",");
            values.append(worstTradeLoss);
            values.append(",");
            values.append(maxConsecutiveWinCount);
            values.append(",");
            values.append(maxConsecutiveProfit);
            values.append(",");
            values.append(maxConsecutiveLossCount);
            values.append(",");
            values.append(maxConsecutiveLoss);
            for (Map.Entry<String, Object> entry : extras.entrySet()) {
                values.append(",");
                values.append(entry.getValue());
            }
            return values.toString();
        }


        void setEndEquity(double endEquity) {
            this.endEquity = endEquity;
            drawdown = startEquity - endEquity;
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
            drawdownRate = drawdown / startEquity;
            if (drawdownRate > maxDrawdownRate) {
                maxDrawdownRate = drawdownRate;
            }
        }

        private void setOrder(IOrder order) {
            IOrder lastOrder = this.order;
            this.order = order;
            if (order.isLong()) {
                longTradeCount = longTradeCount + 1;
            } else {
                shortTradeCount = shortTradeCount + 1;
            }
            double profitLossUSD = order.getProfitLossInUSD();
            if (profitLossUSD > 0) {
                profitTradeCount = profitTradeCount + 1;
                if (lastOrder != null && lastOrder.getProfitLossInUSD() > 0) {
                    consecutiveWinCount = consecutiveWinCount + 1;
                    consecutiveProfit = consecutiveProfit + profitLossUSD;
                    if (consecutiveWinCount > maxConsecutiveWinCount) {
                        maxConsecutiveWinCount = consecutiveWinCount;
                    }
                    if (consecutiveProfit > maxConsecutiveProfit) {
                        maxConsecutiveProfit = consecutiveProfit;
                    }
                } else {
                    consecutiveWinCount = 1;
                    consecutiveProfit = profitLossUSD;
                }

                if (profitLossUSD > bestTradeProfit) {
                    bestTradeProfit = profitLossUSD;
                }
            } else if (profitLossUSD < 0) {
                lossTradeCount = lossTradeCount + 1;
                if (lastOrder != null && lastOrder.getProfitLossInUSD() < 0) {
                    consecutiveLossCount = consecutiveLossCount + 1;
                    consecutiveLoss = consecutiveLoss + profitLossUSD;
                    if (consecutiveLossCount > maxConsecutiveLossCount) {
                        maxConsecutiveLossCount = consecutiveLossCount;
                    }
                    if (consecutiveLoss < maxConsecutiveLoss) {
                        maxConsecutiveLoss = consecutiveLoss;
                    }
                } else {
                    consecutiveLossCount = 1;
                    consecutiveLoss = profitLossUSD;
                }

                if (profitLossUSD < worstTradeLoss) {
                    worstTradeLoss = profitLossUSD;
                }
            }
        }
    }
}
