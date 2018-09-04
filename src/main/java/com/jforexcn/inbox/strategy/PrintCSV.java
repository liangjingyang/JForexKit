package com.jforexcn.inbox.strategy;

/**
 * Created by simple on 26/9/16.
 */

import com.dukascopy.api.Configurable;
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
import com.dukascopy.api.Period;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class PrintCSV implements IStrategy {
    @Configurable("Instrument")
    public Instrument instrument = Instrument.EURUSD;
    @Configurable("Time Period")
    public Period period = Period.FIFTEEN_MINS;

    private IEngine mEngine;
    private IConsole mConsole;
    private IIndicators mIndicators;
    private IHistory mHistory;

    private Set<Instrument> mInstrumentSet = new HashSet<Instrument>();
    private PrintWriter printWriter;
    private String csvFileName;
    private long lastTime = 0L;

    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat barDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static String STRATEGY_TAG = "PrintCSV";


    @Override
    public void onStart(IContext context) throws JFException {
        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mIndicators = context.getIndicators();
        this.mHistory = context.getHistory();

        // subscribe instruments
        mInstrumentSet.add(instrument);
        context.setSubscribedInstruments(mInstrumentSet, true);
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (isStrategyInstrument(instrument)) {
            lastTime = askBar.getTime();
            if (printWriter == null) {
                // date format
                mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
                Date date = new Date();
                try{
                    csvFileName = "historical_data_" + date.getTime() + "_" +
                            instrument.getPrimaryJFCurrency() +
                            instrument.getSecondaryJFCurrency() + "_" +
                            mSimpleDateFormat.format(lastTime);
                    printWriter = new PrintWriter(csvFileName, "UTF-8");
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("date");
                    stringBuilder.append(",");
                    stringBuilder.append("open");
                    stringBuilder.append(",");
                    stringBuilder.append("close");
                    stringBuilder.append(",");
                    stringBuilder.append("high");
                    stringBuilder.append(",");
                    stringBuilder.append("low");
                    printWriter.println(stringBuilder);
                } catch (IOException e) {
                    puts("writer open file error: " + e.getLocalizedMessage());
                }
            } else {
                if (period.equals(this.period)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(barDateFormat.format(new Date(askBar.getTime())));
                    stringBuilder.append(",");
                    stringBuilder.append(askBar.getOpen());
                    stringBuilder.append(",");
                    stringBuilder.append(askBar.getClose());
                    stringBuilder.append(",");
                    stringBuilder.append(askBar.getHigh());
                    stringBuilder.append(",");
                    stringBuilder.append(askBar.getLow());
                    printWriter.println(stringBuilder);
                }
            }
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private void puts(String str) {
        mConsole.getInfo().println(str);
    }


    private ArrayList<IOrder> getFilledOrdersWithStopLossByInstrument(Instrument instrument) throws JFException {
        ArrayList<IOrder> orders = new ArrayList<>();
        for (IOrder order : mEngine.getOrders()) {
            if (order != null &&
                    mInstrumentSet.contains(instrument) &&
                    IOrder.State.FILLED.equals(order.getState()) &&
                    order.getStopLossPrice() > 0 &&
                    instrument.equals(order.getInstrument())
                    ) {
                orders.add(order);
            }
        }
        return orders;
    }

    private ArrayList<IOrder> getFilledOrdersWithoutStopLossByInstrument(Instrument instrument) throws JFException {
        ArrayList<IOrder> orders = new ArrayList<>();
        for (IOrder order : mEngine.getOrders()) {
            if (order != null &&
                    mInstrumentSet.contains(instrument) &&
                    IOrder.State.FILLED.equals(order.getState()) &&
                    order.getStopLossPrice() <= 0 &&
                    instrument.equals(order.getInstrument())
                    ) {
                orders.add(order);
            }
        }
        return orders;
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
        IOrder order = message.getOrder();
        if (order != null) {
        }
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

    @Override
    public void onStop() throws JFException {
        printWriter.close();
        File file = new File(csvFileName);
        File file2 = new File(csvFileName + "_" + mSimpleDateFormat.format(lastTime) + "_" + period);
        file.renameTo(file2);
        puts(STRATEGY_TAG + " OnStop!");
    }

    private boolean isStrategyInstrument(Instrument instrument) {
        return mInstrumentSet.contains(instrument);
    }
}