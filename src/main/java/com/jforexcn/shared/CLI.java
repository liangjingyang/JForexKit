package com.jforexcn.shared;

import com.dukascopy.api.IStrategy;
import com.jforexcn.shared.lib.MailService;
import com.jforexcn.shared.client.StrategyManager;
import com.jforexcn.shared.client.StrategyTestRunner;

import com.jforexcn.shared.client.StrategyRunner;


import org.reflections.Reflections;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.Set;

/**
 * Created by simple on 1/15/16.
 */
public class CLI {

    private static final String CONFIG_FILE_NAME = "JForexCN.properties";

    public static void main(String[] args) throws Exception {

        Reflections reflections = new Reflections("com.jforexcn.shared.strategy");
        Set<Class<? extends IStrategy>> allStrategies =
                reflections.getSubTypesOf(IStrategy.class);
        for (Class<? extends IStrategy> strategy : allStrategies) {
            StrategyManager.register(strategy.getSimpleName(), strategy);
        }

        if (args == null || args.length == 0) {
            printHelp();
            return;
        }

        try {
            Properties configProperties = new Properties();
            FileInputStream file;
            file = new FileInputStream(CONFIG_FILE_NAME);
            configProperties.load(file);
            file.close();

            String[] emailTo = configProperties.getProperty("email.to").split(",");
            String emailFrom = configProperties.getProperty("email.from");
            String emailPassword = configProperties.getProperty("email.password");
            String emailHostname = configProperties.getProperty("email.hostname");
            String emailSmtpPort = configProperties.getProperty("email.smtpPort");
            String emailSSLOn = configProperties.getProperty("email.sslOn");
            String emailSocketConnectionTimeout = configProperties.getProperty("email.socketConnectionTimeout");
            String emailSocketTimeout = configProperties.getProperty("email.socketTimeout");

            MailService.Builder builder = new MailService.Builder(emailTo, emailFrom, emailPassword);
            builder.hostname(emailHostname)
                    .smtpPort(Integer.parseInt(emailSmtpPort))
                    .sslOn(Boolean.parseBoolean(emailSSLOn))
                    .socketConnectionTimeout(Integer.parseInt(emailSocketConnectionTimeout))
                    .socketTimeout(Integer.parseInt(emailSocketTimeout))
                    .buildInstance();

            String command = args[0];

            if (command.equals("test")) {
                String strategy = configProperties.getProperty("test.strategy");
                String username = configProperties.getProperty("test.username");
                String password = configProperties.getProperty("test.password");
                String dateFrom = configProperties.getProperty("test.dateFrom");
                String dateTo = configProperties.getProperty("test.dateTo");
                StrategyTestRunner.run(StrategyManager.get(strategy), username, password, dateFrom, dateTo);
            } else if (args[0].equals("live")) {
                String strategy = configProperties.getProperty("live.strategy");
                String username = configProperties.getProperty("live.username");
                String password = configProperties.getProperty("live.password");
                StrategyRunner.run(StrategyManager.get(strategy), username, password, true);
            } else if (args[0].equals("demo")) {
                String strategy = configProperties.getProperty("demo.strategy");
                String username = configProperties.getProperty("demo.username");
                String password = configProperties.getProperty("demo.password");
                StrategyRunner.run(StrategyManager.get(strategy), username, password, false);
            } else if (args[0].equals("email")) {
                String subject = configProperties.getProperty("email.subject");
                String body = configProperties.getProperty("email.msg");
                MailService.sendMail(subject, body);
            }
        } catch (Exception e) {
            e.printStackTrace();
            printHelp();
        }
    }

    private static void printHelp() {
        System.out.println("Usage: see file " + CONFIG_FILE_NAME + " for details.");
        System.out.println("  command: test, live, demo, email");
        System.out.println("  strategies: " + StrategyManager.listAllStrategies());
    }
}
