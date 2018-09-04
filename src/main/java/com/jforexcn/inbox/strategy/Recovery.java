package com.jforexcn.inbox.strategy;

/**
 * Created by simple on 2/7/16.
 */

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class Recovery implements IStrategy {


    private IEngine mEngine;
    private IConsole mConsole;
    private IIndicators mIndicators;
    private IHistory mHistory;
    private HashMap<Instrument, Integer> mOrderClosingMap = new HashMap<Instrument, Integer>();
    private ArrayList<Instrument> mOrderProcessingList = new ArrayList<Instrument>();

    private OpeningIndicator mOpeningIndicator;
    private HashMap<Instrument, RecoveryParams> mRecoveryParams = new HashMap<Instrument, RecoveryParams>();
    private Set<Instrument> mInstrumentSet = new HashSet<Instrument>();

    private long mOrderIntervalSecond = 1800000;
    private long mLastTickTime = 0;
    private HashMap<Instrument, Boolean> mIsCloseHigher = new HashMap<Instrument, Boolean>();

    private int mMaxOrderCount = 1;

    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    private boolean mInit = false;

    public final static String STRATEGY_TAG = "Recovery";

    @Override
    public void onStart(IContext context) throws JFException {
        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mIndicators = context.getIndicators();
        this.mHistory = context.getHistory();

        // subscribe instruments

        mInstrumentSet.add(Instrument.EURUSD);
        context.setSubscribedInstruments(mInstrumentSet, true);

        // init closing map
        for (Instrument instrument : mInstrumentSet) {
            mOrderClosingMap.put(instrument, 0);
        }

        // rsi params
        HashMap<Instrument, RSIParams> rsiParams = new HashMap<Instrument, RSIParams>();

        RecoveryParams EURUSDRecoveryParams = new RecoveryParams();
        mRecoveryParams.put(Instrument.EURUSD, EURUSDRecoveryParams);
        RSIParams EURUSDRSIParams = new RSIParams();
        EURUSDRSIParams.setrSIPeriod(Period.FIVE_MINS);
        rsiParams.put(Instrument.EURUSD, EURUSDRSIParams);

        // opening indicator
        mOpeningIndicator = new RSIOpeningIndicator(rsiParams);
        // date format
        mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        try {
            IOrder order = message.getOrder();
            IMessage.Type type = message.getType();
            if (order != null && isStrategyInstrument(order.getInstrument()) && isStrategyOrder(order)) {
//        puts("onMessage, messageType: " + message.getType() + "order.state: " + order.getState());
                if (order.getState().equals(IOrder.State.CANCELED)) {
                    handleOrderCanceled(order.getInstrument());
                } else if (order.getState().equals(IOrder.State.CLOSED)) {
                    handleOrderClosed(order.getInstrument());
                } else if (type.equals(IMessage.Type.ORDER_CLOSE_REJECTED)) {
                    handleOrderCloseRejected(order.getInstrument());
                } else if (order.getState().equals(IOrder.State.FILLED)) {
                    handleOrderFilled(order.getInstrument());
                } else {
                    puts("onMessage other type: " + message.getType().toString() +
                            ", content: " + message.getContent());
                }
            }
        } catch (JFException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
//            MailService.sendMail("CRASH! " + getClass().getSimpleName(), "OnMessage exception: " + e.getMessage());
        }
    }

    @Override
    public void onStop() throws JFException {
        puts("OnStop, maxOrderCount: " + mMaxOrderCount);
        // Do nothing, leave the orders.
    }


    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        try {
            if (!mInit) {
                mInit = true;
                mOpeningIndicator.initIndicator(mInstrumentSet, tick);
                for (Instrument strategyInstrument : mInstrumentSet) {
                    updateIsCloseHigher(strategyInstrument, mHistory.getBarStart(Period.FIVE_MINS, tick.getTime()));
                }
            }
            if (isStrategyInstrument(instrument) && (tick.getTime() - mLastTickTime > 950)) {
                mLastTickTime = tick.getTime();
                // add position or close position
                handleAddOrClosePosition(instrument, tick);
                handleNewPosition(instrument, tick);
            }
        } catch (JFException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
//            MailService.sendMail("CRASH! " + getClass().getSimpleName(), "onTick exception: " + e.getMessage());
        }
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        try {
            if (isStrategyInstrument(instrument)) {
                if (period.equals(Period.FIVE_MINS)) {
                    updateIsCloseHigher(instrument, askBar.getTime());
                }
                mOpeningIndicator.updateIndicator(instrument, period, askBar, bidBar);
            }
        } catch (JFException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
//            MailService.sendMail("CRASH! " + getClass().getSimpleName(), "onBar exception: " + e.getMessage());
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private boolean isStrategyInstrument(Instrument instrument) {
        return mInstrumentSet.contains(instrument);
    }

    // 根据ITick新建仓位
    private void handleNewPosition(Instrument instrument, ITick tick) throws JFException {
        if (!isOrderProcessing(instrument) && !isHavePosition(instrument)) {
            IEngine.OrderCommand orderCommand = mOpeningIndicator.getOrderCommand(instrument);
            if (orderCommand != null) {
                RecoveryParams recoveryParams = mRecoveryParams.get(instrument);
                if (orderCommand.equals(IEngine.OrderCommand.BUY)) {
                    String orderLabel = getOrderLabel(instrument, IEngine.OrderCommand.BUY, 1, tick.getTime());
                    puts("new position orderLabel: " + orderLabel);
                    trySubmitOrder(orderLabel, instrument, IEngine.OrderCommand.BUY, recoveryParams.getAmount(), tick.getBid(), 3);
                } else if (orderCommand.equals(IEngine.OrderCommand.SELL)) {
                    String orderLabel = getOrderLabel(instrument, IEngine.OrderCommand.SELL, 1, tick.getTime());
                    puts("new position orderLabel: " + orderLabel);
                    trySubmitOrder(orderLabel, instrument, IEngine.OrderCommand.SELL, recoveryParams.getAmount(), tick.getAsk(), 3);
                }
            }
        }
    }

    // 根据IBar新建仓位
    private void handleNewPosition(Instrument instrument, IBar askBar, IBar bidBar) throws JFException {
        if (!isOrderProcessing(instrument) && !isHavePosition(instrument)) {
            IEngine.OrderCommand orderCommand = mOpeningIndicator.getOrderCommand(instrument);
            if (orderCommand != null) {
                RecoveryParams recoveryParams = mRecoveryParams.get(instrument);
                if (orderCommand.equals(IEngine.OrderCommand.BUY)) {
                    String orderLabel = getOrderLabel(instrument, IEngine.OrderCommand.BUY, 1, bidBar.getTime());
                    puts("new position orderLabel: " + orderLabel);
                    trySubmitOrder(orderLabel, instrument, IEngine.OrderCommand.BUY, recoveryParams.getAmount(), bidBar.getClose(), 3);
                } else if (orderCommand.equals(IEngine.OrderCommand.SELL)) {
                    String orderLabel = getOrderLabel(instrument, IEngine.OrderCommand.SELL, 1, askBar.getTime());
                    puts("new position orderLabel: " + orderLabel);
                    trySubmitOrder(orderLabel, instrument, IEngine.OrderCommand.SELL, recoveryParams.getAmount(), askBar.getClose(), 3);
                }
            }
        }
    }

    // 判断是加仓还是平仓
    private void handleAddOrClosePosition(Instrument instrument, ITick tick) throws JFException {
        if (!isOrderProcessing(instrument) && isHavePosition(instrument)) {
            double totalPips = getTotalProfitLossInPips(instrument);
            RecoveryParams recoveryParams = mRecoveryParams.get(instrument);
//        puts("totalPips: " + totalPips);
            // close position
            if (totalPips > recoveryParams.getTpPips(getStrategyOrders(instrument).size())) {
                puts("totalPips: " + totalPips);
                for (IOrder o : mEngine.getOrders(instrument)) {
                    puts(o.getLabel() + ", " + o.getAmount() + ", " + o.getProfitLossInPips());
                }
                tryCloseAllOrders(instrument);
            } else {
                tryAddPosition(instrument, tick);
            }
        }
    }

    // 加仓
    private void tryAddPosition(Instrument instrument, ITick tick) throws JFException {
        IOrder lastOrder = getLastOrder(instrument);
        IEngine.OrderCommand orderCommand = getAddPositionOrderCommand(lastOrder);
        int orderCount = getStrategyOrders(instrument).size();
        RecoveryParams recoveryParams = mRecoveryParams.get(instrument);
        if (orderCount < recoveryParams.getTpPipsArray().length) {
            double addPrice;
            if (orderCommand.equals(IEngine.OrderCommand.BUY)) {
//                addPrice = lastOrder.getOpenPrice() + recoveryParams.getGridPips() * cInstrument.getPipValue();
//                puts("tryAddposition, buy, addprice: " + addPrice + ", addAmount: " + addAmount + ", bid: " + tick.getBid());
                if (lastOrder.getProfitLossInPips() < -recoveryParams.getGridPips()) {
                    String orderLabel = getOrderLabel(instrument, orderCommand, (orderCount + 1),
                            tick.getTime());
                    double addAmount = getAddAmount(instrument, recoveryParams, orderCount, lastOrder);
                    trySubmitOrder(orderLabel, instrument, orderCommand, addAmount, tick.getBid(), 3);
                }
            } else {
//                addPrice = lastOrder.getOpenPrice() - recoveryParams.getGridPips() * cInstrument.getPipValue();
//                puts("tryAddposition, sell, addprice: " + addPrice + ", addAmount: " + addAmount + ", ask: " + tick.getAsk());
                if (lastOrder.getProfitLossInPips() < -recoveryParams.getGridPips()) {
                    String orderLabel = getOrderLabel(instrument, orderCommand, (orderCount + 1),
                            tick.getTime());
                    double addAmount = getAddAmount(instrument, recoveryParams, orderCount, lastOrder);
                    trySubmitOrder(orderLabel, instrument, orderCommand, addAmount, tick.getAsk(), 3);
                }
            }
        }
    }


    // 关闭所有订单
    private void tryCloseAllOrders(Instrument instrument) throws JFException {
        int closingCount = 0;
        for (IOrder o : getStrategyOrders(instrument)) {
            if (o.getState() == IOrder.State.FILLED || o.getState() == IOrder.State.OPENED) {
                o.close();
                closingCount = closingCount + 1;
            }
        }
        puts("tryCloseAllOrders, closingCount: " + closingCount);
        if (closingCount > mMaxOrderCount) {
            mMaxOrderCount = closingCount;
        }
        setOrderProcessing(instrument);
        mOrderClosingMap.put(instrument, closingCount);
    }

    // 提交订单
    private void trySubmitOrder(String orderLabel, Instrument instrument, IEngine.OrderCommand orderCommand,
                                double amount, double price, double slippage) throws JFException {
        mEngine.submitOrder(orderLabel, instrument, orderCommand, amount, price, slippage);
        setOrderProcessing(instrument);
    }

    // 获取加仓数量
    private double getAddAmount(Instrument instrument, RecoveryParams recoveryParams, int orderCount, IOrder lastOrder) throws JFException {
        double tpPips = recoveryParams.getTpPips(orderCount);
        double totalAmountPips = 0;
        for (IOrder o : mEngine.getOrders(instrument)) {
            double aimPips = o.getProfitLossInPips();
            if (o.getOrderCommand().equals(lastOrder.getOrderCommand())) {
                aimPips = o.getProfitLossInPips() - tpPips;
            } else {
                aimPips = o.getProfitLossInPips() + tpPips;
            }
            totalAmountPips = totalAmountPips + o.getAmount() * aimPips;
        }
        return recoveryParams.getAmount() + Math.ceil(Math.abs(totalAmountPips) / tpPips * 1000) / 1000;
    }

    // 获取对冲后的仓位
    private double getHedgePosition(Instrument instrument) throws JFException {
        List<IOrder> orders = getStrategyOrders(instrument);
        double hedgePosition = 0;
        for (IOrder o : orders) {
            if (o.getOrderCommand() == IEngine.OrderCommand.BUY) {
                hedgePosition = hedgePosition + o.getAmount();
            } else if (o.getOrderCommand() == IEngine.OrderCommand.SELL) {
                hedgePosition = hedgePosition - o.getAmount();
            }
        }
        return Math.abs(hedgePosition);
    }

    //获取策略的订单
    private List<IOrder> getStrategyOrders(Instrument instrument) throws JFException {
        ArrayList<IOrder> orders = new ArrayList<>();
        for (IOrder o : mEngine.getOrders(instrument)) {
            if (isStrategyOrder(o)) {
                orders.add(o);
            }
        }
        return orders;
    }

    // 获取收益点数
    private double getTotalProfitLossInPips(Instrument instrument) throws JFException {
//        double pips = 0;
//        for (IOrder o : getStrategyOrders(cInstrument)) {
//            puts("getHedgePosition(cInstrument): " + getHedgePosition(cInstrument) + ", o.getLabel(): " + o.getLabel() + ", pips: " + pips + "o.getProfitLossInPips(): " + o.getProfitLossInPips() + "o.getAmount(): " + o.getAmount());
//            pips = pips + o.getProfitLossInPips() * (o.getAmount() / (getHedgePosition(cInstrument)));
//        }
        return getLastOrder(instrument).getProfitLossInPips();
    }

    // 根据上个订单获取订单方向
    private IEngine.OrderCommand getAddPositionOrderCommand(IOrder lastOrder) {
        if (lastOrder.getOrderCommand() == IEngine.OrderCommand.BUY) {
            return IEngine.OrderCommand.SELL;
        } else {
            return IEngine.OrderCommand.BUY;
        }
    }


    // 获取上一个订单
    private IOrder getLastOrder(Instrument instrument) throws JFException {
        IOrder order = null;
        for (IOrder o : getStrategyOrders(instrument)) {
            if (order == null || o.getCreationTime() > order.getCreationTime()) {
                order = o;
            }
        }
        return order;
    }

    // 获取第一个订单
    private IOrder getFirstOrder(Instrument instrument) throws JFException {
        IOrder order = null;
        for (IOrder o : getStrategyOrders(instrument)) {
            if (order == null || o.getCreationTime() < order.getCreationTime()) {
                order = o;
            }
        }
        return order;
    }

    // 是否存在策略订单
    private boolean isHavePosition(Instrument instrument) throws JFException {
        return getStrategyOrders(instrument).size() > 0;
    }

    // 是否有在处理中的订单
    private boolean isOrderProcessing(Instrument instrument) {
        return mOrderProcessingList.contains(instrument);
    }
    private void setOrderProcessing(Instrument instrument) {
        if (!mOrderProcessingList.contains(instrument)) {
            mOrderProcessingList.add(instrument);
        }
    }
    private void removeOrderProcessing(Instrument instrument) {
        mOrderProcessingList.remove(instrument);
    }

    // 处理取消订单的message
    private void handleOrderCanceled(Instrument instrument) {
        removeOrderProcessing(instrument);
    }

    // 处理订单关闭的message
    private void handleOrderClosed(Instrument instrument) {
        int closingCount = mOrderClosingMap.get(instrument);
        closingCount = closingCount - 1;
        mOrderClosingMap.put(instrument, closingCount);
        if (closingCount <= 0) {
            removeOrderProcessing(instrument);
//            MailService.sendMail("NOTICE! " + cInstrument + " Order Closed!", "");
        }
    }

    // 处理订单被拒绝的message
    private void handleOrderCloseRejected(Instrument instrument) {
        removeOrderProcessing(instrument);
        puts("Order Close Rejected, cInstrument: " + instrument);
//        MailService.sendMail("ERROR! " + getClass().getSimpleName(), "Order Close Rejected, cInstrument: " + cInstrument);
    }

    // 处理订单filled的message
    private void handleOrderFilled(Instrument instrument) throws JFException {
        removeOrderProcessing(instrument);
        int orderCount = getStrategyOrders(instrument).size();
        RecoveryParams RecoveryParams = mRecoveryParams.get(instrument);
        int maxOrderCount = (RecoveryParams.getTpPipsArray()).length - 1;
        if (orderCount >= maxOrderCount) {
//            MailService.sendMail("WARNING!!! MAX ORDER COUNT",
//                    cInstrument + " order count: " + orderCount);
        }

    }

    // 检测下单间隔
    private boolean checkTimeInterval(long timeInterval, int orderCount) {
        if (orderCount < 4) {
            return true;
        }
        return timeInterval >= mOrderIntervalSecond;
    }

    // 更新5分钟蜡烛图收盘价格对比
    private void updateIsCloseHigher(Instrument instrument, long time) throws JFException {
        // before : now : after = 0 : n : n+m
        List<IBar> barList = mHistory.getBars(instrument, Period.FIVE_MINS, OfferSide.ASK, Filter.WEEKENDS, 2, time, 0);
        if (barList.size() >= 2) {
//            puts("close1: " + barList.get(1).getClose() + ", close0: " + barList.get(0).getClose());
            if (barList.get(1).getClose() >= barList.get(0).getClose()) {
                mIsCloseHigher.put(instrument, true);
            } else {
                mIsCloseHigher.put(instrument, false);
            }
        } else {
            mIsCloseHigher.put(instrument, null);
        }
    }

    // 检测5分钟蜡烛图收盘价格对比, 返回是否可以开单
    private boolean checkCloseHigher(Instrument instrument, IEngine.OrderCommand orderCommand, int orderCount) {
        if (orderCount < 2) {
            return true;
        }
        Boolean isCloseHigher = mIsCloseHigher.get(instrument);
        if (isCloseHigher == null) {
            return false;
        }
        if (orderCommand.equals(IEngine.OrderCommand.BUY) && isCloseHigher) {
            return true;
        } else if (orderCommand.equals(IEngine.OrderCommand.SELL) && !isCloseHigher) {
            return true;
        }
        return false;
    }

    // 获取订单Label
    private String getOrderLabel(Instrument instrument, IEngine.OrderCommand orderCommand, int count, long time) {
        return STRATEGY_TAG + "_" + instrument.toString().replace('/', '_') + "_" + orderCommand + "_" +
                count + "_" + mSimpleDateFormat.format(new Date(time)) + "_" + time;
    }

    // 判断是否是策略订单
    private boolean isStrategyOrder(IOrder order) {
        if (order.getLabel().startsWith(STRATEGY_TAG)) {
            return true;
        }
        return false;
    }



    private void puts(String str) {
        mConsole.getInfo().println(str);
//        StrategyRunner.LOGGER.info(str);
    }


    public interface OpeningIndicator {
        void initIndicator(Set<Instrument> instruments, ITick tick) throws JFException;

        void updateIndicator(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException;

        IEngine.OrderCommand getOrderCommand(Instrument instrument) throws JFException;
    }

    private class RSIOpeningIndicator implements OpeningIndicator {

        private HashMap<Instrument, RSIRecord> mRsiMap = new HashMap<Instrument, RSIRecord>();
        private final HashMap<Instrument, RSIParams> mRSIParams;
        private static final int RSI_LENGTH = 12;


        public final class RSIRecord {
            private double mHigh = 50;
            private double mLow = 50;
            private Double[] mRsiList;

            public RSIRecord() {
            }

            public double getHigh() {
                return mHigh;
            }

            public void setHigh(double high) {
                mHigh = high;
            }

            public double getLow() {
                return mLow;
            }

            public void setLow(double low) {
                mLow = low;
            }

            public Double[] getRsiList() {
                return mRsiList;
            }

            public void setRsiList(Double[] rsiList) {
                mRsiList = rsiList;
            }
        }

        public RSIOpeningIndicator(HashMap<Instrument, RSIParams> rsiParams) {
            mRSIParams = rsiParams;
        }


        private RSIRecord makeRSIRecord(double[] rsi) {
            RSIRecord record = new RSIRecord();
            Double[] rsi2 = new Double[rsi.length];
            double top = 0;
            double bottom = 100;
            for (int i = 0; i < rsi.length; i++) {
                rsi2[i] = rsi[i];
                if (i < RSI_LENGTH - 1) {
                    if (rsi[i] > top) {
                        top = rsi[i];
                    }
                    if (rsi[i] < bottom) {
                        bottom = rsi[i];
                    }
                }
            }
            double low = bottom + (top - bottom) / 3;
            if (top < 50) {
                low = 60;
            }
            double high = top - (top - bottom) / 3;
            if (bottom > 50) {
                high = 40;
            }
            record.setHigh(high);
            record.setLow(low);
            record.setRsiList(rsi2);
            return record;
        }

        @Override
        public void initIndicator(Set<Instrument> instruments, ITick tick) throws JFException {
            for (Instrument instrument : instruments) {
                RSIParams rsiParams = mRSIParams.get(instrument);
                double[] rsi = mIndicators.rsi(instrument, rsiParams.getrSIPeriod(), OfferSide.ASK,
                        IIndicators.AppliedPrice.CLOSE, rsiParams.getrSITimePeriod(), Filter.WEEKENDS, RSI_LENGTH, tick.getTime(), 0);
                RSIRecord record = makeRSIRecord(rsi);
                mRsiMap.put(instrument, record);
            }
        }

        @Override
        public void updateIndicator(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
            if (mRSIParams.containsKey(instrument)) {
                RSIParams rsiParams = mRSIParams.get(instrument);
                if (period.equals(rsiParams.getrSIPeriod())) {
                    double[] rsi = mIndicators.rsi(instrument, rsiParams.getrSIPeriod(), OfferSide.ASK,
                            IIndicators.AppliedPrice.CLOSE, rsiParams.getrSITimePeriod(), Filter.WEEKENDS, RSI_LENGTH, askBar.getTime(), 0);
                    RSIRecord record = makeRSIRecord(rsi);
                    mRsiMap.put(instrument, record);
                }
            }
        }

        @Override
        public IEngine.OrderCommand getOrderCommand(Instrument instrument) throws JFException {
            // last bar the rsi is ready, one or more order will be created
            if (mRsiMap.containsKey(instrument)) {
                RSIRecord record = mRsiMap.get(instrument);
                Double[] rsi = record.getRsiList();
                // rsi, before : now : after = 0 : n : n+m
                if (rsi.length >= RSI_LENGTH) {
                    if (rsi[RSI_LENGTH - 2] >= record.getHigh() && rsi[RSI_LENGTH - 1] < record.getHigh()) {
                        return IEngine.OrderCommand.SELL;
                    } else if (rsi[RSI_LENGTH - 2] <= record.getLow() && rsi[RSI_LENGTH - 1] > record.getLow()) {
                        return IEngine.OrderCommand.BUY;
                    }
                }
            }
            return null;
        }
    }

    public class RSIParams {
        private Period rSIPeriod = Period.FIVE_MINS;
        private int rSITimePeriod = 9;
        private double rSICeiling = 60;
        private double rSIFloor = 40;
        private int rSIShift = 0;


        public Period getrSIPeriod() {
            return rSIPeriod;
        }

        public void setrSIPeriod(Period rSIPeriod) {
            this.rSIPeriod = rSIPeriod;
        }

        public int getrSITimePeriod() {
            return rSITimePeriod;
        }

        public void setrSITimePeriod(int rSITimePeriod) {
            this.rSITimePeriod = rSITimePeriod;
        }

        public double getrSICeiling() {
            return rSICeiling;
        }

        public void setrSICeiling(double rSICeiling) {
            this.rSICeiling = rSICeiling;
        }

        public double getrSIFloor() {
            return rSIFloor;
        }

        public void setrSIFloor(double rSIFloor) {
            this.rSIFloor = rSIFloor;
        }

        public int getrSIShift() {
            return rSIShift;
        }

        public void setrSIShift(int rSIShift) {
            this.rSIShift = rSIShift;
        }
    }

    public class RecoveryParams {

        private int[] tpPipsArray = {15, 15, 15, 15, 15, 15, 15, 15, 15, 15};

        private double mAmount = 0.005;

        private int mGridPips = 50;

        public double getAmount() {
            return mAmount;
        }

        public void setAmount(double amount) {
            this.mAmount = amount;
        }

        public int getTpPips(int orderCount) {
            return tpPipsArray[orderCount];
        }

        public int[] getTpPipsArray() {
            return tpPipsArray;
        }

        public void setTpPipsArray(int[] tpPipsArray) {
            this.tpPipsArray = tpPipsArray;
        }

        public int getGridPips() {
            return mGridPips;
        }

        public void setGridPipsArray(int mGridPips) {
            this.mGridPips = mGridPips;
        }
    }

}