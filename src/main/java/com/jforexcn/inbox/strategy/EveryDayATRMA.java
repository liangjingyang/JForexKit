package com.jforexcn.inbox.strategy;

import com.dukascopy.api.Configurable;
import com.dukascopy.api.Filter;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by simple(simple.continue@gmail.com) on 19/12/2017.
 * UTC 6:00 挂单
 * UTC 21:00 撤单
 * 10日ATR
 * 后退10日ATR的一半挂单
 * 止损也是10日ATR的一半+5pips
 * 止盈是10日ATR
 * 如果撤单时没有触发止损止盈, 直接平仓
 */

public class EveryDayATRMA implements IStrategy {

    @Configurable(value = "Instrument", modifiable = false)
    public Instrument cInstrument = Instrument.EURUSD;
    @Configurable(value = "Amount in mil", modifiable = false)
    public double cAmount = 0.01;
    @Configurable(value = "Slippage", modifiable = false)
    public int cSlippage = 3;
    @Configurable(value = "Stop loss mutiple", modifiable = false)
    public double cStopLossMutiple = 0.6;
    @Configurable(value = "Take profit mutiple", modifiable = false)
    public double cTakeProfitMutiple = 1;
    @Configurable(value = "Full back mutiple", modifiable = false)
    public double cFullBackMutiple = 0.2;
    @Configurable(value = "Daily ATR time cPeriod", modifiable = false)
    public int cAtrTimePeriod = 18;
    @Configurable(value = "MA Period", modifiable = false)
    public Period cMaPeriod = Period.FOUR_HOURS;
    @Configurable(value = "First MA time cPeriod")
    public int cFirstTimePeriod = 12;
    @Configurable(value = "Second MA time cPeriod")
    public int cSecondTimePeriod = 30;
    @Configurable(value = "ADX time cPeriod")
    public int cAdxTimePeriod = 18;
    @Configurable(value = "Adx limit")
    public double cAdxLimit = 15;

    private IEngine engine;
    private IHistory history;
    private IIndicators indicators;
    private IConsole console;


    private static String TAG = "EveryDayATRMA";

    @Override
    public void onStart(IContext context) throws JFException {
        engine = context.getEngine();
        history = context.getHistory();
        indicators = context.getIndicators();
        console = context.getConsole();

        Set<Instrument> instrumentSet = new HashSet<Instrument>();
        instrumentSet.add(cInstrument);
        context.setSubscribedInstruments(instrumentSet, true);
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {

    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (instrument.equals(cInstrument) && period.equals(Period.ONE_HOUR)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(askBar.getTime());
            if (calendar.get(Calendar.HOUR_OF_DAY) == 5) {
                long prevPrevBarTime = history.getPreviousBarStart(period, history.getLastTick(instrument).getTime());
                double[] atrs = indicators.atr(cInstrument, Period.DAILY, OfferSide.ASK, cAtrTimePeriod, Filter.WEEKENDS, 0, prevPrevBarTime, 1);
                double atr = atrs[0];
                double[] firstMas = indicators.ema(cInstrument, cMaPeriod, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE, cFirstTimePeriod, Filter.WEEKENDS, 0, prevPrevBarTime, 1);
                double firstMa = firstMas[0];
                double[] secondMas = indicators.ema(cInstrument, cMaPeriod, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE, cSecondTimePeriod, Filter.WEEKENDS, 0, prevPrevBarTime, 1);
                double secondMa = secondMas[0];
                double[] adxs = indicators.adx(instrument, cMaPeriod, OfferSide.ASK, cAdxTimePeriod, Filter.WEEKENDS, 0, prevPrevBarTime, 1);
                double adx = adxs[0];
                console.getInfo().println("atr: " + atr + ", firstMa: " + firstMa + ", secondMa: " + secondMa + ", adx: " + adx);
                if (adx > cAdxLimit && firstMa > secondMa + atr * 0.1) {
                    IEngine.OrderCommand command = IEngine.OrderCommand.BUYLIMIT;
                    double price = scalePrice(history.getLastTick(instrument).getAsk() - atr * cFullBackMutiple, instrument.getPipScale());
                    double sl = scalePrice(price - atr * cStopLossMutiple, instrument.getPipScale());
                    double tp = scalePrice(price + atr * cTakeProfitMutiple, instrument.getPipScale());
                    engine.submitOrder(getLabel(), instrument, command, cAmount, price, cSlippage, sl, tp);
                } else if (adx > cAdxLimit && firstMa < secondMa - atr * 0.1) {
                    IEngine.OrderCommand command = IEngine.OrderCommand.SELLLIMIT;
                    double price = scalePrice(history.getLastTick(instrument).getBid() + atr * cFullBackMutiple, instrument.getPipScale());;
                    double sl = scalePrice(price + atr * cStopLossMutiple, instrument.getPipScale());
                    double tp = scalePrice(price - atr * cTakeProfitMutiple, instrument.getPipScale());
                    engine.submitOrder(getLabel(), instrument, command, cAmount, price, cSlippage, sl, tp);
                }
            } else if (calendar.get(Calendar.HOUR_OF_DAY) == 20){
                tryCloseOrder();
            }
        }
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder o = message.getOrder();
        if (o != null && isStrategyInstrument(o.getInstrument())) {
            if (IMessage.Type.ORDER_SUBMIT_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_SUBMIT_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_FILL_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_FILL_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CHANGED_REJECTED.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CHANGED_OK.equals(message.getType())) {
            } else if (IMessage.Type.ORDER_CLOSE_REJECTED.equals(message.getType())) {
                o.close();
            } else if (IMessage.Type.ORDER_CLOSE_OK.equals(message.getType())) {
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {

    }

    @Override
    public void onStop() throws JFException {

    }

    private String getLabel() throws JFException {
        return TAG + "_" + history.getLastTick(cInstrument).getTime();
    }

    private void tryCloseOrder() throws JFException {
        for (IOrder o : getOrders()) {
            o.close();
        }
    }

    private List<IOrder> getOrders() throws JFException {
        List<IOrder> orders = new ArrayList<>();
        for (IOrder o : engine.getOrders()) {
            if (isStrategyInstrument(o.getInstrument())) {
                orders.add(o);
            }
        }
        return orders;
    }

    private boolean isStrategyInstrument(Instrument instrument) {
        return cInstrument.equals(instrument);
    }

    private double scalePrice(double price, int pipScale) {
        // pips: 0.0001, price: 0.00001
        return scaleDouble(price, pipScale + 1, RoundingMode.HALF_UP);
    }

    private double scaleDouble(double value, int scale, RoundingMode roundingMode) {
        if (scale < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(scale, roundingMode);
        return bd.doubleValue();
    }
}