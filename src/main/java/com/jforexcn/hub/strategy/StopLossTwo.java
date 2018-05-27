package com.jforexcn.hub.strategy;

/**
 * Created by simple on 8/5/2018.
 */

import com.dukascopy.api.Configurable;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.feed.ITailoredFeedDescriptor;
import com.dukascopy.api.feed.ITickBar;

import java.util.ArrayList;

/**
 * 出场策略2
 * 检查场内Filled订单, 如果订单有止损, 那么判断当前盈利如果达到止损的riskAndReturn倍, 就止盈.
 * 例如: 止损30点, riskAndReturn = 2, 点差spreads = 3, 那么如果盈利为 30 * 2 + 3 = 63点就close订单.
 *
 * 只有止损价格劣于入场价格时才触发, 如果止损价格优于入场价格, 也就是止损用来锁定利润的时候不触发.
 */

public class StopLossTwo extends SubStrategy {

    private static String STRATEGY_TAG = StopLossTwo.class.getSimpleName();

    @Configurable(value = "spreads", stepSize = 1)
    public int cSpreads = 3;
    @Configurable(value = "riskAndReturn", stepSize = 0.1)
    public double cRiskAndReturn = 2;
    @Configurable(value = "applyByLabel")
    public boolean cApplyByLabel = false;

    private int getSpreads() throws JFException {
        if (!isFromHub()) {
            return cSpreads;
        }
        return getConfig(cInstrument.toString(), "spreads", Integer.class);
    }

    private double getRiskAndReturn() throws JFException {
        if (!isFromHub()) {
            return cRiskAndReturn;
        }
        return getConfig("riskAndReturn", Double.class);
    }

    private boolean getApplyByLabel() throws JFException {
        if (!isFromHub()) {
            return cApplyByLabel;
        }
        return getConfig("applyByLabel", Boolean.class);
    }

    @Override
    public void onStart(IContext context) throws JFException {
        super.onStart(context);
        init(context, STRATEGY_TAG);
        helper.logDebug(STRATEGY_TAG + " start!");
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        super.onTick(instrument, tick);
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        super.onBar(instrument, period, askBar, bidBar);
        if (period.equals(Period.TEN_SECS)) {
            for (IOrder order : getFilledOrdersWithStopLossByInstrument(instrument)) {
                if (!cApplyByLabel || checkLabel(order)) {
                    if (IEngine.OrderCommand.BUY.equals(order.getOrderCommand())) {
                        double stopLossValue = order.getOpenPrice() - order.getStopLossPrice();
                        if (stopLossValue > 0) {
                            double takeProfitValue = (order.getProfitLossInPips() - getSpreads()) * order.getInstrument().getPipValue();
                            if (takeProfitValue > stopLossValue * getRiskAndReturn()) {
                                order.close();
                            }
                        }
                    } else if (IEngine.OrderCommand.SELL.equals(order.getOrderCommand())) {
                        double stopLossValue = order.getStopLossPrice() - order.getOpenPrice();
                        if (stopLossValue > 0) {
                            double takeProfitValue = (order.getProfitLossInPips() - getSpreads()) * order.getInstrument().getPipValue();
                            if (takeProfitValue > stopLossValue * getRiskAndReturn()) {
                                order.close();
                            }
                        }
                    }
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

    @Override
    public void onStop() throws JFException {
        super.onStop();
        helper.logDebug(STRATEGY_TAG + " OnStop!");
    }

    private boolean checkLabel(IOrder order) {
        String label = order.getLabel();
        return label.contains(this.getClass().getSimpleName());
    }

    private ArrayList<IOrder> getFilledOrdersWithStopLossByInstrument(Instrument instrument) throws JFException {
        ArrayList<IOrder> orders = new ArrayList<>();
        for (IOrder order : mEngine.getOrders()) {
            if (order != null &&
                    helper.isStrategyInstrument(instrument) &&
                    IOrder.State.FILLED.equals(order.getState()) &&
                    order.getStopLossPrice() > 0 &&
                    instrument.equals(order.getInstrument()) &&
                    !order.getLabel().contains("NoStopLoss")
                    ) {
                orders.add(order);
            }
        }
        return orders;
    }
}