package com.jforexcn.inbox.strategy;

import com.dukascopy.api.Configurable;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by simple(simple.continue@gmail.com) on 24/11/2017.
 */

public class CoinProbability implements IStrategy {

    @Configurable("Instrument")
    public Instrument cInstrument = Instrument.EURUSD;
    @Configurable(value="Offer Side value")
    public OfferSide cOfferSide = OfferSide.ASK;
    @Configurable("Period")
    public Period cPeriod = Period.ONE_HOUR;
    @Configurable("Amount in million")
    public double cMil = 0.1;
    @Configurable("Slippage")
    public int cSlippage = 3;
    @Configurable("Stop loss pips")
    public int cSlPips = 20;
    @Configurable("Take profit pips")
    public int cTpPips = 20;
    @Configurable("Command by the last Nth bar ( 1-20 )")
    public int cLastNBar = 1;

    private IEngine engine;
    private IConsole console;
    private IIndicators indicators;
    private IHistory history;
    private IChart chart;
    private IAccount account;
    private JFUtils utils;

    private IBar previousBar;
    private Statistics statistics;

    private String strategyTag = "CoinProbability";
    private long lastFillTime = 0;


    @Override
    public void onStart(IContext context) throws JFException {

        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.indicators = context.getIndicators();
        this.history = context.getHistory();
        this.chart = context.getChart(cInstrument);
        this.account = context.getAccount();
        this.utils = context.getUtils();

        console.getOut().println(strategyTag + " stop!");

        // subscribe instruments
        Set<Instrument> instrumentSet = new HashSet<Instrument>();
        instrumentSet.add(cInstrument);
        context.setSubscribedInstruments(instrumentSet, true);

        statistics = new Statistics(this);

        submitOrder();
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {

    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (period.equals(cPeriod)) {
            submitOrder();
        }
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder order = message.getOrder();
        if (order != null) {
            if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {
                submitOrder();
            } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {
                submitOrder();
            } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {
                lastFillTime = order.getFillTime();
            } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {
                order.close();
            } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {
                statistics.onOrderClosed(order);
                submitOrder();
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {

    }

    @Override
    public void onStop() throws JFException {
        console.getOut().println(statistics.getCsvValues());
        console.getOut().println(statistics.getCsvHeads());
        console.getOut().println(statistics.getString());
        console.getOut().println(strategyTag + " stop!");
    }

    private IEngine.OrderCommand getCommand() throws JFException {
        previousBar = history.getBar(cInstrument, cPeriod, cOfferSide, cLastNBar);
//        int diff = (int) (Math.abs(previousBar.getOpen() - previousBar.getClose()) * Math.pow(10, cInstrument.getPipScale() + 1) + 0.5);
//        return (diff & 1) != 0 ? IEngine.OrderCommand.SELL : IEngine.OrderCommand.BUY;
        return previousBar.getOpen() > previousBar.getClose() ? IEngine.OrderCommand.SELL : IEngine.OrderCommand.BUY;
    }

    private List<IOrder> getStrategyOrders() throws JFException {
        List<IOrder> orders = new ArrayList<>();
        for (IOrder order : engine.getOrders()) {
            if (order.getLabel().startsWith(strategyTag)) {
                orders.add(order);
            }
        }
        return orders;
    }

    private void submitOrder() throws JFException {
        console.getOut().println("getStrategyOrders().size(): " + getStrategyOrders().size() + ", lastFillTime: " + lastFillTime + "history.getLastTick(cInstrument).getTime(): " +
                history.getLastTick(cInstrument).getTime() +  ", Period.getInterval(): " + cPeriod.getInterval());
        if (getStrategyOrders().size() == 0) {
            if (lastFillTime == 0 || history.getLastTick(cInstrument).getTime() - lastFillTime >= cPeriod.getInterval()) {
                ITick lastTick = history.getLastTick(cInstrument);
                IEngine.OrderCommand command = getCommand();
                double price;
                double sl;
                double tp;
                if (command.isLong()) {
                    price = lastTick.getAsk();
                    sl = price - cInstrument.getPipValue() * cSlPips;
                    tp = price + cInstrument.getPipValue() * cTpPips;
                } else {
                    price = lastTick.getBid();
                    sl = price + cInstrument.getPipValue() * cSlPips;
                    tp = price - cInstrument.getPipValue() * cTpPips;
                }

                engine.submitOrder(strategyTag + "_" + previousBar.getTime(), cInstrument, getCommand(), cMil, price, cSlippage, sl, tp);
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

        Statistics(CoinProbability strategy) throws JFException {
            try {
                this.startEquity = account.getBaseEquity();
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

        void onOrderClosed(IOrder order) {
            setEndEquity(account.getBaseEquity());
            setOrder(order);
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

        String getString() {
            StringBuilder sb = new StringBuilder();
            sb.append("StartEquity:").append(startEquity);
            sb.append(", ");
            sb.append("EndEquity:").append(endEquity);
            sb.append(", ");
            sb.append("MaxDrawdown:").append(maxDrawdown);
            sb.append(", ");
            sb.append("maxDrawdownRate:").append(maxDrawdownRate);
            sb.append(", ");
            sb.append("LongTradeCount:").append(longTradeCount);
            sb.append(", ");
            sb.append("ShortTradeCount:").append(shortTradeCount);
            sb.append(", ");
            sb.append("ProfitTradeCount:").append(profitTradeCount);
            sb.append(", ");
            sb.append("LossTradeCount:").append(lossTradeCount);
            sb.append(", ");
            sb.append("BestTradeProfit:").append(bestTradeProfit);
            sb.append(", ");
            sb.append("WorstTradeLoss:").append(worstTradeLoss);
            sb.append(", ");
            sb.append("MaxConsecutiveWinCount:").append(maxConsecutiveWinCount);
            sb.append(",");
            sb.append("MaxConsecutiveProfit:").append(maxConsecutiveProfit);
            sb.append(",");
            sb.append("MaxConsecutiveLossCount:").append(maxConsecutiveLossCount);
            sb.append(",");
            sb.append("MaxConsecutiveLoss:").append(maxConsecutiveLoss);
            for (Map.Entry<String, Object> entry : extras.entrySet()) {
                sb.append(", ");
                sb.append(entry.getKey());
                sb.append(":");
                sb.append(entry.getValue());
            }
            return sb.toString();
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
