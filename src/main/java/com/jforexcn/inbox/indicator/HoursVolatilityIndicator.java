package com.jforexcn.inbox.indicator;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.indicators.BooleanOptInputDescription;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IIndicatorsProvider;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.IntegerListDescription;
import com.dukascopy.api.indicators.IntegerRangeDescription;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;

import java.util.Calendar;

/**
 * Created by simple(simple.continue@gmail.com) on 24/06/2018.
 */

public class HoursVolatilityIndicator implements IIndicator {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private IBar[][] inputs = new IBar[1][];
    private double[][] outputs = new double[2][];
    private IConsole console;

    private boolean debug = false;
    private IFeedDescriptor feedDescriptor;
    private Calendar calendar = Calendar.getInstance();
    private int startHour = 0;
    private int endHour = 23;
    private int startHour2 = 0;
    private int endHour2 = 23;
    private boolean resetByMonth = false;
    private IBar lastBar;

    private int maType = IIndicators.MaType.SMA.ordinal();
    private int timePeriod = 30;
    private IIndicatorsProvider indicatorsProvider;
    private IIndicator ma;

    private IIndicatorContext context;

    public void onStart(IIndicatorContext context) {
        this.context = context;
        indicatorsProvider = context.getIndicatorsProvider();
        feedDescriptor = context.getFeedDescriptor();

        indicatorInfo =
                new IndicatorInfo("HoursVolatility", "HoursVolatility",
                        "Custom IIndicator", false, false,
                        true, 1, 8, 2);
        inputParameterInfos = new InputParameterInfo[ ]{
                new InputParameterInfo("Bar", InputParameterInfo.Type.BAR),
        };

        int[] maValues = new int[IIndicators.MaType.values().length];
        String[] maNames = new String[IIndicators.MaType.values().length];
        for (int i = 0; i < maValues.length; i++) {
            maValues[i] = i;
            maNames[i] = IIndicators.MaType.values()[i].name();
        }

        optInputParameterInfos = new OptInputParameterInfo[]{
                new OptInputParameterInfo("Time period", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(timePeriod, 1, 2000, 1)),
                new OptInputParameterInfo("MA type", OptInputParameterInfo.Type.OTHER, new IntegerListDescription(maType, maValues, maNames)),
                new OptInputParameterInfo("Start Hour 1", OptInputParameterInfo.Type.OTHER
                        , new IntegerRangeDescription(0, 0, 23, 1)),
                new OptInputParameterInfo("End Hour 1", OptInputParameterInfo.Type.OTHER
                        , new IntegerRangeDescription(17, 0, 23, 1)),
                new OptInputParameterInfo("Start Hour 2", OptInputParameterInfo.Type.OTHER
                        , new IntegerRangeDescription(0, 0, 23, 1)),
                new OptInputParameterInfo("End Hour 2", OptInputParameterInfo.Type.OTHER
                        , new IntegerRangeDescription(17, 0, 23, 1)),
                new OptInputParameterInfo("Reset by month", OptInputParameterInfo.Type.OTHER
                        , new BooleanOptInputDescription(false)),
                new OptInputParameterInfo("Debug", OptInputParameterInfo.Type.OTHER
                        , new BooleanOptInputDescription(true))
        };

        outputParameterInfos = new OutputParameterInfo[] {
                new OutputParameterInfo("HoursVolatility",
                        OutputParameterInfo.Type.DOUBLE,
                        OutputParameterInfo.DrawingStyle.LINE, true),
                new OutputParameterInfo("MA",
                        OutputParameterInfo.Type.DOUBLE,
                        OutputParameterInfo.DrawingStyle.LINE, true)
        };
        this.console = context.getConsole();

    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
        feedDescriptor = context.getFeedDescriptor();
        double pipValue = feedDescriptor.getInstrument().getPipValue();

        //calculating startIndex taking into account maxLookback value
        if (startIndex - getLookback() < 0) {
            startIndex -= startIndex - getLookback();
        }

        if (startIndex > endIndex) {
            return new IndicatorResult(0, 0);
        }
        if (debug) {
            console.getInfo().println("startIndex: " + startIndex + ". endIndex: " + endIndex + ", outputs.length: " + outputs.length + ", out1.length: " + outputs[1].length);
        }

        double[] maInput = new double[inputs[0].length];
        for (int l = 0; l <= endIndex; l++) {
            IBar bar = inputs[0][l];
            double diff = (bar.getClose() - bar.getOpen()) / pipValue;
            if (lastBar == null || l == 0) {
                // the current bar
                maInput[l] = diff;
            } else {
                if (checkHour(bar.getTime())) {
                    if (isDiffMonth(lastBar, bar)) {
                        maInput[l] = diff;
                    } else {
                        maInput[l] = maInput[l - 1] + diff;
                    }
                } else {
                    maInput[l] = maInput[l - 1];
                }
            }
            lastBar = bar;
        }

        int outputLength = endIndex - startIndex + 1;
        System.arraycopy(maInput, startIndex, outputs[0], 0, outputLength);

        if (IIndicators.MaType.values()[maType] == IIndicators.MaType.MAMA){
            ma.setOptInputParameter(0, 0.5);
            ma.setOptInputParameter(1, 0.05);
        }
        else if (IIndicators.MaType.values()[maType] == IIndicators.MaType.T3){
            ma.setOptInputParameter(0, timePeriod);
            ma.setOptInputParameter(1, 0.7);
        }
        else{
            ma.setOptInputParameter(0, timePeriod);
        }
        ma.setInputParameter(0, maInput);

        double[] maOutput = new double[outputLength];
        ma.setOutputParameter(0, maOutput);
        if (IIndicators.MaType.values()[maType] == IIndicators.MaType.MAMA){
            double[] maDummy = new double[outputLength];
            ma.setOutputParameter(1, maDummy);
        }

        ma.calculate(startIndex, endIndex);

        System.arraycopy(maOutput, 0, outputs[1], 0, maOutput.length);
        console.getInfo().println("maInput.length: " + maInput.length + "outputs[0][0]: " + outputs[0][0] + ". outputs[1][0]: " + outputs[1][0]);
        return new IndicatorResult(startIndex, outputLength);
    }

    private boolean isDiffMonth(IBar lastBar, IBar bar) {
        calendar.setTimeInMillis(lastBar.getTime());
        int lastMonth = calendar.get(Calendar.MONTH);
        calendar.setTimeInMillis(bar.getTime());
        int month = calendar.get(Calendar.MONTH);
        return lastMonth != month && resetByMonth;
    }

    private boolean checkHour(long time) {
        calendar.setTimeInMillis(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        boolean res;
        if (startHour > endHour) {
            res = (hour >= startHour && hour <= 23) ||
                    (hour >= 0 && hour <= endHour);
        } else {
            res = hour >= startHour && hour <= endHour;
        }
        boolean res2;
        if (startHour2 > endHour2) {
            res2 = (hour >= startHour2 && hour <= 23) ||
                    (hour >= 0 && hour <= endHour2);
        } else {
            res2 = hour >= startHour2 && hour <= endHour2;
        }
        return res || res2;
    }

    public IndicatorInfo getIndicatorInfo() {
        return indicatorInfo;
    }

    public InputParameterInfo getInputParameterInfo(int index) {
        if (index <= inputParameterInfos.length) {
            return inputParameterInfos[index];
        }
        return null;
    }

    public int getLookback() {
        return timePeriod;
    }

    public int getLookforward() {
        return 0;
    }

    public OptInputParameterInfo getOptInputParameterInfo(int index) {
        if (index <= optInputParameterInfos.length) {
            return optInputParameterInfos[index];
        }
        return null;
    }

    public OutputParameterInfo getOutputParameterInfo(int index) {
        if (index <= outputParameterInfos.length) {
            return outputParameterInfos[index];
        }
        return null;
    }

    public void setInputParameter(int index, Object array) {
        switch (index) {
            case 0: {
                inputs[0] = (IBar[]) array;
                break;
            }
        }
    }

    public void setOptInputParameter(int index, Object value) {
        switch (index) {
            case 0:
                timePeriod = (Integer) value;
                break;
            case 1:
                maType = (Integer) value;
                ma = indicatorsProvider.getIndicator(IIndicators.MaType.values()[maType].name());
                indicatorInfo.setUnstablePeriod(ma.getIndicatorInfo().isUnstablePeriod());
                break;

            case 2: {
                startHour = (int) value;
                break;
            }
            case 3: {
                endHour = (int) value;
                break;
            }
            case 4: {
                startHour2 = (int) value;
                break;
            }
            case 5: {
                endHour2 = (int) value;
                break;
            }
            case 6: {
                resetByMonth = (boolean) value;
                break;
            }
            case 7: {
                debug = (boolean) value;
                break;
            }
            default:
                throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    public void setOutputParameter(int index, Object array) {
        if (index < 2) {
            outputs[index] = (double[]) array;
        }
    }
}
