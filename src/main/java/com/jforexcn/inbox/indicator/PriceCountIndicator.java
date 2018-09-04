package com.jforexcn.inbox.indicator;

/**
 * Created by simple(simple.continue@gmail.com) on 09/04/2018.
 */

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
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
import java.util.List;
import java.util.Map;


public class PriceCountIndicator implements IIndicator, IDrawingIndicator {

    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;

    private IBar[][] inputs = new IBar[1][];
    private Object[][] outputs = new Object[1][];
    private int p = 300;
    private int w = 10;
    private double screenWidthFactor = 0.3;
    private IIndicatorContext context;

    @Override
    public void onStart(IIndicatorContext context) {
        indicatorInfo = new IndicatorInfo("PricezCount", "Price Count", "Custom Indicators", true, false, false, 1, 3, 1);

        inputParameterInfos = new InputParameterInfo[]{
                new InputParameterInfo("Input data", InputParameterInfo.Type.BAR)
        };

        optInputParameterInfos = new OptInputParameterInfo[]{
                new OptInputParameterInfo("N Bars", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(p, 1, 10000, 1)),
                new OptInputParameterInfo("Width in Pips", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(w, 1, 100, 1)),
                new OptInputParameterInfo("Screen width factor", OptInputParameterInfo.Type.OTHER, new DoubleRangeDescription(0.3, 0.1, 0.8, 0.01, 2)),
        };

        outputParameterInfos = new OutputParameterInfo[]{
                createOutputParameter("PriceCount"),
        };
        this.context = context;
    }

    @Override
    public IndicatorResult calculate(int startIndex, int endIndex) {
        if (startIndex - getLookback() < 0) {
            startIndex = getLookback();
        }
        if (startIndex > endIndex) {
            return new IndicatorResult(0, 0);
        }

        if (inputs[0] == null || inputs[0].length < p){
            return emptyResult(startIndex, endIndex);
        }
        double highest = 0, lowest = 0, high = 0, low = 0;

        for (int i = endIndex - getLookback() + 1; i <= endIndex; i++) {
            IBar bar = inputs[0][i];
            high = Math.max(bar.getOpen(), bar.getClose());
            low = Math.min(bar.getOpen(), bar.getClose());
            if (highest == 0 || highest < high) {
                highest = high;
            }
            if (lowest == 0 || lowest > low) {
                lowest = low;
            }
        }

        Instrument instrument = context.getFeedDescriptor().getInstrument();
        double pipValue = instrument.getPipValue();
        double width = w * pipValue;
        double allPips = highest - lowest;
        int sectionCount = (int) (allPips / width + 0.5);
        Object[] sectionArray = new Object[sectionCount];
        for (int c = 0; c < sectionCount; c++) {
            Section section = new Section(c, lowest + (c + 1) * width, lowest + c * width);
            for (int i = endIndex - getLookback() + 1; i <= endIndex; i++) {
                IBar bar = inputs[0][i];
                section.tryAddBar(bar);
            }
            sectionArray[c] = section;
        }
        for (int i = startIndex, j = 0; i <= endIndex; i++, j++) {
            if (i == endIndex) {
                outputs[0][j] = sectionArray;
            } else {
                outputs[0][j] = sectionArray;
            }
        }

        return new IndicatorResult(startIndex, endIndex - startIndex + 1);
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
    public int getLookback() {
        return p;
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
        inputs[index] = (IBar[]) array;
    }

    @Override
    public OptInputParameterInfo getOptInputParameterInfo(int index) {
        if (index < optInputParameterInfos.length) {
            return optInputParameterInfos[index];
        }
        return null;
    }

    @Override
    public void setOptInputParameter(int index, Object value) {
        switch (index) {
            case 0:
                p = (Integer) value;
                break;
            case 1:
                w = (Integer) value;
                break;
            case 2:
                screenWidthFactor = (double) value;
                break;
            default:
                throw new IllegalArgumentException("Invalid optional parameter index!");
        }
    }

    @Override
    public void setOutputParameter(int index, Object array) {
        outputs[index] = (Object[]) array;
    }

    @Override
    public int getLookforward() {
        return 0;
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

        Object[] array = (Object[]) values;

        Object[] sectionArray = (Object[]) array[array.length - 1];
        if (sectionArray == null) {
            return null;
        }

        if (sectionArray.length == 0){
            return null;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(color);
        g2.setFont(new Font("Dialog Input", Font.PLAIN, 9));
        FontMetrics fontMetrics = g2.getFontMetrics();

        int x1 = (int) indicatorDrawingSupport.getMiddleOfCandle(indicatorDrawingSupport.getIndexOfFirstCandleOnScreen()) -
                (int) (indicatorDrawingSupport.getCandleWidthInPixels() / 2);
        int x2 = indicatorDrawingSupport.getChartWidth();

        int highest = (int) ((x2 - x1) * screenWidthFactor);
        float maxAmount = 0;

        for (int i = 0; i < sectionArray.length; i++) {
            Section section = (Section) sectionArray[i];
            if (maxAmount < section.amount) {
                maxAmount = section.amount;
            }
        }

        float pxPerAmount = highest / maxAmount;

        int lastSmall = 0;
        for (int i = 0; i < sectionArray.length; i++) {
            Section section = (Section) sectionArray[i];
            int small = (int) indicatorDrawingSupport.getYForValue(section.up);
            int big = (int) indicatorDrawingSupport.getYForValue(section.down);
            if (lastSmall != 0) {
                big = lastSmall - 1;
            }
            lastSmall = small;
            int width = (int) (pxPerAmount * section.amount);
            int left = x2 - width;
            Rectangle rectangle = new Rectangle(left, (small), width, (big - small));
            float derive = (float) (0.1 + 0.7 * width / highest);
            g2.setComposite(AlphaComposite.SrcOver.derive(derive));
            g2.fill(rectangle);
            shapes.add((Shape)rectangle.clone());
            String label = String.valueOf(section.amount);
            int labelWidth = fontMetrics.stringWidth(label);
            int labelHeight = fontMetrics.getHeight();
            Rectangle labelRect = new Rectangle(x2 - labelWidth - 20 - 2, big - labelHeight, labelWidth, labelHeight);
            g2.drawString(label, labelRect.x, labelRect.y + labelRect.height - 3);
        }

        g2.setComposite(AlphaComposite.SrcOver.derive(0.3f));
        g2.drawLine(x2 - highest + 1, 0, x2 - highest + 1, indicatorDrawingSupport.getChartHeight());

        return null;
    }

    private OutputParameterInfo createOutputParameter(String name){
        OutputParameterInfo param =
                new OutputParameterInfo(name, OutputParameterInfo.Type.OBJECT, OutputParameterInfo.DrawingStyle.NONE, false);
        param.setDrawnByIndicator(true);
        param.setColor(new Color(224, 113, 61));
        return param;
    }

    private IndicatorResult emptyResult(int si, int ei){
        return new IndicatorResult(si, ei - si + 1);
    }

    public class Section {
        private int index;
        private double up;
        private double down;
        private double price;
        private int amount;

        public Section(int index, double up, double down) {
            this.index = index;
            this.up = up;
            this.down = down;
            this.price = up;
            this.amount = 0;
        }

        public void tryAddBar(IBar bar) {
            double high = Math.max(bar.getOpen(), bar.getClose());
            double low = Math.min(bar.getOpen(), bar.getClose());

            if (high >= down && low < up) {
                amount = amount + 1;
            }
        }
    }


}
