package com.jforexcn.shared;

import com.dukascopy.api.IStrategy;
import com.jforexcn.shared.lib.MailService;
import com.jforexcn.shared.lib.StrategyManager;
import com.jforexcn.shared.client.StrategyTestRunner;

import com.jforexcn.shared.client.StrategyRunner;


import org.reflections.Reflections;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Created by simple on 1/15/16.
 */
public class CLI {

    private static final String PROJECT_NAME = "JForexKit";

    public static void main(String[] args) throws Exception {

        Reflections reflections = new Reflections("com.jforexcn.shared.strategy");
        Set<Class<? extends IStrategy>> allStrategies =
                reflections.getSubTypesOf(IStrategy.class);
        for (Class<? extends IStrategy> strategy : allStrategies) {
            StrategyManager.register(strategy.getSimpleName(), strategy);
        }
        reflections = new Reflections("com.jforexcn.inbox.strategy");
        allStrategies =
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
            file = new FileInputStream(PROJECT_NAME + ".properties");
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
        try {
            URLClassLoader cl = (URLClassLoader) CLI.class.getClassLoader();
            URL url = cl.findResource("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(url.openStream());
            Attributes attributes = manifest.getMainAttributes();
            String projectName = attributes.getValue("Implementation-Title");
            String version = attributes.getValue("Implementation-Version");
            System.out.println(projectName + " " + version + " Usage: ");
            System.out.println("java -jar " + PROJECT_NAME + ".jar [command]");
            System.out.println("  command: test, live, demo, email");
            System.out.println("  strategy: " + StrategyManager.listAllStrategies());
            System.out.println("See file " + PROJECT_NAME + ".properties for other arguments.");
        } catch (IOException e) {
            System.out.println("Print Help Error: " + e.getLocalizedMessage());
        }
    }
}
