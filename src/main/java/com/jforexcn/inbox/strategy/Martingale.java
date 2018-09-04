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
import com.jforexcn.shared.lib.MailService;

import com.jforexcn.shared.client.StrategyRunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class Martingale implements IStrategy {

    private IEngine mEngine;
    private IConsole mConsole;
    private IIndicators mIndicators;
    private IHistory mHistory;
    private HashMap<Instrument, Integer> mOrderClosingMap = new HashMap<Instrument, Integer>();
    private ArrayList<Instrument> mOrderProcessingList = new ArrayList<Instrument>();

    private OpeningIndicator mOpeningIndicator;
    private HashMap<Instrument, MartingaleParams> mMartingaleParams = new HashMap<Instrument, MartingaleParams>();
    private Set<Instrument> mInstrumentSet = new HashSet<Instrument>();

    private long mOrderIntervalSecond = 1800000;
    private long mLastTickTime = 0;
    private HashMap<Instrument, Boolean> mIsCloseHigher = new HashMap<Instrument, Boolean>();

    private int mMaxOrderCount = 1;

    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    private boolean mInit = false;

    public final static String STRATEGY_TAG = "Martingale";
    public final static int GROUP_ID = 1;

    @Override
    public void onStart(IContext context) throws JFException {
        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mIndicators = context.getIndicators();
        this.mHistory = context.getHistory();

        // subscribe instruments

        mInstrumentSet.add(Instrument.EURGBP);
        context.setSubscribedInstruments(mInstrumentSet, true);

        // init closing map
        for (Instrument instrument : mInstrumentSet) {
            mOrderClosingMap.put(instrument, 0);
        }

        // rsi params
        HashMap<Instrument, RSIParams> rsiParams = new HashMap<Instrument, RSIParams>();

//
//        MartingaleParams USDJPYMartingaleParams = new MartingaleParams();
//        USDJPYMartingaleParams.setTpPips(10);
////        USDJPYMartingaleParams.setGridPips(80);
//        double[] USDJPYAmountArray = {0.01, 0.018, 0.032, 0.057};
//        USDJPYMartingaleParams.setAmountArray(USDJPYAmountArray);
//        mMartingaleParams.put(Instrument.USDJPY, USDJPYMartingaleParams);
//        RSIParams USDJPYRSIParams = new RSIParams();
//        rsiParams.put(Instrument.USDJPY, USDJPYRSIParams);
//
//        MartingaleParams EURUSDMartingaleParams = new MartingaleParams();
//        EURUSDMartingaleParams.setTpPips(10);
//        double[] EURUSDAmountArray = {0.002, 0.004, 0.008, 0.016, 0.032, 0.032};
//        EURUSDMartingaleParams.setAmountArray(EURUSDAmountArray);
//        mMartingaleParams.put(Instrument.EURUSD, EURUSDMartingaleParams);
//        RSIParams EURUSDRSIParams = new RSIParams();
//        EURUSDRSIParams.setrSITimePeriod(9);
//        rsiParams.put(Instrument.EURUSD, EURUSDRSIParams);

        MartingaleParams GBPAUDMartingaleParams = new MartingaleParams();
        mMartingaleParams.put(Instrument.EURGBP, GBPAUDMartingaleParams);
        RSIParams GBPAUDRSIParams = new RSIParams();
        GBPAUDRSIParams.setrSIPeriod(Period.FIVE_MINS);
        rsiParams.put(Instrument.EURGBP, GBPAUDRSIParams);

//        MartingaleParams GBPCADMartingaleParams = new MartingaleParams();
//        GBPCADMartingaleParams.setTpPips(30);
//        double[] GBPCADAmountArray = {0.003, 0.005, 0.008, 0.012, 0.017, 0.023};
//        GBPCADMartingaleParams.setAmountArray(GBPCADAmountArray);
//        mMartingaleParams.put(Instrument.GBPCAD, GBPCADMartingaleParams);
//        RSIParams GBPCADRSIParams = new RSIParams();
//        GBPCADRSIParams.setrSITimePeriod(9);
//        GBPCADRSIParams.setrSIPeriod(Period.FIFTEEN_MINS);
//        rsiParams.put(Instrument.GBPCAD, GBPCADRSIParams);

        // opening indicator
        mOpeningIndicator = new RSIOpeningIndicator(rsiParams);
        // date format
        mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
        puts(" ====================== " + STRATEGY_TAG + " ====================");
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        try {
            IOrder order = message.getOrder();
            IMessage.Type type = message.getType();
            if (order != null && isStrategyInstrument(order.getInstrument())) {
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
            sendEmail("CRASH! " + getClass().getSimpleName(), "OnMessage exception: " + e.getMessage());
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
            sendEmail("CRASH! " + getClass().getSimpleName(), "onTick exception: " + e.getMessage());
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
            sendEmail("CRASH! " + getClass().getSimpleName(), "onBar exception: " + e.getMessage());
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private boolean isStrategyInstrument(Instrument instrument) {
        return mInstrumentSet.contains(instrument);
    }

    private void handleNewPosition(Instrument instrument, ITick tick) throws JFException {
        if (!isOrderProcessing(instrument) && !isHavePosition(instrument)) {
            IEngine.OrderCommand orderCommand = mOpeningIndicator.getOrderCommand(instrument);
            if (orderCommand != null) {
                MartingaleParams martingaleParams = mMartingaleParams.get(instrument);
                if (orderCommand.equals(IEngine.OrderCommand.BUY)) {
                    String orderLabel = getOrderLabel(instrument, IEngine.OrderCommand.BUY, 1, tick.getTime());
                    puts("new position orderLabel: " + orderLabel);
                    trySubmitOrder(orderLabel, instrument, IEngine.OrderCommand.BUY, (martingaleParams.getAmountArray())[0], tick.getBid(), 5);
                } else if (orderCommand.equals(IEngine.OrderCommand.SELL)) {
                    String orderLabel = getOrderLabel(instrument, IEngine.OrderCommand.SELL, 1, tick.getTime());
                    puts("new position orderLabel: " + orderLabel);
                    trySubmitOrder(orderLabel, instrument, IEngine.OrderCommand.SELL, (martingaleParams.getAmountArray())[0], tick.getAsk(), 5);
                }
            }
        }
    }

    private void handleNewPosition(Instrument instrument, IBar askBar, IBar bidBar) throws JFException {
        if (!isOrderProcessing(instrument) && !isHavePosition(instrument)) {
            IEngine.OrderCommand orderCommand = mOpeningIndicator.getOrderCommand(instrument);
            if (orderCommand != null) {
                MartingaleParams martingaleParams = mMartingaleParams.get(instrument);
                if (orderCommand.equals(IEngine.OrderCommand.BUY)) {
                    String orderLabel = getOrderLabel(instrument, IEngine.OrderCommand.BUY, 1, bidBar.getTime());
                    puts("new position orderLabel: " + orderLabel);
                    trySubmitOrder(orderLabel, instrument, IEngine.OrderCommand.BUY, (martingaleParams.getAmountArray())[0], bidBar.getClose(), 5);
                } else if (orderCommand.equals(IEngine.OrderCommand.SELL)) {
                    String orderLabel = getOrderLabel(instrument, IEngine.OrderCommand.SELL, 1, askBar.getTime());
                    puts("new position orderLabel: " + orderLabel);
                    trySubmitOrder(orderLabel, instrument, IEngine.OrderCommand.SELL, (martingaleParams.getAmountArray())[0], askBar.getClose(), 5);
                }
            }
        }
    }

    private void handleAddOrClosePosition(Instrument instrument, ITick tick) throws JFException {
        if (!isOrderProcessing(instrument) && isHavePosition(instrument)) {
            double totalPips = getTotalProfitLossInPips(instrument);
            MartingaleParams martingaleParams = mMartingaleParams.get(instrument);
//        puts("totalPips: " + totalPips);
            // close position
            if (totalPips > martingaleParams.getTpPips()) {
                tryCloseAllOrders(instrument);
            } else {
                tryAddPosition(instrument, tick);
            }
        }
    }


    private void tryAddPosition(Instrument instrument, ITick tick) throws JFException {
        IOrder lastOrder = getLastOrder(instrument);
        IEngine.OrderCommand orderCommand = lastOrder.getOrderCommand();
        int orderSeq = getOrders(instrument).size();
        long timeInterval = tick.getTime() - lastOrder.getCreationTime();
        MartingaleParams martingaleParams = mMartingaleParams.get(instrument);
        if (orderSeq < martingaleParams.getAmountArray().length &&
                checkTimeInterval(timeInterval, orderSeq) &&
                    checkCloseHigher(instrument, orderCommand, orderSeq)) {
            double addAmount = (martingaleParams.getAmountArray())[orderSeq];
            double addPrice;
            if (orderCommand.equals(IEngine.OrderCommand.BUY)) {
                addPrice = lastOrder.getOpenPrice() - martingaleParams.getGridPips(orderSeq) * instrument.getPipValue();
//                puts("tryAddposition, buy, addprice: " + addPrice + ", addAmount: " + addAmount + ", bid: " + tick.getBid());
                if (tick.getBid() < addPrice) {
                    String orderLabel = getOrderLabel(instrument, orderCommand, (orderSeq + 1),
                            tick.getTime());
                    trySubmitOrder(orderLabel, instrument, orderCommand, addAmount, tick.getBid(), 3);
                }
            } else {
                addPrice = lastOrder.getOpenPrice() + martingaleParams.getGridPips(orderSeq) * instrument.getPipValue();
//                puts("tryAddposition, sell, addprice: " + addPrice + ", addAmount: " + addAmount + ", ask: " + tick.getAsk());
                if (tick.getAsk() > addPrice) {
                    String orderLabel = getOrderLabel(instrument, orderCommand, (orderSeq + 1),
                            tick.getTime());
                    trySubmitOrder(orderLabel, instrument, orderCommand, addAmount, tick.getAsk(), 3);
                }
            }
        }
    }

    private void tryCloseAllOrders(Instrument instrument) throws JFException {
        int closingCount = 0;
        for (IOrder o : getOrders(instrument)) {
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

    private void trySubmitOrder(String orderLabel, Instrument instrument, IEngine.OrderCommand orderCommand,
                                double amount, double price, double slippage) throws JFException {
        mEngine.submitOrder(orderLabel, instrument, orderCommand, amount, price, slippage);
        setOrderProcessing(instrument);
    }

    private double getTotalProfitLossInPips(Instrument instrument) throws JFException {
        double pips = 0;
        MartingaleParams martingaleParams = mMartingaleParams.get(instrument);
        for (IOrder o : getOrders(instrument)) {
            pips = pips + o.getProfitLossInPips() * (o.getAmount() / (martingaleParams.getAmountArray())[0]);
        }
        return pips;
    }

    private IOrder getLastOrder(Instrument instrument) throws JFException {
        IOrder order = null;
        for (IOrder o : getOrders(instrument)) {
            if (order == null || o.getCreationTime() > order.getCreationTime()) {
                order = o;
            }
        }
        return order;
    }

    private List<IOrder> getOrders(Instrument instrument) throws JFException {
        List<IOrder> orders = new ArrayList<>();
        for (IOrder o : mEngine.getOrders(instrument)) {
            if (isStrategyOrder(o)) {
                orders.add(o);
            }
        }
        return orders;
    }


    private boolean isStrategyOrder(IOrder order) {
        return order.getLabel().startsWith(STRATEGY_TAG);
    }

    private boolean isHavePosition(Instrument instrument) throws JFException {
        return getOrders(instrument).size() > 0;
    }

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

    private void handleOrderCanceled(Instrument instrument) {
        removeOrderProcessing(instrument);
    }

    private void handleOrderClosed(Instrument instrument) {
        int closingCount = mOrderClosingMap.get(instrument);
        closingCount = closingCount - 1;
        mOrderClosingMap.put(instrument, closingCount);
        if (closingCount <= 0) {
            removeOrderProcessing(instrument);
//            sendEmail("NOTICE! " + instrument + " Order Closed!", "");
        }
    }

    private void handleOrderCloseRejected(Instrument instrument) {
        removeOrderProcessing(instrument);
        puts("Order Close Rejected, cInstrument: " + instrument);
        sendEmail("ERROR! " + getClass().getSimpleName(), "Order Close Rejected, cInstrument: " + instrument);
    }

    private void handleOrderFilled(Instrument instrument) throws JFException {
        removeOrderProcessing(instrument);
        int orderSeq = getOrders(instrument).size();
        MartingaleParams martingaleParams = mMartingaleParams.get(instrument);
        int maxOrderCount = (martingaleParams.getAmountArray()).length - 1;
        if (orderSeq >= maxOrderCount) {
            sendEmail("WARNING!!! MAX ORDER COUNT",
                    instrument + " order count: " + orderSeq);
        }

    }

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

    private boolean checkTimeInterval(long timeInterval, int orderSeq) {
        if (orderSeq < 4) {
            return true;
        }
        return timeInterval >= mOrderIntervalSecond;
    }

    private boolean checkCloseHigher(Instrument instrument, IEngine.OrderCommand orderCommand, int orderSeq) {
        if (orderSeq < 2) {
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

    private String getOrderLabel(Instrument instrument, IEngine.OrderCommand orderCommand, int orderSeq, long time) {
        return STRATEGY_TAG + "_" + GROUP_ID + "_" + instrument.toString().replace('/', '_') + "_" + orderCommand + "_" +
                orderSeq + "_" + mSimpleDateFormat.format(new Date(time)) + "_" + time;
    }


    private int getOrderSeqFromLabel(IOrder order) {
        String[] splited = order.getLabel().split("_");
        return Integer.valueOf(splited[4]);
    }

//    private boolean isStrategyOrder(IOrder order) {
//        if (order.getLabel().startsWith(STRATEGY_TAG)) {
//            return true;
//        }
//        return false;
//    }

//    private boolean isNoTradingTime(long time) {
//        Date date = new Date(time);
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeZone(TimeZone.getTimeZone("GTM+8:00"));
//        cal.setTime(date);
//        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
//        if (w == 6) {
//            return true;
//        }
//        return false;
//    }

    private void puts(String str) {
//        mConsole.getInfo().println(str);
        StrategyRunner.LOGGER.info(str);
    }

    private void sendEmail(String subject, String body) {
        MailService.sendMail(subject, body);
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

    public class MartingaleParams {

        private int tpPips = 15;

        private double[] mAmountArray = {0.003, 0.005, 0.008, 0.012, 0.017, 0.023, 0.030, 0.040};

        private int[] mGridPipsArray = {0, 20, 20, 20, 30, 40, 50, 60};

        public double[] getAmountArray() {
            return mAmountArray;
        }

        public void setAmountArray(double[] amountArray) {
            this.mAmountArray = amountArray;
        }

        public int getTpPips() {
            return tpPips;
        }

        public void setTpPips(int tpPips) {
            this.tpPips = tpPips;
        }

        public int getGridPips(int orderSeq) {
            return mGridPipsArray[orderSeq];
        }

        public void setGridPipsArray(int[] mGridPipsArray) {
            this.mGridPipsArray = mGridPipsArray;
        }
    }

}