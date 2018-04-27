package com.jforexcn.shared.client;


import com.dukascopy.api.IStrategy;
import com.dukascopy.api.JFCurrency;
import com.dukascopy.api.RequiresFullAccess;
import com.dukascopy.api.system.ISystemListener;
import com.dukascopy.api.system.ITesterClient;
import com.dukascopy.api.system.ITesterClient.DataLoadingMethod;
import com.dukascopy.api.system.ITesterReportData;
import com.dukascopy.api.system.TesterFactory;
import com.jforexcn.shared.lib.StrategyManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Future;

/**
 * * This inbox program demonstrates how to compile and run a strategy from a java file
 */
@RequiresFullAccess
public class StrategyTestRunner {
    public static final Logger LOGGER = StrategyManager.LOGGER;
    private static String jnlpUrl = "http://platform.dukascopy.com/demo/jforex.jnlp";

    public static void run(final IStrategy strategy, String username, String password, String fromStr, String toStr) throws Exception {

        // get the instance of the IClient interface
        final ITesterClient client = TesterFactory.getDefaultInstance();
        // set the listener that will receive system events
        client.setSystemListener(new ISystemListener() {
            @Override
            public void onStart(long processId) {
                LOGGER.info("Strategy started: " + processId);
            }

            @Override
            public void onStop(long processId) {

                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));

                final SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat2.setTimeZone(TimeZone.getTimeZone("GTM"));

                ITesterReportData reportData = client.getReportData(processId);
                LOGGER.info("Strategy name: " + reportData.getStrategyName());
                LOGGER.info("init: " + reportData.getInitialDeposit());
                LOGGER.info("finish: " + reportData.getFinishDeposit());
                LOGGER.info("open orders: " + reportData.getOpenOrders().size());
                LOGGER.info("close orders: " + reportData.getClosedOrders().size());
                LOGGER.info("from: " + dateFormat2.format(reportData.getFrom()));
                LOGGER.info("to: " + dateFormat2.format(reportData.getTo()));
                for (String[] paramList : reportData.getParameterValues()) {
                    String param = "";
                    for (String p : paramList) {
                        param = param + p;
                    }
                    LOGGER.info(" params: " + param);
                }

                LOGGER.info("Strategy stopped: " + processId);

                File reportFile = new File("report_" + dateFormat.format(new Date()) + "." +
                        dateFormat2.format(reportData.getFrom()) + "." +
                        dateFormat2.format(reportData.getTo()) + "." + reportData.getFinishDeposit() + ".html");

                try {
                    client.createReport(processId, reportFile);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
                if (client.getStartedStrategies().size() == 0) {
                    System.exit(0);
                }
            }

            @Override
            public void onConnect() {
                LOGGER.info("Connected");
            }

            @Override
            public void onDisconnect() {
                // tester doesn't disconnect
            }
        });

        LOGGER.info("Connecting...");
        // connect to the server using jnlp, user name and password
        // connection is needed for data downloading
        client.connect(jnlpUrl, username, password);

        // wait for it to connect
        int i = 10; // wait max ten seconds
        while (i > 0 && !client.isConnected()) {
            Thread.sleep(1000);
            i--;
        }
        if (!client.isConnected()) {
            LOGGER.error("Failed to connect Dukascopy servers");
            System.exit(1);
        }

        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date dateFrom = dateFormat.parse(fromStr);
        Date dateTo = dateFormat.parse(toStr);

        LOGGER.info("from: " + fromStr + ", to: " + toStr);

        client.setDataInterval(DataLoadingMethod.ALL_TICKS, dateFrom.getTime(), dateTo.getTime());

        // set instruments that will be used in testing
//        Set<Instrument> instruments = new HashSet<Instrument>();
//        instruments.add(Instrument.EURUSD);
//        LOGGER.info("Subscribing instruments...");
//        client.setSubscribedInstruments(instruments);
        // setting initial deposit
        client.setInitialDeposit(JFCurrency.getInstance("USD"), 10000);
        //client.setCacheDirectory(new File("C:/temp/cacheTemp"));
        // load data
        LOGGER.info("Downloading data");
        Future<?> future = client.downloadData(null);
        // wait for downloading to complete
        future.get();
        // start the strategy
        LOGGER.info("Starting strategy");

        client.startStrategy(strategy);
        // now it's running
    }

}