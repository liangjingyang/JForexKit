package com.jforexcn.inbox.indicator;

import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IIndicatorsProvider;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.IntegerRangeDescription;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;

/**
 * Created by simple(simple.continue@gmail.com) on 29/03/2018.
 */

public class BiasIndicator implements IIndicator {

    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private double[][] inputs = new double[1][];
    private double[][] outputs = new double[1][];
    private IIndicatorContext context;
    private IIndicator smaIndicator;

    public void onStart(IIndicatorContext context) {
        //getting interfaces of RSI and SMA indicator
        IIndicatorsProvider indicatorsProvider = context.getIndicatorsProvider();
        smaIndicator = indicatorsProvider.getIndicator("EMA");
        //inicator with one input, two optional params and two outputs
        indicatorInfo = new IndicatorInfo("BIAS", "BIAS", "Custom indicator", false, false, false, 1, 1, 1);
        //one input array of doubles
        inputParameterInfos = new InputParameterInfo[] {new InputParameterInfo("Input data", InputParameterInfo.Type.DOUBLE)};
        //two optional params, one for every indicator
        optInputParameterInfos = new OptInputParameterInfo[] {new OptInputParameterInfo("SMA Time Period", OptInputParameterInfo.Type.OTHER,
                new IntegerRangeDescription(14, 2, 100, 1))};
        //two output arrays, one for RSI and one for SMA over RSI
        outputParameterInfos = new OutputParameterInfo[] {new OutputParameterInfo("BIAS line", OutputParameterInfo.Type.DOUBLE,
                OutputParameterInfo.DrawingStyle.LINE)};
        this.context = context;
    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
        if (startIndex < getLookback()) {
            startIndex = getLookback();
        }
        if (startIndex > endIndex) {
            //not enough data to calculate indicator
            return new IndicatorResult(0, 0);
        }

        //calculating sma
        double[] smaOutput = new double[endIndex - startIndex + 1];
        smaIndicator.setInputParameter(0, inputs[0]);
        smaIndicator.setOutputParameter(0, smaOutput);
        IndicatorResult smaResult = smaIndicator.calculate(startIndex, endIndex);
        if (smaResult.getNumberOfElements() == 0) {
            //sma returned 0 values
            return new IndicatorResult(0, 0);
        }

        for (int i = 0; i < smaOutput.length; i++) {
            outputs[0][i] = (inputs[0][startIndex + i] - smaOutput[i]) / smaOutput[i] * 100;
        }

        if (smaOutput.length > 1) {
            context.getConsole().getInfo().println("sma: " + smaOutput[smaOutput.length - 2] + ", input: " + inputs[0][endIndex - 1] + ", bias: " + outputs[0][outputs[0].length - 2]);
        }
        if (smaOutput.length > 2) {
            context.getConsole().getInfo().println("sma: " + smaOutput[smaOutput.length - 3] + ", input: " + inputs[0][endIndex - 2] + ", bias: " + outputs[0][outputs[0].length - 3]);
        }

        IndicatorResult result = new IndicatorResult(smaResult.getFirstValueIndex(), smaResult.getNumberOfElements());
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
        return smaIndicator.getLookback();
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
                smaIndicator.setOptInputParameter(0, value);
                break;
        }
    }

    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }
}



