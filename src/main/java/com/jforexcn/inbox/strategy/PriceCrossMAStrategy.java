package com.jforexcn.inbox.strategy;

import com.dukascopy.api.Configurable;
import com.dukascopy.api.Filter;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IDataService;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.ITimeDomain;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by simple(simple.continue@gmail.com) on 2018/6/25.
 */
public class PriceCrossMAStrategy implements IStrategy {
    @Configurable("Instrument")
    public Instrument cInstrument = Instrument.EURUSD;
    @Configurable("Period")
    public Period cPeriod = Period.ONE_HOUR;
    @Configurable("Amount in Million")
    public double cAmount = 0.02;
    @Configurable("Begin Hour")
    public int cTradeBeginHour = 8;
    @Configurable("End Hour")
    public int cTradeEndHour = 14;
    @Configurable("Should lock")
    public boolean cShoudLock = false;

    private IEngine engine;
    private IConsole console;
    private IHistory history;
    private IAccount account;
    private IIndicators indicators;
    private IDataService dataService;

    private HashSet<Instrument> subscribedInstruments = new HashSet<>();
    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GTM"));

    private static String STRATEGY_NAME = "PriceCrossMAStrategy";

    @Override
    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.history = context.getHistory();
        this.account = context.getAccount();
        this.indicators = context.getIndicators();
        this.dataService = context.getDataService();

        subscribedInstruments.add(cInstrument);
        context.setSubscribedInstruments(subscribedInstruments, true);
//        indicators.registerCustomIndicator(HoursVolatilityIndicator.class);
        indicators.registerCustomIndicator(new File("/Users/simple/Library/Application Support/JForex/Indicators/HoursVolatilityIndicator.jfx"));
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }


    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder order = message.getOrder();
        if (order == null) {
            // 非订单相关message, 以及不是本策略相关订单的信息, 忽略
            return;
        }
        if (isStrategyOrder(order)) {
            if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {
                if (order.getState().equals(IOrder.State.FILLED)) {
                    order.close();
                }
            } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {
            }
        }
    }

    @Override
    public void onStop() throws JFException {
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        if (isStrategyInstrument(instrument)) {
            long lastTickTime = history.getTimeOfLastTick(instrument);
            if (isOffline(lastTickTime)) {
                return;
            }
                int hour = getHour(tick.getTime());
                int minute = getMinute(tick.getTime());

                if (hour == cTradeBeginHour && minute < 1) {
                    if (getOrders().size() <= 0) {
                        IEngine.OrderCommand command = getOrderCommand(instrument, OfferSide.ASK);
                        if (command != null) {
                            if (IEngine.OrderCommand.BUY.equals(command)) {
                                String label = getLabel();
                                engine.submitOrder(label, instrument, IEngine.OrderCommand.BUY, cAmount, 0, 3, 0, 0);

                            } else if (IEngine.OrderCommand.SELL.equals(command)) {
                                String label = getLabel();
                                engine.submitOrder(label, instrument, IEngine.OrderCommand.SELL, cAmount, 0, 3, 0, 0);
                            }
                        }
                    }
                } else if (hour == cTradeEndHour && minute > 58) {
                    closeAllOrder();
                }
        }
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {

    }

    private IEngine.OrderCommand getOrderCommand(Instrument instrument, OfferSide offerSide) throws JFException {
        OfferSide[] offerSides = new OfferSide[] {
                offerSide
        };

        IIndicators.AppliedPrice[] appliedPrices = new IIndicators.AppliedPrice[] {
                IIndicators.AppliedPrice.CLOSE
        };

        Object[] optParams = new Object[] {
                336,
                1,
                cTradeBeginHour,
                cTradeEndHour,
                12,
                12,
                false,
                true
        };

        long time = history.getStartTimeOfCurrentBar(instrument, Period.ONE_HOUR);
        try {

            Object[] result = indicators.calculateIndicator(instrument, Period.ONE_HOUR, offerSides, "HOURSVOLATILITY", null,
                    optParams, Filter.ALL_FLATS, 2, time, 0);

            double[] volatilityList = (double[]) result[0];
            double[] maList = (double[]) result[1];

            puts("volatility: " + volatilityList[0] + ", ma: " + maList[0]);

            if (volatilityList[0] > maList[0] && getOrdersByCommand(IEngine.OrderCommand.BUY).size() == 0) {
                return IEngine.OrderCommand.BUY;
            } else if (volatilityList[0] < maList[0] && getOrdersByCommand(IEngine.OrderCommand.SELL).size() == 0) {
                return IEngine.OrderCommand.SELL;
            }
        } catch (Exception e) {
            e.printStackTrace(console.getInfo());
        }
        return null;
    }

    private List<IOrder> getOrders() throws JFException {
        List<IOrder> orders = new ArrayList<>();
        for (IOrder order : engine.getOrders()) {
            if (isStrategyOrder(order)) {
                orders.add(order);
            }
        }
        return orders;
    }

    private List<IOrder> getOrdersByCommand(IEngine.OrderCommand command) throws JFException {
        List<IOrder> orders = new ArrayList<>();
        for (IOrder order : engine.getOrders()) {
            if (isStrategyOrder(order) && command.equals(order.getOrderCommand())) {
                orders.add(order);
            }
        }
        return orders;
    }

    private void closeAllOrder() throws JFException {
        for (IOrder order : engine.getOrders()) {
            if (isStrategyOrder(order) && order.getState().equals(IOrder.State.FILLED)) {
                order.close();
            }
        }
    }

    private void closeAllSellOrder() throws JFException {
        for (IOrder order : engine.getOrders()) {
            if (isStrategyOrder(order) && order.getOrderCommand().equals(IEngine.OrderCommand.SELL)) {
                order.close();
            }
        }
    }

    private void closeAllBuyOrder() throws JFException {
        for (IOrder order : engine.getOrders()) {
            if (isStrategyOrder(order) && order.getOrderCommand().equals(IEngine.OrderCommand.BUY)) {
                order.close();
            }
        }
    }

    private IOrder getLastOrder() throws JFException {
        IOrder lastOrder = null;
        for (IOrder order : engine.getOrders()) {
            if (lastOrder == null || lastOrder.getCreationTime() < order.getCreationTime()) {
                lastOrder = order;
            }
        }
        if (lastOrder != null) {
            if (isLockOrder(lastOrder)) {
                lastOrder = null;
            }
        }
        return lastOrder;
    }

    private IOrder getLockOrder() throws JFException {
        for (IOrder order : engine.getOrders()) {
            if (isLockOrder(order)) {
                return order;
            }
        }
        return null;
    }

    private double getAmountDiff() throws JFException {
        double buyAmount = 0;
        double sellAmount = 0;
        for (IOrder order : engine.getOrders()) {
            if (isStrategyOrder(order)) {
                if (order.getOrderCommand().equals(IEngine.OrderCommand.BUY)) {
                    buyAmount = buyAmount + order.getAmount();
                } else if (order.getOrderCommand().equals(IEngine.OrderCommand.SELL)) {
                    sellAmount = sellAmount + order.getAmount();
                }
            }
        }
        return buyAmount - sellAmount;
    }

    private double getTotalProfitLossInPips() throws JFException {
        double pips = 0;
        for (IOrder order : engine.getOrders()) {
            pips = pips + order.getProfitLossInPips() * order.getAmount();
        }
        return pips;
    }

    private boolean isStrategyInstrument(Instrument instrument) {
        return subscribedInstruments.contains(instrument);
    }


    private boolean isStrategyOrder(IOrder order) {
        return order.getLabel().startsWith(STRATEGY_NAME + "_");
    }

    private String getLabel() {
        return STRATEGY_NAME + "_" + System.currentTimeMillis();
    }

    private String getLockLabel() {
        return STRATEGY_NAME + "_Lock_" + System.currentTimeMillis();
    }

    private boolean isLockOrder(IOrder order) {
        return order.getLabel().startsWith(STRATEGY_NAME + "_Lock_");
    }

    private void puts(String str) {
        console.getInfo().println(STRATEGY_NAME + " === " + str);
    }

    private int getMinute(long time) {
        calendar.setTimeInMillis(time);
        return calendar.get(Calendar.MINUTE);
    }

    private int getHour(long time) {
        calendar.setTimeInMillis(time);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    private long getDays(long time) {
        return time / 86400000L;
    }

    public boolean checkTradeHour(int hour) {
        if (cTradeBeginHour > cTradeEndHour) {
            if ((hour >= cTradeBeginHour && hour <= 23) ||
                    (hour <= cTradeEndHour && hour >= 0)) {
                return true;
            }
        } else {
            if (hour >= cTradeBeginHour && hour <= cTradeEndHour) {
                return true;
            }
        }
        return false;
    }

    private boolean isOffline(long time) throws JFException{
        Set<ITimeDomain> offlines = dataService.getOfflineTimeDomains(time - Period.WEEKLY.getInterval(), time + Period.WEEKLY.getInterval());
        for(ITimeDomain offline : offlines){
            if( time > offline.getStart() &&  time < offline.getEnd()){
                return true;
            }
        }
        return false;
    }


}
