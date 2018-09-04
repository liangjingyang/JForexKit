package com.jforexcn.inbox.indicator;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.LineStyle;
import com.dukascopy.api.drawings.IChartObjectFactory;
import com.dukascopy.api.drawings.IRectangleChartObject;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorChartPanel;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * Created by simple(simple.continue@gmail.com) on 22/12/2017.
 */


public class BarBreakPattern implements IIndicator {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
//    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private IBar[][] inputs = new IBar[1][];
    private int[][] outputs = new int[2][];
    private IFeedDescriptor feedDescriptor;
    private int lookback = 200;

    private IIndicatorContext context;
    private IConsole console;

    private IIndicatorChartPanel chart;
    private IChartObjectFactory factory;

    private static String GUID;

    public void onStart(IIndicatorContext context) {
        indicatorInfo =
                new IndicatorInfo("sBARBREAK", "Bar Break", "Pattern Recognition", true, false, false, 1, 0, 2);
        indicatorInfo.setUnstablePeriod(true);
        inputParameterInfos = new InputParameterInfo[ ]{ new InputParameterInfo("Bar", InputParameterInfo.Type.BAR) };

//        optInputParameterInfos = new OptInputParameterInfo[]{
//                new OptInputParameterInfo("Look Back Bars", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(lookback, 3, 1000, 1))
//        };

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
        feedDescriptor = context.getFeedDescriptor();
        this.context = context;
        this.console = context.getConsole();
    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
        console.getInfo().println("startIndex: " + startIndex + ". endIndex: " + endIndex);
        //calculating startIndex taking into account lookback value
        if (startIndex - getLookback() < 0) {
            startIndex -= startIndex - getLookback();
        }

        if (startIndex > endIndex) {
            return new IndicatorResult(0, 0);
        }

        double pipValue = feedDescriptor.getInstrument().getPipValue();

        int k, l;
        for (k = startIndex, l = 0; k <= endIndex; k++, l++) {
            BreakPattern breakPattern = new BreakPattern(inputs[0][k], pipValue);
            int i;
            // calculate top and bottom
            for (i = k - 1; i >= k - lookback; i--) {
                if (breakPattern.merge(inputs[0][i])) {
                    break;
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
                    if (breakPattern.getRectArray().size() > 0) {

                        // draw rect
                        for (Rect rect : breakPattern.getRectArray()) {
                            GUID = UUID.randomUUID().toString();
                            IRectangleChartObject rectChart = factory.createRectangle("Rect_" + GUID,
                                    rect.getTime1(), rect.getPrice1(),
                                    rect.getTime2(), rect.getPrice2()
                            );
                            rectChart.setLineWidth(1);
                            rectChart.setLineStyle(LineStyle.DASH);
                            rectChart.setColor(Color.ORANGE);
                            chart.add(rectChart);
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
        return lookback;
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
        if (index == 0) {
            lookback = (int) value;
        }
    }

    public void setOutputParameter(int index, Object array) {
        if (index < 2) {
            outputs[index] = (int[]) array;
        }
    }

    private static class BreakPattern {
        private IBar breakBar;
        private IBar barFrom;
        private IBar barTo;
        private int rectWidth = 0;

        private double pipValue;

        private int breakBarOutOfRectPips = 30;
        private int minLookback = 10;

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

        boolean merge(IBar bar) {
            // first set bar
            if (barTo == null) {
                barTo = bar;
            }
            barFrom = bar;

            // second update top and bottom
            boolean isNewTopSection = section.pushAsTop(Math.max(bar.getOpen(), bar.getClose()));
            boolean isNewBottomSection = section.pushAsBottom(Math.min(bar.getOpen(), bar.getClose()));

            // third update touch top and touch bottom
            if (rectWidth >= minLookback) {
                if (isNewTopSection && isNewBottomSection) {
                    Rect rect = new Rect(barFrom.getTime(), section.getLastBottom(), barTo.getTime(), section.getLastTop(), true, true);
                    rectArray.add(rect);
                    lastRect = rect;
                } else if (isNewTopSection &&
                        section.getBottomArraySize() >= 1 &&
                        (lastRect == null || !lastRect.isByTop())) {
                    Rect rect = new Rect(barFrom.getTime(), section.getLastBottom(), barTo.getTime(), section.getLastTop(), true, false);
                    rectArray.add(rect);
                    lastRect = rect;
                } else if (isNewBottomSection &&
                        section.getTopArraySize() >= 1 &&
                        (lastRect == null || !lastRect.isByBottom())) {
                    Rect rect = new Rect(barFrom.getTime(), section.getLastBottom(), barTo.getTime(), section.getLastTop(), false, true);
                    rectArray.add(rect);
                    lastRect = rect;
                }
            }

            // fourth
            boolean breakTop = checkBreakTop();
            boolean breakBottom = checkBreakBottom();
            if (!breakTop && !breakBottom) {
                // jump out if not break
                if (rectWidth >= minLookback) {
                    Rect rect = new Rect(barFrom.getTime(), section.getLastBottom(), barTo.getTime(), section.getLastTop(), false, false);
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

        Rect(long time1, double price1, long time2, double price2, boolean byTop, boolean byBottom) {
            this.time1 = time1;
            this.price1 = price1;
            this.time2 = time2;
            this.price2 = price2;
            this.byTop = byTop;
            this.byBottom = byBottom;
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

        boolean pushAsTop(double apex) {
            if (top == 0 || apex > top - rangePips * pipValue) {
                lastTop = top;
                topArray.add(apex);
                top = Collections.max(topArray);
                if (lastTop == 0) {
                    lastTop = top;
                }
                if (topArray.size() > 3) {
                    topArray.remove(Collections.min(topArray));
                }
                ArrayList<Double> newTopArray = new ArrayList<>();
                for (double p : topArray) {
                    if (top - p <= rangePips * pipValue) {
                        newTopArray.add(p);
                    }
                }
                // add one apex and remove more than 1 apex, the removed 2 (or more) apexes generate a rect
                boolean isNewTopSection = newTopArray.size() - topArray.size() < -1;
                topArray = newTopArray;
                return isNewTopSection;
            }
            return false;
        }

        boolean pushAsBottom(double apex) {
            if (bottom == 0 || apex < bottom + rangePips * pipValue) {
                lastBottom = bottom;
                bottomArray.add(apex);
                bottom = Collections.min(bottomArray);
                if (lastBottom == 0) {
                    lastBottom = bottom;
                }
                if (bottomArray.size() > 3) {
                    bottomArray.remove(Collections.max(bottomArray));
                }
                ArrayList<Double> newBottomArray = new ArrayList<>();
                for (double p : bottomArray) {
                    if (p - bottom <= rangePips * pipValue) {
                        newBottomArray.add(p);
                    }
                }
                // add one apex and remove more than 1 apex, the removed 2 (or more) apexes generate a rect
                boolean isNewBottomSection = newBottomArray.size() - bottomArray.size() < -1;
                bottomArray = newBottomArray;
                return isNewBottomSection;
            }
            return false;
        }
    }
}