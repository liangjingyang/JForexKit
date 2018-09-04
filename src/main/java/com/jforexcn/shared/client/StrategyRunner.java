package com.jforexcn.shared.client;


import com.dukascopy.api.IStrategy;
import com.dukascopy.api.RequiresFullAccess;
import com.dukascopy.api.system.*;
import com.jforexcn.shared.lib.MailService;
import com.jforexcn.shared.lib.StrategyManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@RequiresFullAccess
public class StrategyRunner {
    public static final Logger LOGGER = StrategyManager.LOGGER;
    private static String jnlpUrl = "http://platform.dukascopy.com/demo/jforex.jnlp";

    public static void run(final List<IStrategy> strategies, String username, String password, boolean isLive) throws Exception {

        if (isLive) {
            jnlpUrl = "http://platform.dukascopy.com/live/jforex.jnlp";
        }

        // get the instance of the IClient interface
        final IClient client = ClientFactory.getDefaultInstance();
        // set the listener that will receive system events
        client.setSystemListener(new ISystemListener() {

            private int retryCount = 0;

            @Override
            public void onStart(long processId) {
                LOGGER.info("Strategy started: " + processId);
                MailService.sendMail("Congratulation!!! " + geStrategiesName(strategies) + " Started!",
                        "ProcessId: " + processId);
            }

            @Override
            public void onStop(long processId) {
                LOGGER.info("Strategy stopped: " + processId);
                MailService.sendMail("DOWN!!! " + geStrategiesName(strategies)  + " Stopped!",
                        "ProcessId: " + processId);
                if (client.getStartedStrategies().size() == 0) {
                    System.exit(0);
                }
            }

            @Override
            public void onConnect() {
                retryCount = 0;
                LOGGER.info("Connected");
                MailService.sendMail("Congratulation!!! " + geStrategiesName(strategies)  + " Connect!",
                        "retryCount: " + retryCount);
            }

            @Override
            public void onDisconnect() {
                LOGGER.info("Strategy disconnect, retry: " + retryCount);
                tryClientConnect();

            }

            public void tryClientConnect() {
                retryCount = retryCount + 1;
                if (retryCount > 10 && retryCount <= 100 && retryCount % 10 == 0) {
                    MailService.sendMail("DOWN! " + geStrategiesName(strategies)  +
                            " Connection error, retryCount " + retryCount, "");
                } else if (retryCount < 10) {
                    MailService.sendMail("DOWN! " + geStrategiesName(strategies)  +
                            " Connection error, retryCount " + retryCount, "");
                }
                try {
                    client.connect(jnlpUrl, username, password);
                } catch (Exception e) {
                    e.printStackTrace();
                    tryClientConnect();
                }
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

//        // set instruments that will be used in testing
//        Set<Instrument> instruments = new HashSet<Instrument>();
//        instruments.add(Instrument.EURUSD);
//        LOGGER.info("Subscribing instruments...");
//        client.setSubscribedInstruments(instruments);
        // start the strategy
        LOGGER.info("Starting strategy");
        for (IStrategy strategy : strategies) {
            long id = client.startStrategy(strategy);
            LOGGER.info("==================================== Started " + id + "============== : " + strategy.getClass().getSimpleName());
        }
        // now it's running
    }

    private static String geStrategiesName(List<IStrategy> strategies) {
        StringBuilder sb = new StringBuilder();
        for (IStrategy strategy : strategies) {
            sb.append(strategy.getClass().getSimpleName());
            sb.append(",");
        }
        return sb.toString();
    }

}