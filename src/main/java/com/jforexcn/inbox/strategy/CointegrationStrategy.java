package com.jforexcn.inbox.strategy;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by simple on 18/10/2017.
 */

public class CointegrationStrategy implements IStrategy {

    @Configurable("Instrument Y")
    public Instrument cInstrumentY = Instrument.EURUSD;
    @Configurable("Instrument X")
    public Instrument cInstrumentX = Instrument.USDCHF;
    @Configurable("Period")
    public Period cPeriod = Period.ONE_MIN;
    @Configurable("History Bar Count")
    public int cHistoryBarCount = 7 * 24 * 60;
    @Configurable("Amount")
    public double cAmount = 0.001;


    private IEngine mEngine;
    private IConsole mConsole;
    private IIndicators mIndicators;
    private IHistory mHistory;
    private String STRATEGY_TAG = "CointegrationStrategy" +
            cInstrumentY.getPrimaryJFCurrency() +
            cInstrumentY.getSecondaryJFCurrency() +
            cInstrumentX.getPrimaryJFCurrency() +
            cInstrumentX.getSecondaryJFCurrency() +
            cPeriod.getNumOfUnits() +
            cPeriod.getUnit().getCompactDescription();

    private PairStatus mCalculated = new PairStatus((Object) cInstrumentY, (Object) cInstrumentX);
    private Helper h = new Helper();

    private RegressionLine mLine;
    private HashMap<Instrument, IBar> mPairBars = new HashMap<>(2);

    @Override
    public void onStart(IContext context) throws JFException {
        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mIndicators = context.getIndicators();
        this.mHistory = context.getHistory();

        // subscribe instruments
        h.instrumentSet.add(cInstrumentY);
        h.instrumentSet.add(cInstrumentX);
        context.setSubscribedInstruments(h.instrumentSet, true);
    }

    private void calc(long now) throws JFException {
        long barStart = mHistory.getBarStart(Period.ONE_MIN, now - 60000);
        List<IBar> yBars = mHistory.getBars(cInstrumentY, cPeriod, OfferSide.ASK, Filter.WEEKENDS, cHistoryBarCount, barStart, 0);
        List<IBar> xBars = mHistory.getBars(cInstrumentX, cPeriod, OfferSide.ASK, Filter.WEEKENDS, cHistoryBarCount, barStart, 0);

        mLine = new RegressionLine();
        int count = yBars.size();

        for (int i = 0; i < count; i++) {
            mLine.addDataPoint(xBars.get(i).getClose(), yBars.get(i).getClose());
        }

        mLine.getA1();
        mLine.getA0();
        mLine.getA0Lower();
        mLine.getA0Upper();

        mCalculated.put(cInstrumentY, true);
        mCalculated.put(cInstrumentX, true);
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        if (mCalculated.allFalse()) {
            calc(tick.getTime());
        }
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (h.isStrategyInstrument(instrument)) {
            if (mCalculated.allTrue() && period.equals(cPeriod)) {
                mPairBars.put(instrument, askBar);
                if (checkBarTime(askBar)) {
                    handleOrder();
                }
            } else if (period.equals(Period.DAILY)) {
                mCalculated.put(instrument, false);
            }
        }
    }

    private void handleOrder() throws JFException {
        IBar barY = mPairBars.get(cInstrumentY);
        IBar barX = mPairBars.get(cInstrumentX);
        double a1 = mLine.getA1();
        double a0 = mLine.getA0();
        double a0Lower = mLine.getA0Lower();
        double a0Upper = mLine.getA0Upper();
        double a00 = barY.getClose() - a1 * barX.getClose();

        HashMap<Instrument, IOrder> orderMap = getStrategyOrders();
        if (orderMap.size() == 2) {
            if (a1 < 0) {
                // 负相关, 同BUY, 同SELL
                if (orderMap.get(cInstrumentY).getOrderCommand().equals(IEngine.OrderCommand.BUY) && a00 > (a0 + (a0Upper - a0) / 3)) {
                    // BUY under lower, close at mean and 1/3 upper
                    for (IOrder order : orderMap.values()) {
                        h.close(order);
                    }
                } else if (orderMap.get(cInstrumentY).getOrderCommand().equals(IEngine.OrderCommand.SELL) && a00 < (a0 - (a0 - a0Lower) / 3)) {
                    // SELL over upper, close at mean and 1/3 lower
                    for (IOrder order : orderMap.values()) {
                        h.close(order);
                    }
                }
            } else {
                // 正相关, 方向相反
            }
        } else if (orderMap.size() == 0) {
            if (a1 < 0) {
                // 负相关, 同BUY, 同SELL
                String labelY = STRATEGY_TAG + "_" + cInstrumentY.getPrimaryJFCurrency() + cInstrumentY.getSecondaryJFCurrency() + "_" + barY.getTime();
                String labelX = STRATEGY_TAG + "_" + cInstrumentX.getPrimaryJFCurrency() + cInstrumentX.getSecondaryJFCurrency() + "_" + barY.getTime();
                if (a00 < a0Lower) {
                    h.submitOrder(labelY, cInstrumentY, IEngine.OrderCommand.BUY, cAmount, 0, 3, barY.getTime());
                    h.submitOrder(labelX, cInstrumentX, IEngine.OrderCommand.BUY, cAmount, 0, 3, barY.getTime());
                } else if (a00 > a0Upper) {
                    h.submitOrder(labelY, cInstrumentY, IEngine.OrderCommand.SELL, cAmount, 0, 3, barY.getTime());
                    h.submitOrder(labelX, cInstrumentX, IEngine.OrderCommand.SELL, cAmount, 0, 3, barY.getTime());
                }
            } else {
                // 正相关, 方向相反
            }
        }
    }

    private HashMap<Instrument, IOrder> getStrategyOrders() throws JFException {
        HashMap<Instrument, IOrder> orders = new HashMap<Instrument, IOrder>(2);
        for (IOrder order : mEngine.getOrders()) {
            if (order.getLabel().startsWith(STRATEGY_TAG)) {
                orders.put(order.getInstrument(), order);
            }
        }
        return orders;
    }

    private boolean checkBarTime(IBar bar) {
        IBar barY = mPairBars.get(cInstrumentY);
        IBar barX = mPairBars.get(cInstrumentX);
        if (barY == null || barX == null) {
            return false;
        }
        return barY.getTime() == bar.getTime() &&
                barX.getTime() == bar.getTime();
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder order = message.getOrder();

        if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {

        } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {
            if (order != null) {
                order.close();
            }
        } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {

        }
    }

    @Override
    public void onStop() throws JFException {
    }


    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private class PairStatus {
        private final HashMap<Object, Boolean> pair;

        public PairStatus(Object x, Object y) {
            pair = new HashMap<>(2);
            pair.put(x, false);
            pair.put(y, false);
        }

        public void put(Object o, Boolean b) {
            pair.put(o, b);
        }

        public boolean get(Object o) {
            return pair.get(o);
        }

        public boolean allTrue() {
            Object[] values = pair.values().toArray();
            return ((boolean) values[0]) && ((boolean) values[1]);
        }
        public boolean allFalse() {
            Object[] values = pair.values().toArray();
            return !((boolean) values[0]) && !((boolean) values[1]);
        }
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

        public Helper() {

        }


        private void puts(String str) {
            mConsole.getInfo().println(str);
//            StrategyRunner.LOGGER.info(str);
        }

        private void sendEmail(String subject, String body) {
//            MailService.sendMail(subject, body);
        }

        private boolean isStrategyInstrument(Instrument instrument) {
            return instrumentSet.contains(instrument);
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
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            if (canOrderProcessing(order, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), "setStopLossPrice", now);
                insertOrderProcessing(order.getId(), orderProcessing);
                order.setStopLossPrice(price);
            }
        }

        private void setStopLossPrice(IOrder order, double price, OfferSide side) throws JFException {
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            if (canOrderProcessing(order, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), "setStopLossPrice", now);
                insertOrderProcessing(order.getId(), orderProcessing);
                order.setStopLossPrice(price, side);
            }
        }

        private void close(IOrder order, double amount, double price, double slippage) throws JFException {
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            if (canOrderProcessing(order, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), "close", now);
                insertOrderProcessing(order.getId(), orderProcessing);
                order.close(amount, price, slippage);
            }
        }

        private void close(IOrder order, double amount, double price) throws JFException {
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            if (canOrderProcessing(order, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), "close", now);
                insertOrderProcessing(order.getId(), orderProcessing);
                order.close(amount, price);
            }
        }

        private void close(IOrder order, double amount) throws JFException {
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            if (canOrderProcessing(order, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), "close", now);
                insertOrderProcessing(order.getId(), orderProcessing);
                order.close(amount);
            }
        }

        private void close(IOrder order) throws JFException {
            long now = mHistory.getTimeOfLastTick(order.getInstrument());
            if (canOrderProcessing(order, now)) {
                Helper.OrderProcessing orderProcessing = new Helper.OrderProcessing(order.getId(), "close", now);
                insertOrderProcessing(order.getId(), orderProcessing);
                order.close();
            }
        }

        private boolean canOrderProcessing(IOrder order, long time) {
            if (orderProcessingHashMap.containsKey(order.getId())) {
                if (orderProcessingHashMap.get(order.getId()).isExpired(time)) {
                    removeOrderProcessing(order.getId());
                } else {
                    return false;
                }
            }
            return true;
        }

        private void insertOrderProcessing(String orderId, Helper.OrderProcessing orderProcessing) {
            orderProcessingHashMap.put(orderId, orderProcessing);
        }

        private void removeOrderProcessing(String orderId) {
            orderProcessingHashMap.remove(orderId);
        }

        private class OrderProcessing {
            private final String orderId;
            private final long time;
            private final String action;
            private final int expireTime = 30000;

            public OrderProcessing(String orderId, String action, long time) {
                this.orderId = orderId;
                this.action = action;
                this.time = time;
            }

            public boolean isExpired(long time) {
                if (time - this.time > expireTime) {
                    return true;
                }
                return false;
            }
        }
    }


    /**
     * Helper Methods END ==========================================================================
     */


    public class RegressionLine
    {
        /** sum of x */
        private double sumX;

        /** sum of y */
        private double sumY;

        /** sum of x*x */
        private double sumXX;

        /** sum of x*y */
        private double sumXY;

        /** sum of y*y */
        private double sumYY;

        /** sum of yi-y */
        private double sumDeltaY;

        /** sum of sumDeltaY^2 */
        private double sumDeltaY2;

        /** 误差 */
        private double sse;

        private double sst;

        private double E;

        private ArrayList<Double> listX;

        private ArrayList<Double> listY;

        private double XMin, XMax, YMin, YMax;

        /** line coefficient a0 */
        private double a0;

        /** line coefficient a1 */
        private double a1;

        /** number of data points */
        private int pn;

        /** true if coefficients valid */
        private boolean coefsValid;

        private boolean confidenceIntervalValid;

        private double confidenceLevel = 0.95;

        private double lower = 0.0;

        private double upper = 0.0;

        /**
         * Constructor.
         */
        public RegressionLine() {
            XMax = 0;
            YMax = 0;
            pn = 0;
            listX = new ArrayList<>();
            listY = new ArrayList<>();
        }

        /**
         * Return the current number of data points.
         *
         * @return the count
         */
        public int getDataPointCount() {
            return pn;
        }

        /**
         * Return the coefficient a0.
         *
         * @return the value of a0
         */
        public double getA0() {
            validateCoefficients();
            return a0;
        }

        /**
         * Return the coefficient a1.
         *
         * @return the value of a1
         */
        public double getA1() {
            validateCoefficients();
            return a1;
        }

        /**
         * Return the sum of the x values.
         *
         * @return the sum
         */
        public double getSumX() {
            return sumX;
        }

        /**
         * Return the sum of the y values.
         *
         * @return the sum
         */
        public double getSumY() {
            return sumY;
        }

        /**
         * Return the sum of the x*x values.
         *
         * @return the sum
         */
        public double getSumXX() {
            return sumXX;
        }

        /**
         * Return the sum of the x*y values.
         *
         * @return the sum
         */
        public double getSumXY() {
            return sumXY;
        }

        public double getSumYY() {
            return sumYY;
        }

        public double getXMin() {
            return XMin;
        }

        public double getXMax() {
            return XMax;
        }

        public double getYMin() {
            return YMin;
        }

        public double getYMax() {
            return YMax;
        }

        public void setConfidenceLevel(double confidenceLevel) {
            this.confidenceLevel = confidenceLevel;
            this.confidenceIntervalValid = false;
        }

        public double getA0Lower() {
            validateConfidenceInterval();
            return lower;
        }

        public double getA0Upper() {
            validateConfidenceInterval();
            return upper;
        }

        /**
         * Add a new data point: Update the sums.
         *
         * @param  x,y
         *            the new data point
         */
        public void addDataPoint(double x, double y) {
            sumX += x;
            sumY += y;
            sumXX += x * x;
            sumXY += x * y;
            sumYY += y * y;

            if (x > XMax) {
                XMax = x;
            }
            if (y > YMax) {
                YMax = y;
            }

            // 把每个点的具体坐标存入ArrayList中，备用
            if (x != 0 && y != 0) {
                try {
                    listX.add(pn, x);
                    listY.add(pn, y);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ++pn;
            coefsValid = false;
        }

        /**
         * Return the value of the regression line function at x. (Implementation of
         * Evaluatable.)
         *
         * @param x
         *            the value of x
         * @return the value of the function at x
         */
        public double at(double x) {
            if (pn < 2)
                return Float.NaN;

            validateCoefficients();
            return a0 + a1 * x;
        }

        /**
         * Reset.
         */
        public void reset() {
            pn = 0;
            sumX = sumY = sumXX = sumXY = 0;
            coefsValid = false;
        }

        /**
         * Validate the coefficients. 计算方程系数 y=ax+b 中的a
         */
        private void validateCoefficients() {
            if (coefsValid)
                return;

            if (pn >= 2) {
                double xBar = sumX / pn;
                double yBar = sumY / pn;

                a1 = (pn * sumXY - sumX * sumY) / (pn * sumXX - sumX
                        * sumX);
                a0 = yBar - a1 * xBar;
            } else {
                a0 = a1 = Double.NaN;
            }

            coefsValid = true;
        }

        private void validateConfidenceInterval() {
            if (confidenceIntervalValid) {
                return;
            }
            ArrayList<Double> a0List = new ArrayList<>();
            int count = (int) (listX.size() * (1 - confidenceLevel));
            for (int i = 0; i < listX.size(); i++) {
                a0List.add(i, listY.get(i) - getA1() * listX.get(i));
            }
            Collections.sort(a0List);
            lower = a0List.get(count);
            upper = a0List.get(listX.size() - count);
            System.out.println("lower: " + lower);
            System.out.println("upper: " + upper);

            confidenceIntervalValid = true;
        }


        /**
         * 返回误差
         */
        public double getR() {
            // 遍历这个list并计算分母
            for (int i = 0; i < pn - 1; i++) {
                double Yi = listY.get(i);
                double Y = at(listX.get(i));
                double deltaY = Yi - Y;
                double deltaY2 = deltaY * deltaY;

                sumDeltaY2 += deltaY2;

            }

            sst = sumYY - (sumY * sumY) / pn;
            // System.out.println("sst:" + sst);
            E = 1 - sumDeltaY2 / sst;

            return round(E, 4);
        }

        // 用于实现精确的四舍五入
        private double round(double v, int scale) {

            if (scale < 0) {
                throw new IllegalArgumentException(
                        "The scale must be a positive integer or zero");
            }

            BigDecimal b = new BigDecimal(Double.toString(v));
            BigDecimal one = new BigDecimal("1");
            return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
        }

    }

}
