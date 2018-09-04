package com.jforexcn.tower;

/**
 * Created by simple on 14/8/2018.
 */

import com.dukascopy.api.*;
import com.jforexcn.tower.Util.BaseHelper;

import java.util.List;

/**
 * 开仓策略
 * 收敛-突破1
 *
 *
 */

public class ConvergenceAndBreakOne extends BaseStrategy {

    @Configurable(value = "add position multiple")
    public double cAddPositionMultiple = 0.618;
    @Configurable(value = "add position interval")
    public int cAddPositionInterval = 20;
    @Configurable(value = "the first stoploss")
    public int cFirstStopLoss = 20;
    @Configurable(value = "move back for closing")
    public int cMoveBack = 20;


    @Override
    public void onStart(IContext context) throws JFException {
        super.onStart(context);
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        super.onTick(instrument, tick);
        if (isStrategyInstrument(instrument)) {
            List<IOrder> orders = orderHelper.getStrategyOrdersByInstrument(instrument);
            if (orders.size() > 0) {
                IOrder lastOrder = orderHelper.getStrategyLastOrderByInstrument(instrument);
                double profitLossInPips = lastOrder.getProfitLossInPips();
                if (profitLossInPips >= cAddPositionInterval) {
                    addPosition(orders, lastOrder, tick);
                } else if (profitLossInPips < -Math.abs(cMoveBack)) {
                    closeAll(orders);
                }
            }
        }
    }

    private void addPosition(List<IOrder> orders, IOrder lastOrder, ITick tick) throws JFException {
        double lastAmount = BaseHelper.milToAmount(lastOrder.getAmount());
        double thisAmount = lastAmount * cAddPositionMultiple;
        thisAmount = BaseHelper.clearAmount(instrument, thisAmount);
        logDebug("thisAmount : " + thisAmount);
        if (thisAmount > 0) {
            thisAmount = BaseHelper.amountToMil(instrument, thisAmount);
            String label = getLabel(orders.size() + 1);
            IEngine.OrderCommand orderCommand = lastOrder.getOrderCommand();
            double price = tick.getAsk();
            if (orderCommand.equals(IEngine.OrderCommand.SELL)) {
                price = tick.getBid();
            }
            orderHelper.submitOrder(label, instrument, lastOrder.getOrderCommand(), thisAmount, price, 3, tick.getTime());
        }
    }

    private void closeAll(List<IOrder> orders) throws JFException {
        for (IOrder order : orders) {
            order.close();
        }
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        super.onBar(instrument, period, askBar, bidBar);
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
        super.onAccount(account);
    }

    // common methods

    @Override
    public void onMessage(IMessage message) throws JFException {
        super.onMessage(message);
        IOrder order = message.getOrder();
        if (order != null) {
            if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {

            } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {

            } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {

            } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {

            } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {

            } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {

            } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {
                order.close();
            } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {

            }
        }
    }

    @Override
    public void onStop() throws JFException {
        super.onStop();
    }

    @Override
    public String getStrategyTag() {
        String parent = super.getStrategyTag();
        return parent + this.getClass().getSimpleName();
    }

    public String getLabel(int seq) {
        return getStrategyTag() + "_" + seq + "_" + System.currentTimeMillis();
    }

}