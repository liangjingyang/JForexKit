package com.jforexcn.inbox.indicator;

import com.dukascopy.api.indicators.*;

public class SMAOverRSIIndicator implements IIndicator {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private double[][] inputs = new double[1][];
    private double[][] outputs = new double[2][];
    private IIndicatorContext context;
    private IIndicator rsiIndicator;
    private IIndicator smaIndicator;

    public void onStart(IIndicatorContext context) {
        //getting interfaces of RSI and SMA indicator
        IIndicatorsProvider indicatorsProvider = context.getIndicatorsProvider();
        rsiIndicator = indicatorsProvider.getIndicator("RSI");
        smaIndicator = indicatorsProvider.getIndicator("SMA");
        //inicator with one input, two optional params and two outputs
        indicatorInfo = new IndicatorInfo("SMA_RSI", "SMA over RSI", "My indicator", false, false, false, 1, 2, 2);
        //one input array of doubles
        inputParameterInfos = new InputParameterInfo[] {new InputParameterInfo("Input data", InputParameterInfo.Type.DOUBLE)};
        //two optional params, one for every indicator
        optInputParameterInfos = new OptInputParameterInfo[] {new OptInputParameterInfo("RSI Time Period", OptInputParameterInfo.Type.OTHER,
                new IntegerRangeDescription(14, 2, 100, 1)), new OptInputParameterInfo("SMA Time Period", OptInputParameterInfo.Type.OTHER,
                new IntegerRangeDescription(14, 2, 100, 1))};
        //two output arrays, one for RSI and one for SMA over RSI
        outputParameterInfos = new OutputParameterInfo[] {new OutputParameterInfo("RSI line", OutputParameterInfo.Type.DOUBLE,
                OutputParameterInfo.DrawingStyle.LINE), new OutputParameterInfo("SMA line", OutputParameterInfo.Type.DOUBLE,
                OutputParameterInfo.DrawingStyle.LINE)};
    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
        if (startIndex < getLookback()) {
            startIndex = getLookback();
        }
        if (startIndex > endIndex) {
            //not enough data to calculate indicator
            return new IndicatorResult(0, 0);
        }

        //calculating rsi
        int smaLookback = smaIndicator.getLookback();
        //first alocate buffer for rsi results
        double[] rsiOutput = new double[endIndex - startIndex + 1 + smaLookback];
        //init rsi indicator with input data and array for output
        rsiIndicator.setInputParameter(0, inputs[0]);
        rsiIndicator.setOutputParameter(0, rsiOutput);
        IndicatorResult rsiResult = rsiIndicator.calculate(startIndex - smaLookback, endIndex);
        if (rsiResult.getNumberOfElements() < smaLookback) {
            //not enough data to calculate sma
            return new IndicatorResult(0, 0);
        }

        //calculating sma
        smaIndicator.setInputParameter(0, rsiOutput);
        smaIndicator.setOutputParameter(0, outputs[1]);
        IndicatorResult smaResult = smaIndicator.calculate(0, rsiResult.getNumberOfElements() - 1);
        if (smaResult.getNumberOfElements() == 0) {
            //sma returned 0 values
            return new IndicatorResult(0, 0);
        }

        //copy rsi values to output excluding first values used for sma lookback
        System.arraycopy(rsiOutput, smaResult.getFirstValueIndex(), outputs[0], 0, smaResult.getNumberOfElements());
        //creating result, first value index for our input is FVI for rsi + FVI for sma, because we calculated sma starting from 0 element
        IndicatorResult result = new IndicatorResult(rsiResult.getFirstValueIndex() + smaResult.getFirstValueIndex(), smaResult.getNumberOfElements());
        return result;
    }

    public IndicatorInfo getIndicatorInfo() {
        return indicatorInfo;
    }

    public InputParameterInfo getInputParameterInfo(int index) {
        if (index < inputParameterInfos.length) {
            return inputParameterInfos[index];
        }
        return null;
    }

    public int getLookback() {
        return rsiIndicator.getLookback() + smaIndicator.getLookback();
    }

    public int getLookforward() {
        return 0;
    }

    public OptInputParameterInfo getOptInputParameterInfo(int index) {
        if (index < optInputParameterInfos.length) {
            return optInputParameterInfos[index];
        }
        return null;
    }

    public OutputParameterInfo getOutputParameterInfo(int index) {
        if (index < outputParameterInfos.length) {
            return outputParameterInfos[index];
        }
        return null;
    }

    public void setInputParameter(int index, Object array) {
        inputs[index] = (double[]) array;
    }

    public void setOptInputParameter(int index, Object value) {
        //set optional params in indicator
        switch (index) {
            case 0:
                rsiIndicator.setOptInputParameter(0, value);
                break;
            case 1:
                smaIndicator.setOptInputParameter(0, value);
                break;
        }
    }

    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }
}

