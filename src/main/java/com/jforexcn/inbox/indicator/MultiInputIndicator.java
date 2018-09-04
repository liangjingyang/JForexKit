package com.jforexcn.inbox.indicator;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.indicators.*;

public class MultiInputIndicator implements IIndicator {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private IBar[][] inputs = new IBar[3][];
    private double[][] outputs = new double[1][];

    public void onStart(IIndicatorContext context) {
        indicatorInfo = new IndicatorInfo("EXAMPIND", "Sums previous values", "My indicator", false, false, false, 3, 0, 1);
        inputParameterInfos = new InputParameterInfo[] {
                new InputParameterInfo("Main", InputParameterInfo.Type.BAR){{
                }},
                new InputParameterInfo("ASK", InputParameterInfo.Type.BAR){{
                    setOfferSide(OfferSide.ASK);
                    setInstrument(Instrument.GBPUSD);
                }},
                new InputParameterInfo("BID", InputParameterInfo.Type.BAR){{
                    setOfferSide(OfferSide.BID);
                    setInstrument(Instrument.GBPUSD);
                }}};
        optInputParameterInfos = new OptInputParameterInfo[] {new OptInputParameterInfo("Time cPeriod", OptInputParameterInfo.Type.OTHER,
                new IntegerRangeDescription(2, 2, 100, 1))};
        outputParameterInfos = new OutputParameterInfo[] {new OutputParameterInfo("out", OutputParameterInfo.Type.DOUBLE,
                OutputParameterInfo.DrawingStyle.LINE)};
    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
        if (startIndex - getLookback() < 0) {
            startIndex -= startIndex - getLookback();
        }
        int i, j;
        for (i = startIndex, j = 0; i <= endIndex; i++, j++) {
            int timeIndex = getTimeIndex(inputs[0][i].getTime(), inputs[1]);
            int timeIndex2= getTimeIndex(inputs[0][i].getTime(), inputs[2]);
            outputs[0][j] = (timeIndex == -1 || timeIndex2 == -1)?
                    Double.NaN : ((IBar)inputs[2][timeIndex2]).getHigh() + ((IBar)inputs[1][timeIndex]).getHigh();
        }
        return new IndicatorResult(startIndex, j);
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
        return 0;
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
        inputs[index] = (IBar[]) array;
    }

    public void setOptInputParameter(int index, Object value) {
    }

    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }

    private int getTimeIndex(long time, IBar[] target) {
        if (target == null) {
            return -1;
        }

        int first = 0;
        int upto = target.length;

        while (first < upto) {
            int mid = (first + upto) / 2;

            IBar data = target[mid];

            if (data.getTime() == time) {
                return mid;
            }
            else if (time < data.getTime()) {
                upto = mid;
            }
            else if (time > data.getTime()) {
                first = mid + 1;
            }
        }
        return -1;
    }
}

