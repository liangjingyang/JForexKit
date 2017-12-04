package com.jforexcn.shared.strategy;

/**
 * Created by simple on 19/10/16.
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
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class OrderWatcher implements IStrategy {


    private IEngine mEngine;
    private IConsole mConsole;
    private IIndicators mIndicators;
    private IHistory mHistory;

    private Set<Instrument> mInstrumentSet = new HashSet<Instrument>();

    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    private static String STRATEGY_TAG = "OrderWatcher";

    @Override
    public void onStart(IContext context) throws JFException {
        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mIndicators = context.getIndicators();
        this.mHistory = context.getHistory();

        // subscribe instruments
        mInstrumentSet.add(Instrument.EURUSD);
        mInstrumentSet.add(Instrument.USDJPY);
        mInstrumentSet.add(Instrument.XAUUSD);
        mInstrumentSet.add(Instrument.GBPUSD);
        mInstrumentSet.add(Instrument.AUDUSD);
        mInstrumentSet.add(Instrument.USDCAD);
        mInstrumentSet.add(Instrument.USDCHF);
        mInstrumentSet.add(Instrument.NZDUSD);
        mInstrumentSet.add(Instrument.GBPAUD);
        mInstrumentSet.add(Instrument.GBPJPY);
        context.setSubscribedInstruments(mInstrumentSet, true);

        // date format
        mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        try {
            IOrder order = message.getOrder();
            IMessage.Type type = message.getType();
            if (order != null) {
                if (type.equals(IMessage.Type.ORDER_CLOSE_OK)) {
                    handleMessage(message, order);
                } else if (type.equals(IMessage.Type.ORDER_FILL_OK)) {
                    handleMessage(message, order);
                } else if (type.equals(IMessage.Type.ORDER_CHANGED_OK)) {
                    handleMessage(message, order);
                } else {
                    // all rejected status should handle on their strategy
                    puts("onMessage other type: " + message.getType().toString() +
                            ", content: " + message.getContent());
                }
            }
        } catch (JFException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            sendEmail("CRASH! " + STRATEGY_TAG, "OnMessage exception: " + e.getMessage());
        }
    }
    @Override
    public void onStop() throws JFException {
        puts(STRATEGY_TAG + " OnStop!");
        sendEmail(STRATEGY_TAG + " OnStop!", "");
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

    private void puts(String str) {
//        mConsole.getInfo().println(str);
        StrategyRunner.LOGGER.info(str);
    }

    private void sendEmail(String subject, String body) {
        MailService.sendMail(subject, body);
    }

    private void handleMessage(IMessage message, IOrder order) throws JFException {
        sendEmail(getOrderSubject(message, order), getOrdersSummary());
    }

    private String getOrderSubject(IMessage message, IOrder order) {
        StringBuilder subject = new StringBuilder();
        subject.append(formatMessageType(message));
        subject.append("   ");
        subject.append(getOrderSummary(order));
        return subject.toString();
    }

    private String getOrderSummary(IOrder order) {
        StringBuilder summary = new StringBuilder();
        summary.append(order.getInstrument() + ":");
        summary.append(order.getAmount() + "   ");
        summary.append("Cost" + ":");
        summary.append(order.getOpenPrice() + "   ");
        summary.append("P/L:");
        summary.append(order.getProfitLossInPips());
        return summary.toString();
    }

    private String getOrderFullSummary(IOrder order) {
        StringBuilder summary = new StringBuilder();
        summary.append("\n");
        summary.append(order.getInstrument() + ":");
        summary.append("  State: " + order.getState() + "\n");
        summary.append("  Amount: " + order.getAmount() + "\n");
        summary.append("  Cost: " + order.getOpenPrice() + "\n");
        summary.append("  P/L: " + order.getProfitLossInPips() + "\n");
        summary.append("  StopLost: " + order.getStopLossPrice() + "\n");
        summary.append("  TakeProfit: " + order.getTakeProfitPrice() + "\n");
        return summary.toString();
    }

    private String getOrdersSummary() throws JFException {
        StringBuilder summary = new StringBuilder();
        for (IOrder o : mEngine.getOrders()) {
            summary.append(getOrderFullSummary(o));
        }
        return summary.toString();
    }

    private String formatMessageType(IMessage message) {
        if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {
            return "%Changed%";
        } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {
            return "+Filled+";
        } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {
            return "-Closed-";
        } else {
            return message.getType().toString();
        }
    }

}