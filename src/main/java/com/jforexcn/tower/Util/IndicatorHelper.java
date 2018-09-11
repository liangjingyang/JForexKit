package com.jforexcn.tower.Util;


import com.dukascopy.api.*;

import java.util.List;

/**
 * Created by simple on 2018/8/18
 */
public class IndicatorHelper {
    /**
     * 获取一个矩形, 上下边内包所有 Bars. 高不大于 Height (Pips), 矩形右边是 current bar, 宽度在 minBarNum, maxBarNum 之间.
     *
     * 返回 Object[] result = new Object[4] { true, min, max, sma}
     */
    public static Object[] getRect(IContext context, Instrument instrument, Period period, double minHeight, double maxHeight, int minBarNum, int maxBarNum, boolean checkSMA) throws JFException {
        Object[] result = new Object[] { false, 0.0, 0.0, 0.0 };
        if (minHeight > maxHeight || minBarNum > maxBarNum) return result;
        IHistory history = context.getHistory();
        IIndicators indicators = context.getIndicators();
        long time = history.getStartTimeOfCurrentBar(instrument, period);
        List<IBar> barList = history.getBars(instrument, period, OfferSide.BID, Filter.WEEKENDS, maxBarNum, time, 0);
        double[] minList = indicators.min(instrument, period, OfferSide.BID, IIndicators.AppliedPrice.LOW, maxBarNum, Filter.WEEKENDS, 2, time, 0);
        double down = minList[0];
        double[] maxList = indicators.max(instrument, period, OfferSide.BID, IIndicators.AppliedPrice.HIGH, maxBarNum, Filter.WEEKENDS, 2, time, 0);
        double up = maxList[0];
        double[] smaList = indicators.sma(instrument, period, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, maxBarNum, Filter.WEEKENDS, 2, time, 0);
        double sma = smaList[0];
        double diff = Math.abs(up - down);
        int i = 0, j = maxBarNum;
        IBar bar;
        boolean updated;
        while (!((boolean) result[0]) && j > minBarNum) {
            if (diff >= minHeight * instrument.getPipValue()
                    && diff <= maxHeight * instrument.getPipValue()
            ) {
                if (!checkSMA || (sma >= down + diff / 3
                        && sma <= up - diff / 3)) {
                    result[0] = true;
                    result[1] = down;
                    result[2] = up;
                    result[3] = sma;
                    return result;
                }
            }
            j = j - 1;
            bar = barList.get(i);
            updated = false;
//            if (bar.getHigh() >= up) {
                maxList = indicators.max(instrument, period, OfferSide.BID, IIndicators.AppliedPrice.HIGH, j, Filter.WEEKENDS, 2, time, 0);
                up = maxList[0];
                updated = true;
//            }
//            if (bar.getLow() <= down) {
                minList = indicators.min(instrument, period, OfferSide.BID, IIndicators.AppliedPrice.LOW, j, Filter.WEEKENDS, 2, time, 0);
                down = minList[0];
                updated = true;
//            }
            if (updated) {
                smaList = indicators.sma(instrument, period, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, j, Filter.WEEKENDS, 2, time, 0);
                sma = smaList[0];
            }
            diff = Math.abs(up - down);
            i = i + 1;
//            context.getConsole().getInfo().println(" =============== j: " + j);
//            context.getConsole().getInfo().println(" =============== i: " + i);
//            context.getConsole().getInfo().println(" =============== up: " + up);
//            context.getConsole().getInfo().println(" =============== down: " + down);
        }
        result[1] = down;
        result[2] = up;
        result[3] = sma;
        return result;
    }
}
