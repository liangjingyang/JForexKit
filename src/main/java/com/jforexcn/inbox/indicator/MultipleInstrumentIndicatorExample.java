package com.jforexcn.inbox.indicator;

import com.dukascopy.api.*;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import com.dukascopy.api.indicators.*;

public class MultipleInstrumentIndicatorExample implements IIndicator, IDrawingIndicator {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private IBar[][] inputs = new IBar[4][];
    private double[][] outputs = new double[4][];
    private IIndicator mainInputMA;
    private IIndicator additionalInputMA;
    private int defaultMAPeriod = 20;
    private IConsole console;

    public void onStart(IIndicatorContext context) {
        console = context.getConsole();
        IIndicatorsProvider provider = context.getIndicatorsProvider();

        mainInputMA = provider.getIndicator("MA");
        mainInputMA.setOptInputParameter(0, defaultMAPeriod);
        mainInputMA.setOptInputParameter(1, IIndicators.MaType.SMA.ordinal());
        additionalInputMA = provider.getIndicator("MA");
        additionalInputMA.setOptInputParameter(0, defaultMAPeriod);
        mainInputMA.setOptInputParameter(1, IIndicators.MaType.SMA.ordinal());

        //indicator info
        indicatorInfo = new IndicatorInfo("INDEXIND", "MA for various instruments", "", true, false, true, 4, 2, 4);

        //indicator inputs
        InputParameterInfo gbpUsdInput = new InputParameterInfo("Input data", InputParameterInfo.Type.BAR);
        gbpUsdInput.setInstrument(Instrument.GBPUSD);
        InputParameterInfo audCadInput = new InputParameterInfo("Input data", InputParameterInfo.Type.BAR);
        audCadInput.setInstrument(Instrument.AUDCAD);
        InputParameterInfo eurJpyInput = new InputParameterInfo("Input data", InputParameterInfo.Type.BAR);
        eurJpyInput.setInstrument(Instrument.EURJPY);
        inputParameterInfos = new InputParameterInfo[] {
                new InputParameterInfo("Input data", InputParameterInfo.Type.BAR),
                gbpUsdInput, audCadInput, eurJpyInput};

        //optional parameters
        int[] maValues = new int[IIndicators.MaType.values().length];
        String[] maNames = new String[IIndicators.MaType.values().length];
        for (int i = 0; i < maValues.length; i++) {
            maValues[i] = i;
            maNames[i] = IIndicators.MaType.values()[i].name();
        }
        optInputParameterInfos = new OptInputParameterInfo[] {
                new OptInputParameterInfo("Time cPeriod", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(defaultMAPeriod, 2, 100, 1)),
                new OptInputParameterInfo("MA Type", OptInputParameterInfo.Type.OTHER, new IntegerListDescription(IIndicators.MaType.SMA.ordinal(), maValues, maNames))
        };

        OutputParameterInfo gbpUsdOutput = new OutputParameterInfo("gbpUsd_MA", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LINE);
        OutputParameterInfo audCadOutput = new OutputParameterInfo("audCad_MA", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LINE);
        OutputParameterInfo eurJpyOutput = new OutputParameterInfo("eurJpy_MA", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LINE);

        //manual drawing is used here to display moving averages on any chart (MA values differ for various instruments)
        gbpUsdOutput.setDrawnByIndicator(true);
        audCadOutput.setDrawnByIndicator(true);
        eurJpyOutput.setDrawnByIndicator(true);

        outputParameterInfos = new OutputParameterInfo[] {
                new OutputParameterInfo("MA", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LINE),
                gbpUsdOutput, audCadOutput, eurJpyOutput};
    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
        double[] mmaInput = new double[inputs[0].length];
        for (int i = 0; i < inputs[0].length; i++) {
            IBar bar = inputs[0][i];
            mmaInput[i] = bar.getClose();
        }
        mainInputMA.setInputParameter(0, mmaInput);
        mainInputMA.setOutputParameter(0, outputs[0]);
        IndicatorResult result = mainInputMA.calculate(startIndex, endIndex);

        for (int i = 1; i < inputs.length; i++){
            calculateMA(i, result);
        }
        return result;
    }

    private void calculateMA(int inputIndex, IndicatorResult result){
        double[] maInput = new double[inputs[inputIndex].length];
        for (int i = 0; i < inputs[inputIndex].length; i++) {
            IBar bar = inputs[inputIndex][i];
            maInput[i] = bar.getClose();
        }

        if (maInput.length - additionalInputMA.getLookback() > 0) {
            double[] maOutput = new double[maInput.length - additionalInputMA.getLookback()];
            additionalInputMA.setInputParameter(0, maInput);
            additionalInputMA.setOutputParameter(0, maOutput);
            IndicatorResult maResult = additionalInputMA.calculate(0, maInput.length - 1);

            int j = 0;
            for (int i = 0; i < result.getNumberOfElements(); i++) {
                IBar bar = inputs[0][i + result.getFirstValueIndex()];
                long barTime = bar.getTime();

                while (j < maResult.getFirstValueIndex() + maResult.getNumberOfElements() && inputs[inputIndex][j].getTime() < barTime) {
                    j++;
                }
                if (j >= maResult.getFirstValueIndex() + maResult.getNumberOfElements() || inputs[inputIndex][j].getTime() != barTime || j < maResult.getFirstValueIndex()) {
                    outputs[inputIndex][i] = Double.NaN;
                } else {
                    outputs[inputIndex][i] = maOutput[j - maResult.getFirstValueIndex()];
                }
            }
        } else {
            for (int i = 0; i < result.getNumberOfElements(); i++) {
                outputs[inputIndex][i] = Double.NaN;
            }
        }
    }

    public Point drawOutput(Graphics g, int outputIdx, Object valuesArr, Color color, Stroke stroke, IIndicatorDrawingSupport indicatorDrawingSupport, java.util.List<Shape> shapes, Map<Color, java.util.List<Point>> handles) {
        IBar[] candles = indicatorDrawingSupport.getCandles();
        double[] values = (double[]) valuesArr;
        if (candles.length != values.length) {
            console.getErr().println(candles.length + " <> " + values.length);
        }
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double maMin = Double.MAX_VALUE;
        double maMax = Double.MIN_VALUE;
        for (int i = indicatorDrawingSupport.getIndexOfFirstCandleOnScreen() - 1, j = indicatorDrawingSupport.getIndexOfFirstCandleOnScreen() + indicatorDrawingSupport.getNumberOfCandlesOnScreen() + 1; i < j; i++) {
            if (i < 0 || i >= candles.length) {
                continue;
            }
            IBar candle = candles[i];
            min = Math.min(min, candle.getLow());
            max = Math.max(max, candle.getHigh());
            if (!Double.isNaN(values[i])) {
                maMin = Math.min(maMin, values[i]);
                maMax = Math.max(maMax, values[i]);
            }
        }
        GeneralPath path = new GeneralPath();
        boolean plottingStarted = false;
        float x = 0;
        float y = 0;
        for (int i = indicatorDrawingSupport.getIndexOfFirstCandleOnScreen() - 1, j = indicatorDrawingSupport.getIndexOfFirstCandleOnScreen() + indicatorDrawingSupport.getNumberOfCandlesOnScreen() + 1; i < j; i++) {
            if (i < 0 || i >= candles.length) {
                continue;
            }
            if (Double.isNaN(values[i])) {
                continue;
            }
            double allignedValue = ((values[i] - maMin) / (maMax - maMin)) * (max - min) + min;
            x = indicatorDrawingSupport.getMiddleOfCandle(i);
            y = indicatorDrawingSupport.getYForValue(allignedValue);
            if (plottingStarted) {
                path.lineTo(x, y);
            } else {
                path.moveTo(x, y);
                plottingStarted = true;
            }
        }
        g.setColor(color);
        ((Graphics2D) g).setStroke(stroke);
        ((Graphics2D) g).draw(path);
        return new Point((int) x, (int) y);
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
        return Math.max(mainInputMA.getLookback(), additionalInputMA.getLookback());
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
        switch (index) {
            case 0:
                mainInputMA.setOptInputParameter(0, (Integer) value);
                additionalInputMA.setOptInputParameter(0, (Integer) value);
                break;
            case 1:
                mainInputMA.setOptInputParameter(1, (Integer) value);
                additionalInputMA.setOptInputParameter(1, (Integer) value);
                break;
            default:
                throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }
}
