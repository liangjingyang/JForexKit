package com.jforexcn.tower;

/**
 * Created by simple on 14/8/2018.
 */

import com.dukascopy.api.*;

import java.util.List;


/**
 * 开仓策略
 * 突破 MinMax
 *
 *
 */

@RequiresFullAccess
@Library("/data/github/JForexKit/build/libs/JForexKit-3.0.jar")
public class MinMaxBreakOne extends PositionAndStopLossOne {

    @Configurable(value = "Period")
    public Period cPeriod = Period.FIFTEEN_MINS;
    @Configurable(value = "MinMax Period")
    public int cMinMaxPeriod = 34;
    @Configurable(value = "Order amount in mil")
    public double cOrderAmountInMil = 0.01;

    private double min = 0;
    private double max = 0;


    @Override
    public void onStart(IContext context) throws JFException {
        super.onStart(context);
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        super.onTick(instrument, tick);
    }


    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        super.onBar(instrument, period, askBar, bidBar);
        if (cPeriod.equals(period) && isStrategyInstrument(instrument)) {
            long time = askBar.getTime();
            double[] highList = indicators.max(instrument, period, OfferSide.ASK, IIndicators.AppliedPrice.HIGH, cMinMaxPeriod, Filter.WEEKENDS, 2, time, 0);
            double[] lowList = indicators.min(instrument, period, OfferSide.BID, IIndicators.AppliedPrice.LOW, cMinMaxPeriod, Filter.WEEKENDS, 2, time, 0);
            min = lowList[0];
            max = highList[0];
            logDebug("min: " + min + ", max: " + max);
            List<IOrder> orders = orderHelper.getStrategyOrdersByInstrument(instrument);
            if (orders.size() <= 0) {
                if (askBar.getClose() > max && askBar.getOpen() <= max) {
                    // break up
                    IEngine.OrderCommand command = IEngine.OrderCommand.BUY;
                    double price = askBar.getClose();
                    orderHelper.submitOrder(getLabel(1), instrument, command, cOrderAmountInMil, price, 3, askBar.getTime());
                } else if (bidBar.getClose() < min && bidBar.getOpen() >= min) {
                    // break down
                    IEngine.OrderCommand command = IEngine.OrderCommand.SELL;
                    double price = askBar.getClose();
                    orderHelper.submitOrder(getLabel(1), instrument, command, cOrderAmountInMil, price, 3, bidBar.getTime());
                }
            }

        }
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
            } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {
            }
        }
    }

    @Override
    public void onStop() throws JFException {
        super.onStop();
    }

    public String getLabel(int seq) {
        return getStrategyTag() + "_" + seq + "_" + System.currentTimeMillis();
    }

}