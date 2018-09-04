package com.jforexcn.inbox.indicator;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IChartObject;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.LineStyle;
import com.dukascopy.api.Period;
import com.dukascopy.api.drawings.IChartObjectFactory;
import com.dukascopy.api.drawings.IRectangleChartObject;
import com.dukascopy.api.drawings.ITextChartObject;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.indicators.BooleanOptInputDescription;
import com.dukascopy.api.indicators.DoubleRangeDescription;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorChartPanel;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IIndicatorsProvider;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.IntegerRangeDescription;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by simple(simple.continue@gmail.com) on 22/12/2017.
 */


public class BarBreakPattern2 implements IIndicator {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private IBar[][] inputs = new IBar[1][];
    private int[][] outputs = new int[2][];
    private IFeedDescriptor feedDescriptor;

    private boolean  isDrawRect = true;
    private int breakBarOutOfRectPips = 30;
    private int minLookback = 10;
    private int maxLookback = 200;
    private IIndicator atr;
    private int atrTimePeriod = 10;
    private double atrMultiple = 100;
    private double[][][] atrInputs = new double[1][][];


    private IIndicatorContext context;
    private IConsole console;

    private IIndicatorChartPanel chart;
    private IChartObjectFactory factory;

    private HashMap<String, Rect> rectMap;


    public void onStart(IIndicatorContext context) {
        rectMap = new HashMap<>();
        feedDescriptor = context.getFeedDescriptor();

        IIndicatorsProvider provider = context.getIndicatorsProvider();
        atr = provider.getIndicator("ATR");
        atr.setOptInputParameter(0, atrTimePeriod);

        indicatorInfo =
                new IndicatorInfo("sBARBREAK2", "Bar Break", "Pattern Recognition", true, false, false, 2, 6, 2);
        indicatorInfo.setUnstablePeriod(true);
        inputParameterInfos = new InputParameterInfo[ ]{
                new InputParameterInfo("Bar", InputParameterInfo.Type.BAR),
                new InputParameterInfo("Atr Price", InputParameterInfo.Type.PRICE)
        };

        optInputParameterInfos = new OptInputParameterInfo[]{
                new OptInputParameterInfo("Break Bar Break Pips", OptInputParameterInfo.Type.OTHER
                        , new IntegerRangeDescription(30, 1, 2000, 1)),
                new OptInputParameterInfo("Min Look Back Candle", OptInputParameterInfo.Type.OTHER
                        , new IntegerRangeDescription(10, 1, 2000, 1)),
                new OptInputParameterInfo("Max Look Back Candle", OptInputParameterInfo.Type.OTHER
                        , new IntegerRangeDescription(200, 10, 2000, 1)),
                new OptInputParameterInfo("Draw Rect", OptInputParameterInfo.Type.OTHER
                        , new BooleanOptInputDescription(true)),
                new OptInputParameterInfo("ATR Time cPeriod", OptInputParameterInfo.Type.OTHER
                        , new IntegerRangeDescription(10, 1, 2000, 1)),
                new OptInputParameterInfo("ATR Mutiple", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(100, 0.1, 2000, 1, 1))

        };

        outputParameterInfos = new OutputParameterInfo[] {
                new OutputParameterInfo("Break Up Bar", OutputParameterInfo.Type.INT, OutputParameterInfo.DrawingStyle.PATTERN_BOOL) {
                    {
                        setColor(Color.GREEN);
                    }
                },
                new OutputParameterInfo("Break Down Bar", OutputParameterInfo.Type.INT, OutputParameterInfo.DrawingStyle.PATTERN_BOOL) {
                    {
                        setColor(Color.RED);
                    }
                }
        };

        this.context = context;
        this.console = context.getConsole();
    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
//        console.getInfo().println("startIndex: " + startIndex + ". endIndex: " + endIndex);
        //calculating startIndex taking into account maxLookback value
        if (startIndex - getLookback() < 0) {
            startIndex -= startIndex - getLookback();
        }

        if (startIndex > endIndex) {
            return new IndicatorResult(0, 0);
        }

        int atrStartIndex = startIndex - maxLookback - 1;
        int atrLength = endIndex - atrStartIndex + 1;
        if (atrLength < 0) {
            return new IndicatorResult(startIndex, endIndex - startIndex + 1);
        }
        double[] atrOutputs = new double[atrLength];
        atr.setInputParameter(0, atrInputs[0]);
        atr.setOutputParameter(0, atrOutputs);
//        console.getInfo().println("maxLookback:" + maxLookback + ", atrInputs[0]:" + atrInputs[0][0].length);
        atr.calculate(atrStartIndex, endIndex);

        double pipValue = feedDescriptor.getInstrument().getPipValue();
        int k, l;
        IBar lastBreakBar = null;
        for (k = startIndex, l = 0; k <= endIndex; k++, l++) {
            if (isSameDirectionWithLastBreakBar(lastBreakBar, k)) {
                continue;
            }
            lastBreakBar = null;
            BreakPattern breakPattern = new BreakPattern(inputs[0][k], pipValue);
            int i, m;
            // calculate top and bottom
            for (i = k - 1, m = 1; i >= k - maxLookback; i--, m++) {
                double prevAtr = atrOutputs[i - 1 - atrStartIndex];
                if (breakPattern.merge(inputs[0][i], prevAtr)) {
                    break;
                }
                if (m == maxLookback && breakPattern.getRectArray().size() == 0) {
                    IBar barFrom = inputs[0][k - 1 - 20];
                    Rect rect;
                    if (inputs[0][k].getClose() > inputs[0][k].getOpen()) {
                        rect = new Rect(barFrom.getTime(), breakPattern.section.getLastTop() - 2 * pipValue,
                                inputs[0][k].getTime(), breakPattern.section.getLastTop(), false, false,
                                prevAtr, barFrom);
                    } else {
                        rect = new Rect(barFrom.getTime(), breakPattern.section.getLastBottom() + 2 * pipValue,
                                inputs[0][k].getTime(), breakPattern.section.getLastBottom(), false, false,
                                prevAtr, barFrom);
                    }
                    breakPattern.getRectArray().add(rect);
                }
            }

            if (factory == null) {
                chart  = context.getIndicatorChartPanel();
                //no chart opened - either indicator called from strategy or indicator
                if (chart == null) {
                    return new IndicatorResult(startIndex, endIndex - startIndex + 1);
                }
                factory = chart.getChartObjectFactory();
            }

            if (outputs[0][l] != 1 && outputs[1][l] != 1) {
                if (breakPattern.getRectArray().size() <= 0) {
                    // draw nothing
                    outputs[0][l] = 0;
                    outputs[1][l] = 0;
                } else {
                    if (breakPattern.getRectArray().size() > 0 && isDrawRect && isNotContinuous(l)) {
                        lastBreakBar = inputs[0][k];
                        // draw rect
                        for (Rect rect : breakPattern.getRectArray()) {
                            String rectKey = String.valueOf(rect.getTime2());
                            if (rectMap.get(rectKey) == null) {
                                String guid = UUID.randomUUID().toString();
                                IRectangleChartObject rectChart = factory.createRectangle("Rect_" + guid,
                                        rect.getTime1(), rect.getPrice1(),
                                        rect.getTime2(), rect.getPrice2()
                                );
                                rectChart.setLineWidth(1);
                                rectChart.setLineStyle(LineStyle.DASH);
                                rectChart.setColor(Color.ORANGE);
                                chart.add(rectChart);
                                rectMap.put(rectKey, rect);

//                                guid = UUID.randomUUID().toString();
//                                ITextChartObject textChartObject = factory.createText("Text_" + guid, rect.getAtrBar().getTime(), rect.getAtrBar().getHigh() + 10 * pipValue);
//                                textChartObject.setFontColor(Color.BLUE);
//                                textChartObject.setLineStyle(1);
//                                chart.add(textChartObject);
                            }
                        }
                    }

                    if (breakPattern.isBrokeTop()) {
                        outputs[0][l] = 1;
                    } else if (breakPattern.isBrokeBottom()) {
                        outputs[1][l] = 1;
                    }
                }
            }
        }

        return new IndicatorResult(startIndex, l);
    }

    private boolean isNotContinuous(int l) {
        return l == 0 || (outputs[0][l - 1] == 0 && outputs[1][l - 1] == 0);
    }

    private boolean isSameDirectionWithLastBreakBar(IBar lastBreakBar, int k) {
        return lastBreakBar != null &&
                ((isLong(inputs[0][k]) && isLong(lastBreakBar)) || (!isLong(inputs[0][k]) && !isLong(lastBreakBar)));
    }

    private boolean isLong(IBar bar) {
        return bar.getClose() > bar.getOpen();
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
        return maxLookback + atrTimePeriod + 1;
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
        switch (index) {
            case 0: {
                inputs[0] = (IBar[]) array;
                break;
            }
            case 1: {
                atrInputs[0] = (double[][]) array;
                break;
            }
        }

    }


    public void setOptInputParameter(int index, Object value) {
        switch (index) {
            case 0: {
                breakBarOutOfRectPips = (int) value;
                break;
            }
            case 1: {
                minLookback = (int) value;
                break;
            }
            case 2: {
                maxLookback = (int) value;
                break;
            }
            case 3: {
                isDrawRect = (boolean) value;
                break;
            }
            case 4: {
                atrTimePeriod = (int) value;
                atr.setOptInputParameter(0, atrTimePeriod);
                break;
            }
            case 5: {
                atrMultiple = (double) value;
                break;
            }
        }
    }

    public void setOutputParameter(int index, Object array) {
        if (index < 2) {
            outputs[index] = (int[]) array;
        }
    }

    private class BreakPattern {
        private IBar breakBar;
        private IBar barFrom;
        private IBar barTo;
        private int rectWidth = 0;

        private double pipValue;

        private boolean brokeTop;
        private boolean brokeBottom;

        private ArrayList<Rect> rectArray = new ArrayList<>();
        private Rect lastRect;

        private final Section section;

        BreakPattern(IBar breakBar, double pipValue) {
            this.breakBar = breakBar;
            this.pipValue = pipValue;
            section = new Section(pipValue);
        }

        ArrayList<Rect> getRectArray() {
            return rectArray;
        }

        IBar getBreakBar() {
            return breakBar;
        }

        boolean isBrokeTop() {
            return brokeTop;
        }

        boolean isBrokeBottom() {
            return brokeBottom;
        }

        boolean merge(IBar bar, double atr) {
            // first set bar
            if (barTo == null) {
                barTo = bar;
            }
            barFrom = bar;

            // second update top and bottom
            section.pushAsTop(Math.max(bar.getOpen(), bar.getClose()));
            section.pushAsBottom(Math.min(bar.getOpen(), bar.getClose()));

            boolean breakTop = checkBreakTop();
            boolean breakBottom = checkBreakBottom();
            boolean followAtrLimit = checkAtr(atr);
            if ((!breakTop && !breakBottom)) {
                // jump out if not break
                if (rectWidth >= minLookback && followAtrLimit) {
                    Rect rect = new Rect(barFrom.getTime(), section.getLastBottom(), barTo.getTime(), section.getLastTop(), false, false, atr, bar);
                    rectArray.add(rect);
                    lastRect = rect;
                }
                return true;
            } else {
                rectWidth = rectWidth + 1;
                brokeTop = breakTop;
                brokeBottom = breakBottom;
            }
            return false;
        }

        boolean checkAtr(double atr) {
            return section.getTop() - section.getBottom() < atr * atrMultiple;
        }

        boolean checkBreakTop() {
            return breakBar.getClose() - section.getTop() > breakBarOutOfRectPips * pipValue;
        }

        boolean checkBreakBottom() {
            return section.getBottom() - breakBar.getClose() > breakBarOutOfRectPips * pipValue;
        }
    }

    private static class Rect {
        private final long time1;
        private final double price1;
        private final long time2;
        private final double price2;
        private final boolean byTop;
        private final boolean byBottom;
        private final double atr;
        private final IBar atrBar;

        Rect(long time1, double price1, long time2, double price2, boolean byTop, boolean byBottom, double atr, IBar atrBar) {
            this.time1 = time1;
            this.price1 = price1;
            this.time2 = time2;
            this.price2 = price2;
            this.byTop = byTop;
            this.byBottom = byBottom;
            this.atr = atr;
            this.atrBar = atrBar;
        }

        long getTime1() {
            return time1;
        }

        double getPrice1() {
            return price1;
        }

        long getTime2() {
            return time2;
        }

        double getPrice2() {
            return price2;
        }

        boolean isByTop() {
            return byTop;
        }

        boolean isByBottom() {
            return byBottom;
        }


        public double getAtr() {
            return atr;
        }

        public IBar getAtrBar() {
            return atrBar;
        }

        public boolean equal(Rect rect1, Rect rect2) {
            return rect1.getTime1() == rect2.getTime1() &&
                    rect1.getTime2() == rect2.getTime2();
        }
    }

    private static class Section {
        private ArrayList<Double> topArray = new ArrayList<>();
        private ArrayList<Double> bottomArray = new ArrayList<>();
        private double top;
        private double bottom;
        private double lastTop;
        private double lastBottom;
        private int rangePips = 30;
        private final double pipValue;
        private double atr;

        Section(double pipValue) {
            this.pipValue = pipValue;
        }

        int getTopArraySize() {
            return topArray.size();
        }

        int getBottomArraySize() {
            return bottomArray.size();
        }

        double getTop() {
            return top;
        }

        double getBottom() {
            return bottom;
        }

        double getLastTop() {
            return lastTop;
        }

        double getLastBottom() {
            return lastBottom;
        }

        void pushAsTop(double apex) {
            if (top == 0 || apex > top - rangePips / 2 * pipValue) {
                lastTop = top;
                topArray.add(apex);
                if (topArray.size() > 3) {
                    topArray.remove(Collections.min(topArray));
                }
                double sum = 0;
                for (double d : topArray) sum += d;
                top = sum / topArray.size();

                if (lastTop == 0) {
                    lastTop = top;
                }
            }
        }

        void pushAsBottom(double apex) {
            if (bottom == 0 || apex < bottom + rangePips / 2 * pipValue) {
                lastBottom = bottom;
                bottomArray.add(apex);
                if (bottomArray.size() > 3) {
                    bottomArray.remove(Collections.max(bottomArray));
                }
                double sum = 0;
                for (double d : bottomArray) sum += d;
                bottom = sum / bottomArray.size();

                if (lastBottom == 0) {
                    lastBottom = bottom;
                }
            }
        }

        public double getAtr() {
            return atr;
        }
    }
}