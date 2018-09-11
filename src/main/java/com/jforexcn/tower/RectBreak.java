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
public class RectBreak extends StopLossOne {
    @Configurable("minHeight")
    public double cMinHeightPips = 0;
    @Configurable("maxHeightPips")
    public double cMaxHeightPips = 40;
    @Configurable("minBarNum")
    public int cMinBarNum = 12;
    @Configurable("maxBarNum")
    public int cMaxBarNum = 120;
    @Configurable("slippage")
    public double cSlippage = 1;
    @Configurable("stopLossPips")
    public int cStopLossPips = 10;
    @Configurable("takeProfitPips")
    public int cTakeProfitPips = 13;

    private Calendar calendar = Calendar.getInstance();
    private double up;
    private double down;
    private double sma;
    private boolean available;
    private double breakUpLevel;
    private double breakDownLevel;
    private double tpDistance; // in price
    private double slDistance = 0.0050; // in price

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
                updateRect();
            } else if (Period.ONE_MIN.equals(period) && available) {
//                if (bidBar.getLow() <= breakUpLevel && bidBar.getClose() >= breakUpLevel && bidBar.getClose() < lowLevel2) {
                if (bidBar.getClose() >= breakUpLevel) {
                    IEngine.OrderCommand command = IEngine.OrderCommand.BUY;
                    List<IOrder> orders = orderHelper.getStrategyOrdersByInstrumentAndCommand(instrument, command);
                    if (orders.size() == 0) {
                        double price = bidBar.getClose();
                        long time = history.getLastTick(instrument).getTime();
                        double sl = BaseHelper.scalePrice(price - slDistance, instrument.getPipScale());
//                        double sl = BaseHelper.scalePrice(price - cStopLossPips * instrument.getPipValue(), instrument.getPipScale());
                        double tp = BaseHelper.scalePrice(price + tpDistance, instrument.getPipScale());
                        orderHelper.submitOrder(getLabel(1), instrument, command, 0.1, price, cSlippage, sl, 0, time);
                    }
//                } else if (bidBar.getHigh() >= breakDownLevel && bidBar.getClose() <= breakDownLevel && bidBar.getClose() > highLevel2) {
                } else if (bidBar.getClose() <= breakDownLevel) {
                    IEngine.OrderCommand command = IEngine.OrderCommand.SELL;
                    List<IOrder> orders = orderHelper.getStrategyOrdersByInstrumentAndCommand(instrument, command);
                    if (orders.size() == 0) {
                        double price = bidBar.getClose();
                        long time = history.getLastTick(instrument).getTime();
//                        double sl = BaseHelper.scalePrice(price + cStopLossPips * instrument.getPipValue(), instrument.getPipScale());
                        double sl = BaseHelper.scalePrice(price + slDistance, instrument.getPipScale());
                        double tp = BaseHelper.scalePrice(price - tpDistance, instrument.getPipScale());
                        orderHelper.submitOrder(getLabel(1), instrument, command, 0.1, price, cSlippage, sl, 0, time);
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

    private void updateRect() throws JFException {
        Object[] result = IndicatorHelper.getRect(context, instrument, Period.ONE_HOUR, cMinHeightPips, cMaxHeightPips, cMinBarNum, cMaxBarNum, false);
        available = (boolean) result[0];
        down = (double) result[1];
        up = (double) result[2];
        sma = (double) result[3];
        double range = (up - down) * 0.3;
        breakUpLevel = up + range;
        breakDownLevel = down - range;
        tpDistance = (up - down) * 0.7;
        tpDistance = Math.min(cTakeProfitPips * instrument.getPipValue(), tpDistance);
        slDistance = (up - down) * 0.7;
        slDistance = Math.min(cStopLossPips * instrument.getPipValue(), slDistance);
        printDebug();
    }

    private void printDebug() {
        logDebug("=============");
        logDebug("available: " + available);
        logDebug("down: " + down);
        logDebug("up: " + up);
        logDebug("breakUpLevel: " + breakUpLevel);
        logDebug("breakDownLevel: " + breakDownLevel);
        logDebug("-------------");
    }
}
