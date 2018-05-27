package com.jforexcn.hub.indicator;

import com.dukascopy.api.IBar;
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
 * Created by simple(simple.continue@gmail.com) on 16/04/2018.
 */

public class OneBarHammer implements IIndicator {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private IBar[][] inputs = new IBar[1][];
    private int[][] outputs = new int[2][];
    private IFeedDescriptor feedDescriptor;

    private int barMinPips = 15;
    private int breakPips = 3;
    private int lookback = 3;

    private double bodyRate = 0.3;
    private double shortShadowRate = 0.2;
    private double longShadowRate = 0.55;

    private IIndicatorContext context;


    public void onStart(IIndicatorContext context) {
        this.context = context;

        indicatorInfo =
                new IndicatorInfo("ONEBARHAMMER", "OneBarHammer", "Custom IIndicator", true, false, false, 1, 6, 2);
        indicatorInfo.setUnstablePeriod(true);
        inputParameterInfos = new InputParameterInfo[ ]{
                new InputParameterInfo("Bar", InputParameterInfo.Type.BAR),
        };

        optInputParameterInfos = new OptInputParameterInfo[]{
                new OptInputParameterInfo("Body Rate <=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(0.3, 0.01, 1, 0.01, 2)),
                new OptInputParameterInfo("Short Shadow Rate <=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(0.2, 0.01, 1, 0.01, 2)),
                new OptInputParameterInfo("Long Shadow Rate >=", OptInputParameterInfo.Type.OTHER
                        , new DoubleRangeDescription(0.55, 0.01, 1, 0.01, 2)),
                new OptInputParameterInfo("Bar Min Pips", OptInputParameterInfo.Type.OTHER
                        , new IntegerRangeDescription(15, 1, 2000, 1)),
                new OptInputParameterInfo("Back Compare to", OptInputParameterInfo.Type.OTHER
                        , new IntegerRangeDescription(3, 1, 2000, 1)),
                new OptInputParameterInfo("Break How Many Pips", OptInputParameterInfo.Type.OTHER
                        , new IntegerRangeDescription(3, 0, 2000, 1))
        };

        outputParameterInfos = new OutputParameterInfo[] {
                new OutputParameterInfo("Kick Up Hammer Bar", OutputParameterInfo.Type.INT, OutputParameterInfo.DrawingStyle.PATTERN_BOOL) {
                    {
                        setColor(Color.GREEN);
                    }
                },
                new OutputParameterInfo("Kick Down Hammer Bar", OutputParameterInfo.Type.INT, OutputParameterInfo.DrawingStyle.PATTERN_BOOL) {
                    {
                        setColor(Color.RED);
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
        feedDescriptor = context.getFeedDescriptor();
        double pipValue = feedDescriptor.getInstrument().getPipValue();

        int k, l;
        for (k = startIndex, l = 0; k <= endIndex; k++, l++) {
            IBar bar = inputs[0][k];
//            实体不超过本K线总体35%长度
//            上影线不超过总体15%
//            下影线长度至少为总体的50%
            double hl = bar.getHigh() - bar.getLow();
            double body = Math.abs(bar.getOpen() - bar.getClose());
            double upper = bar.getHigh() - Math.max(bar.getOpen(), bar.getClose());
            double lower = Math.min(bar.getOpen(), bar.getClose()) - bar.getLow();
            if (body <= hl * bodyRate) {
                //kick up
                if (upper <= hl * shortShadowRate &&
                        lower >= hl * longShadowRate) {
                    if (isBigEnough(k, pipValue) && isLowest(k, pipValue)) {
                        outputs[0][l] = 1;
                    } else {
                        outputs[0][l] = 0;
                    }
                }

                // kick down
                if (lower <= hl * shortShadowRate &&
                        upper >= hl * longShadowRate) {
                    if (isBigEnough(k, pipValue) && isHighest(k, pipValue)) {
                        outputs[1][l] = 1;
                    } else {
                        outputs[1][l] = 0;
                    }
                }
            }
        }
        return new IndicatorResult(startIndex, l);
    }

    public boolean isBigEnough(int k, double pipValue)  {
        IBar bar = inputs[0][k];
        return (bar.getHigh() - bar.getLow()) > (barMinPips * pipValue);
    }


    public boolean isHighest(int k, double pipValue)  {
        IBar bar = inputs[0][k];
        double high = bar.getHigh();
        for (int j = k - 1; j >= k - lookback; j--) {
            if ((high - inputs[0][j].getHigh()) < breakPips * pipValue) {
                return false;
            }
        }
        return true;
    }

    public boolean isLowest(int k, double pipValue)  {
        IBar bar = inputs[0][k];
        double low = bar.getLow();
        for (int j = k - 1; j >= k - lookback; j--) {
            if ((inputs[0][j].getLow() - low) < breakPips * pipValue) {
                return false;
            }
        }
        return true;
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
                bodyRate = (double) value;
                break;
            } case 1: {
                shortShadowRate = (double) value;
                break;
            } case 2: {
                longShadowRate = (double) value;
                break;
            } case 3: {
                barMinPips = (int) value;
                break;
            } case 4: {
                lookback = (int) value;
                break;
            } case 5: {
                breakPips = (int) value;
                break;
            }
        }
    }

    public void setOutputParameter(int index, Object array) {
        if (index < 2) {
            outputs[index] = (int[]) array;
        }
    }
}
