package com.jforexcn.inbox.indicator;


import com.dukascopy.api.indicators.DoubleRangeDescription;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IIndicatorsProvider;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.IntegerRangeDescription;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;

public class STDDEVATRIndicator implements IIndicator{
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private double[][][] atrInputs = new double[1][][];
    private double[][] stddevInputs = new double[1][];
    private int atrTimePeriod = 2;
    private int stddevTimePeriod = 5;
    private double nbDev = 1;
    private double[][] outputs = new double[2][];
    private IIndicatorsProvider indicatorsProvider;
    private IIndicator atrIndicator;
    private IIndicator stddevIndicator;

    public void onStart(IIndicatorContext context) {
        indicatorsProvider = context.getIndicatorsProvider();
        atrIndicator = indicatorsProvider.getIndicator("ATR");
        stddevIndicator = indicatorsProvider.getIndicator("STDDEV");
        indicatorInfo = new IndicatorInfo("ATRSTDDEV", "ATRSTDDEV", "My indicator", false, false, false, 2, 3, 2);
        inputParameterInfos = new InputParameterInfo[] {
                new InputParameterInfo("atr Input", InputParameterInfo.Type.PRICE),
                new InputParameterInfo("stddev Input", InputParameterInfo.Type.DOUBLE)};
        optInputParameterInfos = new OptInputParameterInfo[] {
                new OptInputParameterInfo("ATR Time cPeriod", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(atrTimePeriod, 2, 100, 1)),
                new OptInputParameterInfo("STDDEV Time cPeriod", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(stddevTimePeriod, 2, 100, 1)),
                new OptInputParameterInfo("Nb Dev", OptInputParameterInfo.Type.OTHER, new DoubleRangeDescription(nbDev, 1, 100, 0.2, 2))};
        outputParameterInfos = new OutputParameterInfo[] {
                new OutputParameterInfo("ATR", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LINE),
                new OutputParameterInfo("STDDEV", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LINE)};
    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
        if (startIndex - getLookback() < 0) {
            startIndex -= startIndex - getLookback();
        }
        atrIndicator.setInputParameter(0, atrInputs[0]);
        atrIndicator.setOutputParameter(0, outputs[0]);
        stddevIndicator.setInputParameter(0, stddevInputs[0]);
        stddevIndicator.setOutputParameter(0, outputs[1]);
        atrIndicator.calculate(startIndex, endIndex);
        return stddevIndicator.calculate(startIndex, endIndex);
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
        return Math.max(atrIndicator.getLookback(), stddevIndicator.getLookback());
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
        if (index == 0) atrInputs[0] = (double[][]) array;
        else if (index == 1) stddevInputs[0] = (double[]) array;
    }

    public void setOptInputParameter(int index, Object value) {
        switch (index) {
            case 0:
                atrTimePeriod = (Integer) value;
                atrIndicator.setOptInputParameter(0, atrTimePeriod);
                break;
            case 1:
                stddevTimePeriod = (Integer) value;
                stddevIndicator.setOptInputParameter(0, stddevTimePeriod);
                break;
            case 2:
                nbDev = (Double) value;
                stddevIndicator.setOptInputParameter(1, nbDev);
                break;
            default:
                throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }
}