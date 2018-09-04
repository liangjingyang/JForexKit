package com.jforexcn.inbox.indicator;

import com.dukascopy.api.Filter;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IIndicators.MaType;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.indicators.DoubleRangeDescription;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IIndicatorsProvider;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.IntegerListDescription;
import com.dukascopy.api.indicators.IntegerRangeDescription;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;
import com.dukascopy.api.indicators.PeriodListDescription;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author N.S.OFFICE
 * 专业编程 MT4/MT5/JForex
 * http://nscall.taobao.com/
 * 联系方式 : QQ1987774728
 * 版权属于：ridgejiang（旺旺） 190353986@qq.com
 * 
 * Version 1.0
 * 完成日期 : 2017.08.10
 * 升级修改日志：
 * 2017.08.10
 * 将外线是上下线距离的N倍，修正为外线是上下线距离一半（即上线到中线或下线到中线的距离）的N倍。
 */
public class MVATRChannel implements IIndicator {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private double[][] inputs1 = new double[1][];
    private IBar[][] inputs2 = new IBar[2][];
    private double[][][] inputs3 = new double[1][][];
    private double[][] outputs = new double[12][];
    
    private double atrMultiple;
    private double line1Multiple;
    private double line2Multiple;
    private double line3Multiple;
    private double line4Multiple;
    private IIndicator ma;
    private IIndicator atr;
    private InputParameterInfo atrInput21;
    private InputParameterInfo atrInput3;
    private final List<Period> atrPeriods = new ArrayList<>();
    private Period selectedAtrPeriod;
    private IIndicatorContext indicatorContext;
    private IHistory history;
    
    @Override
    public void onStart(IIndicatorContext context) {
        this.history = context.getHistory();
        this.indicatorContext = context;
        
        IIndicatorsProvider provider = context.getIndicatorsProvider();
        ma = provider.getIndicator("MA");
        atr = provider.getIndicator("ATR");
        int[] maTypeOrdinals = new int[MaType.values().length];
        String[] maTypeNames = new String[MaType.values().length];
        for (int i = 0; i < maTypeOrdinals.length; i++) {
            maTypeOrdinals[i] = i;
            maTypeNames[i] = MaType.values()[i].name();
        }
        int[] offerSideOrdinals = new int[OfferSide.values().length];
        String[] offerSideNames = new String[OfferSide.values().length];
        for (int i = 0; i < offerSideOrdinals.length; i++) {
            offerSideOrdinals[i] = i;
            offerSideNames[i] = OfferSide.values()[i].name();
        }
        int[] filterOrdinals = new int[Filter.values().length];
        String[] filterNames = new String[Filter.values().length];
        for (int i = 0; i < filterOrdinals.length; i++) {
            filterOrdinals[i] = i;
            filterNames[i] = Filter.values()[i].name();
        }
        
        for (Period p : Period.values()){
            if (p.equals(Period.TICK)) {
                continue;
            }
            atrPeriods.add(p);
        }
        atrInput21 = new InputParameterInfo("ATR Bar", InputParameterInfo.Type.BAR);
        atrInput21.setPeriod(Period.DAILY);
        atrInput3 = new InputParameterInfo("ATR Price", InputParameterInfo.Type.PRICE);
        atrInput3.setPeriod(Period.DAILY);
        
        indicatorInfo = new IndicatorInfo("MAATRCHANNEL", "MA ATR Channel", "Custom indicator"
                , true, false, true, 4, 11, 12);
        
        inputParameterInfos = new InputParameterInfo[] {
            new InputParameterInfo("MA Price", InputParameterInfo.Type.DOUBLE),
            new InputParameterInfo("Main Bar", InputParameterInfo.Type.BAR),
            atrInput21,
            atrInput3
	};
        
        optInputParameterInfos = new OptInputParameterInfo[] {
            new OptInputParameterInfo("Time cPeriod", OptInputParameterInfo.Type.OTHER
                    , new IntegerRangeDescription(14, 1, 2000, 1)),
            new OptInputParameterInfo("MA type", OptInputParameterInfo.Type.OTHER
                    , new IntegerListDescription(MaType.SMA.ordinal(), maTypeOrdinals, maTypeNames)),
            new OptInputParameterInfo("ATR Period", OptInputParameterInfo.Type.OTHER
                    , new PeriodListDescription(Period.DAILY
                            , atrPeriods.toArray(new Period[atrPeriods.size()]))),
            new OptInputParameterInfo("图表价格", OptInputParameterInfo.Type.OTHER
                    , new IntegerListDescription(OfferSide.BID.ordinal(), offerSideOrdinals, offerSideNames)),
            new OptInputParameterInfo("图表过滤", OptInputParameterInfo.Type.OTHER
                    , new IntegerListDescription(Filter.WEEKENDS.ordinal(), filterOrdinals, filterNames)),
            new OptInputParameterInfo("Time Period", OptInputParameterInfo.Type.OTHER
                    , new IntegerRangeDescription(14, 1, 2000, 1)),
            new OptInputParameterInfo("ATR倍数", OptInputParameterInfo.Type.OTHER
                    , new DoubleRangeDescription(2.0, 0.0001, 2000, 0.0001, 4)),
            new OptInputParameterInfo("通道倍数1", OptInputParameterInfo.Type.OTHER
                    , new DoubleRangeDescription(1.0, 0.0001, 2000, 0.0001, 4)),
            new OptInputParameterInfo("通道倍数2", OptInputParameterInfo.Type.OTHER
                    , new DoubleRangeDescription(1.5, 0.0001, 2000, 0.0001, 4)),
            new OptInputParameterInfo("通道倍数3", OptInputParameterInfo.Type.OTHER
                    , new DoubleRangeDescription(2.0, 0.0001, 2000, 0.0001, 4)),
            new OptInputParameterInfo("通道倍数4", OptInputParameterInfo.Type.OTHER
                    , new DoubleRangeDescription(2.5, 0.0001, 2000, 0.0001, 4))
        };
        
        outputParameterInfos = new OutputParameterInfo[] {
            new OutputParameterInfo("+4外线", OutputParameterInfo.Type.DOUBLE
                    , OutputParameterInfo.DrawingStyle.LINE) {{
                            setColor(Color.RED);
            }},
            new OutputParameterInfo("+3外线", OutputParameterInfo.Type.DOUBLE
                    , OutputParameterInfo.DrawingStyle.LINE) {{
                            setColor(Color.RED);
            }},
            new OutputParameterInfo("+2外线", OutputParameterInfo.Type.DOUBLE
                    , OutputParameterInfo.DrawingStyle.LINE) {{
                            setColor(Color.RED);
            }},
            new OutputParameterInfo("+1外线", OutputParameterInfo.Type.DOUBLE
                    , OutputParameterInfo.DrawingStyle.LINE) {{
                            setColor(Color.RED);
            }},
            new OutputParameterInfo("上线", OutputParameterInfo.Type.DOUBLE
                    , OutputParameterInfo.DrawingStyle.LINE) {{
                            setColor(Color.BLUE);
                            setLineWidth(2);
            }},
            new OutputParameterInfo("中线", OutputParameterInfo.Type.DOUBLE
                    , OutputParameterInfo.DrawingStyle.LINE) {{
                            setColor(Color.GREEN);
            }},
            new OutputParameterInfo("下线", OutputParameterInfo.Type.DOUBLE
                    , OutputParameterInfo.DrawingStyle.LINE) {{
                            setColor(Color.BLUE);
                            setLineWidth(2);
            }},
            new OutputParameterInfo("-1外线", OutputParameterInfo.Type.DOUBLE
                    , OutputParameterInfo.DrawingStyle.LINE) {{
                            setColor(Color.RED);
            }},
            new OutputParameterInfo("-2外线", OutputParameterInfo.Type.DOUBLE
                    , OutputParameterInfo.DrawingStyle.LINE) {{
                            setColor(Color.RED);
            }},
            new OutputParameterInfo("-3外线", OutputParameterInfo.Type.DOUBLE
                    , OutputParameterInfo.DrawingStyle.LINE) {{
                            setColor(Color.RED);
            }},
            new OutputParameterInfo("-4外线", OutputParameterInfo.Type.DOUBLE
                    , OutputParameterInfo.DrawingStyle.LINE) {{
                            setColor(Color.RED);
            }},
            new OutputParameterInfo("Previous ATR", OutputParameterInfo.Type.DOUBLE
                    , OutputParameterInfo.DrawingStyle.NONE)
        };
    }

    @Override
    public IndicatorInfo getIndicatorInfo() {
        return indicatorInfo;
    }

    @Override
    public InputParameterInfo getInputParameterInfo(int index) {
        if (index <= inputParameterInfos.length) {
            return inputParameterInfos[index];
        }
        return null;
    }

    @Override
    public OptInputParameterInfo getOptInputParameterInfo(int index) {
        if (index <= optInputParameterInfos.length) {
            return optInputParameterInfos[index];
        }
        return null;
    }

    @Override
    public OutputParameterInfo getOutputParameterInfo(int index) {
        if (index <= outputParameterInfos.length) {
            return outputParameterInfos[index];
        }
        return null;
    }

    @Override
    public void setInputParameter(int index, Object array) {
        switch (index) {
            case 0:
            {
                inputs1[0] = (double[]) array;
                break;
            }
            case 1:
            {
                inputs2[0] = (IBar[]) array;
                break;
            }
            case 2:
            {
                inputs2[1] = (IBar[]) array;
                break;
            }
            case 3:
            {
                inputs3[0] = (double[][]) array;
                break;
            }
            default:
            {
                throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    @Override
    public void setOptInputParameter(int index, Object value) {
        switch (index) {
            case 0:
            {
                int period = (int) value;
                ma.setOptInputParameter(0, period);
                break;
            }
            case 1:
            {
                int maType = (int) value;
                ma.setOptInputParameter(1, maType);
                break;
            }
            case 2:
            {
                Period period = (Period) value;
                if (!atrPeriods.contains(period)) {
                    throw new IllegalArgumentException("Period not supported");
                }
                atrInput21.setPeriod(period);
                atrInput3.setPeriod(period);
                selectedAtrPeriod = period;
                break;
            }
            case 3:
            {
                OfferSide offerSide = OfferSide.values()[(int) value];
                atrInput21.setOfferSide(offerSide);
                atrInput3.setOfferSide(offerSide);
                break;
            }
            case 4:
            {
                Filter filter = Filter.values()[(int) value];
                atrInput21.setFilter(filter);
                atrInput3.setFilter(filter);
                break;
            }
            case 5:
            {
                int period = (int) value;
                atr.setOptInputParameter(0, period);
                break;
            }
            case 6:
            {
                atrMultiple = (double) value;
                break;
            }
            case 7:
            {
                line1Multiple = (double) value;
                break;
            }
            case 8:
            {
                line2Multiple = (double) value;
                break;
            }
            case 9:
            {
                line3Multiple = (double) value;
                break;
            }
            case 10:
            {
                line4Multiple = (double) value;
                break;
            }
            default:
            {
                throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    @Override
    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }

    @Override
    public int getLookback() {
        return ma.getLookback();
    }

    @Override
    public int getLookforward() {
        return 0;
    }

    @Override
    public IndicatorResult calculate(int startIndex, int endIndex) {
        if (startIndex - getLookback() < 0) {
            startIndex -= startIndex - getLookback();
        }
        if (startIndex > endIndex) {
            return new IndicatorResult(0, 0);
        }
        
        int maLength = endIndex - startIndex + 1;
        double[] arrayMa = new double[maLength];
        ma.setInputParameter(0, inputs1[0]);
        ma.setOutputParameter(0, arrayMa);
        ma.calculate(startIndex, endIndex);
        
        int atrLookback = atr.getLookback();
        int atrLength = inputs3[0][0].length - atrLookback;
        if (atrLength <= 0) {
            return new IndicatorResult(startIndex, endIndex - startIndex + 1);
        }
        double[] arrayAtr = new double[atrLength];
        atr.setInputParameter(0, inputs3[0]);
        atr.setOutputParameter(0, arrayAtr);
        atr.calculate(0, inputs3[0][0].length - 1);
        
        int i, j;
        for (i = startIndex, j = 0; i <= endIndex; i++, j++) {
            int timeIndex = getTimeIndex(inputs2[0][i].getTime(), inputs2[1]);
            if (timeIndex > atrLookback) {
                
                int index = timeIndex - atrLookback - 1;
                if (index >= arrayAtr.length) {
                    return new IndicatorResult(startIndex, endIndex - startIndex + 1);
                }
                
                double previousAtr = arrayAtr[index];
                double channelDepth = atrMultiple * previousAtr;
                double middle = arrayMa[j];
                
                outputs[0][j] = middle + line4Multiple * channelDepth;
                outputs[1][j] = middle + line3Multiple * channelDepth;
                outputs[2][j] = middle + line2Multiple * channelDepth;
                outputs[3][j] = middle + line1Multiple * channelDepth;
                outputs[4][j] = middle + atrMultiple * previousAtr;
                outputs[5][j] = middle;
                outputs[6][j] = middle - atrMultiple * previousAtr;
                outputs[7][j] = middle - line1Multiple * channelDepth;
                outputs[8][j] = middle - line2Multiple * channelDepth;
                outputs[9][j] = middle - line3Multiple * channelDepth;
                outputs[10][j] = middle - line4Multiple * channelDepth;
                
                outputs[11][j] = previousAtr;
                
            }
        }
        
        return new IndicatorResult(startIndex, j);
    }

    public int getTimeIndex(long time, IBar[] source) {
    	if ((source == null) || (source.length <= 0)) {
            return -1;
    	}
        
        int length = source.length;
        if (time >= source[length - 1].getTime()) {
            return length - 1;
        }
        
        long barTime;
        try {
            barTime = history.getBarStart(selectedAtrPeriod, time);
        } catch (JFException e) {
            indicatorContext.getConsole().getWarn().println("Failed to get start time of bar - " + e);
            return -1;
        }
        
        int curIndex = 0;
        int upto = source.length;
	    
        while (curIndex < upto) {
            
            int midIndex = (curIndex + upto) / 2;

            IBar midBar = source[midIndex];

            if (midBar.getTime() == barTime) {
                return midIndex;
            } else if (barTime < midBar.getTime()) {
                upto = midIndex;
            } 
            else if (barTime > midBar.getTime()) {
                curIndex = midIndex + 1;
            } 
        }
        
        return curIndex;
    }
    
}
