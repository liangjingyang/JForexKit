package com.jforexcn.inbox.indicator;

/**
 * Created by simple(simple.continue@gmail.com) on 09/04/2018.
 */

import com.dukascopy.api.IBar;
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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.util.List;
import java.util.Map;


/**
 * Created by: S.Vishnyakov
 * Date: Feb 23, 2010
 * Time: 10:44:05 AM
 */
public class MurreyChannel implements IIndicator, IDrawingIndicator {

    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;

    private IBar[][] inputs = new IBar[1][];
    private Object[][] outputs = new Object[1][];
    private int p = 90;
    private int w = 5;
    private GeneralPath generalPath = new GeneralPath();
    private Rectangle rectangle;
    private IIndicatorContext context;

    @Override
    public void onStart(IIndicatorContext context) {
        indicatorInfo = new IndicatorInfo("Test Murrey", "Test Murrey", "Custom Indicators", true, false, false, 1, 2, 13);

        inputParameterInfos = new InputParameterInfo[]{
                new InputParameterInfo("Input data", InputParameterInfo.Type.BAR)
        };

        optInputParameterInfos = new OptInputParameterInfo[]{
                new OptInputParameterInfo("N Period", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(p, 1, 2000, 1)),
                new OptInputParameterInfo("Width in Pips", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(w, 1, 100, 1))
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

        int pipScale = context.getFeedDescriptor().getInstrument().getPipScale();
        double allPips = Math.pow(10, pipScale) * (highest - lowest);
        int sectionCount = (int) (allPips / w + 0.5);
        Object[] sectionArray = new Object[sectionCount];
        for (int c = 0; c < sectionCount - 1; c++) {
            Section section = new Section(c, lowest + c * w, lowest + (c + 1) * w);
            for (int i = endIndex - getLookback() + 1; i <= endIndex; i++) {
                IBar bar = inputs[0][i];
                section.tryAddBar(bar);
            }
            sectionArray[c] = section;
        }

        outputs[0] = sectionArray;

        return new IndicatorResult(endIndex, 1);
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

        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(color);
        g2.setStroke(stroke);
        g2.setFont(new Font("Dialog Input", Font.PLAIN, 9));

        Object[] output = (Object[]) values;
        if (output.length == 0){
            return null;
        }

        int x1 = (int)indicatorDrawingSupport.getMiddleOfCandle(indicatorDrawingSupport.getIndexOfFirstCandleOnScreen()) -
                (int) (indicatorDrawingSupport.getCandleWidthInPixels() / 2);
        int x2 = indicatorDrawingSupport.getChartWidth();

        int highest = ((x2 - x1) / 3);
        int maxAmount = 0;

        for (int i = 0; i < output.length; i++) {
            Section section = (Section) output[i];
            if (maxAmount < section.amount) {
                maxAmount = section.amount;
            }
        }

        int pxPerAmount = highest / maxAmount;

        for (int i = 0; i < output.length; i++) {
            Section section = (Section) output[i];
            int up = (int)indicatorDrawingSupport.getYForValue(section.up);
            int down = (int)indicatorDrawingSupport.getYForValue(section.down);
            int width = pxPerAmount * section.amount;
            int left = x2 - width;
            Rectangle rectangle = new Rectangle(left, up, width, up - down);
            g2.draw(rectangle);
            shapes.add((Shape)rectangle.clone());
        }

        return null;
    }

    private OutputParameterInfo createOutputParameter(String name){
        OutputParameterInfo param =
                new OutputParameterInfo(name, OutputParameterInfo.Type.OBJECT, OutputParameterInfo.DrawingStyle.NONE, false);
        param.setDrawnByIndicator(true);
        param.setColor(new Color(128, 128, 128));
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
