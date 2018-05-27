package com.jforexcn.hub.strategy;

/**
 * Created by simple on 19/10/16.
 */

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NotificationCenter extends SubStrategy {
    private static String STRATEGY_TAG = "NotificationCenter";
    private Map<Instrument, ITick> lastTicks = new HashMap<>();
    private Set<Instrument> upFinishedInstruments = new HashSet<>();
    private Set<Instrument> downFinishedInstruments = new HashSet<>();

    private String getEmail() throws JFException {
        return getConfig("email", String.class);
    }

    private double getUpWatermark(Instrument instrument) throws JFException {
        return getConfig(instrument.toString(), "upWatermark", Double.class);
    }

    private double getDownWatermark(Instrument instrument) throws JFException {
        return getConfig(instrument.toString(), "downWatermark", Double.class);
    }

    @Override
    public void onStart(IContext context) throws JFException {
        init(context, STRATEGY_TAG);
        helper.setMailTo(getEmail());
        helper.logDebug(STRATEGY_TAG + " start!");
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
                    helper.logDebug("onMessage other type: " + message.getType().toString() +
                            ", content: " + message.getContent());
                }
            }
        } catch (JFException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            helper.sendMail("CRASH! " + STRATEGY_TAG, "OnMessage exception: " + e.getMessage());
        }
    }
    @Override
    public void onStop() throws JFException {
        helper.sendMail(STRATEGY_TAG + " OnStop!", "");
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        ITick lastTick = lastTicks.get(instrument);
        if (lastTick != null) {
            double upWatermark = getUpWatermark(instrument);
            double downWatermark = getDownWatermark(instrument);
            if (!upFinishedInstruments.contains(instrument) &&
                    tick.getAsk() >= upWatermark &&
                    lastTick.getAsk() < upWatermark) {
                helper.sendMail("Up Break " + instrument.toString() + " Ask: " + tick.getAsk(), "");
                upFinishedInstruments.add(instrument);
            }
            if (!downFinishedInstruments.contains(instrument) &&
                    tick.getBid() <= downWatermark &&
                    lastTick.getBid() > downWatermark) {
                helper.sendMail("Down Break " + instrument.toString() + " Bid: " + tick.getBid(), "");
                downFinishedInstruments.add(instrument);
            }
        }
        lastTicks.put(instrument, tick);
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private void handleMessage(IMessage message, IOrder order) throws JFException {
        helper.sendMail(getOrderSubject(message, order), getOrdersSummary());
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