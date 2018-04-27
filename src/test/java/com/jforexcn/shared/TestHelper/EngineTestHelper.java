package com.jforexcn.shared.TestHelper;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.dukascopy.api.instrument.IFinancialInstrument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by simple on 12/12/16.
 */

public class EngineTestHelper extends BaseTestHelper implements IEngine {

    private List<IOrder> orders = new ArrayList<>();
    private OrderTestHelper.OrderBuilder orderBuilder = new OrderTestHelper.OrderBuilder();
    private int idCount = 0;

    public void appendOrder(OrderTestHelper order) {
        this.orders.add(order);
        order.setStrategy(getStrategy());
    }

    public void onBar(Instrument instrument, Period period, BarTestHelper askBar, BarTestHelper bidBar) throws JFException {
        for (IOrder order : orders) {
            OrderTestHelper o = (OrderTestHelper) order;
            if (o.isLong()) {
                o.setProfitLossInPips((bidBar.getClose() - o.getOpenPrice()) / instrument.getPipValue());
            } else {
                o.setProfitLossInPips((o.getOpenPrice() - askBar.getClose()) / instrument.getPipValue());
            }
        }
        getStrategy().onBar(instrument, period, askBar, bidBar);
    }

    @Override
    public IOrder submitOrder(String label, Instrument instrument, OrderCommand orderCommand, double amount, double price, double slippage, double stopLossPrice, double takeProfitPrice, long goodTillTime, String comment) throws JFException {
        this.idCount = this.idCount + 1;
        OrderTestHelper order = orderBuilder
                .reset()
                .setId("TEST" + idCount)
                .setLabel(label)
                .setInstrument(instrument)
                .setOrderCommand(orderCommand)
                .setAmount(amount)
                .setOriginalAmount(amount)
                .setOpenPrice(price)
                .setStopLossPrice(stopLossPrice)
                .setTakeProfitPrice(takeProfitPrice)
                .setCreationTime(System.currentTimeMillis())
                .setFillTime(System.currentTimeMillis())
                .setState(IOrder.State.FILLED)
                .create();
        appendOrder(order);
        MessageTestHelper messageTestHelper = getMessageBuilder()
                .setType(IMessage.Type.ORDER_FILL_OK)
                .setCreationTime(System.currentTimeMillis())
                .setOrder(order)
                .create();
        getStrategy().onMessage(messageTestHelper);
        return order;
    }

    @Override
    public IOrder submitOrder(String label, Instrument instrument, OrderCommand orderCommand, double amount, double price, double slippage, double stopLossPrice, double takeProfitPrice, long goodTillTime) throws JFException {
        return submitOrder(label, instrument, orderCommand, amount, price, slippage, stopLossPrice, takeProfitPrice, goodTillTime, "Test");
    }

    @Override
    public IOrder submitOrder(String label, Instrument instrument, OrderCommand orderCommand, double amount, double price, double slippage, double stopLossPrice, double takeProfitPrice) throws JFException {
        return submitOrder(label, instrument, orderCommand, amount, price, slippage, stopLossPrice, takeProfitPrice, 0, "Test");
    }

    @Override
    public IOrder submitOrder(String label, Instrument instrument, OrderCommand orderCommand, double amount, double price, double slippage) throws JFException {
        return submitOrder(label, instrument, orderCommand, amount, price, slippage, 0, 0, 0, "Test");
    }

    @Override
    public IOrder submitOrder(String label, Instrument instrument, OrderCommand orderCommand, double amount, double price) throws JFException {
        return submitOrder(label, instrument, orderCommand, amount, price, 0, 0, 0, 0, "Test");
    }

    @Override
    public IOrder submitOrder(String label, Instrument instrument, OrderCommand orderCommand, double amount) throws JFException {
        return submitOrder(label, instrument, orderCommand, amount, 0, 0, 0, 0, 0, "Test");
    }

    @Override
    public IOrder getOrder(String label) throws JFException {
        if (label == null) {
            return null;
        }
        for (IOrder order : this.orders) {
            if (label.equals(order.getLabel())) {
                return order;
            }
        }
        return null;
    }

    @Override
    public IOrder getOrderById(String orderId) {
        if (orderId == null) {
            return null;
        }
        for (IOrder order : this.orders) {
            if (orderId.equals(order.getId())) {
                return order;
            }
        }
        return null;
    }

    @Override
    public List<IOrder> getOrders(Instrument instrument) throws JFException {
        List<IOrder> instrumentOrders = new ArrayList<>();
        if (instrument == null) {
            return instrumentOrders;
        }
        for (IOrder order : this.orders) {
            if (instrument.equals(order.getInstrument())) {
                instrumentOrders.add(order);
            }
        }
        return instrumentOrders;
    }

    @Override
    public List<IOrder> getOrders() throws JFException {
        return this.orders;
    }

    @Override
    public void mergeOrders(IOrder... orders) throws JFException {

    }

    @Override
    public IOrder mergeOrders(String label, IOrder... orders) throws JFException {
        return null;
    }

    @Override
    public IOrder mergeOrders(String label, String comment, IOrder... orders) throws JFException {
        return null;
    }

    @Override
    public IOrder mergeOrders(String label, Collection<IOrder> orders) throws JFException {
        return null;
    }

    @Override
    public IOrder mergeOrders(String label, String comment, Collection<IOrder> orders) throws JFException {
        return null;
    }

    @Override
    public void closeOrders(IOrder... orders) throws JFException {

    }

    @Override
    public void closeOrders(Collection<IOrder> orders) throws JFException {

    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public String getAccount() {
        return null;
    }

    @Override
    public void broadcast(String topic, String message) throws JFException {

    }

    @Override
    public StrategyMode getStrategyMode() {
        return null;
    }

    @Override
    public RunMode getRunMode() {
        return null;
    }

    @Override
    public boolean isTradable(Instrument instrument) {
        return false;
    }

    @Override
    public IOrder submitOrder(String label, IFinancialInstrument financialInstrument, OrderCommand orderCommand, double amount, double price, double slippage, double stopLossPrice, double takeProfitPrice, long goodTillTime, String comment) throws JFException {
        return null;
    }

    @Override
    public IOrder submitOrder(String label, IFinancialInstrument financialInstrument, OrderCommand orderCommand, double amount, double price, double slippage, double stopLossPrice, double takeProfitPrice, long goodTillTime) throws JFException {
        return null;
    }

    @Override
    public IOrder submitOrder(String label, IFinancialInstrument financialInstrument, OrderCommand orderCommand, double amount, double price, double slippage, double stopLossPrice, double takeProfitPrice) throws JFException {
        return null;
    }

    @Override
    public IOrder submitOrder(String label, IFinancialInstrument financialInstrument, OrderCommand orderCommand, double amount, double price, double slippage) throws JFException {
        return null;
    }

    @Override
    public IOrder submitOrder(String label, IFinancialInstrument financialInstrument, OrderCommand orderCommand, double amount, double price) throws JFException {
        return null;
    }

    @Override
    public IOrder submitOrder(String label, IFinancialInstrument financialInstrument, OrderCommand orderCommand, double amount) throws JFException {
        return null;
    }

    @Override
    public List<IOrder> getOrders(IFinancialInstrument financialInstrument) throws JFException {
        return null;
    }
}
