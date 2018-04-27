package com.jforexcn.hub;

/**
 * Created by simple on 26/9/16.
 */

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

public class StopLossOne extends SubStrategy {

    private static String STRATEGY_TAG = StopLossOne.class.getSimpleName();

    private static double MAX_STOP_LOSS_PIPS = 100;
    private static int SPREADS = 5;


    @Override
    public void onStart(IContext context) throws JFException {
        init(context, STRATEGY_TAG);
        helper.logDebug(STRATEGY_TAG + " start!");
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
//        helper.logDebug("onBar " + instrument + " period: " + period);
        if (period.equals(Period.TEN_SECS)) {
            // 没有止损的订单必须设置止损,
            for (IOrder order : getFilledOrdersWithoutStopLossByInstrument(instrument)) {
                if (IEngine.OrderCommand.BUY.equals(order.getOrderCommand())) {
                    helper.setStopLossPrice(order, bidBar.getClose() - MAX_STOP_LOSS_PIPS * instrument.getPipValue());
                    helper.logDebug("Set MAX stop loss BUY id:" + order.getId() + ", cInstrument:" + instrument);
                } else if (IEngine.OrderCommand.SELL.equals(order.getOrderCommand())) {
                    helper.setStopLossPrice(order, askBar.getClose() + MAX_STOP_LOSS_PIPS * instrument.getPipValue());
                    helper.logDebug("Set MAX stop loss SELL id:" + order.getId() + ", cInstrument:" + instrument);
                }
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private ArrayList<IOrder> getFilledOrdersWithStopLossByInstrument(Instrument instrument) throws JFException {
        ArrayList<IOrder> orders = new ArrayList<>();
        for (IOrder order : mEngine.getOrders()) {
            if (order != null &&
                    helper.isStrategyInstrument(instrument) &&
                    IOrder.State.FILLED.equals(order.getState()) &&
                    order.getStopLossPrice() > 0 &&
                    instrument.equals(order.getInstrument())
                    ) {
                orders.add(order);
            }
        }
        return orders;
    }

    private ArrayList<IOrder> getFilledOrdersWithoutStopLossByInstrument(Instrument instrument) throws JFException {
        ArrayList<IOrder> orders = new ArrayList<>();
        for (IOrder order : mEngine.getOrders()) {
            if (order != null &&
                    helper.isStrategyInstrument(instrument) &&
                    IOrder.State.FILLED.equals(order.getState()) &&
                    order.getStopLossPrice() <= 0 &&
                    instrument.equals(order.getInstrument())
                    ) {
                orders.add(order);
            }
        }
        return orders;
    }

    // common methods

    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder order = message.getOrder();
        if (order != null) {
            helper.removeOrderProcessing(order.getId());
        }
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
        helper.logDebug(STRATEGY_TAG + " OnStop!");
    }

    @Override
    public void onFeedData(ITailoredFeedDescriptor feedDescriptor, ITickBar tickBar) {
        try {
            if (FeedDescriptors.containsDescriptor(feedDescriptor)) {
                Instrument instrument = feedDescriptor.getInstrument();
                if (feedDescriptor.getTickBarSize().getSize() == FeedDescriptors.TICK_BAR_SIZE_SMALL) {
                    // 有止损的, 止损劣于入场价格的, 如果达到一定盈利, 就把止损移动到入场价格
                    double lossPrice;
                    for (IOrder order : getFilledOrdersWithStopLossByInstrument(instrument)) {
                        if (feedDescriptor.getOfferSide().equals(OfferSide.ASK) &&
                                IEngine.OrderCommand.BUY.equals(order.getOrderCommand())) {
                            lossPrice = order.getOpenPrice() - order.getStopLossPrice();
                            if (lossPrice > 0) {
                                double spreadsPrice = SPREADS * instrument.getPipValue();
                                if (tickBar.getClose() > order.getOpenPrice() + lossPrice + spreadsPrice) {
                                    helper.setStopLossPrice(order, order.getOpenPrice() + spreadsPrice);
                                    helper.logDebug("Set Stop Loss to Open Price BUY id:" + order.getId() + ", cInstrument:" + instrument);
                                }
                            }

                        } else if (feedDescriptor.getOfferSide().equals(OfferSide.BID) &&
                                IEngine.OrderCommand.SELL.equals(order.getOrderCommand())) {
                            lossPrice = (order.getStopLossPrice() - order.getOpenPrice());
                            if (lossPrice > 0) {
                                double spreadsPrice = SPREADS * instrument.getPipValue();
                                if (tickBar.getClose() < order.getOpenPrice() - lossPrice - spreadsPrice) {
                                    helper.setStopLossPrice(order, order.getOpenPrice() - spreadsPrice);
                                    helper.logDebug("Set Stop Loss to Open Price SELL id:" + order.getId() + ", cInstrument:" + instrument);
                                }
                            }
                        }
                    }
                } else if (feedDescriptor.getTickBarSize().getSize() == FeedDescriptors.TICK_BAR_SIZE_LARGE) {
                    // 有止损价格的, 并且止损价高于开仓价格的, 移动止损: 2/3个10日ATR
                    double half_atr;
                    double stopLossPrice;
                    for (IOrder order : getFilledOrdersWithStopLossByInstrument(instrument)) {
                        if (feedDescriptor.getOfferSide().equals(OfferSide.ASK) &&
                                IEngine.OrderCommand.BUY.equals(order.getOrderCommand()) &&
                                order.getOpenPrice() <= order.getStopLossPrice()) {
                            half_atr = helper.scalePrice(mIndicators.atr(instrument, Period.DAILY, OfferSide.BID, 10, 0) / 3 * 2, instrument.getPipScale());
                            if (tickBar.getClose() > order.getStopLossPrice() + half_atr) {
                                stopLossPrice = tickBar.getClose() - half_atr;
                                helper.logDebug("1000tick bar BUY1 cInstrument: " + instrument + ", order.label: " + order.getLabel() + ", half_atr: " + half_atr + ", stopLossPrice: " + stopLossPrice + "close: " + tickBar.getClose() + ", open " + tickBar.getOpen());
                                helper.setStopLossPrice(order, stopLossPrice);
                            } else if (tickBar.getClose() > tickBar.getOpen()) {
                                stopLossPrice = order.getStopLossPrice() + (tickBar.getClose() - tickBar.getOpen()) / 2;
                                helper.logDebug("1000tick bar BUY2 cInstrument: " + instrument + ", order.label: " + order.getLabel() + ", half_atr: " + half_atr + ", stopLossPrice: " + stopLossPrice + "close: " + tickBar.getClose() + ", open " + tickBar.getOpen());
                                helper.setStopLossPrice(order, stopLossPrice);
                            }
                        } else if (feedDescriptor.getOfferSide().equals(OfferSide.BID) &&
                                IEngine.OrderCommand.SELL.equals(order.getOrderCommand()) &&
                                order.getOpenPrice() >= order.getStopLossPrice()) {
                            half_atr = helper.scalePrice(mIndicators.atr(instrument, Period.DAILY, OfferSide.BID, 10, 0) / 3 * 2, instrument.getPipScale());
                            if (tickBar.getClose() < order.getStopLossPrice() - half_atr) {
                                stopLossPrice = tickBar.getClose() + half_atr;
                                helper.logDebug("1000tick SELL1 cInstrument: " + instrument + ", order.label: " + order.getLabel() + ", half_atr: " + half_atr + ", stopLossPrice: " + stopLossPrice + "close: " + tickBar.getClose() + ", open " + tickBar.getOpen());
                                helper.setStopLossPrice(order, stopLossPrice);
                            } else if (tickBar.getClose() < tickBar.getOpen()) {
                                stopLossPrice = order.getStopLossPrice() + (tickBar.getClose() - tickBar.getOpen()) / 2;
                                helper.logDebug("1000tick bar SELL2 cInstrument: " + instrument + ", order.label: " + order.getLabel() + ", half_atr: " + half_atr + ", stopLossPrice: " + stopLossPrice + "close: " + tickBar.getClose() + ", open " + tickBar.getOpen());
                                helper.setStopLossPrice(order, stopLossPrice);
                            }
                        }
                    }
                }
            }
        } catch(Exception exception) {
            helper.logError(STRATEGY_TAG + " onFeedData Exception: " + exception.getLocalizedMessage());
            throw new RuntimeException(exception);
        }
    }
}