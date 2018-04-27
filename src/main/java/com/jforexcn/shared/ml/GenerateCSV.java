package com.jforexcn.shared.ml;

/**
 * Created by simple on 26/3/18.
 */

import com.dukascopy.api.Configurable;
import com.dukascopy.api.Filter;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IDataService;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.ITimeDomain;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class GenerateCSV implements IStrategy {
    @Configurable("Instrument")
    public Instrument cInstrument = Instrument.GBPJPY;
    @Configurable("Time Period")
    public Period cPeriod = Period.FOUR_HOURS;
    @Configurable("Look Back")
    public int cLookBack = 10;


    private IEngine engine;
    private IConsole console;
    private IIndicators indicators;
    private IHistory history;
    private IDataService dataService;

    private Set<Instrument> instrumentSet = new HashSet<Instrument>();
    private PrintWriter printWriter;
    private String csvFileName;
    private long lastTime = 0L;
    private IBar lastBar;
    private int flushCounter = 0;

    private SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat barDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static String STRATEGY_TAG = "GenerateCSV";


    @Override
    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.indicators = context.getIndicators();
        this.history = context.getHistory();
        this.dataService = context.getDataService();

        // subscribe instruments
        instrumentSet.add(cInstrument);
        context.setSubscribedInstruments(instrumentSet, true);
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (isStrategyInstrument(instrument) && period.equals(this.cPeriod) && !isOffline()) {

            lastTime = askBar.getTime();
            if (printWriter == null) {
                // date format
                fileDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
                Date date = new Date();
                try{
                    csvFileName = "historical_data_" + date.getTime() + "_" +
                            instrument.getPrimaryJFCurrency() +
                            instrument.getSecondaryJFCurrency() + "_" +
                            fileDateFormat.format(lastTime);
                    printWriter = new PrintWriter(csvFileName, "UTF-8");
                    StringBuilder stringBuilder = new StringBuilder();
                    generateTitles(stringBuilder);
                    printWriter.println(stringBuilder);
                } catch (IOException e) {
                    puts("writer open file error: " + e.getLocalizedMessage());
                }
            } else {
                if (lastBar != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(barDateFormat.format(new Date(lastBar.getTime())));
                    stringBuilder.append(",");
                    List<IBar> bars = history.getBars(cInstrument, cPeriod, OfferSide.ASK, Filter.WEEKENDS, cLookBack, lastBar.getTime(), 0);
                    generateBars(stringBuilder, bars);
                    generateIndicators(stringBuilder, askBar, bars);
                    generateTrends(stringBuilder, askBar);
                    printWriter.println(stringBuilder);
                    try_flush();
                }
            }
            lastBar = askBar;
        }
    }

    private boolean isOffline() throws JFException {
        long lastTickTime = history.getLastTick(cInstrument).getTime();
        Set<ITimeDomain> offlines = dataService.getOfflineTimeDomains(lastTickTime - Period.WEEKLY.getInterval(), lastTickTime + Period.WEEKLY.getInterval());
        for(ITimeDomain offline : offlines){
            if( lastTickTime > offline.getStart() &&  lastTickTime < offline.getEnd()){
                return true;
            }
        }
        return false;
    }

    private void try_flush() {
        flushCounter = flushCounter + 1;
        if (flushCounter >= 10000) {
            printWriter.flush();
            flushCounter = 0;
        }
    }

    private StringBuilder generateTitles(StringBuilder stringBuilder) {
        stringBuilder.append("date");
        stringBuilder.append(",");
        for (int i = 0; i < cLookBack; i ++) {
            String barSeq = "_" + String.valueOf(i);
            stringBuilder.append("open" + barSeq);
            stringBuilder.append(",");
            stringBuilder.append("high" + barSeq);
            stringBuilder.append(",");
            stringBuilder.append("low" + barSeq);
            stringBuilder.append(",");
            stringBuilder.append("close" + barSeq);
            stringBuilder.append(",");
            stringBuilder.append("abs_C-O" + barSeq);
            stringBuilder.append(",");
            stringBuilder.append("H-L" + barSeq);
            stringBuilder.append(",");
            stringBuilder.append("H-maxOC" + barSeq);
            stringBuilder.append(",");
            stringBuilder.append("minOC-L" + barSeq);
            stringBuilder.append(",");
            stringBuilder.append("bar_up" + barSeq);
            stringBuilder.append(",");
            stringBuilder.append("bar_down" + barSeq);
            stringBuilder.append(",");
            stringBuilder.append("volumn" + barSeq);
            stringBuilder.append(",");
        }
        for (int i = 0; i < cLookBack; i ++) {
            String barSeq = "_" + String.valueOf(i);
            stringBuilder.append("ema" + barSeq);
            stringBuilder.append(",");
        }
        stringBuilder.append("ema-sma");
        stringBuilder.append(",");
        stringBuilder.append("ema_greater");
        stringBuilder.append(",");
        stringBuilder.append("rsi");
        stringBuilder.append(",");
        stringBuilder.append("daily_atr");
        stringBuilder.append(",");
        stringBuilder.append("bbuppper-bbmiddle");
        stringBuilder.append(",");
        stringBuilder.append("bbmiddle-bblower");
        stringBuilder.append(",");
        stringBuilder.append("close-bbmiddle");
        stringBuilder.append(",");
        stringBuilder.append("bbmiddle_greater_than_close");
        stringBuilder.append(",");
        for (int i = 0; i < cLookBack; i ++) {
            String barSeq = "_" + String.valueOf(i);
            stringBuilder.append("bias" + barSeq);
            stringBuilder.append(",");
        }
        stringBuilder.append("sine");
        stringBuilder.append(",");
        stringBuilder.append("lead_sine");
        stringBuilder.append(",");
        stringBuilder.append("open");
        stringBuilder.append(",");
        stringBuilder.append("high");
        stringBuilder.append(",");
        stringBuilder.append("low");
        stringBuilder.append(",");
        stringBuilder.append("close");
        stringBuilder.append(",");
        stringBuilder.append("up");
        stringBuilder.append(",");
        stringBuilder.append("down");
        stringBuilder.append(",");
        stringBuilder.append("pips_diff");
        return stringBuilder;
    }

    private StringBuilder generateBars(StringBuilder stringBuilder, List<IBar> bars) throws JFException {
        for (int i = 0; i < cLookBack; i ++) {
            IBar bar = bars.get(i);
            int barUp = 0, barDown = 0;
            if (bar.getClose() > bar.getOpen()) {
                barUp = 1;
            } else if (bar.getClose() < bar.getOpen()) {
                barDown = 1;
            }
            stringBuilder.append(bar.getOpen());
            stringBuilder.append(",");
            stringBuilder.append(bar.getHigh());
            stringBuilder.append(",");
            stringBuilder.append(bar.getLow());
            stringBuilder.append(",");
            stringBuilder.append(bar.getClose());
            stringBuilder.append(",");
            stringBuilder.append(Math.abs(bar.getOpen() - bar.getClose()) * Math.pow(10, cInstrument.getPipScale()));
            stringBuilder.append(",");
            stringBuilder.append((bar.getHigh() - bar.getLow()) * Math.pow(10, cInstrument.getPipScale()));
            stringBuilder.append(",");
            stringBuilder.append((bar.getHigh() - Math.max(bar.getOpen(), bar.getClose())) * Math.pow(10, cInstrument.getPipScale()));
            stringBuilder.append(",");
            stringBuilder.append((Math.min(bar.getOpen(), bar.getClose()) - bar.getLow()) * Math.pow(10, cInstrument.getPipScale()));
            stringBuilder.append(",");
            stringBuilder.append(barUp);
            stringBuilder.append(",");
            stringBuilder.append(barDown);
            stringBuilder.append(",");
            stringBuilder.append(bar.getVolume() / 100);
            stringBuilder.append(",");
        }
        return stringBuilder;
    }


    private StringBuilder generateIndicators(StringBuilder stringBuilder, IBar askBar, List<IBar> bars) throws JFException {
        // ema
        double[] ema = indicators.ema(cInstrument, cPeriod, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE, cLookBack, Filter.WEEKENDS, cLookBack, lastBar.getTime(), 0);
        for (int i = 0; i < cLookBack; i ++) {
            stringBuilder.append(ema[i]);
            stringBuilder.append(",");
        }
        double[] sma = indicators.sma(cInstrument, cPeriod, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE, cLookBack * 2, Filter.WEEKENDS, 1, lastBar.getTime(), 0);
        stringBuilder.append((ema[0] - sma[0]) * Math.pow(10, cInstrument.getPipScale()));
        stringBuilder.append(",");
        stringBuilder.append(ema[0] > sma[0] ? 1: 0);
        stringBuilder.append(",");

        // rsi
        double[] rsi = indicators.rsi(cInstrument, cPeriod, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE, cLookBack, Filter.WEEKENDS, 1, lastBar.getTime(), 0);
        stringBuilder.append(rsi[0]);
        stringBuilder.append(",");

        // 10 days atr
        double[] atr = indicators.atr(cInstrument, cPeriod, OfferSide.ASK, cLookBack, Filter.WEEKENDS, 1, lastBar.getTime(), 0);
        stringBuilder.append(atr[0]);
        stringBuilder.append(",");

        // bbands10
        double[][] bbands = indicators.bbands(cInstrument, cPeriod, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE, cLookBack, 2, 2, IIndicators.MaType.EMA, Filter.WEEKENDS, 1, lastBar.getTime(), 0);
        stringBuilder.append((bbands[0][0] - bbands[1][0]) * Math.pow(10, cInstrument.getPipScale()));
        stringBuilder.append(",");
        stringBuilder.append((bbands[1][0] - bbands[2][0]) * Math.pow(10, cInstrument.getPipScale()));
        stringBuilder.append(",");
        stringBuilder.append((Math.abs(lastBar.getClose() - bbands[1][0])) * Math.pow(10, cInstrument.getPipScale()));
        stringBuilder.append(",");
        stringBuilder.append(bbands[1][0] > lastBar.getClose() ? 1 : 0);
        stringBuilder.append(",");

        // bias
        for (int i = 0; i < cLookBack; i ++) {
            IBar bar = bars.get(i);
            stringBuilder.append((bar.getClose() - ema[i]) / ema[i] * 100 * Math.pow(10, cInstrument.getPipScale()));
            stringBuilder.append(",");
        }

        // SINE
        double[][] sine = indicators.ht_sine(cInstrument, cPeriod, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE, Filter.WEEKENDS, 1, lastBar.getTime(), 0);
        stringBuilder.append(sine[0][0] * Math.pow(10, cInstrument.getPipScale()));
        stringBuilder.append(",");
        stringBuilder.append(sine[1][0] * Math.pow(10, cInstrument.getPipScale()));
        stringBuilder.append(",");
        return stringBuilder;
    }


    private StringBuilder generateTrends(StringBuilder stringBuilder, IBar askBar) {
        stringBuilder.append(askBar.getOpen() * Math.pow(10, cInstrument.getPipScale()));
        stringBuilder.append(",");
        stringBuilder.append(askBar.getHigh() * Math.pow(10, cInstrument.getPipScale()));
        stringBuilder.append(",");
        stringBuilder.append(askBar.getLow() * Math.pow(10, cInstrument.getPipScale()));
        stringBuilder.append(",");
        stringBuilder.append(askBar.getClose() * Math.pow(10, cInstrument.getPipScale()));
        stringBuilder.append(",");
        int up = 0;
        int down = 0;
        double pipsDiff = (askBar.getClose() - askBar.getOpen()) * Math.pow(10, cInstrument.getPipScale());
        if (askBar.getClose() > askBar.getOpen()) {
            if (pipsDiff > 0) {
                up = 1;
            }
        } else if (askBar.getClose() < askBar.getOpen()) {
            if (pipsDiff < 0) {
                down = 1;
            }
        }
        stringBuilder.append(up);
        stringBuilder.append(",");
        stringBuilder.append(down);
        stringBuilder.append(",");
        stringBuilder.append(pipsDiff);
        return stringBuilder;
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private void puts(String str) {
        console.getInfo().println(str);
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
        File file2 = new File(csvFileName + "_" + fileDateFormat.format(lastTime) + "_" + cPeriod.toString().replace(' ', '_') + ".csv");
        file.renameTo(file2);
        puts(STRATEGY_TAG + " OnStop!");
    }

    private boolean isStrategyInstrument(Instrument instrument) {
        return instrumentSet.contains(instrument);
    }
}