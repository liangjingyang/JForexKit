package com.jforexcn.hub.indicator;

import com.dukascopy.api.IIndicators;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.indicators.DoubleRangeDescription;
import com.dukascopy.api.indicators.IChartInstrumentsListener;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.InstrumentListDescription;
import com.dukascopy.api.indicators.IntegerListDescription;
import com.dukascopy.api.indicators.IntegerRangeDescription;
import com.dukascopy.api.indicators.OptInputDescription;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;

/**
 * Created by simple(simple.continue@gmail.com) on 13/05/2018.
 */

public class TwoInstrumentCorrelation implements IIndicator, IChartInstrumentsListener {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;

    private double[][] inputs = new double[2][];
    private double[][] outputs = new double[5][];

    private double a1 = 1;
    private double b1 = 0;
    private double a2 = 1;
    private double b2 = 0;
    private double offset = 0;

    private IIndicator ma;

    private int shortMaTimePeriod = 120;
    private int longMaTimePeriod = 360;
    private int maType = IIndicators.MaType.SMA.ordinal();
    private int formulaType = FormulaType.subtraction.ordinal();

    private IIndicatorContext context;

    public enum FormulaType {
        subtraction,
        division
    }

    @Override
    public void onStart(IIndicatorContext context) {

        indicatorInfo = new IndicatorInfo("[ZZ]2InstrumentCorrelation",
                "[ZZ] instrument1 - instrument2", "Custom Indicator",
                false, false, false, 2, 11, 5);

        inputParameterInfos = new InputParameterInfo[] {
                new InputParameterInfo("Price 0", InputParameterInfo.Type.DOUBLE),
                new InputParameterInfo("Price 1", InputParameterInfo.Type.DOUBLE)
        };

        int[] maValues = new int[IIndicators.MaType.values().length];
        String[] maNames = new String[IIndicators.MaType.values().length];
        for (int i = 0; i < maValues.length; i++) {
            maValues[i] = i;
            maNames[i] = IIndicators.MaType.values()[i].name();
        }

        int[] formulaValues = new int[FormulaType.values().length];
        String[] formulaNames = new String[FormulaType.values().length];
        for (int i = 0; i < formulaValues.length; i++) {
            formulaValues[i] = i;
            formulaNames[i] = FormulaType.values()[i].name();
        }

        optInputParameterInfos = new OptInputParameterInfo[] {
                new OptInputParameterInfo("First instrument", OptInputParameterInfo.Type.OTHER,
                        new InstrumentListDescription(Instrument.AUDJPY, Instrument.values())),
                new OptInputParameterInfo("Second instrument", OptInputParameterInfo.Type.OTHER,
                        new InstrumentListDescription(Instrument.NZDJPY, Instrument.values())),
                new OptInputParameterInfo("Instrument1 a", OptInputParameterInfo.Type.OTHER,
                        new DoubleRangeDescription(a1, 0, 2000, 0.1, 5)),
                new OptInputParameterInfo("Instrument1 b", OptInputParameterInfo.Type.OTHER,
                        new DoubleRangeDescription(b1, -2000, 2000, 0.01, 5)),
                new OptInputParameterInfo("Instrument2 a", OptInputParameterInfo.Type.OTHER,
                        new DoubleRangeDescription(a2, 0, 2000, 0.1, 5)),
                new OptInputParameterInfo("Instrument2 b", OptInputParameterInfo.Type.OTHER,
                        new DoubleRangeDescription(b2, -2000, 2000, 0.01, 5)),
                new OptInputParameterInfo("Result Offset", OptInputParameterInfo.Type.OTHER,
                        new DoubleRangeDescription(offset, -200000, 200000, 0.01, 5)),
                new OptInputParameterInfo("Short MA Time period", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(shortMaTimePeriod, 1, 2000, 1)),
                new OptInputParameterInfo("Long MA Time period", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(longMaTimePeriod, 1, 2000, 1)),
                new OptInputParameterInfo("MA type", OptInputParameterInfo.Type.OTHER, new IntegerListDescription(maType, maValues, maNames)),
                new OptInputParameterInfo("Formula type", OptInputParameterInfo.Type.OTHER, new IntegerListDescription(formulaType, formulaValues, formulaNames))

        };

        outputParameterInfos = new OutputParameterInfo[] {
                new OutputParameterInfo("Diff", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LINE, true),
                new OutputParameterInfo("Short MA", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LINE, true),
                new OutputParameterInfo("Long MA", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LINE, true),
                new OutputParameterInfo("Distance", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.HISTOGRAM, true),
                new OutputParameterInfo("Slope", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.HISTOGRAM, true)
        };

        ma = context.getIndicatorsProvider().getIndicator("MA");

        onInstrumentsChanged(context.getChartInstruments());
        context.addChartInstrumentsListener(this);
        this.context = context;
    }

    @Override
    public void onInstrumentsChanged(Instrument[] chartInstr) {
        Instrument[] masterInstr, slaveInstr;
        if (chartInstr != null) {
            masterInstr = new Instrument[] {chartInstr[0]};
            if (chartInstr.length > 1) {
                slaveInstr = new Instrument[chartInstr.length - 1];
                for (int i = 1; i < chartInstr.length; i++) {
                    slaveInstr[i - 1] = chartInstr[i];
                }
            } else {
                slaveInstr = masterInstr;
            }
        } else {
            // call from API
            masterInstr = slaveInstr = Instrument.values();
        }

        optInputParameterInfos[0].setDescription(new InstrumentListDescription(masterInstr[0], masterInstr));
        optInputParameterInfos[1].setDescription(new InstrumentListDescription(slaveInstr[0], slaveInstr));
    }

    @Override
    public IndicatorResult calculate(int startIndex, int endIndex) {
        if (startIndex - getLookback() < 0) {
            startIndex = getLookback();
        }
        if (startIndex > endIndex) {
            return new IndicatorResult(0, 0);
        }

        double[] diffOutputs = new double[inputs[0].length];
        int i;
        for (i = 0; i <= endIndex; i++) {
            if (formulaType == FormulaType.subtraction.ordinal()) {
                diffOutputs[i] = (inputs[0][i] * a1 + b1) - (inputs[1][i] * a2 + b2) + offset;
            } else {
                diffOutputs[i] = (inputs[0][i] * a1 + b1) / (inputs[1][i] * a2 + b2) + offset;
            }
        }
        System.arraycopy(diffOutputs, startIndex, outputs[0], 0, endIndex - startIndex + 1);

        ma.setInputParameter(0, diffOutputs);
        ma.setOptInputParameter(1, maType);

        ma.setOptInputParameter(0, shortMaTimePeriod);
        int shortMaLength = diffOutputs.length - ma.getLookback();
        double[] shortMaOutputs = new double[shortMaLength];
        ma.setOutputParameter(0, shortMaOutputs);
//        context.getConsole().getInfo().println("startIndex: " + startIndex + ", endIndex: " + endIndex + ", j: " + j + ", ma.getLookback(): " + ma.getLookback());
        ma.calculate(ma.getLookback(), diffOutputs.length - 1);
        System.arraycopy(shortMaOutputs, longMaTimePeriod + 1, outputs[1], 0, endIndex - startIndex + 1);

        ma.setOptInputParameter(0, longMaTimePeriod);
        int longMaLength = diffOutputs.length - ma.getLookback();
        double[] longMaOutputs = new double[longMaLength];
        ma.setOutputParameter(0, longMaOutputs);
        ma.calculate(ma.getLookback(), diffOutputs.length - 1);
        System.arraycopy(longMaOutputs, shortMaTimePeriod + 1, outputs[2], 0, endIndex - startIndex + 1);

        for (int z = 0; z < outputs[0].length; z++) {
            outputs[3][z] = outputs[0][z] - outputs[1][z];
        }

        outputs[4][0] = Double.NaN;
        for (int y = 1; y < outputs[1].length; y++) {
            outputs[4][y] = (outputs[1][y] - outputs[1][y-1]) * 100;
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
            case 1:
                OptInputDescription descr = optInputParameterInfos[index].getDescription();
                Instrument[] values = ((InstrumentListDescription) descr).getValues();
                Instrument instr = null;
                if (value != null) {
                    for (int i = 0; i < values.length; i++) {
                        if (value.equals(values[i])) {
                            instr = values[i];
                            break;
                        }
                    }
                }
                if (instr == null) {
                    instr = values[0];
                }
                inputParameterInfos[index].setInstrument(instr);
                break;
            case 2:
                a1 = (double) value;
                break;
            case 3:
                b1 = (double) value;
                break;
            case 4:
                a2 = (double) value;
                break;
            case 5:
                b2 = (double) value;
                break;
            case 6:
                offset = (double) value;
                break;
            case 7:
                shortMaTimePeriod = (int) value;
                break;
            case 8:
                longMaTimePeriod = (int) value;
                break;
            case 9:
                maType = (int) value;
                break;
            case 10:
                formulaType = (int) value;
                break;
        }
    }

    @Override
    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }

    @Override
    public int getLookback() {
        return shortMaTimePeriod + longMaTimePeriod;
    }

    @Override
    public int getLookforward() {
        return 0;
    }
}