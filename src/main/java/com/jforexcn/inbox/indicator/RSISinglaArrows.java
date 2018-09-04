package com.jforexcn.inbox.indicator;

/*
 * Copyright 2009 Dukascopy® (Suisse) SA. All rights reserved.
 * DUKASCOPY PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import java.awt.Color;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IIndicatorsProvider;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.IntegerRangeDescription;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;

import static com.dukascopy.api.indicators.OutputParameterInfo.DrawingStyle.*;
import static com.dukascopy.api.indicators.OutputParameterInfo.Type.*;

public class RSISinglaArrows implements IIndicator {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    //Price includes 5 arrays: open, close, high, low, volume
    private double[][][] inputsPriceArr = new double[1][][];
    //price array depending on AppliedPrice
    private double[][] inputsDouble = new double[1][];
    private double[][] outputs = new double[2][];

    IIndicator rsiIndicator;
    int rsiTimePeriod = 14;

    //output indices
    private static final int DOWN = 0;
    private static final int UP = 1;
    //input indices
    private static final int HIGH = 2;
    private static final int LOW = 3;

    public void onStart(IIndicatorContext context) {
        indicatorInfo = new IndicatorInfo("RSI_Signals", "RSI signals", "Custom indicator", true, false, true, 2, 1, 2);
        inputParameterInfos = new InputParameterInfo[] {
                new InputParameterInfo("Price arrays", InputParameterInfo.Type.PRICE),
                new InputParameterInfo("Price double", InputParameterInfo.Type.DOUBLE)
        };
        optInputParameterInfos = new OptInputParameterInfo[] { new OptInputParameterInfo("Rsi time cPeriod",
                OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(rsiTimePeriod, 1, 200, 1)) };
        outputParameterInfos = new OutputParameterInfo[] {
                new OutputParameterInfo("Maximums", DOUBLE, ARROW_SYMBOL_DOWN) {{ setColor(Color.RED); }},
                new OutputParameterInfo("Minimums", DOUBLE, ARROW_SYMBOL_UP) {{ setColor(Color.GREEN); }} };

        IIndicatorsProvider indicatorsProvider = context.getIndicatorsProvider();
        rsiIndicator = indicatorsProvider.getIndicator("RSI");
    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
        if (startIndex - getLookback() < 0) {
            startIndex -= startIndex - getLookback();
        }
        // calculating rsi
        double[] rsiOutput = new double[endIndex - startIndex + 1];
        rsiIndicator.setInputParameter(0, inputsDouble[0]);
        rsiIndicator.setOutputParameter(0, rsiOutput);
        rsiIndicator.calculate(startIndex, endIndex);

        int i, j;
        for (i = startIndex, j = 0; i <= endIndex; i++, j++) {
            //place down signal on the high price of the corresponding bar
            outputs[DOWN][j] = rsiOutput[j] < 30 ? inputsPriceArr[0][HIGH][i] : Double.NaN;
            //place up signal on the low price of the corresponding bar
            outputs[UP][j] = rsiOutput[j] > 70 ? inputsPriceArr[0][LOW][i] : Double.NaN;
        }

        return new IndicatorResult(startIndex, endIndex-startIndex + 1);
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
        return rsiTimePeriod;
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
        if(index == 0)
            inputsPriceArr[0] = (double[][]) array;
        else if(index == 1)
            inputsDouble[0] = (double[]) array;
    }

    public void setOptInputParameter(int index, Object value) {
        if (index == 0) {
            //set rsi time cPeriod
            rsiTimePeriod = (Integer) value;
            rsiIndicator.setOptInputParameter(0, (Integer) value);
        }
    }

    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }
}