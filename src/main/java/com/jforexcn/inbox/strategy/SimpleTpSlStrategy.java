package com.jforexcn.inbox.strategy;

import com.dukascopy.api.*;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IMessage.Type;

/**
 * The strategy maintains one order with TP and SL.
 * As soon as the order gets closed either a new order gets created:
 * - on close by TP the same direction order gets opened
 * - on close by SL the opposite direction order gets opened and with a different TP distance
 * - on close from outside we stop the strategy
 *
 * On strategy stop the strategy closes its order
 */
public class SimpleTpSlStrategy implements IStrategy {
    // Configurable parameters
    @Configurable("Instrument")
    public Instrument instrument = Instrument.EURUSD;
    @Configurable("Amount")
    public double amount = 0.001;
    @Configurable("Stop loss")
    public int slPips = 10;
    @Configurable("Take profit on loss")
    public int tpPipsOnLoss = 10;
    @Configurable("Take profit on profit")
    public int tpPipsOnProfit = 5;

    private IEngine engine;
    private IHistory history;
    private IConsole console;
    private IContext context;
    private IOrder order;

    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.history = context.getHistory();
        this.console = context.getConsole();
        this.context = context;
        // subscribe the cInstrument that we are going to work with
        context.setSubscribedInstruments(java.util.Collections.singleton(instrument));
        // Fetching previous daily bar from history
        IBar prevDailyBar = history.getBar(instrument, Period.DAILY, OfferSide.ASK, 1);
        // Identifying the side of the initial order
        OrderCommand orderCmd = prevDailyBar.getClose() > prevDailyBar.getOpen() 
                ? OrderCommand.BUY 
                : OrderCommand.SELL;
        // submitting the order with the specified cAmount, command and take profit
        submitOrder(amount, orderCmd, tpPipsOnLoss);
    }

    public void onAccount(IAccount account) throws JFException {
    }

    public void onMessage(IMessage message) throws JFException {
        if (message.getType() != Type.ORDER_CLOSE_OK
                || !message.getOrder().equals(order) //only respond to our own order close
            ) {
            return;
        }
        console.getInfo().format("%s closed with P/L %.1f pips", order.getLabel(), order.getProfitLossInPips()).println();
        if (message.getReasons().contains(IMessage.Reason.ORDER_CLOSED_BY_TP)) {
            // on close by TP we keep the order direction
            submitOrder(amount, order.getOrderCommand(), tpPipsOnProfit);
        } else if (message.getReasons().contains(IMessage.Reason.ORDER_CLOSED_BY_SL)) {
            //  on close by SL we change the order direction and use other TP distance
            OrderCommand orderCmd = order.isLong() ? OrderCommand.SELL : OrderCommand.BUY;
            submitOrder(amount, orderCmd, tpPipsOnLoss);
        } else {
            //on manual close or close by another strategy we stop our strategy
            console.getOut().println("Order closed either from outside the strategy. Stopping the strategy.");
            context.stop();
        }
    }

    private void submitOrder(double amount, OrderCommand orderCmd, double tpPips) throws JFException {
        double slPrice, tpPrice;
        ITick lastTick = history.getLastTick(instrument);
        // Calculating stop loss and take profit prices
        if (orderCmd == OrderCommand.BUY) {
            slPrice = lastTick.getAsk() - slPips * instrument.getPipValue();
            tpPrice = lastTick.getAsk() + tpPips * instrument.getPipValue();
        } else {
            slPrice = lastTick.getBid() + slPips * instrument.getPipValue();
            tpPrice = lastTick.getBid() - tpPips * instrument.getPipValue();
        }
        // Submitting the order for the specified cInstrument at the current market price
        order = engine.submitOrder(orderCmd.toString() + System.currentTimeMillis(), instrument, orderCmd, amount, 0, 20, slPrice, tpPrice);
    }
    
    public void onStop() throws JFException {
        if(order.getState() == IOrder.State.FILLED || order.getState() == IOrder.State.OPENED){
            order.close();
        }
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {}
 
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {}

}
