package com.jforexcn.hub.strategy.alone;

import com.dukascopy.api.Configurable;
import com.dukascopy.api.Filter;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Library;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.RequiresFullAccess;
import com.jforexcn.hub.lib.Helper;
import com.jforexcn.hub.indicator.TwoInstrumentCorrelation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by simple(simple.continue@gmail.com) on 13/05/2018.
 *
 * 说明: JPY是避险货币, JPY兑其他货币的走向比较一致. AUD和NZD是相关性比较高的两个货币, 所以主要用AUDJPY和NZDJPY对冲.
 *      同时, 用AUDNZD来对冲相关性调整的风险
 *
 *      假设, AUD, NZD完全一致, 那么如果把AUDJPY和NZDJPY叠加到一张图表上, 每一个时间点的间距应该是
 */

@RequiresFullAccess
@Library("/data/github/JForexKit/build/libs/JForexKit-3.0.jar")
public class CorrelationOne implements IStrategy {
    private static String STRATEGY_TAG = CorrelationOne.class.getSimpleName();

    @Configurable("Period")
    public Period cPeriod = Period.ONE_HOUR;
    @Configurable("Instrument A")
    public Instrument cInstrumentA = Instrument.AUDJPY;
    @Configurable("Instrument B")
    public Instrument cInstrumentB = Instrument.NZDJPY;
    @Configurable("Instrument ")
    public Instrument cInstrumentZ = Instrument.AUDNZD;

    @Configurable("Instrument A amount in mil")
    public double cAmountA = 0.001;
    @Configurable("Instrument B amount in mil")
    public double cAmountB = 0.001;
    @Configurable("Instrument Z amount in mil")
    public double cAmountZ = 0.001;

    @Configurable("Grid Interval in pips")
    public int cGridInterval = 30;
    @Configurable("Grid Level")
    public int cGridLevel = 6;
    @Configurable("Grid Multiple")
    public double cGridMultiple = 2;

    @Configurable("Debug")
    public boolean cDebug = true;

    private Helper helper = new Helper();
    private String indicatorName = "[ZZ]2InstrumentCorrelation";
    private int closing = 0;

    @Override
    public void onStart(IContext context) throws JFException {
        Set<Instrument> instrumentSet = new HashSet<>();
        instrumentSet.add(cInstrumentA);
        instrumentSet.add(cInstrumentB);
        instrumentSet.add(cInstrumentZ);
        helper.addInstruments(instrumentSet);
        helper.init(context, STRATEGY_TAG);
        helper.setDebug(cDebug);
        helper.mContext.setSubscribedInstruments(instrumentSet);
        indicatorName = helper.mContext.getIndicators().registerCustomIndicator(TwoInstrumentCorrelation.class);
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (!isClosing() && instrument.equals(cInstrumentA)) {
            if (period.equals(Period.ONE_HOUR)) {
                Object[] correlations = calculateCorrelations(instrument);
                double[] diffList = (double[]) correlations[0];
                double[] shortMaList = (double[]) correlations[1];
                double[] longMaList = (double[]) correlations[2];
                double[] distanceList = (double[]) correlations[3];
                double[] slopeList = (double[]) correlations[4];
                helper.logDebug("diffList[1]: " + diffList[1] + ", shortMaList[1]: " + shortMaList[1] +
                        ", longMaList[1]: " + longMaList[1] + ", distanceList[1]: " + distanceList[1] +
                        ", slopeList[1]: " + slopeList[1]);
            }
            if (period.equals(Period.ONE_MIN)) {
                Object[] correlations = calculateCorrelations(instrument);
                double[] diffList = (double[]) correlations[0];
                double[] shortMaList = (double[]) correlations[1];
                double[] longMaList = (double[]) correlations[2];
                double[] distanceList = (double[]) correlations[3];
                double[] slopeList = (double[]) correlations[4];


                IOrder lastOrder = getLastOrder();
                if (lastOrder == null) { // 第一单
                    int gridLevel = 1;
                    if (Math.abs(distanceList[1]) > 30) { // 达到一个格子才入场
                        tryOpenPositionAll(gridLevel, distanceList[1], shortMaList[1], longMaList[1], slopeList[1]);
                    }
                } else {
                    // 必须先处理平仓逻辑, 才能处理开仓逻辑
                    // 如果diff 穿越shortMa就平仓
                    IOrder lastMainOrder = getLastOrderByInstrument(cInstrumentA);
                    if ((lastMainOrder.getOrderCommand().equals(IEngine.OrderCommand.BUY) && distanceList[0] <= 0 && distanceList[1] >=0) ||
                            (lastMainOrder.getOrderCommand().equals(IEngine.OrderCommand.SELL) && distanceList[1] <= 0 && distanceList[0] >=0)) {
                        tryCloseAllOrders();
                    }

                    long now = askBar.getTime();
                    long lastTime = lastOrder.getFillTime();
                    int lastGridLevel = getGridLevelFromLabel(lastOrder.getLabel());
                    Map<Instrument, IOrder> lastGridLevelOrders = getGridLevelOrders(lastGridLevel);
                    if (lastGridLevelOrders.size() == 3) { // 有单, 上一组已完成,
                        if (now > lastTime + cPeriod.getInterval()) { // 有单, 上一组已完成, 必须要有间隔
                            int gridLevel = lastGridLevel + 1;
                            if (gridLevel <= cGridLevel) { // 不能超过组数上限
                                IOrder lastOrderA = lastGridLevelOrders.get(cInstrumentA);
                                IOrder lastOrderB = lastGridLevelOrders.get(cInstrumentB);
                                if (lastOrderA != null && lastOrderB != null) {
                                    double priceA = lastOrderA.getOpenPrice();
                                    double priceB = lastOrderB.getOpenPrice();
                                    if (lastOrderA.getOrderCommand().equals(IEngine.OrderCommand.BUY) &&
                                            diffList[1] * cInstrumentA.getPipValue() < priceA - priceB - cGridInterval * cInstrumentA.getPipValue()) {
                                        // 必须要与上个diff, 也就是A, B上一个订单的成交价的差值有 gridInterval的距离
                                        tryOpenPositionAll(gridLevel, distanceList[1], shortMaList[1], longMaList[1], slopeList[1]); // open all, 先尝试把3单都下了
                                    } else if (lastOrderA.getOrderCommand().equals(IEngine.OrderCommand.SELL) &&
                                            diffList[1] * cInstrumentA.getPipValue() > priceA - priceB + cGridInterval * cInstrumentA.getPipValue()) {
                                        // 必须要与上个diff, 也就是A, B上一个订单的成交价的差值有 gridInterval的距离
                                        tryOpenPositionAll(gridLevel, distanceList[1], shortMaList[1], longMaList[1], slopeList[1]); // open all, 先尝试把3单都下了
                                    }
                                }
                            }
                        }
                    } else {
                        if (!lastGridLevelOrders.containsKey(cInstrumentA)) { // 如果有漏单, 补单
                            tryOpenPositionA(lastGridLevel, distanceList[1], shortMaList[1], longMaList[1], slopeList[1]);
                        }
                        if (!lastGridLevelOrders.containsKey(cInstrumentB)) { // 如果有漏单, 补单
                            tryOpenPositionB(lastGridLevel, distanceList[1], shortMaList[1], longMaList[1], slopeList[1]);
                        }
                        if (!lastGridLevelOrders.containsKey(cInstrumentZ)) { // 如果有漏单, 补单
                            tryOpenPositionZ(lastGridLevel, distanceList[1], shortMaList[1], longMaList[1], slopeList[1]);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        helper.onMessage(message);
        IOrder order = message.getOrder();
        if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {
        } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {
        } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {
        } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {
        } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {
        } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {
        } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {
            order.close();
        } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {
            if (helper.isStrategyOrder(order)) {
                closing = closing - 1;
                if (!isClosing()) {
                    helper.sendEmail(STRATEGY_TAG + " close " + closing + " orders finished! ", "Account equity: " + helper.mAccount.getEquity());
                }
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {

    }

    @Override
    public void onStop() throws JFException {

    }

    private boolean isClosing() {
        return closing > 0;
    }

    private void tryOpenPositionAll(int gridLevel, double distance, double shortMa, double longMa, double slope) throws JFException {
        String labelA = getLabel(cInstrumentA, gridLevel);
        String labelB = getLabel(cInstrumentB, gridLevel);
        String labelZ = getLabel(cInstrumentZ, gridLevel);
        IEngine.OrderCommand commandA = distance > 0 ? IEngine.OrderCommand.SELL : IEngine.OrderCommand.BUY;
        IEngine.OrderCommand commandB = distance > 0 ? IEngine.OrderCommand.BUY : IEngine.OrderCommand.SELL;
        IEngine.OrderCommand commandZ = slope <= 0 ? IEngine.OrderCommand.SELL : IEngine.OrderCommand.BUY;
        double amountA = cAmountA * Math.pow(cGridMultiple, gridLevel - 1);
        double amountB = cAmountB * Math.pow(cGridMultiple, gridLevel - 1);
        double amountZ = cAmountZ * Math.pow(cGridMultiple, gridLevel - 1);
        if (Math.abs(slope) > 60) {
            amountZ = amountZ * 3;
        } else if (Math.abs(slope) > 30) {
            amountZ = amountZ * 2;
        }
        long time = helper.mHistory.getLastTick(cInstrumentA).getTime();
        helper.submitOrder(labelA, cInstrumentA, commandA, amountA, 0, 3, time);
        helper.submitOrder(labelB, cInstrumentB, commandB, amountB, 0, 3, time);
        helper.submitOrder(labelZ, cInstrumentZ, commandZ, amountZ, 0, 3, time);
    }

    private void tryOpenPositionA(int gridLevel, double distance, double shortMa, double longMa, double slope) throws JFException {
        String labelA = getLabel(cInstrumentA, gridLevel);
        IEngine.OrderCommand commandA = distance > 0 ? IEngine.OrderCommand.SELL : IEngine.OrderCommand.BUY;
        double amountA = cAmountA * Math.pow(cGridMultiple, gridLevel - 1);
        long time = helper.mHistory.getLastTick(cInstrumentA).getTime();
        helper.submitOrder(labelA, cInstrumentA, commandA, amountA, 0, 3, time);
    }

    private void tryOpenPositionB(int gridLevel, double distance, double shortMa, double longMa, double slope) throws JFException {
        String labelB = getLabel(cInstrumentB, gridLevel);
        IEngine.OrderCommand commandB = distance > 0 ? IEngine.OrderCommand.BUY : IEngine.OrderCommand.SELL;
        double amountB = cAmountB * Math.pow(cGridMultiple, gridLevel - 1);
        long time = helper.mHistory.getLastTick(cInstrumentA).getTime();
        helper.submitOrder(labelB, cInstrumentB, commandB, amountB, 0, 3, time);
    }

    private void tryOpenPositionZ(int gridLevel, double distance, double shortMa, double longMa, double slope) throws JFException {
        String labelZ = getLabel(cInstrumentZ, gridLevel);
        IEngine.OrderCommand commandZ = slope <= 0 ? IEngine.OrderCommand.SELL : IEngine.OrderCommand.BUY;
        double amountZ = cAmountZ * Math.pow(cGridMultiple, gridLevel - 1);
        if (Math.abs(slope) > 60) {
            amountZ = amountZ * 3;
        } else if (Math.abs(slope) > 30) {
            amountZ = amountZ * 2;
        }
        long time = helper.mHistory.getLastTick(cInstrumentA).getTime();
        helper.submitOrder(labelZ, cInstrumentZ, commandZ, amountZ, 0, 3, time);
    }

    private void resubmitCanceledOrder(IOrder order) throws JFException {
        if (order.getState().equals(IOrder.State.CANCELED)) {
            long time = helper.mHistory.getLastTick(cInstrumentA).getTime();
            helper.submitOrder(order.getLabel(), order.getInstrument(), order.getOrderCommand(), order.getAmount(), 0, 3, time);
        }
    }

    private void tryCloseAllOrders() throws JFException {
        for (IOrder order : helper.mEngine.getOrders()) {
            if (helper.isStrategyOrder(order)) {
                order.close();
                closing = closing + 1;
            }
        }
        helper.sendEmail(STRATEGY_TAG + " close " + closing + " orders started! ", "");
    }

    private IOrder getLastOrderByInstrument(Instrument instrument) throws JFException {
        IOrder lastOrder = null;
        for (IOrder order : helper.mEngine.getOrders()) {
            if (instrument.equals(order.getInstrument()) &&
                    helper.isStrategyOrder(order)) {
                // no condition orders, all orders are market orders.
                if (lastOrder == null || lastOrder.getFillTime() < order.getFillTime()) {
                    lastOrder = order;
                }
            }
        }
        return lastOrder;
    }

    private IOrder getLastOrder() throws JFException {
        IOrder lastOrder = null;
        for (IOrder order : helper.mEngine.getOrders()) {
            if (helper.isStrategyOrder(order)) {
                if (lastOrder == null || lastOrder.getCreationTime() < order.getCreationTime()) {
                    lastOrder = order;
                }
            }
        }
        return lastOrder;
    }

    private Map<Instrument, IOrder> getGridLevelOrders(int gridLevel) throws JFException {
        Map<Instrument, IOrder> orders = new HashMap<>();
        for (IOrder order : helper.mEngine.getOrders()) {
            if (helper.isStrategyOrder(order)) {
                int level = getGridLevelFromLabel(order.getLabel());
                if (level == gridLevel) {
                    orders.put(order.getInstrument(), order);
                }
            }
        }
        return orders;
    }


    private String getLabel(Instrument instrument, int gridLevel) {
        return STRATEGY_TAG + "_" + instrument.name() + "_" + gridLevel;
    }

    private int getGridLevelFromLabel(String label) {
        String[] fields = label.split("_");
        return Integer.valueOf(fields[2]);
    }

    private Object[] calculateCorrelations(Instrument calculateInstrument) throws JFException {
        OfferSide[] offerSides = new OfferSide[] {
                OfferSide.ASK,
                OfferSide.ASK
        };
        IIndicators.AppliedPrice[] appliedPrices = new IIndicators.AppliedPrice[] {
                IIndicators.AppliedPrice.CLOSE,
                IIndicators.AppliedPrice.CLOSE
        };
        Object[] optParams = new Object[] {
                cInstrumentA,
                cInstrumentB,
                100D,
                0D,
                100D,
                0D,
                0D,
                120,
                360,
                IIndicators.MaType.SMA.ordinal(),
                TwoInstrumentCorrelation.FormulaType.subtraction.ordinal()
        };
        long time = helper.mHistory.getBar(calculateInstrument, cPeriod, OfferSide.ASK, 1).getTime();
        // calculateIndicator(Instrument instrument, Period period, OfferSide[] offerSides, String functionName,
        //        AppliedPrice[] inputTypes, Object[] optParams, Filter filter, int numberOfCandlesBefore, long time, int numberOfCandlesAfter)
        Object[] correlations = helper.mIndicators.calculateIndicator(calculateInstrument, cPeriod, offerSides, indicatorName, appliedPrices,
                optParams, Filter.ALL_FLATS, 2, time, 0);
        return correlations;
    }

}
