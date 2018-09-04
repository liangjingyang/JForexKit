package com.jforexcn.inbox.indicator;

import com.dukascopy.api.*;
import com.dukascopy.api.indicators.*;

/**
 * CORREL indicator for two instruments.
 */
public class Cointegration implements IIndicator, IChartInstrumentsListener {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private double[][] inputs = new double[2][];
    private double[][] outputs = new double[1][];

    private IIndicator correl;

    @Override
    public void onStart(IIndicatorContext context) {
        indicatorInfo = new IndicatorInfo("Cointegration",
                "Cointegration for two instruments",
                "Statistic Functions", false, false, false, 2, 3, 1);

        inputParameterInfos = new InputParameterInfo[] {
                new InputParameterInfo("Price 0", InputParameterInfo.Type.DOUBLE),
                new InputParameterInfo("Price 1", InputParameterInfo.Type.DOUBLE)
        };

        optInputParameterInfos = new OptInputParameterInfo[] {
                new OptInputParameterInfo("Time cPeriod",
                        OptInputParameterInfo.Type.OTHER,
                        new IntegerRangeDescription(30, 1, 2000, 1)),
                new OptInputParameterInfo("First cInstrument",
                        OptInputParameterInfo.Type.OTHER,
                        new IntegerListDescription(-1,
                                new int[] {-1}, new String[] {""})),
                new OptInputParameterInfo("Second cInstrument",
                        OptInputParameterInfo.Type.OTHER,
                        new IntegerListDescription(-1,
                                new int[] {-1}, new String[] {""}))
        };

        outputParameterInfos = new OutputParameterInfo[] {
                new OutputParameterInfo("Line",
                        OutputParameterInfo.Type.DOUBLE,
                        OutputParameterInfo.DrawingStyle.LINE, true)
        };

        IIndicatorsProvider indicatorsProvider =
                context.getIndicatorsProvider();
        correl = indicatorsProvider.getIndicator("CORREL");

        onInstrumentsChanged(context.getChartInstruments());
        context.addChartInstrumentsListener(this);
    }

    @Override
    public void onInstrumentsChanged(Instrument[] chartInstr) {
        int masterValue = (chartInstr != null ? chartInstr[0].ordinal() : -1);
        String masterName = (chartInstr != null ? chartInstr[0].name() : "");

        int[] slaveValues;
        String[] slaveNames;
        if ((chartInstr != null) && (chartInstr.length > 1)) {
            slaveValues = new int[chartInstr.length - 1];
            slaveNames = new String[chartInstr.length - 1];
            for (int i = 1; i < chartInstr.length; i++) {
                slaveValues[i - 1] = chartInstr[i].ordinal();
                slaveNames[i - 1] = chartInstr[i].name();
            }
        } else {
            slaveValues = new int[] {-1};
            slaveNames = new String[] {""};
        }

        optInputParameterInfos[1].setDescription(
                new IntegerListDescription(masterValue,
                        new int[] {masterValue}, new String[] {masterName}));
        optInputParameterInfos[2].setDescription(
                new IntegerListDescription(slaveValues[0],
                        slaveValues, slaveNames));
    }

    @Override
    public IndicatorResult calculate(int startIndex, int endIndex) {
        if (startIndex - getLookback() < 0) {
            startIndex = getLookback();
        }
        if (startIndex > endIndex) {
            return new IndicatorResult(0, 0);
        }

        if ((startIndex < inputs[0].length) && (startIndex < inputs[1].length)) {
            int maxLength = Math.max(inputs[0].length, inputs[1].length);
            int diffLength = Math.abs(inputs[0].length - inputs[1].length);
            double[][] inputs_ = new double[2][maxLength];
            double[][] outputs_ = new double[1][outputs[0].length - diffLength];
            System.arraycopy(inputs[0], 0, inputs_[0], maxLength - inputs[0].length, inputs[0].length);
            System.arraycopy(inputs[1], 0, inputs_[1], maxLength - inputs[1].length, inputs[1].length);

            correl.setInputParameter(0, inputs_[0]);
            correl.setInputParameter(1, inputs_[1]);
            correl.setOutputParameter(0, outputs_[0]);
            correl.calculate(startIndex + diffLength, endIndex);

            System.arraycopy(outputs_[0], 0, outputs[0], diffLength, outputs_[0].length);
            for (int i = 0; i < diffLength; i++) {
                outputs[0][i] = Double.NaN;
            }

        } else {
            // data for second cInstrument aren't ready yet
            for (int i = 0; i < outputs[0].length; i++) {
                outputs[0][i] = Double.NaN;
            }
        }

        return new IndicatorResult(startIndex, outputs[0].length);
    }

    @Override
    public IndicatorInfo getIndicatorInfo() {
        return indicatorInfo;
    }

    @Override
    public InputParameterInfo getInputParameterInfo(int index) {
        if (index < inputParameterInfos.length) {
            return inputParameterInfos[index];
        }
        return null;
    }

    @Override
    public OptInputParameterInfo getOptInputParameterInfo(int index) {
        if (index < optInputParameterInfos.length) {
            return optInputParameterInfos[index];
        }
        return null;
    }

    @Override
    public OutputParameterInfo getOutputParameterInfo(int index) {
        if (index < outputParameterInfos.length) {
            return outputParameterInfos[index];
        }
        return null;
    }

    @Override
    public void setInputParameter(int index, Object array) {
        inputs[index] = (double[]) array;
    }

    @Override
    public void setOptInputParameter(int index, Object value) {
        switch (index) {
            case 0:
                correl.setOptInputParameter(0, (Integer) value);
                break;
            case 1:
            case 2:
                OptInputDescription descr = optInputParameterInfos[index].getDescription();
                int[] values = ((IntegerListDescription) descr).getValues();
                int instr = -1;
                for (int i = 0; i < values.length; i++) {
                    if (values[i] == (Integer) value) {
                        instr = values[i];
                        break;
                    }
                }
                if (instr < 0) {
                    // value not found
                    instr = values[0];
                }
                if (instr >= 0) {
                    inputParameterInfos[index - 1].setInstrument(Instrument.values()[instr]);
                } else {
                    inputParameterInfos[index - 1].setInstrument(null);
                }
                break;
        }
    }

    @Override
    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }

    @Override
    public int getLookback() {
        return correl.getLookback();
    }

    @Override
    public int getLookforward() {
        return 0;
    }
}
