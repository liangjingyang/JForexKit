package com.jforexcn.tower;

import com.dukascopy.api.*;
import com.jforexcn.tower.Util.BaseHelper;
import com.jforexcn.tower.Util.IndicatorHelper;

import java.util.Calendar;
import java.util.List;

/**
 * Created by simple on 2018/9/9
 *
 * 获取一个满足条件的矩形, 矩形的高在 minHeight 和 maxHeight 之间,
 * 矩形的右边是当前 Bar, 矩形的左边(回溯窗口)在 minBarNum 和 maxBarNum 之间
 *
 * 当价格触及矩形的上下边缘时, 做翻转
 * GBP/USD 的清淡交易时间 UTC 21:00 ~ 01:00
 *
 */

@RequiresFullAccess
@Library("/data/github/JForexKit/build/libs/JForexKit-3.0.jar")
public class RectVolatility extends BaseStrategy {
    @Configurable("minHeight")
    public double cMinHeightPips = 10;
    @Configurable("maxHeightPips")
    public double cMaxHeightPips = 30;
    @Configurable("minBarNum")
    public int cMinBarNum = 120;
    @Configurable("maxBarNum")
    public int cMaxBarNum = 180;
    @Configurable("slippage")
    public double cSlippage = 1;
    @Configurable("stopLossPips")
    public int cStopLossPips = 30;
    @Configurable("takeProfitPips")
    public int cTakeProfitPips = 10;

    private Calendar calendar = Calendar.getInstance();
    private double up;
    private double down;
    private double sma;
    private boolean available;
    private double lowLevel1;
    private double highLevel1;
    private double lowLevel2;
    private double highLevel2;
    private double tpDistance; // in price
    private double slDistance = 0.0050; // in price
    private int hour;

    @Override
    public void onStart(IContext context) throws JFException {
        super.onStart(context);
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        super.onTick(instrument, tick);
    }


    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        super.onBar(instrument, period, askBar, bidBar);
        if (isStrategyInstrument(instrument)) {
            if (period.equals(Period.ONE_HOUR)) {
                calendar.setTimeInMillis(bidBar.getTime());
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                if (hour >= 20) {
                    updateRect(hour - 20);
                } else if (hour >= 1 && hour < 20) {
                    available = false;
                }
                if (hour == 4) {
                    orderHelper.closeAllStrategyOrders();
                }
//                if (hour == 19) {
//                    updateRect2(bidBar);
//                } else if (hour >= 1 && hour < 20) {
//                    available = false;
//                }
            } else if (Period.ONE_MIN.equals(period) && available) {
//                if (bidBar.getLow() <= lowLevel1 && bidBar.getClose() >= lowLevel1 && bidBar.getClose() < lowLevel2) {
                if (bidBar.getClose() <= lowLevel1) {
                    IEngine.OrderCommand command = IEngine.OrderCommand.BUY;
                    List<IOrder> orders = orderHelper.getStrategyOrdersByInstrumentAndCommand(instrument, command);
                    if (orders.size() == 0) {
                        double price = bidBar.getClose();
                        long time = history.getLastTick(instrument).getTime();
//                        double sl = BaseHelper.scalePrice(price - slDistance, instrument.getPipScale());
                        double sl = BaseHelper.scalePrice(price - cStopLossPips * instrument.getPipValue(), instrument.getPipScale());
                        double tp = BaseHelper.scalePrice(price + tpDistance, instrument.getPipScale());
                        orderHelper.submitOrder(getLabel(1), instrument, command, 0.1, price, cSlippage, sl, tp, time);
                    }
//                } else if (bidBar.getHigh() >= highLevel1 && bidBar.getClose() <= highLevel1 && bidBar.getClose() > highLevel2) {
                } else if (bidBar.getClose() >= highLevel1) {
                    IEngine.OrderCommand command = IEngine.OrderCommand.SELL;
                    List<IOrder> orders = orderHelper.getStrategyOrdersByInstrumentAndCommand(instrument, command);
                    if (orders.size() == 0) {
                        double price = bidBar.getClose();
                        long time = history.getLastTick(instrument).getTime();
                        double sl = BaseHelper.scalePrice(price + cStopLossPips * instrument.getPipValue(), instrument.getPipScale());
//                        double sl = BaseHelper.scalePrice(price + slDistance, instrument.getPipScale());
                        double tp = BaseHelper.scalePrice(price - tpDistance, instrument.getPipScale());
                        orderHelper.submitOrder(getLabel(1), instrument, command, 0.1, price, cSlippage, sl, tp, time);
                    }
                }

            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
        super.onAccount(account);
    }

    // common methods

    @Override
    public void onMessage(IMessage message) throws JFException {
        super.onMessage(message);
        IOrder order = message.getOrder();
        if (order != null) {
            if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {
            }
        }
    }

    @Override
    public void onStop() throws JFException {
        super.onStop();
    }

    public String getLabel(int seq) {
        return getStrategyTag() + "_" + seq + "_" + System.currentTimeMillis();
    }

    private void updateRect(int multiple) throws JFException {
        int maxBarNum = cMaxBarNum + 60 * multiple;
        Object[] result = IndicatorHelper.getRect(context, instrument, Period.ONE_MIN, cMinHeightPips, cMaxHeightPips, cMinBarNum, maxBarNum, false);
        available = (boolean) result[0];
        down = (double) result[1];
        up = (double) result[2];
        sma = (double) result[3];
        double range1 = (up - down) * 0.2;
        double range2 = (up - down) * 0.4;
        lowLevel1 = down + range1;
        highLevel1 = up - range1;
        lowLevel2 = down + range2;
        highLevel2 = up - range2;
        tpDistance = (up - down) * 0.5;
        tpDistance = Math.min(cTakeProfitPips * instrument.getPipValue(), tpDistance);
        printDebug();
    }

    private void updateRect2(IBar bidBar) throws JFException {
        available = true;
        down = bidBar.getClose() - 10 * instrument.getPipValue();
        up = bidBar.getClose() + 10 * instrument.getPipValue();
        double range1 = 0.0003;
        double range2 = 0.0006;
        lowLevel1 = down + range1;
        highLevel1 = up - range1;
        lowLevel2 = down + range2;
        highLevel2 = up - range2;
        tpDistance = 0.0008;
        slDistance = 0.0008;
        printDebug();
    }

    private void printDebug() {
        logDebug("=============");
        logDebug("available: " + available);
        logDebug("down: " + down);
        logDebug("up: " + up);
        logDebug("sma: " + sma);
        logDebug("lowLevel1: " + lowLevel1);
        logDebug("highLevel1: " + highLevel1);
        logDebug("lowLevel2: " + lowLevel2);
        logDebug("highLevel2: " + highLevel2);
        logDebug("-------------");
    }
}
