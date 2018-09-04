package com.jforexcn.inbox.indicator;


import java.util.Arrays;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Period;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;

public class MultiplePeriodsIndicator implements IIndicator{
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private IBar[][] inputs = new IBar[4][];
    private double[][] outputs = new double[3][];

    public void onStart(IIndicatorContext context) {
        indicatorInfo = new IndicatorInfo("MULTIPLEPERIODS", "Multiple cPeriod indicator", "", false, false, false, 4, 0, 3);
        //indicator looks way better with setSparceIndicator property set to true
        indicatorInfo.setSparseIndicator(true);
        inputParameterInfos = new InputParameterInfo[] {
                //main input from chart
                new InputParameterInfo("Main", InputParameterInfo.Type.BAR),
                //additional input
                new InputParameterInfo("FirstPeriod", InputParameterInfo.Type.BAR){{
                    setPeriod(Period.ONE_MIN);}},
                new InputParameterInfo("SecondPeriod", InputParameterInfo.Type.BAR){{
                    setPeriod(Period.FIVE_MINS);}},
                new InputParameterInfo("ThirdPeriod", InputParameterInfo.Type.BAR){{
                    setPeriod(Period.TEN_MINS);}}};

        outputParameterInfos = new OutputParameterInfo[] {
                new OutputParameterInfo("First", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LINE),
                new OutputParameterInfo("Second", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LINE),
                new OutputParameterInfo("Third", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LINE)};
    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
        //default values from indicator outputs
        Arrays.fill(outputs[0], Double.NaN);
        Arrays.fill(outputs[1], Double.NaN);
        Arrays.fill(outputs[2], Double.NaN);

        int i;
        for (i = 1; i < inputs.length; i++){
            calculateOutput(i, endIndex - startIndex + 1);
        }

        return new IndicatorResult(startIndex, outputs[0].length);
    }

    private void calculateOutput(int inputIndex, int elements){
        double[] input = new double[inputs[inputIndex].length];

        for (int i = 0; i < inputs[inputIndex].length; i++) {
            IBar bar = inputs[inputIndex][i];
            input[i] = bar.getClose();
        }

        int j = 0;
        //synchronize additional input with the main input
        for (int i = 0; i < elements; i++) {
            IBar bar = inputs[0][i];
            long barTime = bar.getTime();

            while (j < inputs[inputIndex].length && inputs[inputIndex][j].getTime() < barTime) {
                j++;
            }
            if (j >= inputs[inputIndex].length || inputs[inputIndex][j].getTime() != barTime) {
                outputs[inputIndex - 1][i] = Double.NaN;
            } else {
                //bar located, set up output
                outputs[inputIndex - 1][i] = inputs[inputIndex][j].getClose();
            }
        }
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
}


