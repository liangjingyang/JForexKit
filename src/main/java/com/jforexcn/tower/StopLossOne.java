package com.jforexcn.tower;

/**
 * Created by simple on 8/5/18.
 */

import com.dukascopy.api.*;

public class StopLossOne extends BaseStrategy {

    @Configurable("trailingPeriod")
    public Period cTrailingPeriod = Period.ONE_MIN;
    @Configurable(value = "maxStopLossPips", stepSize = 1)
    public int cMaxStopLossPips = 50;
    @Configurable(value = "rrForBreakEven", stepSize = 0.1)
    public double cRrForBreakEven = 1;
    @Configurable(value = "trailingStopLossPips", stepSize = 1)
    public int cTrailingStopLossPips = 50;
    @Configurable(value = "trailingSpeed", stepSize = 0.1)
    public double cTrailingSpeed = 0.5;
    @Configurable(value = "minTrailingPips", stepSize = 1)
    public int cMinTrailingPips = 0;

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
        if (isStrategyInstrument(instrument)) {
            logDebug("onBar " + instrument + " period: " + period);
            if (period.equals(Period.TEN_SECS)) {
                // 没有止损的订单必须设置止损,
                for (IOrder order : orderHelper.getFilledOrdersWithoutStopLossByInstrument(instrument)) {
                    if (IEngine.OrderCommand.BUY.equals(order.getOrderCommand())) {
                        orderHelper.setStopLossPrice(order, bidBar.getClose() - cMaxStopLossPips * instrument.getPipValue());
                        logDebug("Set MAX stop loss BUY id:" + order.getId() + ", cInstrument:" + instrument);
                    } else if (IEngine.OrderCommand.SELL.equals(order.getOrderCommand())) {
                        orderHelper.setStopLossPrice(order, askBar.getClose() + cMaxStopLossPips * instrument.getPipValue());
                        logDebug("Set MAX stop loss SELL id:" + order.getId() + ", cInstrument:" + instrument);
                    }
                }
                double lossPrice;
                for (IOrder order : orderHelper.getFilledOrdersWithStopLossByInstrument(instrument)) {
                    if (IEngine.OrderCommand.BUY.equals(order.getOrderCommand())) {
                        lossPrice = order.getOpenPrice() - order.getStopLossPrice();
                        if (lossPrice > 0) {
                            double spreadsPrice = spreads * instrument.getPipValue();
                            if (bidBar.getClose() > order.getOpenPrice() + lossPrice * cRrForBreakEven + spreadsPrice) {
                                orderHelper.setStopLossPrice(order, order.getOpenPrice() + spreadsPrice);
                                logDebug("Set Stop Loss to Open Price BUY id:" + order.getId() + ", cInstrument:" + instrument);
                            }
                        }

                    } else if (IEngine.OrderCommand.SELL.equals(order.getOrderCommand())) {
                        lossPrice = (order.getStopLossPrice() - order.getOpenPrice());
                        if (lossPrice > 0) {
                            double spreadsPrice = spreads * instrument.getPipValue();
                            if (askBar.getClose() < order.getOpenPrice() - lossPrice * cRrForBreakEven - spreadsPrice) {
                                orderHelper.setStopLossPrice(order, order.getOpenPrice() - spreadsPrice);
                                logDebug("Set Stop Loss to Open Price SELL id:" + order.getId() + ", cInstrument:" + instrument);
                            }
                        }
                    }
                }
            }
            if (period.equals(cTrailingPeriod)) {
                double trailingSLValue = cTrailingStopLossPips * instrument.getPipValue();
                double minTrailingValue = cMinTrailingPips * instrument.getPipValue();
                double stopLossPrice;
                for (IOrder order : orderHelper.getFilledOrdersWithStopLossByInstrument(instrument)) {
                    if (IEngine.OrderCommand.BUY.equals(order.getOrderCommand()) &&
                            order.getOpenPrice() <= order.getStopLossPrice()) {
                        if (bidBar.getClose() > order.getStopLossPrice() + trailingSLValue) {
                            stopLossPrice = bidBar.getClose() - trailingSLValue;
                            logDebug("1000tick bar BUY1 cInstrument: " + instrument + ", order.label: " + order.getLabel() + ", half_atr: " + trailingSLValue + ", stopLossPrice: " + stopLossPrice + "close: " + bidBar.getClose() + ", open " + bidBar.getOpen());
                            orderHelper.setStopLossPrice(order, stopLossPrice);
                        } else {
                            double trailingValue = Math.abs(bidBar.getClose() - bidBar.getOpen()) * cTrailingSpeed;
                            if (trailingValue > minTrailingValue) {
                                stopLossPrice = order.getStopLossPrice() + trailingValue;
                                logDebug("1000tick bar BUY2 cInstrument: " + instrument + ", order.label: " + order.getLabel() + ", half_atr: " + trailingSLValue + ", stopLossPrice: " + stopLossPrice + "close: " + bidBar.getClose() + ", open " + bidBar.getOpen());
                                orderHelper.setStopLossPrice(order, stopLossPrice);
                            }
                        }
                    } else if (IEngine.OrderCommand.SELL.equals(order.getOrderCommand()) &&
                            order.getOpenPrice() >= order.getStopLossPrice()) {
                        if (askBar.getClose() < order.getStopLossPrice() - trailingSLValue) {
                            stopLossPrice = askBar.getClose() + trailingSLValue;
                            logDebug("1000tick SELL1 cInstrument: " + instrument + ", order.label: " + order.getLabel() + ", half_atr: " + trailingSLValue + ", stopLossPrice: " + stopLossPrice + "close: " + askBar.getClose() + ", open " + askBar.getOpen());
                            orderHelper.setStopLossPrice(order, stopLossPrice);
                        } else {
                            double trailingValue = Math.abs(askBar.getOpen() - askBar.getClose()) * cTrailingSpeed;
                            if (trailingValue > minTrailingValue) {
                                stopLossPrice = order.getStopLossPrice() - trailingValue;
                                logDebug("1000tick bar SELL2 cInstrument: " + instrument + ", order.label: " + order.getLabel() + ", half_atr: " + trailingSLValue + ", stopLossPrice: " + stopLossPrice + "close: " + askBar.getClose() + ", open " + askBar.getOpen());
                                orderHelper.setStopLossPrice(order, stopLossPrice);
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
        logDebug(strategyTag + " OnStop!");
    }


}