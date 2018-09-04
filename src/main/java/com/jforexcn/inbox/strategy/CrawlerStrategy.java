package com.jforexcn.inbox.strategy;

/**
 * Created by simple on 2/7/16.
 */

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

import com.jforexcn.shared.client.StrategyRunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class CrawlerStrategy implements IStrategy {

    private IEngine mEngine;
    private IConsole mConsole;
    private IIndicators mIndicators;
    private IHistory mHistory;

    private Connection mConnection;
    private int mInsertCount = 0;
    private Statement mStatement = null;

    private Set<Instrument> mInstrumentSet = new HashSet<Instrument>();

    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");


    @Override
    public void onStart(IContext context) throws JFException {
        try {
            Class.forName("org.postgresql.Driver");
            mConnection = null;
            mConnection = DriverManager.getConnection(
                    "jdbc:postgresql://127.0.0.1:5432/forex", "forex", "vvars0ng");
            mConnection.setAutoCommit(false);
        } catch (Exception e) {
            throw new JFException(e.getMessage());
        }

        this.mEngine = context.getEngine();
        this.mConsole = context.getConsole();
        this.mIndicators = context.getIndicators();
        this.mHistory = context.getHistory();

        // subscribe instruments
        mInstrumentSet.add(Instrument.GBPAUD);
//        mInstrumentSet.add(Instrument.GBPCAD);
        context.setSubscribedInstruments(mInstrumentSet, true);

        // date format
        mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
        puts("CrawlerStrategy start!");
    }

    @Override
    public void onMessage(IMessage message) throws JFException {
    }

    @Override
    public void onStop() throws JFException {
        puts("CrawlerStrategy OnStop");
        try {
            mStatement.executeBatch();
            mConnection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            e = e.getNextException();
            throw new JFException(e.getMessage());
        }
        // Do nothing, leave the orders.
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        // assume no data in database
//        if (mInstrumentSet.contains(cInstrument)) {
//            insertTick(cInstrument, tick);
//        }
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (mInstrumentSet.contains(instrument) && period.equals(Period.ONE_MIN)) {
            insertBar(instrument, askBar, period);
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }

    private void puts(String str) {
//        mConsole.getInfo().println(str);
        StrategyRunner.LOGGER.info(str);
    }

    private void insertTick(Instrument instrument, ITick tick) throws JFException {
        String table = instrumentToTable(instrument, "tick");
        String sql = "INSERT INTO " + table +
                " (time, ask, bid, ask_volume, bid_volume, total_ask_volume, total_bid_volume) " +
                "VALUES (" +
                tick.getTime() + ", " +
                tick.getAsk() + ", " +
                tick.getBid() + ", " +
                tick.getAskVolume() + ", " +
                tick.getBidVolume() + ", " +
                tick.getTotalAskVolume() + ", " +
                tick.getTotalBidVolume() + ");";
        insertSql(sql);

    }

    private void insertBar(Instrument instrument, IBar bar, Period period) throws JFException {
        String table = instrumentToTable(instrument, "bar");
        String sql = "INSERT INTO " + table +
                " (time, open, close, low, high, volume, cPeriod) " +
                "VALUES (" +
                bar.getTime() + ", " +
                bar.getOpen() + ", " +
                bar.getClose() + ", " +
                bar.getLow() + ", " +
                bar.getHigh() + ", " +
                bar.getVolume() + ", '" +
                period.name() + "');";
        insertSql(sql);

    }

    private void insertSql(String sql) throws JFException {
        try {
            if (mStatement == null) {
                mStatement = mConnection.createStatement();
                mInsertCount = 0;
            }
            mStatement.addBatch(sql);
            mInsertCount = mInsertCount + 1;
            if (mInsertCount >= 3000) {
                mStatement.executeBatch();
                mConnection.commit();
//                puts("insertSql 1000");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            e = e.getNextException();
            throw new JFException(e.getMessage());
        }
    }

    private String instrumentToTable(Instrument instrument, String suffix) {
        String prefix = instrument.toString().replace('/', '_').toLowerCase();
        return prefix + "_" + suffix;
    }
}