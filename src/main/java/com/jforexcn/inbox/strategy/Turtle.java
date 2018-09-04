package com.jforexcn.inbox.strategy;

import com.dukascopy.api.Configurable;
import com.dukascopy.api.Filter;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IChart;
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
import com.dukascopy.api.JFUtils;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.indicators.IIndicator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.dukascopy.api.Instrument.XAGUSD;
import static com.dukascopy.api.Instrument.XAUUSD;

/**
 * Created by simple(simple.continue@gmail.com) on 12/10/2017.
 */

public class Turtle implements IStrategy {

    @Configurable("a0 Strategy Tag")
    public String cStrategyTag = "Turtle";
    @Configurable("a1 Instrument")
    public Instrument cInstrument = Instrument.GBPJPY;
    @Configurable("a2 Period")
    public Period cPeriod = Period.FOUR_HOURS;
    @Configurable("a3 Instrument For Calculate Currency")
    public Instrument cCurrencyInstrument = Instrument.GBPUSD;

    @Configurable("a4 Previous N Bars For Stop Loss")
    public int cPreviousBarsForSL = 5;
    @Configurable("a5 Previous M Bars For Add Position")
    public int cPreviousBarsForAP = 5;
    @Configurable("a6 Slippage Pips")
    public int cSlippage = 0;

    @Configurable("b1 Virtual Equity")
    public double cVirtualEquity = 10000;
    @Configurable(value = "b2 Total Risk Rate", stepSize = 0.001)
    public double cTotalRiskRate = 0.05;
    @Configurable(value = "b3 Single Position Risk Rate", stepSize = 0.001)
    public double cSinglePositionRiskRate = 0.02;

    @Configurable("c1 Risk Extra Pips")
    public int cRiskExtraPips = 5;
    @Configurable("c2 Stop Loss Extra Pips")
    public int cStopLossExtraPips = 5;
    @Configurable("c3 Take Profit Extra Pips")
    public int cTakeProfitExtraPips = 5;
    @Configurable("c4 Add Position Extra Pips")
    public int cAddPositionExtraPips = 3;
    @Configurable("c5 Add Position Automatically")
    public boolean cAddPositionAuto = false;
    @Configurable("c6 Add Position Interval Pips")
    public double cAddPositionIntervalPips = 30.0;

    @Configurable("d1 MAATRChannel.jfx File")
    public File indicatorJfxFile = new File("MAATRChannel.jfx");
    @Configurable("d2 MAATRCHANNEL Line (-5~5)")
    public int cMaAtrChannelLine = 0;
    @Configurable("df MAATRCHANNEL Ma Period")
    public Period cMaAtrChannelMaPeriod = Period.ONE_HOUR;
    @Configurable("d3 MAATRCHANNEL Ma Time Period")
    public int cMaAtrChannelMaTimePeriod = 144;
    @Configurable("d4 MAATRCHANNEL Ma Type")
    public IIndicators.MaType cMaAtrChannelMaType = IIndicators.MaType.EMA;
    @Configurable("d5 MAATRCHANNEL Atr Period")
    public Period cMaAtrChannelAtrPeriod = Period.WEEKLY;
    @Configurable("de MAATRCHANNEL Base Period")
    public Period cMaAtrChannelBasePeriod = Period.WEEKLY;
    @Configurable("d6 MAATRCHANNEL OfferSide")
    public OfferSide cMaAtrChannelOfferSide = OfferSide.ASK;
    @Configurable("d7 MAATRCHANNEL Atr Time Period")
    public int cMaAtrChannelAtrTimePeriod = 52;
    @Configurable(value = "d8 MAATRCHANNEL Atr Multiple", stepSize = 0.001)
    public double cMaAtrChannelAtrMultiple = 0.25;
    @Configurable(value = "d9 MAATRCHANNEL Line 1 Multiple", stepSize = 0.001)
    public double cMaAtrChannelLine1Multiple = 2;
    @Configurable(value = "da MAATRCHANNEL Line 2 Multiple", stepSize = 0.001)
    public double cMaAtrChannelLine2Multiple = 3;
    @Configurable(value = "db MAATRCHANNEL Line 3 Multiple", stepSize = 0.001)
    public double cMaAtrChannelLine3Multiple = 4;
    @Configurable(value = "dc MAATRCHANNEL Line 4 Multiple", stepSize = 0.001)
    public double cMaAtrChannelLine4Multiple = 5;

    @Configurable("e1 First Order Automatically")
    public boolean cFirstOrderAuto = false;
    @Configurable("e2 First Order Open While Broke N Bars")
    public int cFirstOrderOpenBreakBars = 20;
//    @Configurable("ce First Order Close While Broke M Bars")
//    public int cFirstOrderCloseBreakBars = 10;
    @Configurable("e3 First Order Amount")
    public int cFirstOrderAmount = 0;

    @Configurable("z1 Debug")
    public boolean cDebug = false;

    private String indName = "MAATRCHANNEL";
    private IIndicator indicator;

    private IEngine mEngine;
    private IConsole mConsole;
    private IIndicators mIndicators;
    private IHistory mHistory;
    private IChart mChart;
    private IAccount mAccount;
    private JFUtils mUtils;

    private Helper h = new Helper();
    private Statistics statistics;

    private boolean init = false;
    private double stopLossPrice = 0.0;
    private double takeProfitPrice = 0.0;
    private double addPositionPrice = 0.0;

    private IEngine.OrderCommand getAddPositionCommand() throws JFException {
        List<IOrder> orders = getInstrumentOrders();
        if (orders.size() > 0) {
            IEngine.OrderCommand filledCommand = orders.get(0).getOrderCommand();
            if (filledCommand.isLong()) {
                return IEngine.OrderCommand.BUYSTOP_BYBID;
            } else if (filledCommand.isShort()) {
                return IEngine.OrderCommand.SELLSTOP_BYASK;
            }
        }
        return null;
    }

    private double getAllFilledAmount() throws JFException {
        List<IOrder> orders = getInstrumentOrders();
        double mil = 0;
        for (IOrder order : orders) {
            mil = mil + order.getRequestedAmount();
        }
        return h.milToAmount(mil);
    }

    private double getAddPositionAmount() throws JFException {
        // amount = equity / price;  all risk equity = price * amount
        double pipToCurrency = h.pipToCurrency(cInstrument);
        double riskPips = Math.abs(addPositionPrice - stopLossPrice) / cInstrument.getPipValue();
        double totalLimitAmount = (cVirtualEquity * cTotalRiskRate) / ((riskPips + cRiskExtraPips) * pipToCurrency);
        double singleLimitAmount = (cVirtualEquity * cSinglePositionRiskRate) / ((riskPips + cRiskExtraPips) * pipToCurrency);
        double filledAmount = getAllFilledAmount();
        double positionAmount = Math.min((totalLimitAmount - filledAmount), singleLimitAmount);
        positionAmount = h.clearAmount(cInstrument, Math.max(positionAmount, 0));
        h.logDebug("pipToCurrency: " + pipToCurrency + ", riskPips: " + riskPips + ", totalLimitAmount: " + totalLimitAmount + ", singleLimitAmount: " + singleLimitAmount + ", filledAmount: " + filledAmount + ", AddPositionAmount: " + positionAmount);
        return h.amountToMil(cInstrument, positionAmount);
    }

    private List<IOrder> getInstrumentOrders() throws JFException {
        ArrayList<IOrder> orders = new ArrayList<IOrder>();
        for (IOrder order : mEngine.getOrders()) {
            if (h.isStrategyInstrument(order.getInstrument())) {
                orders.add(order);
            }
        }
        return orders;
    }

    private List<IOrder> getInstrumentCreatedAndOpenedOrders() throws JFException {
        ArrayList<IOrder> orders = new ArrayList<IOrder>();
        for (IOrder order : mEngine.getOrders()) {
            if (h.isStrategyInstrument(order.getInstrument()) &&
                    (order.getState().equals(IOrder.State.OPENED) || order.getState().equals(IOrder.State.CREATED))) {
                orders.add(order);
            }
        }
        return orders;
    }

    private List<IOrder> getInstrumentFilledOrders() throws JFException {
        ArrayList<IOrder> orders = new ArrayList<IOrder>();
        for (IOrder order : mEngine.getOrders()) {
            if (h.isStrategyInstrument(order.getInstrument()) &&
                    order.getState().equals(IOrder.State.FILLED)) {
                orders.add(order);
            }
        }
        return orders;
    }

    private IOrder getInstrumentLastFilledOrder() throws JFException {
        IOrder order = null;
        for (IOrder o : mEngine.getOrders()) {
            if (h.isStrategyInstrument(o.getInstrument()) &&
                    o.getState().equals(IOrder.State.FILLED)) {
                if (order == null) {
                    order = o;
                } else if (o.getFillTime() > order.getFillTime()) {
                    order = o;
                }
            }
        }
        return order;
    }

    private void calc() throws JFException {
        IBar lastBidBar = mHistory.getBar(cInstrument, cPeriod, OfferSide.BID, 1);
        IBar lastAskBar = mHistory.getBar(cInstrument, cPeriod, OfferSide.ASK, 1);

        // for sl
        double highestBidHighForSL;
        double lowestAskLowForSL;
        if (cPreviousBarsForSL == 1) {
            highestBidHighForSL = lastBidBar.getHigh();
            lowestAskLowForSL = lastAskBar.getLow();
        } else {
            // minMax only can handle time cPeriod >= 2
            double[][] bidHighMinMaxForSL = mIndicators.minMax(cInstrument, cPeriod, OfferSide.BID, IIndicators.AppliedPrice.HIGH, cPreviousBarsForSL, Filter.WEEKENDS, 1, lastBidBar.getTime(), 0);
            highestBidHighForSL = bidHighMinMaxForSL[1][0];
            double[][] askLowMinMaxForSL = mIndicators.minMax(cInstrument, cPeriod, OfferSide.ASK, IIndicators.AppliedPrice.LOW, cPreviousBarsForSL, Filter.WEEKENDS, 1, lastAskBar.getTime(), 0);
            lowestAskLowForSL = askLowMinMaxForSL[0][0];
        }

        // for tp
        double highestBidHighForAP;
        double lowestAskLowForAP;
        if (cPreviousBarsForSL == 1) {
            highestBidHighForAP = lastBidBar.getHigh();
            lowestAskLowForAP = lastAskBar.getLow();
        } else {
            // minMax only can handle time cPeriod >= 2
            double[][] bidHighMinMaxForAP = mIndicators.minMax(cInstrument, cPeriod, OfferSide.BID, IIndicators.AppliedPrice.HIGH, cPreviousBarsForAP, Filter.WEEKENDS, 1, lastBidBar.getTime(), 0);
            highestBidHighForAP = bidHighMinMaxForAP[1][0];
            double[][] askLowMinMaxForAP = mIndicators.minMax(cInstrument, cPeriod, OfferSide.ASK, IIndicators.AppliedPrice.LOW, cPreviousBarsForAP, Filter.WEEKENDS, 1, lastAskBar.getTime(), 0);
            lowestAskLowForAP = askLowMinMaxForAP[0][0];
        }

        if (IEngine.OrderCommand.SELLSTOP_BYASK.equals(getAddPositionCommand())) {
            double st = highestBidHighForSL + cStopLossExtraPips * cInstrument.getPipValue();
            if (stopLossPrice <= 0 || st < stopLossPrice) {
                stopLossPrice = st;
            }
            Object[] optInputs = new Object[] {
                    cMaAtrChannelMaTimePeriod, // Ma time cPeriod
                    cMaAtrChannelMaType.ordinal(), // MA Type
                    cMaAtrChannelAtrPeriod, // ATR Period
                    cMaAtrChannelOfferSide.ordinal(), // OfferSide
                    Filter.WEEKENDS.ordinal(), // filter
                    cMaAtrChannelAtrTimePeriod, // ATR time cPeriod
                    cMaAtrChannelAtrMultiple, // ATR Factor
                    cMaAtrChannelLine1Multiple, // Channel Factor 1
                    cMaAtrChannelLine2Multiple, // Channel Factor 2
                    cMaAtrChannelLine3Multiple, // Channel Factor 3
                    cMaAtrChannelLine4Multiple // Channel Factor 4
            };
            int lineIndex = 5 - cMaAtrChannelLine;
            Object[] indResult = mIndicators.calculateIndicator(cInstrument, cMaAtrChannelMaPeriod, new OfferSide[] { cMaAtrChannelOfferSide, cMaAtrChannelOfferSide }, cMaAtrChannelBasePeriod, indName,
                      new IIndicators.AppliedPrice[] { IIndicators.AppliedPrice.CLOSE }, optInputs, Filter.WEEKENDS, 1, lastBidBar.getTime(), 0);
            h.logDebug("SELLSTOP: " + ((double[])indResult[lineIndex])[0] + ", " + ((double[])indResult[5 - cMaAtrChannelLine])[0] + ", " + ((double[])indResult[5])[0]);
            takeProfitPrice = ((double[])indResult[lineIndex])[0] - cTakeProfitExtraPips * cInstrument.getPipValue();

            addPositionPrice = lowestAskLowForAP - cAddPositionExtraPips * cInstrument.getPipValue();

        } else if (IEngine.OrderCommand.BUYSTOP_BYBID.equals(getAddPositionCommand())) {
            //stop loss

            double st = lowestAskLowForSL - cStopLossExtraPips * cInstrument.getPipValue();
            if (stopLossPrice <= 0 || st > stopLossPrice) {
                stopLossPrice = st;
            }

            Object[] optInputs = new Object[] {
                    cMaAtrChannelMaTimePeriod, // Ma time cPeriod
                    cMaAtrChannelMaType.ordinal(), // MA Type
                    cMaAtrChannelAtrPeriod, // ATR Period
                    cMaAtrChannelOfferSide.ordinal(), // OfferSide
                    Filter.WEEKENDS.ordinal(), // filter
                    cMaAtrChannelAtrTimePeriod, // ATR time cPeriod
                    cMaAtrChannelAtrMultiple, // ATR Factor
                    cMaAtrChannelLine1Multiple, // Channel Factor 1
                    cMaAtrChannelLine2Multiple, // Channel Factor 2
                    cMaAtrChannelLine3Multiple, // Channel Factor 3
                    cMaAtrChannelLine4Multiple // Channel Factor 4
            };
            int lineIndex = 5 - cMaAtrChannelLine;
            Object[] indResult = mIndicators.calculateIndicator(cInstrument, cMaAtrChannelMaPeriod, new OfferSide[] { cMaAtrChannelOfferSide, cMaAtrChannelOfferSide }, cMaAtrChannelBasePeriod, indName,
                    new IIndicators.AppliedPrice[] { IIndicators.AppliedPrice.CLOSE }, optInputs, Filter.WEEKENDS, 1, lastAskBar.getTime(), 0);
            h.logDebug("BUYSTOP: " + ((double[])indResult[lineIndex])[0] + ", " + ((double[])indResult[5 + cMaAtrChannelLine])[0] + ", " + ((double[])indResult[5])[0]);
            takeProfitPrice = ((double[])indResult[lineIndex])[0] + cTakeProfitExtraPips * cInstrument.getPipValue();

            addPositionPrice = highestBidHighForAP + cAddPositionExtraPips * cInstrument.getPipValue();
        } else {
            if (cDebug) {
                Object[] optInputs = new Object[]{
                        cMaAtrChannelMaTimePeriod, // Ma time cPeriod
                        cMaAtrChannelMaType.ordinal(), // MA Type
                        cMaAtrChannelAtrPeriod, // ATR Period
                        OfferSide.ASK.ordinal(), // OfferSide
                        Filter.WEEKENDS.ordinal(), // filter
                        cMaAtrChannelAtrTimePeriod, // ATR time cPeriod
                        cMaAtrChannelAtrMultiple, // ATR Factor
                        cMaAtrChannelLine1Multiple, // Channel Factor 1
                        cMaAtrChannelLine2Multiple, // Channel Factor 2
                        cMaAtrChannelLine3Multiple, // Channel Factor 3
                        cMaAtrChannelLine4Multiple // Channel Factor 4
                };
                int lineIndex = 5 - cMaAtrChannelLine;
                Object[] indResult = mIndicators.calculateIndicator(cInstrument, cMaAtrChannelMaPeriod, new OfferSide[]{cMaAtrChannelOfferSide, cMaAtrChannelOfferSide }, cMaAtrChannelBasePeriod, indName,
                        new IIndicators.AppliedPrice[]{IIndicators.AppliedPrice.CLOSE}, optInputs, Filter.WEEKENDS, 1, lastBidBar.getTime(), 0);
                h.logDebug("Else: " + ((double[]) indResult[lineIndex])[0] + ", " + ((double[]) indResult[5 + cMaAtrChannelLine])[0] + ", " + ((double[]) indResult[5])[0]);
            }
            stopLossPrice = 0;
            takeProfitPrice = 0;
            addPositionPrice = 0;
        }

        stopLossPrice = h.scalePrice(stopLossPrice, cInstrument.getPipScale());
        takeProfitPrice = h.scalePrice(takeProfitPrice, cInstrument.getPipScale());
        addPositionPrice = h.scalePrice(addPositionPrice, cInstrument.getPipScale());

        h.logDebug("sl: " + stopLossPrice + ", tp: " + takeProfitPrice + ", ap: " + addPositionPrice + ", am: " + getAddPositionAmount());
    }


    private void setSlTp(IOrder order) throws JFException {
        if (order.getState() == IOrder.State.FILLED) {
            {
                if (order.isLong()) {
                    // order is long, sl order is short, so ask
                    h.setStopLossPrice(order, stopLossPrice, OfferSide.ASK);
                } else {
                    // order is short, sl order is long, so bid
                    h.setStopLossPrice(order, stopLossPrice, OfferSide.BID);
                }
                // tp can not choose OfferSide
                h.setTakeProfitPrice(order, takeProfitPrice);
            }
        }
    }

    private void setSlTp() throws JFException {
        for (IOrder order : getInstrumentOrders()) {
            setSlTp(order);
        }
    }

    private void tryLimitOrder(Instrument instrument) throws JFException {
        IEngine.OrderCommand command = getAddPositionCommand();
        if (cAddPositionAuto && command != null && getInstrumentCreatedAndOpenedOrders().size() == 0) {
            boolean isAddPosition = false;
            IOrder lastFilledOrder = getInstrumentLastFilledOrder();
            if (lastFilledOrder == null) {
                isAddPosition = true;
            } else {
                h.logDebug("TryLimitOrder, profitLossInPips: " + lastFilledOrder.getProfitLossInPips() + ", cAddPositionIntervalPips: " + cAddPositionIntervalPips);
                if (lastFilledOrder.getProfitLossInPips() >= cAddPositionIntervalPips) {
                    isAddPosition = true;
                }
            }

            if (isAddPosition)  {
                double amount = getAddPositionAmount();
                if (amount >= h.getMinMil(instrument)) {
                    String label = h.getStrategyTag() + "_" + h.getLastTickTime(instrument);
                    h.submitOrder(label, instrument, command,
                            amount, addPositionPrice, cSlippage, mHistory.getLastTick(cInstrument).getTime());
                }
            }
        }
    }

    private void tryFirstOrder(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (cFirstOrderAuto && getInstrumentOrders().size() == 0) {
            IBar lastBar = mHistory.getBar(cInstrument, cPeriod, OfferSide.BID, 2);
            double[][] bidMinMax = mIndicators.minMax(instrument, period, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, cFirstOrderOpenBreakBars, Filter.WEEKENDS, 1, lastBar.getTime(), 0);
            double bidMin = bidMinMax[0][0];
            h.logDebug("tryFirstOrder bid: " + bidMinMax[0][0] + ", " + bidMinMax[1][0] + ", bidclose: " + bidBar.getClose());
            if (bidBar.getClose() < bidMin) {
                long lastTime = h.getLastTickTime(instrument);
                String label = h.getStrategyTag() + "_FirstOrder_" + lastTime;
                double amount = h.amountToMil(instrument, cFirstOrderAmount);
                h.submitOrder(label, instrument, IEngine.OrderCommand.SELL,
                        amount, 0.0, cSlippage, lastTime);
            }
            lastBar = mHistory.getBar(cInstrument, cPeriod, OfferSide.ASK, 2);
            double[][] askMinMax = mIndicators.minMax(instrument, period, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE, cFirstOrderOpenBreakBars, Filter.WEEKENDS, 1, lastBar.getTime(), 0);
            double askMax = askMinMax[1][0];
            h.logDebug("tryFirstOrder ask: " + askMinMax[0][0] + ", " + askMinMax[1][0] + ", askclose: " + askBar.getClose());
            if (askBar.getClose() > askMax) {
                long lastTime = h.getLastTickTime(instrument);
                String label = h.getStrategyTag() + "_FirstOrder_" + lastTime;
                double amount = h.amountToMil(instrument, cFirstOrderAmount);
                h.submitOrder(label, instrument, IEngine.OrderCommand.BUY,
                        amount, 0.0, cSlippage, lastTime);
            }
        }
    }

    private void tryCloseAllOpenedOrders(Instrument instrument) throws JFException {
        List<IOrder> orders = getInstrumentCreatedAndOpenedOrders();
        for (IOrder order : orders) {
            h.close(order);
        }
    }
    @Override
    public void onStart(IContext context) throws JFException {
        //TODO: check args

        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mIndicators = context.getIndicators();
        this.mHistory = context.getHistory();
        this.mChart = context.getChart(cInstrument);
        this.mAccount = context.getAccount();
        this.mUtils = context.getUtils();

        h.setDebug(cDebug);
        // subscribe instruments
        h.instrumentSet.add(cInstrument);
        Set<Instrument> instrumentSetForCurrency = new HashSet<Instrument>();
        instrumentSetForCurrency.add(cInstrument);
        instrumentSetForCurrency.add(cCurrencyInstrument);
        context.setSubscribedInstruments(instrumentSetForCurrency, true);

        this.mIndicators.registerCustomIndicator(indicatorJfxFile);

//        if (mChart != null) {
//            indicator = context.getIndicators().getIndicator("MAATRCHANNEL");
//            Object[] optInputs = new Object[]{
//                    cMaAtrChannelMaTimePeriod, // Ma time cPeriod
//                    cMaAtrChannelMaType.ordinal(), // MA Type
//                    cMaAtrChannelAtrPeriod, // ATR Period
//                    cMaAtrChannelOfferSide.ordinal(), // OfferSide
//                    Filter.WEEKENDS.ordinal(), // filter
//                    cMaAtrChannelAtrTimePeriod, // ATR time cPeriod
//                    cMaAtrChannelAtrMultiple, // ATR Factor
//                    cMaAtrChannelLine1Multiple, // Channel Factor 1
//                    cMaAtrChannelLine2Multiple, // Channel Factor 2
//                    cMaAtrChannelLine3Multiple, // Channel Factor 3
//                    cMaAtrChannelLine4Multiple // Channel Factor 4
//            };
//            mChart.add(indicator, optInputs);
//        }

        statistics = new Statistics(this);

        h.logInfo(h.getStrategyTag() + " Start!");
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        if (h.isStrategyInstrument(instrument)) {
            if (!init) {
                init = true;
                calc();
                setSlTp();
                tryLimitOrder(instrument);
            }
        }
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        // only take care of orders whose cInstrument is equal to cInstrument
        if (h.isStrategyInstrument(instrument)) {
            if (cPeriod.equals(period)) {
                // move stop loss and take profit
                calc();
                setSlTp();
                // try to open limit order, but only one can be opened
                tryLimitOrder(instrument);
                tryFirstOrder(instrument, period, askBar, bidBar);
            }
        }
    }



    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder order = message.getOrder();
        if (order != null &&
                h.isStrategyInstrument(order.getInstrument())) {

            if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {

            } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {

            } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {

            } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {
                    calc();
                    setSlTp(order);
                    h.orderFilled(order);
            } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {
                    setSlTp(order);
            } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {

            } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {
                    order.close();
            } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {
                // cancel order manual order.state: CANCEL; Here only care CLOSED
                if (order.getState().equals(IOrder.State.CLOSED)) {
                    tryCloseAllOpenedOrders(order.getInstrument());
                    calc();
                    statistics.onOrderClosed(order, mAccount.getBaseEquity());
                }
            }
        }
    }

    @Override
    public void onStop() throws JFException {
        h.logInfo(h.getStrategyTag() + " Stop!");
//        h.logDebug("BaseEquity: " + mAccount.getBaseEquity());
//        h.logDebug("Equity: " + mAccount.getEquity());
//        h.logDebug("Balance: " + mAccount.getBalance());
//        h.logDebug("Leverage: " + mAccount.getLeverage());
//        h.logDebug("UseOfLeverage: " + mAccount.getUseOfLeverage());
//        h.logDebug("OverWeekEndLeverage: " + mAccount.getOverWeekEndLeverage());
//        h.logDebug("UsedMargin: " + mAccount.getUsedMargin());
//        h.logDebug("MarginCutLevel: " + mAccount.getMarginCutLevel());
//        h.logDebug("StopLossLevel: " + mAccount.getStopLossLevel());
//        h.logDebug("CreditLine: " + mAccount.getCreditLine());
//        h.logDebug("AccountCurrency: " + mAccount.getAccountCurrency());
//        h.logDebug("AccountState: " + mAccount.getAccountState());

        statistics.setEndEquity(mAccount.getBaseEquity());
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String csvFileName = "turtle_report.csv";

            File csv = new File(csvFileName);
            StringBuilder stringBuilder;
            PrintWriter printWriter = null;
            if (csv.length() <= 0) {
                // printWriter must initial here, or csv.length() always zero.
                printWriter = new PrintWriter(new FileOutputStream(csv, true));
                stringBuilder = new StringBuilder();
                stringBuilder.append("date");
                stringBuilder.append(",");
                stringBuilder.append(statistics.getCsvHeads());
                printWriter.println(stringBuilder);
                printWriter.flush();
            }
            if (printWriter == null) {
                printWriter = new PrintWriter(new FileOutputStream(csv, true));;
            }
            stringBuilder = new StringBuilder();
            stringBuilder.append(dateFormat.format(new Date(System.currentTimeMillis())));
            stringBuilder.append(",");
            stringBuilder.append(statistics.getCsvValues());
            printWriter.println(stringBuilder);
            printWriter.close();
//            if (!cDebug && mChart != null && indicator != null) {
//                mChart.removeIndicator(indicator);
//            }
        } catch (Exception e) {
            throw new JFException(e.getMessage());
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }


    /**
     * Helper Methods begin ========================================================================
     */

    private class Helper {

        /**
         * common methods
         */
        private Set<Instrument> instrumentSet = new HashSet<Instrument>();
        private HashMap<String, Long> submitTimeMap = new HashMap<>();
        private HashMap<String, Long> filledTimeMap = new HashMap<>();
        
        private boolean debug = true;

        Helper() {
        }

        public String getStrategyTag() {
            return cStrategyTag +
                    cInstrument.getPrimaryJFCurrency() +
                    cInstrument.getSecondaryJFCurrency() +
                    cPeriod.getNumOfUnits() +
                    cPeriod.getUnit().getCompactDescription();
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }

        public long getLastTickTime(Instrument instrument) throws JFException {
            long lastTickTime = mHistory.getTimeOfLastTick(instrument);
            if (lastTickTime <= 0) {
                lastTickTime = System.currentTimeMillis();
            }
            return lastTickTime;
        }

        private double pipToCurrency(Instrument instrument) throws JFException {
            return mUtils.convertPipToCurrency(instrument, mAccount.getAccountCurrency());
        }

        private double clearAmount(Instrument instrument, double amount) {
            switch (instrument.toString()){
                case "XAU/USD" : return amount >= 1 ? amount : 0;
                case "XAG/USD" : return amount >= 50 ? amount : 0;
                default : return amount >= 1000 ? amount : 0;
            }
        }

        private double getMinMil(Instrument instrument){
            switch (instrument.toString()){
                case "XAU/USD" : return 0.000001;
                case "XAG/USD" : return 0.00005;
                default : return 0.001;
            }
        }

        private int getMinAmount(Instrument instrument){
            switch (instrument.toString()){
                case "XAU/USD" : return 1;
                case "XAG/USD" : return 50;
                default : return 1000;
            }
        }

        private int getMilScale(Instrument instrument){
            switch (instrument.toString()){
                case "XAU/USD" : return 6;
                case "XAG/USD" : return 5;
                default : return 3;
            }
        }

        private double amountToMil(Instrument instrument, double amount) {
            BigDecimal mil = new BigDecimal(amount);
            BigDecimal divisor = new BigDecimal(1000000);
            mil = mil.divide(divisor, getMilScale(instrument), RoundingMode.DOWN);
            return mil.doubleValue();
        }

        private double milToAmount(double mil) {
            BigDecimal amount = new BigDecimal(mil);
            BigDecimal multiplicand = new BigDecimal(1000000);
            amount = amount.multiply(multiplicand);
            amount = amount.setScale(0, RoundingMode.DOWN);
            return amount.doubleValue();
        }

        private double scalePrice(double price, int pipScale) {
            // pips: 0.0001, price: 0.00001
            return scaleDouble(price, pipScale + 1, RoundingMode.HALF_UP);
        }

        private double scaleDouble(double value, int scale, RoundingMode roundingMode) {
            if (scale < 0) {
                throw new IllegalArgumentException();
            }
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(scale, roundingMode);
            return bd.doubleValue();
        }

        private void logInfo(String str) {
            mConsole.getInfo().println(str);
//            StrategyRunner.LOGGER.info(str);
        }

        private void logDebug(String str) {
            if (debug) {
                mConsole.getInfo().println("== DEBUG == " + str);
//            StrategyRunner.LOGGER.info(str);
            }
        }

        private void sendEmail(String subject, String body) {
//            MailService.sendMail(subject, body);
        }

        private boolean isStrategyInstrument(Instrument instrument) {
            return instrumentSet.contains(instrument);
        }

        private boolean isStrategyOrder(IOrder order) {
            return order.getLabel().startsWith(getStrategyTag());
        }

        private void orderFilled(IOrder order) {
            String key = order.getInstrument() + "#" + order.getOrderCommand();
            filledTimeMap.put(key, order.getFillTime());
        }

        private long getLastFilledTime(Instrument instrument, IEngine.OrderCommand command) {
            Long time = filledTimeMap.get(instrument + "#" + command);
            if (time != null) {
                return time;
            }
            return 0L;
        }

        /**
         * allow only one order per cInstrument one minute.
         */
        private void submitOrder(String label, Instrument instrument, IEngine.OrderCommand orderCommand,
                                 double amount, double price, double slippage, long time) throws JFException {
            Long lastSubmitTime = submitTimeMap.get(instrument + "#" + orderCommand);
            if (lastSubmitTime == null) {
                lastSubmitTime = 0L;
            }
            if (time - lastSubmitTime > 60 * 1000) {
                mEngine.submitOrder(label, instrument, orderCommand, amount, price, slippage);
                submitTimeMap.put(instrument + "#" + orderCommand, time);
            }
        }


        /**
         * To ensure only one request per order
         */
        private HashMap<String, Helper.OrderProcessing> orderProcessingHashMap = new HashMap<>();

        private void setStopLossPrice(IOrder order, double price) throws JFException {
            if (order.getStopLossPrice() == price) {
                return;
            }
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            String processingKey = getOrderProcessingKey(order.getId(), "setStopLossPrice");
            if (canOrderProcessing(processingKey, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), now);
                insertOrderProcessing(processingKey, orderProcessing);
                order.setStopLossPrice(price);
            }
        }

        private void setStopLossPrice(IOrder order, double price, OfferSide side) throws JFException {
            if (order.getStopLossPrice() == price) {
                return;
            }
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            String processingKey = getOrderProcessingKey(order.getId(), "setStopLossPrice");
            if (canOrderProcessing(processingKey, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), now);
                insertOrderProcessing(processingKey, orderProcessing);
                order.setStopLossPrice(price, side);
            }
        }

        private void setTakeProfitPrice(IOrder order, double price) throws JFException {
            if (order.getTakeProfitPrice() == price) {
                return;
            }
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            String processingKey = getOrderProcessingKey(order.getId(), "setTakeProfitPrice");
            if (canOrderProcessing(processingKey, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), now);
                insertOrderProcessing(processingKey, orderProcessing);
                order.setTakeProfitPrice(price);
            }
        }


        private void close(IOrder order, double amount, double price, double slippage) throws JFException {
            if (!(order.getState().equals(IOrder.State.FILLED) || order.getState().equals(IOrder.State.OPENED))) {
                return;
            }
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            String processingKey = getOrderProcessingKey(order.getId(), "close");
            if (canOrderProcessing(processingKey, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), now);
                insertOrderProcessing(processingKey, orderProcessing);
                order.close(amount, price, slippage);
            }
        }

        private void close(IOrder order, double amount, double price) throws JFException {
            if (!(order.getState().equals(IOrder.State.FILLED) || order.getState().equals(IOrder.State.OPENED))) {
                return;
            }
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            String processingKey = getOrderProcessingKey(order.getId(), "close");
            if (canOrderProcessing(processingKey, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), now);
                insertOrderProcessing(processingKey, orderProcessing);
                order.close(amount, price);
            }
        }

        private void close(IOrder order, double amount) throws JFException {
            if (!(order.getState().equals(IOrder.State.FILLED) || order.getState().equals(IOrder.State.OPENED))) {
                return;
            }
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            String processingKey = getOrderProcessingKey(order.getId(), "close");
            if (canOrderProcessing(processingKey, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), now);
                insertOrderProcessing(processingKey, orderProcessing);
                order.close(amount);
            }
        }

        private void close(IOrder order) throws JFException {
            if (!(order.getState().equals(IOrder.State.FILLED) || order.getState().equals(IOrder.State.OPENED))) {
                return;
            }
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            String processingKey = getOrderProcessingKey(order.getId(), "close");
            if (canOrderProcessing(processingKey, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), now);
                insertOrderProcessing(processingKey, orderProcessing);
                order.close();
            }
        }

        private boolean canOrderProcessing(String processingKey, long time) {
            if (orderProcessingHashMap.containsKey(processingKey)) {
                if (orderProcessingHashMap.get(processingKey).isExpired(time)) {
                    removeOrderProcessing(processingKey);
                } else {
                    return false;
                }
            }
            return true;
        }

        private void insertOrderProcessing(String processingKey, Helper.OrderProcessing orderProcessing) {
            orderProcessingHashMap.put(processingKey, orderProcessing);
        }

        private void removeOrderProcessing(String processingKey) {
            orderProcessingHashMap.remove(processingKey);
        }

        private String getOrderProcessingKey(String orderId, String action) {
            return orderId + "@" + action;
        }

        private class OrderProcessing {
            private final String orderId;
            private final long time;
            private final int expireTime = 3000;

            OrderProcessing(String orderId, long time) {
                this.orderId = orderId;
                this.time = time;
            }

            boolean isExpired(long time) {
                if (time - this.time > expireTime) {
                    return true;
                }
                return false;
            }
        }
    }

    private class Statistics {
        private final double startEquity;
        private double endEquity = 0;
        private int longTradeCount = 0;
        private int shortTradeCount = 0;
        private int profitTradeCount = 0;
        private int lossTradeCount = 0;
        private double bestTradeProfit = 0;
        private double worstTradeLoss = 0;

        private int maxConsecutiveWinCount = 0;
        private double maxConsecutiveProfit = 0;
        private int maxConsecutiveLossCount = 0;
        private double maxConsecutiveLoss = 0;
        private double maxDrawdown = 0;
        private double maxDrawdownRate = 0;

        private int consecutiveWinCount = 0;
        private double consecutiveProfit = 0;
        private int consecutiveLossCount = 0;
        private double consecutiveLoss = 0;
        private double drawdown = 0;
        private double drawdownRate = 0;

        private IOrder order;
        private TreeMap<String, Object> extras = new TreeMap<>();

        Statistics(Turtle strategy) throws JFException {
            try {
                this.startEquity = strategy.mAccount.getBaseEquity();
                setEndEquity(this.startEquity);
                Field[] fields = strategy.getClass().getDeclaredFields();
                for(Field field: fields){
                    if(field.isAnnotationPresent(Configurable.class)) {
                        Object value = field.get(strategy);
                        extras.put(field.getDeclaredAnnotation(Configurable.class).value(), value);
                    }
                }
            } catch (Exception e) {
                throw new JFException(e.getMessage());
            }
        }

        void onOrderClosed(IOrder order, double currentEquity) {
            setEndEquity(currentEquity);
            setOrder(order);
            h.logDebug(getCsvValues());
        }

        public void putExtra(String key, Object value) {
            extras.put(key, value);
        }

        public void putExtras(Map<String, Object> map) {
            this.extras.putAll(map);
        }

        String getCsvHeads() {
            StringBuilder heads = new StringBuilder();
            heads.append("StartEquity");
            heads.append(",");
            heads.append("EndEquity");
            heads.append(",");
            heads.append("MaxDrawdown");
            heads.append(",");
            heads.append("maxDrawdownRate");
            heads.append(",");
            heads.append("LongTradeCount");
            heads.append(",");
            heads.append("ShortTradeCount");
            heads.append(",");
            heads.append("ProfitTradeCount");
            heads.append(",");
            heads.append("LossTradeCount");
            heads.append(",");
            heads.append("BestTradeProfit");
            heads.append(",");
            heads.append("WorstTradeLoss");
            heads.append(",");
            heads.append("MaxConsecutiveWinCount");
            heads.append(",");
            heads.append("MaxConsecutiveProfit");
            heads.append(",");
            heads.append("MaxConsecutiveLossCount");
            heads.append(",");
            heads.append("MaxConsecutiveLoss");
            for (Map.Entry<String, Object> entry : extras.entrySet()) {
                heads.append(",");
                heads.append(entry.getKey());
            }
            return heads.toString();
        }

        String getCsvValues() {
            StringBuilder values = new StringBuilder();
            values.append(startEquity);
            values.append(",");
            values.append(endEquity);
            values.append(",");
            values.append(maxDrawdown);
            values.append(",");
            values.append(maxDrawdownRate);
            values.append(",");
            values.append(longTradeCount);
            values.append(",");
            values.append(shortTradeCount);
            values.append(",");
            values.append(profitTradeCount);
            values.append(",");
            values.append(lossTradeCount);
            values.append(",");
            values.append(bestTradeProfit);
            values.append(",");
            values.append(worstTradeLoss);
            values.append(",");
            values.append(maxConsecutiveWinCount);
            values.append(",");
            values.append(maxConsecutiveProfit);
            values.append(",");
            values.append(maxConsecutiveLossCount);
            values.append(",");
            values.append(maxConsecutiveLoss);
            for (Map.Entry<String, Object> entry : extras.entrySet()) {
                values.append(",");
                values.append(entry.getValue());
            }
            return values.toString();
        }


        void setEndEquity(double endEquity) {
            this.endEquity = endEquity;
            drawdown = startEquity - endEquity;
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
            drawdownRate = drawdown / startEquity;
            if (drawdownRate > maxDrawdownRate) {
                maxDrawdownRate = drawdownRate;
            }
        }

        private void setOrder(IOrder order) {
            IOrder lastOrder = this.order;
            this.order = order;
            if (order.isLong()) {
                longTradeCount = longTradeCount + 1;
            } else {
                shortTradeCount = shortTradeCount + 1;
            }
            double profitLossUSD = order.getProfitLossInUSD();
            if (profitLossUSD > 0) {
                profitTradeCount = profitTradeCount + 1;
                if (lastOrder != null && lastOrder.getProfitLossInUSD() > 0) {
                    consecutiveWinCount = consecutiveWinCount + 1;
                    consecutiveProfit = consecutiveProfit + profitLossUSD;
                    if (consecutiveWinCount > maxConsecutiveWinCount) {
                        maxConsecutiveWinCount = consecutiveWinCount;
                    }
                    if (consecutiveProfit > maxConsecutiveProfit) {
                        maxConsecutiveProfit = consecutiveProfit;
                    }
                } else {
                    consecutiveWinCount = 1;
                    consecutiveProfit = profitLossUSD;
                }

                if (profitLossUSD > bestTradeProfit) {
                    bestTradeProfit = profitLossUSD;
                }
            } else if (profitLossUSD < 0) {
                lossTradeCount = lossTradeCount + 1;
                if (lastOrder != null && lastOrder.getProfitLossInUSD() < 0) {
                    consecutiveLossCount = consecutiveLossCount + 1;
                    consecutiveLoss = consecutiveLoss + profitLossUSD;
                    if (consecutiveLossCount > maxConsecutiveLossCount) {
                        maxConsecutiveLossCount = consecutiveLossCount;
                    }
                    if (consecutiveLoss < maxConsecutiveLoss) {
                        maxConsecutiveLoss = consecutiveLoss;
                    }
                } else {
                    consecutiveLossCount = 1;
                    consecutiveLoss = profitLossUSD;
                }

                if (profitLossUSD < worstTradeLoss) {
                    worstTradeLoss = profitLossUSD;
                }
            }
        }
    }
}
