package com.jforexcn.inbox.indicator.klineshape;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.indicators.DoubleRangeDescription;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.IntegerRangeDescription;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;

import java.awt.Color;

/**
 * Created by simple(simple.continue@gmail.com) on 18/04/2018.
 */

public class TwoBarPattern implements IIndicator {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private IBar[][] inputs = new IBar[1][];
    private int[][] outputs = new int[1][];
    private IFeedDescriptor feedDescriptor;


    private int lookback = 2;

    private double barMinPips1 = 20;
    private double bodyMinPercent1 = 0.35;
    private double bodyMaxPercent1 = 0.35;
    private double upShadowMinPercent1 = 0.15;
    private double upShadowMaxPercent1 = 0.15;
    private double lowShadowMinPercent1 = 0.5;
    private double lowShadowMaxPercent1 = 0.5;


    private double barMinPips2 = 20;
    private double bodyMinPercent2 = 0.35;
    private double bodyMaxPercent2 = 0.35;
    private double upShadowMinPercent2 = 0.15;
    private double upShadowMaxPercent2 = 0.15;
    private double lowShadowMinPercent2 = 0.5;
    private double lowShadowMaxPercent2 = 0.5;

    private double bodyMultiple = 0.7;

    double pipValue;


    public void onStart(IIndicatorContext context) {
        feedDescriptor = context.getFeedDescriptor();

        indicatorInfo =
                new IndicatorInfo("sTwoBarPattern", "Two Bar Pattern", "Custom IIndicator", true, false, false, 1, 15, 1);
        indicatorInfo.setUnstablePeriod(true);
        inputParameterInfos = new InputParameterInfo[ ]{
                new InputParameterInfo("Bar", InputParameterInfo.Type.BAR),
        };

        optInputParameterInfos = new OptInputParameterInfo[]{
                new OptInputParameterInfo("Bar1 High-Low Pips >=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(10, 0, 1000, 1, 1)),
                new OptInputParameterInfo("Bar1 Body Percent >=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(0.6, 0, 1, 0.01, 2)),
                new OptInputParameterInfo("Bar1 Body Percent <=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(1, 0, 1, 0.01, 2)),
                new OptInputParameterInfo("Bar1 Up Shadow Percent >=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(0, 0, 1, 0.01, 2)),
                new OptInputParameterInfo("Bar1 Up Shadow Percent <=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(1, 0, 1, 0.01, 2)),
                new OptInputParameterInfo("Bar1 low Shadow Percent >=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(0, 0, 1, 0.01, 2)),
                new OptInputParameterInfo("Bar1 low Shadow Percent <=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(1, 0, 1, 0.01, 2)),


                new OptInputParameterInfo("Bar2 High-Low Pips >=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(10, 0, 1000, 1, 1)),
                new OptInputParameterInfo("Bar2 Body Percent >=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(0.6, 0, 1, 0.01, 2)),
                new OptInputParameterInfo("Bar2 Body Percent <=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(1, 0, 1, 0.01, 2)),
                new OptInputParameterInfo("Bar2 Up Shadow Percent >=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(0, 0, 1, 0.01, 2)),
                new OptInputParameterInfo("Bar2 Up Shadow Percent <=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(1, 0, 1, 0.01, 2)),
                new OptInputParameterInfo("Bar2 low Shadow Percent >=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(0, 0, 1, 0.01, 2)),
                new OptInputParameterInfo("Bar2 low Shadow Percent <=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(1, 0, 1, 0.01, 2)),

                new OptInputParameterInfo("Bar2 body / Bar1 body >=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(0.7, 0, 100, 0.01, 2)),

        };

        outputParameterInfos = new OutputParameterInfo[] {
                new OutputParameterInfo("TwoBarPattern", OutputParameterInfo.Type.INT, OutputParameterInfo.DrawingStyle.PATTERN_BOOL) {
                    {
                        setColor(Color.GREEN);
                    }
                }
        };
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

        pipValue = feedDescriptor.getInstrument().getPipValue();

        int k, l;
        for (k = startIndex, l = 0; k <= endIndex; k++, l++) {
            IBar bar1 = inputs[0][k - 1];
            IBar bar2 = inputs[0][k];
            if (checkDirection(bar1, bar2) &&
                    checkPips(bar1, barMinPips1) &&
                    checkPips(bar2, barMinPips2) &&
                    checkBody(bar1, bodyMinPercent1, bodyMaxPercent1) &&
                    checkBody(bar2, bodyMinPercent2, bodyMaxPercent2) &&
                    checkUpShadow(bar1, upShadowMinPercent1, upShadowMaxPercent1) &&
                    checkUpShadow(bar2, upShadowMinPercent2, upShadowMaxPercent2) &&
                    checkLowShadow(bar1, lowShadowMinPercent1, lowShadowMaxPercent1) &&
                    checkLowShadow(bar2, lowShadowMinPercent2, lowShadowMaxPercent2) &&
                    checkBodyMultiple(bar1, bar2)
                    ) {
                outputs[0][l] = 1;
            } else {
                outputs[0][l] = 0;
            }
        }
        return new IndicatorResult(startIndex, l);
    }

    private boolean checkDirection(IBar bar1, IBar bar2) {
        return (bar1.getOpen() - bar1.getClose()) * (bar2.getOpen() - bar2.getClose()) < 0 || barMinPips1 == 0;
    }

    private boolean checkPips(IBar bar, double minPips) {
        return (bar.getHigh() - bar.getLow()) >= minPips * pipValue;
    }

    private boolean checkBody(IBar bar, double min, double max) {
        double body = Math.abs(bar.getOpen() - bar.getClose());
        double percent = body / (bar.getHigh() - bar.getLow());
        return percent >= min && percent <= max;
    }

    private boolean checkUpShadow(IBar bar, double min, double max) {
        double upShadow = bar.getHigh() - Math.max(bar.getOpen(), bar.getClose());
        double percent = upShadow / (bar.getHigh() - bar.getLow());
        return percent >= min && percent <= max;
    }

    private boolean checkLowShadow(IBar bar, double min, double max) {
        double lowShadow =  Math.min(bar.getOpen(), bar.getClose()) - bar.getLow();
        double percent = lowShadow / (bar.getHigh() - bar.getLow());
        return percent >= min && percent <= max;
    }

    private boolean checkBodyMultiple(IBar bar1, IBar bar2) {
        double body1 = Math.abs(bar1.getOpen() - bar1.getClose());
        double body2 = Math.abs(bar2.getOpen() - bar2.getClose());
        return body2 >= body1 * bodyMultiple;
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
        return lookback + 1;
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
        }
    }

    public void setOptInputParameter(int index, Object value) {
        switch (index) {
            case 0: {
                barMinPips1 = (double) value;
                break;
            } case 1: {
                bodyMinPercent1 = (double) value;
                break;
            } case 2: {
                bodyMaxPercent1 = (double) value;
                break;
            } case 3: {
                upShadowMinPercent1 = (double) value;
                break;
            } case 4: {
                upShadowMaxPercent1 = (double) value;
                break;
            } case 5: {
                lowShadowMinPercent1 = (double) value;
                break;
            } case 6: {
                lowShadowMaxPercent1 = (double) value;
                break;
            }case 7: {
                barMinPips2 = (double) value;
                break;
            } case 8: {
                bodyMinPercent2 = (double) value;
                break;
            } case 9: {
                bodyMaxPercent2 = (double) value;
                break;
            } case 10: {
                upShadowMinPercent2 = (double) value;
                break;
            } case 11: {
                upShadowMaxPercent2 = (double) value;
                break;
            } case 12: {
                lowShadowMinPercent2 = (double) value;
                break;
            } case 13: {
                lowShadowMaxPercent2 = (double) value;
                break;
            } case 14: {
                bodyMultiple = (double) value;
                break;
            }
        }
    }

    public void setOutputParameter(int index, Object array) {
        if (index < 1) {
            outputs[index] = (int[]) array;
        }
    }
}
