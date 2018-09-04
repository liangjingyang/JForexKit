package com.jforexcn.inbox.indicator;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.JFTimeZone;
import com.dukascopy.api.Period;
import com.dukascopy.api.Unit;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.indicators.BooleanOptInputDescription;
import com.dukascopy.api.indicators.DoubleRangeDescription;
import com.dukascopy.api.indicators.IDrawingIndicator;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IIndicatorDrawingSupport;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.IntegerRangeDescription;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by simple(simple.continue@gmail.com) on 16/04/2018.
 */

public class MergeTickBarIndicator implements IIndicator, IDrawingIndicator {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private IBar[][] inputs = new IBar[1][];
    private Object[][] outputs = new Object[1][];
    private IConsole console;

    private int mergeBars = 3;
    private int offsetPips = 0;
    private boolean showWeekNumber = true;
    private boolean debug = false;
    private IFeedDescriptor feedDescriptor;
    private double pipValue;
    private MergedBar currentMergedBar;

    public void onStart(IIndicatorContext context) {

        feedDescriptor = context.getFeedDescriptor();

        indicatorInfo =
                new IndicatorInfo("MergeTickBar v2.0", "Merge Tick Bar", "Custom IIndicator", true, false, true, 1, 4, 1);
//        indicatorInfo.setUnstablePeriod(true);
        inputParameterInfos = new InputParameterInfo[ ]{
                new InputParameterInfo("Bar", InputParameterInfo.Type.BAR),
        };

        optInputParameterInfos = new OptInputParameterInfo[]{
                new OptInputParameterInfo("Merge Bars", OptInputParameterInfo.Type.OTHER
                        , new IntegerRangeDescription(3, 1, 10, 1)),
                new OptInputParameterInfo("Offset Pips", OptInputParameterInfo.Type.OTHER
                        , new IntegerRangeDescription(0, -200, 200, 1)),
                new OptInputParameterInfo("Show Week Number", OptInputParameterInfo.Type.OTHER
                        , new BooleanOptInputDescription(true)),
                new OptInputParameterInfo("Debug", OptInputParameterInfo.Type.OTHER
                        , new BooleanOptInputDescription(false))
        };

        outputParameterInfos = new OutputParameterInfo[] {
                createOutputParameter("Merged Bar")
        };
        this.console = context.getConsole();

    }

    private OutputParameterInfo createOutputParameter(String name){
        OutputParameterInfo param =
                new OutputParameterInfo(name, OutputParameterInfo.Type.OBJECT, OutputParameterInfo.DrawingStyle.NONE, false);
        param.setDrawnByIndicator(true);
        param.setColor(new Color(41, 158, 97));
        param.setColor2(new Color(219, 82, 74));
        return param;
    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
        pipValue = feedDescriptor.getInstrument().getPipValue();

        //calculating startIndex taking into account maxLookback value
        if (startIndex - getLookback() < 0) {
            startIndex -= startIndex - getLookback();
        }

        if (startIndex > endIndex) {
            return new IndicatorResult(0, 0);
        }
        if (debug) {
            console.getInfo().println("startIndex: " + startIndex + ". endIndex: " + endIndex + ", last Bar Time: " + inputs[0][endIndex].getTime());
        }
        int k, l;
        int week = 0;
        IBar lastBar = null;
        Calendar cal = Calendar.getInstance();
        boolean needNewBar;
        if (startIndex == endIndex) {
            IBar bar = inputs[0][endIndex];
            if (currentMergedBar.getSize() >= mergeBars && bar.getTime() > currentMergedBar.getLastTime()) {
                currentMergedBar = new MergedBar();
            }
            currentMergedBar.merge(bar);
            int size = currentMergedBar.getSize();
            if (size > 1) {
                for (int i = 0; i < currentMergedBar.getSize(); i++) {
                    outputs[0][i] = null;
                }
            }
            outputs[0][size - 1] = currentMergedBar;
            return new IndicatorResult(startIndex - size + 1, size);
        } else {
            MergedBar mergedBar = new MergedBar();
            for (k = startIndex, l = 0; k <= endIndex; k++, l++) {
                needNewBar = false;
                IBar bar = inputs[0][k];
                if (mergedBar.getSize() == mergeBars) {
                    // 合并够数了
                    for (int i = 0; i < mergedBar.getSize(); i++) {
                        if (l > i) {
                            outputs[0][l - 1 - i] = null;
                        }
                    }
                    outputs[0][l - 1] = mergedBar;
                    needNewBar = true;
                } else if (isSunday(bar, cal)) {
                    // 来到了周日的bar
                    if (lastBar == null) {
                        // 没有之前的bar
                        week = week + 1;
                        needNewBar = true;
                    } else if (!isSunday(lastBar, cal)) {
                        // 上一个Bar是上周的, 先结束, 不合并
                        for (int i = 0; i < mergedBar.getSize(); i++) {
                            if (l >= i) {
                                outputs[0][l - 1 - i] = null;
                            }
                        }
                        outputs[0][l - 1] = mergedBar;
                        week = week + 1;
                        needNewBar = true;
                    }
                }

                if (needNewBar) {
                    mergedBar = new MergedBar();
                    mergedBar.setWeek(week);
                }
                mergedBar.merge(bar);
                for (int i = 0; i < mergedBar.getSize(); i++) {
                    if (l >= i) {
                        outputs[0][l - i] = null;
                    }
                }
                outputs[0][l] = mergedBar;
                lastBar = bar;
                if (currentMergedBar == null || mergedBar.getLastTime() >= currentMergedBar.getLastTime()) {
                    currentMergedBar = mergedBar;
                }
            }
            return new IndicatorResult(startIndex, l);
        }

    }

    @Override
    public Point drawOutput(
            Graphics g,
            int outputIdx,
            Object values,
            Color color,
            Stroke stroke,
            IIndicatorDrawingSupport indicatorDrawingSupport,
            List<Shape> shapes,
            Map<Color, List<Point>> handles
    ) {
        if (values == null) {
            return null;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(new Font("Dialog Input", Font.PLAIN, 9));
        FontMetrics fontMetrics = g2.getFontMetrics();

        Object[] bars = (Object[]) values;
        if (debug) {
            console.getInfo().println("====== draw bars.length: " + bars.length);
            if (bars.length >= 3) {
                console.getInfo().println("====== draw last:  " + bars[bars.length - 1] + ", last2: " + bars[bars.length - 2] + ", last3: " + bars[bars.length - 3]);
            }
        }
        for (int i = 0; i < bars.length; i++ ) {
            MergedBar bar = (MergedBar) bars[i];
            if (bar == null) {
                continue;
            }
            if (i == bars.length - 2 && bar.getSize() < mergeBars) {
                continue;
            }
            bar.setOffset(offsetPips * pipValue);
            float candleWidth = indicatorDrawingSupport.getCandleWidthInPixels();
            float candleSpace = indicatorDrawingSupport.getSpaceBetweenCandlesInPixels();
            float startX = indicatorDrawingSupport.getXForTime(bar.getTime()) - candleWidth / 2;
//            startX = startX - (candleWidth + candleSpace) * (bar.getSize() - 1);
            float width = candleWidth * bar.getSize() + candleSpace * (bar.getSize() - 1);
            float openY = indicatorDrawingSupport.getYForValue(bar.getOpen());
            float closeY = indicatorDrawingSupport.getYForValue(bar.getClose());
            float highY = indicatorDrawingSupport.getYForValue(bar.getHigh());
            float lowY = indicatorDrawingSupport.getYForValue(bar.getLow());
            float middleX = startX + width / 2;
            Rectangle rectangle = new Rectangle((int) startX+1, (int) Math.min(openY, closeY) - 1, (int) width, (int) Math.abs(openY - closeY) + 2);
            float derive = 1.0f;

            g2.setColor(color);
            if (bar.getClose() < bar.getOpen()) {
                g2.setColor(indicatorDrawingSupport.getDowntrendColor());
            }
            g2.setComposite(AlphaComposite.SrcOver.derive(derive));
            g2.fill(rectangle);
            shapes.add((Shape) rectangle.clone());

            g2.drawLine((int) middleX, (int) highY, (int) middleX, (int) Math.min(openY, closeY));
            g2.drawLine((int) middleX, (int) Math.max(openY, closeY), (int) middleX, (int) lowY);

            if (showWeekNumber) {
                String label = String.valueOf(bar.getWeek());
                int labelWidth = fontMetrics.stringWidth(label);
                int labelHeight = fontMetrics.getHeight();
                Rectangle labelRect = new Rectangle((int) (middleX - labelWidth / 2), (int) (lowY + 3), labelWidth, labelHeight);
                g2.setColor(new Color(128, 128, 128));
                g2.drawString(label, labelRect.x, labelRect.y + labelRect.height - 3);
            }
        }

        return null;
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
        return mergeBars;
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
                mergeBars = (int) value;
                break;
            }
            case 1: {
                offsetPips = (int) value;
                break;
            }
            case 2: {
                showWeekNumber = (boolean) value;
                break;
            }
            case 3: {
                debug = (boolean) value;
                break;
            }
        }
    }

    public void setOutputParameter(int index, Object array) {
        if (index < 2) {
            outputs[index] = (Object[]) array;
        }
    }

    private boolean isSunday(IBar bar, Calendar cal) {
        cal.setTimeInMillis(bar.getTime());
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SUNDAY;
    }

    public class MergedBar implements IBar {

        private double open;
        private double close;
        private double low;
        private double high;
        private double volume;
        private long time;
        private int size;
        private long lastTime;
        private int week;
        private double offset;

        public MergedBar() {
        }

        public MergedBar(IBar bar) {
            this.size = 1;
            this.open = bar.getOpen();
            this.close = bar.getClose();
            this.low = bar.getLow();
            this.high = bar.getHigh();
            this.volume = bar.getVolume();
            this.time = bar.getTime();
            this.lastTime = bar.getTime();
        }

        public MergedBar(List<IBar> bars) {
            for (int i = 0; i < bars.size(); i++) {
                IBar bar = bars.get(i);
                if (i == 0) {
                    this.size = 1;
                    this.open = bar.getOpen();
                    this.close = bar.getClose();
                    this.low = bar.getLow();
                    this.high = bar.getHigh();
                    this.volume = bar.getVolume();
                    this.time = bar.getTime();
                    this.lastTime = bar.getTime();
                } else {
                    this.merge(bar);
                }
            }
        }

        public void merge(IBar bar) {
            if (this.size == 0) {
                this.size = 1;
                this.open = bar.getOpen();
                this.close = bar.getClose();
                this.low = bar.getLow();
                this.high = bar.getHigh();
                this.volume = bar.getVolume();
                this.time = bar.getTime();
                this.lastTime = bar.getTime();
            } else {
                if (bar.getTime() > this.lastTime) {
                    this.size = this.size + 1;
                    this.lastTime = bar.getTime();
                }
                this.low = this.low > bar.getLow() ? bar.getLow() : this.low;
                this.high = this.high < bar.getHigh() ? bar.getHigh() : this.high;
                this.volume = this.volume + bar.getVolume();
                if (this.time <= bar.getTime()) {
                    this.close = bar.getClose();
                } else if (this.time >= bar.getTime()) {
                    this.open = bar.getOpen();
                    this.time = bar.getTime();
                }
            }
        }

        @Override
        public double getOpen() {
            return this.open + this.offset;
        }

        @Override
        public double getClose() {
            return this.close + this.offset;
        }

        @Override
        public double getLow() {
            return this.low + this.offset;
        }

        @Override
        public double getHigh() {
            return this.high + this.offset;
        }

        @Override
        public double getVolume() {
            return this.volume;
        }

        @Override
        public long getTime() {
            return this.time;
        }

        public long getLastTime() {
            return this.lastTime;
        }

        public void setOpen(double open) {
            this.open = open;
        }

        public void setClose(double close) {
            this.close = close;
        }

        public void setLow(double low) {
            this.low = low;
        }

        public void setHigh(double high) {
            this.high = high;
        }

        public void setVolume(double volume) {
            this.volume = volume;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public boolean isBuy() {
            return this.open < this.close;
        }


        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getWeek() {
            return week;
        }

        public void setWeek(int week) {
            this.week = week;
        }

        public void setOffset(double offset) {
            this.offset = offset;
        }
    }
}
