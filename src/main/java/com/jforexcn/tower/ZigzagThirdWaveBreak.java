package com.jforexcn.tower;

/**
 * Created by simple on 3/9/2018.
 */

import SevenZip.Compression.LZMA.Base;
import com.dukascopy.api.*;
import com.jforexcn.tower.Util.BaseHelper;

import java.util.List;

/**
 * 完整策略
 * 1. 做3浪
 * 2. 1浪满足长度 N
 * 3. 2浪小于一浪的 M%, 并且小于 P 点
 * 4, 1浪斜率 > 2浪
 * 5. 3浪突破1浪高点
 * 6. 3浪斜率 > 2浪 * X
 * 7. 初始止损2浪的 Y%
 * 8. 移动止损 Z
 * 9. 止盈2浪的100%
 */

@RequiresFullAccess
@Library("/data/github/JForexKit/build/libs/JForexKit-3.0.jar")
public class ZigzagThirdWaveBreak extends BaseStrategy {

    @Configurable(value = "Amount in million")
    public double cAmount = 0.01;
    @Configurable(value = "Period")
    public Period cPeriod = Period.ONE_MIN;
    @Configurable(value = "FirstWavePips")
    public int cFirstWaveMinPips = 5;
    @Configurable(value = "SecondWaveFactor")
    public double cSecondToFirstLengthFactor = 0.3;
    @Configurable(value = "SecondWaveMaxPips")
    public int cSecondWaveMaxPips = 20;
    @Configurable(value = "SecondWaveStrongFactor")
    public double cSecondToFirstStrongFactor = 2;
    @Configurable(value = "ThirdWaveStrongFactor")
    public double cSecondToThirdStrongFactor = 2;
    @Configurable(value = "cInitSLFactor")
    public double cInitSLFactor = 0.5;
    @Configurable(value = "cInitTPFactor")
    public double cInitTPFactor = 1;

    private final int lookback = 400;
    private Wave wave = new Wave();

    @Override
    public void onStart(IContext context) throws JFException {
        super.onStart(context);
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        super.onTick(instrument, tick);
        if (isStrategyInstrument(instrument)) {
            if (wave.updateTick(tick)) {
                printDebug();
                if (fit()) {
                    List<IOrder> orders = orderHelper.getStrategyOrdersByInstrument(instrument);
                    if (orders.size() <= 0) {
                        double openPrice = 0, initSL = 0, initTP = 0;
                        IEngine.OrderCommand command;
                        if (wave.c > wave.b) { // 3th wave break down,
                            command = IEngine.OrderCommand.SELL;
                            openPrice = tick.getBid();
                            initSL = wave.d - (wave.d - wave.c) * cInitSLFactor;
                            initSL = BaseHelper.scalePrice(initSL, instrument.getPipScale());
                            initTP = wave.d + (wave.d - wave.c) * cInitTPFactor;
                            initTP = BaseHelper.scalePrice(initTP, instrument.getPipScale());
                            if (openPrice < wave.b) {
                                orderHelper.submitOrder(getLabel(1), instrument, command, cAmount,
                                        openPrice, 1, initSL, initTP, tick.getTime());
                            }
                        } else {
                            command = IEngine.OrderCommand.BUY;
                            openPrice = tick.getAsk();
                            initSL = wave.d - (wave.d - wave.c) * cInitSLFactor;
                            initSL = BaseHelper.scalePrice(initSL, instrument.getPipScale());
                            initTP = wave.d + (wave.d - wave.c) * cInitTPFactor;
                            initTP = BaseHelper.scalePrice(initTP, instrument.getPipScale());
                            if (openPrice > wave.b) {
                                orderHelper.submitOrder(getLabel(0), instrument, command, cAmount,
                                        openPrice, 1, initSL, initTP, tick.getTime());
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        super.onBar(instrument, period, askBar, bidBar);
        if (cPeriod.equals(period) && isStrategyInstrument(instrument)) {
            long time = askBar.getTime();
            Object[] zigzagResult = calculateZigzag(time);
            double[] zigzagValues = (double[]) zigzagResult[0];
            double[] zigzagDistances = (double[]) zigzagResult[1];
            wave.updateZigzag(zigzagValues, zigzagDistances);
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

    public void printDebug() {
        logDebug(wave.toString());
        logDebug("fit: " + fit());
    }

    private Object[] calculateZigzag(long time) throws JFException {
        OfferSide[] offerSides = new OfferSide[] { OfferSide.ASK };
        IIndicators.AppliedPrice[] appliedPrices = new IIndicators.AppliedPrice[] {};
        Object[] optParams = new Object[] { 12, 5, 3 };
        Object[] result = indicators.calculateIndicator(instrument, cPeriod, offerSides, "Zigzag", appliedPrices,
                optParams, Filter.ALL_FLATS, lookback, time, 0);
        return result;
    }

    private boolean fit() {
        /**
         * 2. 1浪满足长度 N
         * 3. 2浪小于一浪的 M%, 并且小于 P 点
         * 4, 1浪斜率 > 2浪
         * 6. 3浪斜率 > 2浪 * X
         * */
        logDebug("" + (wave.a > 0) + (wave.d > 0) + (wave.ab >= cFirstWaveMinPips) + (wave.bc <= cSecondWaveMaxPips) +
                (wave.bc <= wave.ab * cSecondToFirstLengthFactor) + (wave.bcStrong <= wave.abStrong * cSecondToFirstStrongFactor) +
                (wave.bcStrong <= wave.cdStrong * cSecondToThirdStrongFactor));
        return wave.a > 0 &&
                wave.d > 0 && // this 2 statement means the wave update completed
                wave.ab >= cFirstWaveMinPips &&
                wave.bc <= cSecondWaveMaxPips &&
                wave.bc <= wave.ab * cSecondToFirstLengthFactor &&
                wave.bcStrong <= wave.abStrong * cSecondToFirstStrongFactor &&
                wave.bcStrong <= wave.cdStrong * cSecondToThirdStrongFactor;
    }

    private class Wave {
        private double a;
        private double b;
        private double c;
        private double d;
        private double ab;
        private double bc;
        private double cd;
        private int abBars;
        private int bcBars;
        private int cdBars;
        private double abStrong;
        private double bcStrong;
        private double cdStrong;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("a:").append(a).append(" ");
            sb.append("b:").append(b).append(" ");
            sb.append("c:").append(c).append(" ");
            sb.append("d:").append(d).append(" ");
            sb.append("ab:").append(ab).append(" ");
            sb.append("bc:").append(bc).append(" ");
            sb.append("cd:").append(cd).append(" ");
            sb.append("\n");
            sb.append("abBars:").append(abBars).append(" ");
            sb.append("bcBars:").append(bcBars).append(" ");
            sb.append("cdBars:").append(cdBars).append(" ");
            sb.append("abStrong:").append(abStrong).append(" ");
            sb.append("bcStrong:").append(bcStrong).append(" ");
            sb.append("cdStrong:").append(cdStrong).append(" ");
            return sb.toString();
        }

        private boolean updateTick(ITick tick) {
            if (c > b) { // 3th wave break down,
                d = tick.getAsk();
            } else {
                d = tick.getAsk();
            }

            cd = Math.abs(d - c) / instrument.getPipValue();
            cdStrong = cd / cdBars;
            return (a > 0 && d > 0);
        }

        private void updateZigzag(double[] zigzagValues, double[] zigzagDistances) {
            a = 0;
            b = 0;
            c = 0;
            d = 0;
            int dIndex = 0, cIndex = 0, bIndex = 0, aIndex = 0;
            for (int i = zigzagValues.length - 1; i >= 0; i--) {
                if (i == zigzagValues.length - 1) {
                    dIndex = i;
                    continue;
                }
                if (Double.isNaN(zigzagValues[i])) {
                    continue;
                }
                if (c == 0.0) {
                    c = zigzagValues[i];
                    bc = Math.abs(zigzagDistances[i]);
                    cIndex = i;
                } else if (b == 0.0) {
                    b = zigzagValues[i];
                    ab = Math.abs(zigzagDistances[i]);
                    bIndex = i;
                } else {
                    a = zigzagValues[i];
                    aIndex = i;
                    break;
                }
            }

            cdBars = dIndex - cIndex;
            bcBars = cIndex - bIndex;
            abBars = bIndex - aIndex;

            abStrong = ab / abBars;
            bcStrong = bc / bcBars;
        }
    }
}