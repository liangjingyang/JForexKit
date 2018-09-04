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
import com.dukascopy.api.Period;

import com.jforexcn.shared.lib.MailService;
import com.jforexcn.shared.client.StrategyRunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;


public class TwoSideHourStrategy implements IStrategy {

    private IEngine mEngine;
    private IConsole mConsole;
    private IIndicators mIndicators;
    private IHistory mHistory;

    private Set<Instrument> mInstrumentSet = new HashSet<Instrument>();
    private HashSet<IEngine.OrderCommand> processingHashSet = new HashSet<>();
    private HashSet<IEngine.OrderCommand> todayFilledSet = new HashSet<>();


    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    private static String STRATEGY_TAG = "Strategy_TwoSideHour";

    private static int START_HOUR = 13;
    private static int HOLD_HOUR = 3;
    private static int END_HOUR = START_HOUR + HOLD_HOUR >= 24 ? START_HOUR + HOLD_HOUR - 24 : START_HOUR + HOLD_HOUR;
    private static int CLEAR_HOUR = START_HOUR - 2 < 0 ? START_HOUR - 2 + 24 : START_HOUR - 2;
    private static int STOP_LOSS_PIPS = 20;
    private static int TAKE_PROFIT_PIPS = 70;

    @Override
    public void onStart(IContext context) throws JFException {
        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mIndicators = context.getIndicators();
        this.mHistory = context.getHistory();

        // subscribe instruments
        mInstrumentSet.add(Instrument.EURUSD);
        context.setSubscribedInstruments(mInstrumentSet, true);

        // date format
        mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));

    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        try {
            IOrder order = message.getOrder();
            if (order != null && isStrategyInstrument(order.getInstrument())) {
                if (IOrder.State.CANCELED.equals(order.getState())) {
                    removeProcessing(order.getOrderCommand());
                } else if (IOrder.State.FILLED.equals(order.getState())) {
                    if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {
                        addTodayFilled(order.getOrderCommand());
                    }
                    removeProcessing(order.getOrderCommand());
                } else if (IOrder.State.CLOSED.equals(order.getState())) {
                    setStopLoss(order.getInstrument());
                    removeProcessing(order.getOrderCommand());
                } else {
                    puts("onMessage other type: " + message.getType().toString() +
                            ", content: " + message.getContent());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendEmail("CRASH! " + getClass().getSimpleName(), "OnMessage exception: " + e.getMessage());
        }
    }

    @Override
    public void onStop() throws JFException {
        puts(STRATEGY_TAG + "OnStop!");
        sendEmail(STRATEGY_TAG + "OnStop!", "");
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {

    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (Period.ONE_MIN.equals(period)) {
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone("GTM"));
            today.setTimeInMillis(askBar.getTime());
            int hour = today.get(Calendar.HOUR_OF_DAY);
            if (hour == START_HOUR) {
                List<IEngine.OrderCommand> commands = getOrderCommands(instrument);
                for (IEngine.OrderCommand command : commands) {
                    if (IEngine.OrderCommand.SELL.equals(command) && !isProcessing(command) && !isTodayFilled(command)) {
                        mEngine.submitOrder(getOrderLabel(instrument, command, bidBar.getTime()),
                                instrument, IEngine.OrderCommand.SELL, 0.03, bidBar.getClose(), 1,
                                bidBar.getClose() + STOP_LOSS_PIPS * instrument.getPipValue(),
                                bidBar.getClose() - TAKE_PROFIT_PIPS * instrument.getPipValue());
                        addProcessing(command);
                    } else if (IEngine.OrderCommand.BUY.equals(command) && !isProcessing(command) && !isTodayFilled(command)) {
                        mEngine.submitOrder(getOrderLabel(instrument, command, askBar.getTime()),
                                instrument, IEngine.OrderCommand.BUY, 0.03, askBar.getClose(), 1,
                                askBar.getClose() - STOP_LOSS_PIPS * instrument.getPipValue(),
                                askBar.getClose() + TAKE_PROFIT_PIPS * instrument.getPipValue());
                        addProcessing(command);
                    }
                }
            } else if (hour == END_HOUR) {
                List<IOrder> orders = getStrategyOrders(instrument);
                for (IOrder order : orders) {
                    order.close();
                }
            }
        } else if (Period.ONE_HOUR.equals(period)) {
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone("GTM"));
            today.setTimeInMillis(askBar.getTime());
            int hour = today.get(Calendar.HOUR_OF_DAY);
            if (hour == CLEAR_HOUR) {
                clearTodayFilled();
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private boolean isStrategyInstrument(Instrument instrument) {
        return mInstrumentSet.contains(instrument);
    }

    private boolean isProcessing(IEngine.OrderCommand orderCommand) {
        return processingHashSet.contains(orderCommand);
    }

    private void addProcessing(IEngine.OrderCommand orderCommand) {
        if (!isProcessing(orderCommand)) {
            processingHashSet.add(orderCommand);
        }
    }

    private void removeProcessing(IEngine.OrderCommand orderCommand) {
        if (isProcessing(orderCommand)) {
            processingHashSet.remove(orderCommand);
        }
    }

    private boolean isTodayFilled(IEngine.OrderCommand orderCommand) {
        return todayFilledSet.contains(orderCommand);
    }

    private void addTodayFilled(IEngine.OrderCommand orderCommand) {
        if (!isTodayFilled(orderCommand)) {
            todayFilledSet.add(orderCommand);
        }
    }

    private void removeTodayFilled(IEngine.OrderCommand orderCommand) {
        if (isTodayFilled(orderCommand)) {
            todayFilledSet.remove(orderCommand);
        }
    }

    private void clearTodayFilled() {
        todayFilledSet.clear();
    }

    private String getOrderLabel(Instrument instrument, IEngine.OrderCommand orderCommand, long time) {
        return STRATEGY_TAG + "_" + instrument.toString().replace('/', '_') + "_" + orderCommand +
                "_" + mSimpleDateFormat.format(new Date(time)) + "_" + time;
    }

    private void puts(String str) {
//        mConsole.getInfo().println(str);
        StrategyRunner.LOGGER.info(str);
    }

    private void sendEmail(String subject, String body) {
        MailService.sendMail(subject, body);
    }

    private List<IOrder> getStrategyOrders(Instrument instrument) throws JFException {
        ArrayList<IOrder> orders = new ArrayList<>();
        for (IOrder order : mEngine.getOrders()) {
            if (order != null &&
                    mInstrumentSet.contains(instrument) &&
                    instrument.equals(order.getInstrument()) &&
                    isStrategyOrder(order))
            {
                orders.add(order);
            }
        }
        return orders;
    }

    private boolean isStrategyOrder(IOrder order) {
        return order.getLabel().startsWith(STRATEGY_TAG);
    }

    private List<IEngine.OrderCommand> getOrderCommands(Instrument instrument) throws JFException {
        ArrayList<IEngine.OrderCommand> orderCommandArrayList = new ArrayList<>();
        List<IOrder> orders = getStrategyOrders(instrument);
        if (orders.size() == 0) {
            orderCommandArrayList.add(IEngine.OrderCommand.BUY);
            orderCommandArrayList.add(IEngine.OrderCommand.SELL);
        } else if (orders.size() == 1) {
            IOrder order = orders.get(0);
            if (IEngine.OrderCommand.BUY.equals(order.getOrderCommand())) {
                orderCommandArrayList.add(IEngine.OrderCommand.SELL);
            } else if (IEngine.OrderCommand.SELL.equals(order.getOrderCommand())) {
                orderCommandArrayList.add(IEngine.OrderCommand.BUY);
            }
        }
        return orderCommandArrayList;
    }

    private void setStopLoss(Instrument instrument) throws JFException {
        List<IOrder> orders = getStrategyOrders(instrument);
        for (IOrder order : orders) {
            if (IEngine.OrderCommand.BUY.equals(order.getOrderCommand())) {
                if (order.getOpenPrice() > order.getStopLossPrice()) {
                    order.setStopLossPrice(order.getOpenPrice() + STOP_LOSS_PIPS / 2 * instrument.getPipValue());
                }
            } else if (IEngine.OrderCommand.SELL.equals(order.getOrderCommand())) {
                if (order.getOpenPrice() < order.getStopLossPrice()) {
                    order.setStopLossPrice(order.getOpenPrice() - STOP_LOSS_PIPS / 2 * instrument.getPipValue());
                }
            }
         }
    }

}