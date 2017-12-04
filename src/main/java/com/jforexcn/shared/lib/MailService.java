package com.jforexcn.shared.lib;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

/**
 * Created by simple on 3/22/16.
 */


public class MailService {

    private static MailService instance;

    private final String hostname;
    private final int smtpPort;
    private final boolean sslOn;
    private final String[] to;
    private final String from;
    private final String password;
    private final int socketConnectionTimeout;
    private final int socketTimeout;

    public static class Builder {
        private String hostname = "smtp.gmail.com";
        private int smtpPort = 465;
        private boolean sslOn = true;
        private final String[] to;
        private final String from;
        private final String password;
        private int socketConnectionTimeout = 15000;
        private int socketTimeout = 15000;

        public Builder(String[] to, String from, String password) {
            this.to = to;
            this.from = from;
            this.password = password;
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }
        public Builder smtpPort(int smtpPort) {
            this.smtpPort = smtpPort;
            return this;
        }
        public Builder sslOn(boolean sslOn) {
            this.sslOn = sslOn;
            return this;
        }
        public Builder socketConnectionTimeout(int socketConnectionTimeout) {
            this.socketConnectionTimeout = socketConnectionTimeout;
            return this;
        }
        public Builder socketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public MailService build() {
            return new MailService(this);
        }

        public MailService buildInstance() {
            instance = new MailService(this);
            return instance;
        }
    }

    private MailService(Builder builder) {
        hostname = builder.hostname;
        smtpPort = builder.smtpPort;
        sslOn = builder.sslOn;
        to = builder.to;
        from = builder.from;
        password = builder.password;
        socketConnectionTimeout = builder.socketConnectionTimeout;
        socketTimeout = builder.socketTimeout;
    }



    public static void sendMail(final String subject, final String body) {
        sendMailSync(subject, body);
    }


    public static void sendMailSync(String subject, String msg) {

        try {
            Email email = new SimpleEmail();
            email.setHostName(instance.hostname);
            email.setSmtpPort(instance.smtpPort);

            // 45.32.155.8
            email.setAuthenticator(new DefaultAuthenticator(instance.from, instance.password));

            email.setSSLOnConnect(instance.sslOn);
            email.setFrom(instance.from);
            email.setSubject(subject + " - JForex");
            email.setMsg(msg + "\n\nThis is a email from Strategy ... :-)\n");
            email.addTo(instance.to);
            email.setSocketConnectionTimeout(instance.socketConnectionTimeout);
            email.setSocketTimeout(instance.socketTimeout);
            email.send();
        } catch (Exception e) {
            e.printStackTrace();
            e.getCause().printStackTrace();
        }
    }
}
