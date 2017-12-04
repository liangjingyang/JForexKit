package com.jforexcn.shared.client;


import com.dukascopy.api.*;
import com.dukascopy.api.system.ISystemListener;
import com.dukascopy.api.system.ITesterClient;
import com.dukascopy.api.system.ITesterClient.DataLoadingMethod;
import com.dukascopy.api.system.TesterFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * * This inbox program demonstrates how to compile and run a strategy from a java file
 */
@RequiresFullAccess
public class StrategyFileTestRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(StrategyFileTestRunner.class);
    private static String jnlpUrl = "https://www.dukascopy.com/client/demo/jclient/jforex.jnlp";
    private static String userName;
    private static String password;

    public static void run(String[] args) throws Exception {
        if (args.length < 5) {

            LOGGER.error("Insufficient parameter count " + args.length + ". Expected 5 arguments.");
            return;
        }

        userName = args[0];
        password = args[1];
        String strategyPath = args[2];
        String fromStr = args[3];
        String toStr = args[4];

        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager sjfm = jc.getStandardFileManager(null, null, null);
        File javaFile = new File(strategyPath);
        String qualifiedClassName = getQualifiedName(javaFile.getAbsolutePath());

        jc.getTask(null, null, null, null, null, sjfm.getJavaFileObjects(javaFile)).call();

        sjfm.close();
        LOGGER.info("Class has been successfully compiled");

        URL[] urls = new URL[]{new URL("file://.")};
        URLClassLoader ucl = new URLClassLoader(urls);
        Class targetClass = ucl.loadClass(qualifiedClassName);
        IStrategy strategy = (IStrategy) targetClass.newInstance();

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
                LOGGER.info("Strategy stopped: " + processId);
                File reportFile = new File("C:\\temp\\report.html");
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
        client.connect(jnlpUrl, userName, password);

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

        final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date dateFrom = dateFormat.parse(fromStr);
        Date dateTo = dateFormat.parse(toStr);


        client.setDataInterval(DataLoadingMethod.ALL_TICKS, dateFrom.getTime(), dateTo.getTime());

        // set instruments that will be used in testing
        Set<Instrument> instruments = new HashSet<Instrument>();
        instruments.add(Instrument.EURUSD);
        LOGGER.info("Subscribing instruments...");
        client.setSubscribedInstruments(instruments);
        // setting initial deposit
        client.setInitialDeposit(Instrument.EURUSD.getSecondaryCurrency(), 50000);
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


    private static String getQualifiedName(String path) throws Exception {
        String fileContents = readFileContents(path);
        String className = findRegex(fileContents, "class ([\\p{Alnum}.]+) ");
        String packageName = findRegex(fileContents, "package ([\\p{Alnum}.]+);");
        if (packageName.isEmpty()) {
            throw new RuntimeException("Please define the package of your strategy");
        }
        return packageName + "." + className;
    }


    private static String readFileContents(String path) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }


    private static String findRegex(String src, String regexpString) {
        Pattern pattern = Pattern.compile(regexpString, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(src);
        return matcher.find() ? matcher.group(1) : "";
    }


}