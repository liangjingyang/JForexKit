package com.jforexcn.inbox.strategy;

/**
 * Created by simple on 19/06/18.
**/

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

public class MinMaxStrategy implements IStrategy {

    @Configurable("Instrument")
    public Instrument cInstrument = Instrument.XAUUSD;
    @Configurable("Period")
    public Period cPeriod = Period.FIFTEEN_MINS;
    @Configurable("StopLossPips")
    public int cStopLossPips = 100;
    @Configurable("ReturnRiskRate")
    public int cReturnRiskRate = 3;
    @Configurable("Amount in Million")
    public double cAmount = 0.0001;

    private IEngine engine;
    private IConsole console;
    private IHistory history;
    private IAccount account;
    private IIndicators indicators;

    private HashSet<Instrument> subscribedInstruments = new HashSet<>();

    private static String STRATEGY_NAME = "MinMaxStrategy";


    @Override
    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.history = context.getHistory();
        this.account = context.getAccount();
        this.indicators = context.getIndicators();

        subscribedInstruments.add(cInstrument);
        context.setSubscribedInstruments(subscribedInstruments, true);
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder order = message.getOrder();
        if (order == null) {
            // 非订单相关message, 以及不是本策略相关订单的信息, 忽略
            return;
        }
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

    @Override
    public void onStop() throws JFException {
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (isThisInstrument(instrument)) {
            if (period.equals(cPeriod)) {
                List<IOrder> orders = getOrders();
                if (orders.size() == 0) {
                    ITick lastTick = history.getLastTick(cInstrument);
                    long time = lastTick.getTime();
                    double[][] minMaxList = indicators.minMax(cInstrument, Period.ONE_HOUR, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE, 24, Filter.ALL_FLATS, 3, time, 0);
                    double min = minMaxList[0][0];
                    double max = minMaxList[1][0];
                    double[] atr = indicators.atr(cInstrument, Period.ONE_HOUR, OfferSide.ASK, 20, Filter.ALL_FLATS, 4, time, 0);
                    if (atr[3] < 30 * cInstrument.getPipValue()) {
                        if (bidBar.getClose() > max) {
                            double stopLossPrice = lastTick.getAsk() - cStopLossPips * cInstrument.getPipValue();
                            puts("BUY, min: " + min + ", max: " + max + ", stoplossprice: " + stopLossPrice + ", bidbar.close: " + bidBar.getClose());
                            engine.submitOrder(STRATEGY_NAME + System.currentTimeMillis(), cInstrument, IEngine.OrderCommand.BUY, cAmount, 0.0, 3.0, stopLossPrice, 0);
                        } else if (askBar.getClose() < min) {
                            double stopLossPrice = lastTick.getBid() + cStopLossPips * cInstrument.getPipValue();
                            puts("SELL, min: " + min + ", max: " + max + ", stoplossprice: " + stopLossPrice + ", bidbar.close: " + askBar.getClose());
                            engine.submitOrder(STRATEGY_NAME + System.currentTimeMillis(), cInstrument, IEngine.OrderCommand.SELL, cAmount, 0.0, 3.0, stopLossPrice, 0);
                        }
                    }
                } else {
                    for (IOrder order : orders) {
                        if (order.getState().equals(IOrder.State.FILLED)) {
                            updateStopPosition(order);
                        }
                    }
                }
            }
        }
    }

    private void updateStopPosition(IOrder order) throws JFException {
        double takeProfitPips = order.getProfitLossInPips();
        if (takeProfitPips > cStopLossPips * cReturnRiskRate) {
            double multiple = takeProfitPips / cStopLossPips;
            multiple = 0.2535 * Math.log(multiple) + 0.1172;
            if (multiple > 0.9) {
                multiple = 0.9;
            }
            int awayFromOpenPips = (int) (multiple * takeProfitPips);
            puts("multiple: " + multiple + ", away: " + awayFromOpenPips);
            if (order.getOrderCommand().equals(IEngine.OrderCommand.BUY)) {
                double stopLossPrice = order.getOpenPrice() + awayFromOpenPips * cInstrument.getPipValue();
                if (stopLossPrice > order.getStopLossPrice()) {
                    order.setStopLossPrice(stopLossPrice);
                }
            } else if (order.getOrderCommand().equals(IEngine.OrderCommand.SELL)) {
                double stopLossPrice = order.getOpenPrice() - awayFromOpenPips * cInstrument.getPipValue();
                if (stopLossPrice < order.getStopLossPrice()) {
                    order.setStopLossPrice(stopLossPrice);
                }
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private List<IOrder> getOrders() throws JFException {
        List<IOrder> orders = new ArrayList<>();
        for (IOrder order : engine.getOrders()) {
            if (order.getInstrument().equals(cInstrument)) {
                orders.add(order);
            }
        }
        return orders;
    }


    private boolean isThisInstrument(Instrument instrument) {
        return subscribedInstruments.contains(instrument);
    }


    private void puts(String str) {
        console.getInfo().println(STRATEGY_NAME + " === " + str);
    }

}