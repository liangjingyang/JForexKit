package com.jforexcn.inbox.indicator;

import com.dukascopy.api.Filter;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.IntegerListDescription;
import com.dukascopy.api.indicators.IntegerRangeDescription;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;
import com.dukascopy.api.indicators.PeriodListDescription;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 联系方式 : QQ190353986
 * 版权属于：ridgejiang（旺旺） 190353986@qq.com
 *
 * Version 1.0
 * 完成日期 : 2017.08.10
 * 升级修改日志：
 * 2017.08.10
 * 将“波动”外置出单独的周期设置，不再固定采用D1，也不和ATR共用一个周期。
 */
public class ATR_New implements IIndicator {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private IBar[][] inputs1 = new IBar[3][];
    private double[][][] inputs2 = new double[1][][];
    private double[][] outputs = new double[3][];

    private InputParameterInfo dailyInput11;
    private final List<Period> myPeriods = new ArrayList<>();
    private Period selectedDailyPeriod;
    private IIndicator atr;
    private InputParameterInfo atrInput12;
    private InputParameterInfo atrInput2;
    private Period selectedAtrPeriod;
    private IIndicatorContext indicatorContext;
    private IHistory history;

    @Override
    public void onStart(IIndicatorContext context) {
        this.history = context.getHistory();
        this.indicatorContext = context;

        int[] offerSideOrdinals = new int[OfferSide.values().length];
        String[] offerSideNames = new String[OfferSide.values().length];
        for (int i = 0; i < offerSideOrdinals.length; i++) {
            offerSideOrdinals[i] = i;
            offerSideNames[i] = OfferSide.values()[i].name();
        }
        int[] filterOrdinals = new int[Filter.values().length];
        String[] filterNames = new String[Filter.values().length];
        for (int i = 0; i < filterOrdinals.length; i++) {
            filterOrdinals[i] = i;
            filterNames[i] = Filter.values()[i].name();
        }

        for (Period p : Period.values()) {
            if (p.equals(Period.TICK)
                    || p.equals(Period.ONE_YEAR)) {
                continue;
            }
            myPeriods.add(p);
        }

        selectedDailyPeriod = Period.DAILY;
        selectedAtrPeriod = Period.DAILY;
        atr = context.getIndicatorsProvider().getIndicator("ATR");

        dailyInput11 = new InputParameterInfo("Daily Bar", InputParameterInfo.Type.BAR);
        dailyInput11.setPeriod(selectedDailyPeriod);
        atrInput12 = new InputParameterInfo("ATR Bar", InputParameterInfo.Type.BAR);
        atrInput12.setPeriod(selectedAtrPeriod);
        atrInput2 = new InputParameterInfo("ATR Price", InputParameterInfo.Type.PRICE);
        atrInput2.setPeriod(selectedAtrPeriod);

        indicatorInfo = new IndicatorInfo("VOLATILITYATRPERCENT_NEW", "Volatility ATR Percent"
                , "Custom indicators", false, false, true, 4, 5, 3);

        inputParameterInfos = new InputParameterInfo[] {
                new InputParameterInfo("Main Bar", InputParameterInfo.Type.BAR),
                dailyInput11,
                atrInput12,
                atrInput2
        };

        optInputParameterInfos = new OptInputParameterInfo[] {
                new OptInputParameterInfo("图表价格", OptInputParameterInfo.Type.OTHER
                        , new IntegerListDescription(OfferSide.BID.ordinal(), offerSideOrdinals, offerSideNames)),
                new OptInputParameterInfo("图表过滤", OptInputParameterInfo.Type.OTHER
                        , new IntegerListDescription(Filter.WEEKENDS.ordinal(), filterOrdinals, filterNames)),
                new OptInputParameterInfo("波动 Period", OptInputParameterInfo.Type.OTHER
                        , new PeriodListDescription(Period.DAILY
                        , myPeriods.toArray(new Period[myPeriods.size()]))),
                new OptInputParameterInfo("ATR Period", OptInputParameterInfo.Type.OTHER
                        , new PeriodListDescription(Period.DAILY
                        , myPeriods.toArray(new Period[myPeriods.size()]))),
                new OptInputParameterInfo("ATR Time Period", OptInputParameterInfo.Type.OTHER
                        , new IntegerRangeDescription(14, 1, 2000, 1))
        };

        outputParameterInfos = new OutputParameterInfo[] {
                new OutputParameterInfo("Current Volatility", OutputParameterInfo.Type.DOUBLE
                        , OutputParameterInfo.DrawingStyle.LINE),
                new OutputParameterInfo("Previous ATR", OutputParameterInfo.Type.DOUBLE
                        , OutputParameterInfo.DrawingStyle.LINE),
                new OutputParameterInfo("Percent(%)", OutputParameterInfo.Type.DOUBLE
                        , OutputParameterInfo.DrawingStyle.LINE)
        };
    }

    @Override
    public IndicatorInfo getIndicatorInfo() {
        return indicatorInfo;
    }

    @Override
    public InputParameterInfo getInputParameterInfo(int index) {
        if (index <= inputParameterInfos.length) {
            return inputParameterInfos[index];
        }
        return null;
    }

    @Override
    public OptInputParameterInfo getOptInputParameterInfo(int index) {
        if (index <= optInputParameterInfos.length) {
            return optInputParameterInfos[index];
        }
        return null;
    }

    @Override
    public OutputParameterInfo getOutputParameterInfo(int index) {
        if (index <= outputParameterInfos.length) {
            return outputParameterInfos[index];
        }
        return null;
    }

    @Override
    public void setInputParameter(int index, Object array) {
        switch (index) {
            case 0:
            {
                inputs1[0] = (IBar[]) array;
                break;
            }
            case 1:
            {
                inputs1[1] = (IBar[]) array;
                break;
            }
            case 2:
            {
                inputs1[2] = (IBar[]) array;
                break;
            }
            case 3:
            {
                inputs2[0] = (double[][]) array;
                break;
            }
            default:
            {
                throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    @Override
    public void setOptInputParameter(int index, Object value) {
        switch (index) {
            case 0:
            {
                OfferSide offerSide = OfferSide.values()[(int) value];
                dailyInput11.setOfferSide(offerSide);
                atrInput12.setOfferSide(offerSide);
                atrInput2.setOfferSide(offerSide);
                break;
            }
            case 1:
            {
                Filter filter = Filter.values()[(int) value];
                dailyInput11.setFilter(filter);
                atrInput12.setFilter(filter);
                atrInput2.setFilter(filter);
                break;
            }
            case 2:
            {
                Period period = (Period) value;
                if (!myPeriods.contains(period)) {
                    throw new IllegalArgumentException("Period not supported");
                }
                dailyInput11.setPeriod(period);
                selectedDailyPeriod = period;
                break;
            }
            case 3:
            {
                Period period = (Period) value;
                if (!myPeriods.contains(period)) {
                    throw new IllegalArgumentException("Period not supported");
                }
                atrInput12.setPeriod(period);
                atrInput2.setPeriod(period);
                selectedAtrPeriod = period;
                break;
            }
            case 4:
            {
                int period = (int) value;
                atr.setOptInputParameter(0, period);
                break;
            }
            default:
            {
                throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    @Override
    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }

    @Override
    public int getLookback() {
        return 0;
    }

    @Override
    public int getLookforward() {
        return 0;
    }

    @Override
    public IndicatorResult calculate(int startIndex, int endIndex) {
        if (startIndex - getLookback() < 0) {
            startIndex -= startIndex - getLookback();
        }
        if (startIndex > endIndex) {
            return new IndicatorResult(0, 0);
        }

        int atrLookback = atr.getLookback();
        int atrLength = inputs2[0][0].length - atrLookback;
        if (atrLength <= 0) {
            return new IndicatorResult(startIndex, endIndex - startIndex + 1);
        }
        double[] arrayAtr = new double[atrLength];
        atr.setInputParameter(0, inputs2[0]);
        atr.setOutputParameter(0, arrayAtr);
        atr.calculate(0, inputs2[0][0].length - 1);

        int i, j;
        for (i = startIndex, j = 0; i <= endIndex; i++, j++) {
            boolean getVolatility = false;
            int timeIndexDaily = getTimeIndex(inputs1[0][i].getTime(), inputs1[1], selectedDailyPeriod);
            if (timeIndexDaily > -1) {
                if (timeIndexDaily >= inputs1[1].length) {
                    return new IndicatorResult(startIndex, endIndex - startIndex + 1);
                }
                outputs[0][j] = inputs1[1][timeIndexDaily].getHigh()
                        - inputs1[1][timeIndexDaily].getLow();
                getVolatility = true;
            }

            boolean getPreviousAtr = false;
            int timeIndexAtr = getTimeIndex(inputs1[0][i].getTime(), inputs1[2], selectedAtrPeriod);
            if (timeIndexAtr > atrLookback) {
                int index = timeIndexAtr - atrLookback - 1;
                if (index >= arrayAtr.length) {
                    return new IndicatorResult(startIndex, endIndex - startIndex + 1);
                }
                outputs[1][j] = arrayAtr[index];
                getPreviousAtr = true;
            }

            if (getVolatility && getPreviousAtr) {
                outputs[2][j] = (outputs[0][j] / outputs[1][j]) * 100.0;
            }
        }

        return new IndicatorResult(startIndex, j);
    }

    public int getTimeIndex(long time, IBar[] source, Period period) {
        if ((source == null) || (source.length <= 0)) {
            return -1;
        }

        int length = source.length;
        if (time >= source[length - 1].getTime()) {
            return length - 1;
        }

        long barTime;
        try {
            barTime = history.getBarStart(period, time);
        } catch (JFException e) {
            indicatorContext.getConsole().getWarn().println("Failed to get start time of bar - " + e);
            return -1;
        }

        int curIndex = 0;
        int upto = source.length;

        while (curIndex < upto) {

            int midIndex = (curIndex + upto) / 2;

            IBar midBar = source[midIndex];

            if (midBar.getTime() == barTime) {
                return midIndex;
            } else if (barTime < midBar.getTime()) {
                upto = midIndex;
            }
            else if (barTime > midBar.getTime()) {
                curIndex = midIndex + 1;
            }
        }

        return curIndex;
    }

}

