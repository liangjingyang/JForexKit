package com.jforexcn.inbox.strategy;

/**
 * Created by simple on 19/06/18.
**/

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class DailyVolatilityStrategy implements IStrategy {

    @Configurable("Instrument")
    public Instrument cInstrument = Instrument.EURUSD;
    @Configurable("Period")
    public Period cPeriod = Period.ONE_MIN;
    @Configurable("StopLossPips")
    public int cStopLossPips = 30;
    @Configurable("TakeProfitInPips")
    public int cTakeProfitInPips = 5;
    @Configurable("Amount in Million")
    public double cAmount = 0.001;
    @Configurable("Begin Hour")
    public int cTradeBeginHour = 6;
    @Configurable("End Hour")
    public int cTradeEndHour = 19;

    private IEngine engine;
    private IConsole console;
    private IHistory history;
    private IAccount account;
    private IIndicators indicators;
    private IDataService dataService;

    private HashSet<Instrument> subscribedInstruments = new HashSet<>();
    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GTM"));

    private static String STRATEGY_NAME = "DailyVolatilityStrategy";
    private int dailyOrderCount = 1;
    private int lockLevel = 4;

    private double totalProfitLossInPips = 0;
    private boolean isClosing;


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
                if (getOrders().size() == 0) {
                    isClosing = false;
                    lockLevel = 4;
                    totalProfitLossInPips = 0;
                    dailyOrderCount = 1;
                }
            }
        }
    }

    @Override
    public void onStop() throws JFException {
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        if (!isClosing && !isOffline(tick.getTime())) {
            int minute = getMinute(tick.getTime());
            int hour = getHour(tick.getTime());
            long days = getDays(tick.getTime());

            int orderCount = getOrders().size();
            if (getTotalProfitLossInPips() + totalProfitLossInPips > cTakeProfitInPips * cAmount) {
                isClosing = true;
                closeAllOrder();
            }
            IOrder lastOrder = getLastOrder();
            if (lastOrder != null) {
                if (lastOrder.getOrderCommand().equals(IEngine.OrderCommand.BUY) &&
                        lastOrder.getProfitLossInPips() <= -cStopLossPips) {
                    dailyOrderCount = dailyOrderCount * 2;
                    if (dailyOrderCount > 1024) {
                        dailyOrderCount = 1024;
                    }
                    double amount = cAmount * dailyOrderCount;
                    String label = getLabel();
                    if (orderCount == lockLevel) {
                        amount = getAmountDiff();
                        label = getLockLabel();
                    }
                    engine.submitOrder(label, cInstrument, IEngine.OrderCommand.SELL, amount, tick.getBid(), 3, 0, 0);
                } else if (lastOrder.getOrderCommand().equals(IEngine.OrderCommand.SELL) &&
                        lastOrder.getProfitLossInPips() <= -cStopLossPips) {
                    dailyOrderCount = dailyOrderCount * 2;
                    if (dailyOrderCount > 1024) {
                        dailyOrderCount = 1024;
                    }
                    double amount = cAmount * dailyOrderCount;
                    puts("111 dailyOrderCount: " + dailyOrderCount + ", amount" + amount);
                    String label = getLabel();
                    if (orderCount == lockLevel) {
                        amount = getAmountDiff();
                        label = getLockLabel();
                    }
                    puts("222 amount: " + amount);
                    engine.submitOrder(label, cInstrument, IEngine.OrderCommand.BUY, amount, tick.getAsk(), 3, 0, 0);
                }

            }


        }
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (isStrategyInstrument(instrument)) {
            if (!isClosing && !isOffline(askBar.getTime()) && Period.ONE_HOUR.equals(period)) {
                int hour = getHour(askBar.getTime());
                if (checkTradeHour(hour)) {
                    List<IOrder> orders = getOrders();
                    long time = history.getBarStart(Period.DAILY, askBar.getTime());
                    List<IBar> askBars = history.getBars(instrument, Period.DAILY, OfferSide.ASK, Filter.ALL_FLATS, 2, time, 0);
                    if (orders.size() == 0) {
                        double body = askBars.get(0).getClose() - askBars.get(0).getOpen();
                        double range = askBars.get(0).getHigh() - askBars.get(0).getLow();
                        if (Math.abs(body) >= 20 * instrument.getPipValue() && range > 50 * instrument.getPipValue()) {
                            if (body > 0) {
                                engine.submitOrder(getLabel(), instrument, IEngine.OrderCommand.BUY, cAmount, askBar.getClose(), 3, 0, 0);
                            } else {
                                engine.submitOrder(getLabel(), instrument, IEngine.OrderCommand.SELL, cAmount, bidBar.getClose(), 3, 0, 0);
                            }
                        }
                    } else {
                        IOrder lockOrder = getLockOrder();
                        if (lockOrder != null && Math.abs(lockOrder.getProfitLossInPips()) > 50) {
                            lockLevel = lockLevel + 2;
                            totalProfitLossInPips = totalProfitLossInPips + lockOrder.getProfitLossInPips() * lockOrder.getAmount();
                            double amount = cAmount * dailyOrderCount;
                            engine.submitOrder(getLabel(), instrument, lockOrder.getOrderCommand(), amount, askBar.getClose(), 3, 0, 0);
                            lockOrder.close();
                        }
                    }
                }
            }
        }
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
        return Math.abs(buyAmount - sellAmount);
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