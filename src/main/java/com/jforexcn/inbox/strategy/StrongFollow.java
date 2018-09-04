package com.jforexcn.inbox.strategy;

import com.dukascopy.api.Configurable;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by simple(simple.continue@gmail.com) on 18/12/2017.
 */

public class StrongFollow implements IStrategy {

    @Configurable(value = "Instrument", modifiable = false)
    public Instrument cInstrument = Instrument.EURUSD;
    @Configurable(value = "Amount in mil", modifiable = false)
    public double cAmount = 0.01;
    @Configurable(value = "Slippage", modifiable = false)
    public int cSlippage = 3;
    @Configurable(value = "Order close after how many bars", modifiable = false)
    public int cCloseBarCount = 3;
    @Configurable(value = "Stop loss pips", modifiable = false)
    public int cStopLossPips = 30;
    @Configurable(value = "Take profit pips", modifiable = false)
    public int cTakeProfitPips = 60;
    @Configurable(value = "Close by SlTp", modifiable = false)
    public boolean cCloseBySLTP = true;
    @Configurable(value = "Period", modifiable = false)
    public Period cPeriod = Period.FOUR_HOURS;
    @Configurable(value = "Bar rect min pips", modifiable = false)
    public double cBarRectPips = 10;


    private IEngine engine;
    private IHistory history;
    private IIndicators indicators;
    private IConsole console;


    private static String TAG = "StrongFollow";

    @Override
    public void onStart(IContext context) throws JFException {
        engine = context.getEngine();
        history = context.getHistory();
        indicators = context.getIndicators();
        console = context.getConsole();

        Set<Instrument> instrumentSet = new HashSet<Instrument>();
        instrumentSet.add(cInstrument);
        context.setSubscribedInstruments(instrumentSet, true);
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {

    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (instrument.equals(cInstrument) && period.equals(cPeriod)) {
            tryCloseOrder();
            IEngine.OrderCommand command = getCommandByLastBar(askBar);
            if (IEngine.OrderCommand.BUY.equals(command)) {
                double price = history.getLastTick(instrument).getAsk();
                if (cCloseBySLTP) {
                    double sl = price - cStopLossPips * instrument.getPipValue();
                    double tp = price + cTakeProfitPips * instrument.getPipValue();
                    engine.submitOrder(getLabel(), instrument, command, cAmount, price, cSlippage, sl, tp);
                } else {
                    engine.submitOrder(getLabel(), instrument, command, cAmount, price, cSlippage);
                }
            } else if (IEngine.OrderCommand.SELL.equals(command)) {
                double price = history.getLastTick(instrument).getBid();
                if (cCloseBySLTP) {
                    double sl = price + cStopLossPips * instrument.getPipValue();
                    double tp = price - cTakeProfitPips * instrument.getPipValue();
                    engine.submitOrder(getLabel(), instrument, command, cAmount, price, cSlippage, sl, tp);
                } else {
                    engine.submitOrder(getLabel(), instrument, command, cAmount, price, cSlippage);
                }
            }
        }
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder o = message.getOrder();
        if (o != null && isStrategyInstrument(o.getInstrument())) {
            if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {
                o.close();
            } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {

    }

    @Override
    public void onStop() throws JFException {

    }

    private String getLabel() throws JFException {
        return TAG + "_" + history.getLastTick(cInstrument).getTime();
    }

    private void tryCloseOrder() throws JFException {
        for (IOrder o : getOrders()) {
            if (shouldCloseOrderByBar(o)) {
                o.close();
            }
        }
    }

    private boolean shouldCloseOrderByBar(IOrder order) throws JFException {
        if (cCloseBySLTP) {
            return false;
        }
        long filledTime = order.getFillTime();
        long lastTickTime = history.getLastTick(order.getInstrument()).getTime();
        return lastTickTime - filledTime >= cPeriod.getInterval() * cCloseBarCount - 30000;
    }

    private List<IOrder> getOrders() throws JFException {
        List<IOrder> orders = new ArrayList<>();
        for (IOrder o : engine.getOrders()) {
            if (isStrategyInstrument(o.getInstrument())) {
                orders.add(o);
            }
        }
        return orders;
    }

    private boolean isStrategyInstrument(Instrument instrument) {
        return cInstrument.equals(instrument);
    }

    private IEngine.OrderCommand getCommandByLastBar(IBar bar) throws JFException {
        double lastAtr = indicators.atr(cInstrument, cPeriod, OfferSide.ASK, 1, 1);
        double averageAtr = indicators.atr(cInstrument, cPeriod, OfferSide.ASK, 14, 2);
        if (bar.getClose() > bar.getOpen()) {
            if (lastAtr >= cBarRectPips * cInstrument.getPipValue() &&
                    lastAtr / averageAtr > 3) {
                return IEngine.OrderCommand.BUY;
            }
        } else if (bar.getClose() < bar.getOpen()) {
            if (lastAtr >= cBarRectPips * cInstrument.getPipValue() &&
                    lastAtr / averageAtr > 3) {
                return IEngine.OrderCommand.SELL;
            }
        }
        return null;
    }
}