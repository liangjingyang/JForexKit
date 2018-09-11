package com.jforexcn.tower.Util;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.jforexcn.shared.lib.MailService;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by simple on 2018/8/18
 */
public class BaseHelper {


    public static long getLastTickTime(Instrument instrument, IContext context) throws JFException {
        long lastTickTime = context.getHistory().getTimeOfLastTick(instrument);
        if (lastTickTime <= 0) {
            lastTickTime = System.currentTimeMillis();
        }
        return lastTickTime;
    }

    public static double pipToCurrency(Instrument instrument, IContext context) throws JFException {
        return context.getUtils().convertPipToCurrency(instrument, context.getAccount().getAccountCurrency());
    }

    public static double clearAmount(Instrument instrument, double amount) {
        switch (instrument.toString()){
            case "XAU/USD" : return amount >= 1 ? amount : 0;
            case "XAG/USD" : return amount >= 50 ? amount : 0;
            default : return amount >= 1000 ? amount : 0;
        }
    }

    public static double getMinMil(Instrument instrument){
        switch (instrument.toString()){
            case "XAU/USD" : return 0.000001;
            case "XAG/USD" : return 0.00005;
            default : return 0.001;
        }
    }

    public static int getMinAmount(Instrument instrument){
        switch (instrument.toString()){
            case "XAU/USD" : return 1;
            case "XAG/USD" : return 50;
            default : return 1000;
        }
    }

    public static int getMilScale(Instrument instrument){
        switch (instrument.toString()){
            case "XAU/USD" : return 6;
            case "XAG/USD" : return 5;
            default : return 3;
        }
    }

    public static double amountToMil(Instrument instrument, double amount) {
        BigDecimal mil = new BigDecimal(amount);
        BigDecimal divisor = new BigDecimal(1000000);
        mil = mil.divide(divisor, getMilScale(instrument), RoundingMode.DOWN);
        return mil.doubleValue();
    }

    public static double milToAmount(double mil) {
        BigDecimal amount = new BigDecimal(mil);
        BigDecimal multiplicand = new BigDecimal(1000000);
        amount = amount.multiply(multiplicand);
        amount = amount.setScale(0, RoundingMode.DOWN);
        return amount.doubleValue();
    }

    public static double scalePrice(double price, int pipScale) {
        // pips: 0.0001, price: 0.00001
        return scaleDouble(price, pipScale + 1, RoundingMode.HALF_UP);
    }

    public static double scaleDouble(double value, int scale, RoundingMode roundingMode) {
        if (scale < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(scale, roundingMode);
        return bd.doubleValue();
    }

    public static void sendMail(String subject, String content) {
        MailService.sendMail(subject, content);
    }
}
