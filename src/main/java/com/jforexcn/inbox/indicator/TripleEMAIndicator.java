package com.jforexcn.inbox.indicator;

/**
 * Created by simple on 03/03/2017.
 */


import com.dukascopy.api.indicators.*;

public class TripleEMAIndicator implements IIndicator {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private double[][] inputs = new double[1][];
    private int[] timePeriod = new int[1];
    private double[][] outputs = new double[1][];
    private IIndicator ema;

    public void onStart(IIndicatorContext context) {
        IIndicatorsProvider indicatorsProvider = context.getIndicatorsProvider();
        ema = indicatorsProvider.getIndicator("EMA");
        indicatorInfo = new IndicatorInfo("THREEEMA", "Shows three different EMA indicator", "My indicator",
                true, false, true, 1, 3, 3);
        inputParameterInfos = new InputParameterInfo[] {new InputParameterInfo("Input data", InputParameterInfo.Type.DOUBLE)};
        optInputParameterInfos = new OptInputParameterInfo[] {
//                new OptInputParameterInfo(
//                        "Time cPeriod EMA1",
//                        OptInputParameterInfo.Type.OTHER,
//                        new IntegerRangeDescription(5, 2, 1000, 1)
//                ),
//                new OptInputParameterInfo(
//                        "Time cPeriod EMA2",
//                        OptInputParameterInfo.Type.OTHER,
//                        new IntegerRangeDescription(10, 2, 1000, 1)
//                ),
                new OptInputParameterInfo(
                        "Time cPeriod EMA3",
                        OptInputParameterInfo.Type.OTHER,
                        new IntegerRangeDescription(20, 2, 1000, 1)
                )
        };
        outputParameterInfos = new OutputParameterInfo[] {
//                new OutputParameterInfo(
//                        "EMA1",
//                        OutputParameterInfo.Type.DOUBLE,
//                        OutputParameterInfo.DrawingStyle.LINE
//                ),
//                new OutputParameterInfo(
//                        "EMA2",
//                        OutputParameterInfo.Type.DOUBLE,
//                        OutputParameterInfo.DrawingStyle.LINE
//                ),
                new OutputParameterInfo(
                        "EMA3",
                        OutputParameterInfo.Type.DOUBLE,
                        OutputParameterInfo.DrawingStyle.LINE
                )
        };
    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
        //calculating startIndex taking into account lookback value
        if (startIndex - getLookback() < 0) {
            startIndex -= startIndex - getLookback();
        }

        ema.setInputParameter(0, inputs[0]);

        //calculate first ema
        ema.setOptInputParameter(0, timePeriod[0]);
        ema.setOutputParameter(0, outputs[0]);
        ema.calculate(startIndex, endIndex);

        //calculate second ema
        ema.setOptInputParameter(0, timePeriod[1]);
        ema.setOutputParameter(0, outputs[1]);
        ema.calculate(startIndex, endIndex);

        //calculate third ema
        ema.setOptInputParameter(0, timePeriod[2]);
        ema.setOutputParameter(0, outputs[2]);
        return ema.calculate(startIndex, endIndex);
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
        ema.setOptInputParameter(0, timePeriod[0]);
        int ema1Lookback = ema.getLookback();
        ema.setOptInputParameter(0, timePeriod[1]);
        int ema2Lookback = ema.getLookback();
        ema.setOptInputParameter(0, timePeriod[2]);
        int ema3Lookback = ema.getLookback();
        return Math.max(ema1Lookback, Math.max(ema2Lookback, ema3Lookback));
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
        inputs[index] = (double[]) array;
    }

    public void setOptInputParameter(int index, Object value) {
        timePeriod[index] = (Integer) value;
    }

    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }
}
