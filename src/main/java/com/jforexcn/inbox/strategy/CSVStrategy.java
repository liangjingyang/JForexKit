package com.jforexcn.inbox.strategy;

/**
 * Created by simple(simple.continue@gmail.com) on 26/11/2017.
 */

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.dukascopy.api.RequiresFullAccess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * This is strategy sample, which demonstrates CSV file parsing.
 * When strategy starts, it takes file by a given path and parses it. It looks in
 * each line and search for second occurrence of "," symbol. It takes all characters
 * to the left from "," symbol till space symbol accrues. Converts value to Double
 * and stores it in Array. After that it takes all gathered values prints out on console.
 **/

@RequiresFullAccess
public class CSVStrategy implements IStrategy {

    private IConsole console;
    private IEngine engine;
    private HashMap<Instrument, ArrayList<CSVLine>> csvLinesMap = new HashMap<Instrument, ArrayList<CSVLine>>();
    private int orderCount = 0;
    private String cSVFileName = "./strategy.csv";
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    public void setCSVFileName(String cSVFileName) {
        this.cSVFileName = cSVFileName;
    }

    public void onStart(IContext context) throws JFException {
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
        this.console = context.getConsole();
        this.engine = context.getEngine();
        try {
            console.getOut().println("onStart1, csvLinesMap size: " + csvLinesMap.size());
            csvLinesMap = parse(cSVFileName);
            console.getOut().println("onStart2, csvLinesMap size: " + csvLinesMap.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        console.getOut().println(csvLinesMap.keySet());
        context.setSubscribedInstruments(csvLinesMap.keySet(), true);
    }

    public void onAccount(IAccount account) throws JFException {
    }

    public void onMessage(IMessage message) throws JFException {
    }

    public void onStop() throws JFException {
        console.getOut().println("CSVStrategy stop, cSVlines size: " + csvLinesMap.size() + ", orderCount: " + orderCount);
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {

    }

    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (csvLinesMap.containsKey(instrument)) {
            ArrayList<CSVLine> delList = new ArrayList<CSVLine>();
            CSVLine cl;
            ArrayList<CSVLine> csvLines = csvLinesMap.get(instrument);
            for (int i = csvLines.size() - 1; i >= 0; i--) {
                cl = csvLines.get(i);
                if (askBar.getTime() > cl.getTime() + 30000) {
                    delList.add(cl);
                } else if (cl.getTime() == askBar.getTime()) {
                    console.getOut().println("time begin =============");
                    console.getOut().println("cInstrument: " + instrument + ", cl.date: " + cl.getDate() +
                            ", tick.date: " + simpleDateFormat.format(askBar.getTime()) + ", cl.time: " + cl.getTime() +
                            ", tick.time: " + askBar.getTime());
                    console.getOut().println("cl.price: " + cl.getPrice() + ", tick.ask: " + askBar.getClose() +
                            ", tick.bid: " + bidBar.getClose());
                    console.getOut().println("time end =============");
                    engine.submitOrder("csv" + orderCount + String.valueOf(askBar.getTime()),
                            cl.getInstrument(), cl.getOrderCommand(), cl.getAmount(), 0, 1);
                    orderCount = orderCount + 1;
                } else {
                    break;
                }
            }
            for (CSVLine delCl : delList) {
                csvLines.remove(delCl);
            }
        }
    }

    /**
     * Parse file by given path in directory tree. Takes from file each strokes
     * second token and place it as Double into ArrayList
     *
     * @param path - file path
     * @return ArrayList of Doubles
     * @throws IOException
     */
    private HashMap<Instrument, ArrayList<CSVLine>> parse(String path) throws IOException {

        HashMap<Instrument, ArrayList<CSVLine>> csvMap = new HashMap<Instrument, ArrayList<CSVLine>>();
        File file = new File(path);

        BufferedReader bufRdr = new BufferedReader(new FileReader(file));
        String line = null;

        while ((line = bufRdr.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line, ";");
            CSVLine cl = new CSVLine();
            String date;
            try {
                date = st.nextToken();
                cl.setDate(date);
                cl.setTime(simpleDateFormat.parse(date).getTime());
                cl.setInstrument(convertToInstrument(st.nextToken()));
                cl.setOrderCommand(convertToOrderCommand(st.nextToken()));
                cl.setInOut(convertToInOut(st.nextToken()));
                cl.setAmount(Double.valueOf(st.nextToken()));
                cl.setPrice(Double.valueOf(st.nextToken()));
            } catch (Exception e) {
                console.getOut().println(e.getStackTrace());
            }
            if (cl.getTime() != 0 && cl.getAmount() != 0 && cl.getInOut() != null &&
                    cl.getInstrument() != null && cl.getOrderCommand() != null && cl.getPrice() != 0) {
                if (!csvMap.containsKey(cl.getInstrument())) {
                    csvMap.put(cl.getInstrument(), new ArrayList<CSVLine>());
                }
                csvMap.get(cl.getInstrument()).add(0, cl);
                //                console.getOut().println("cl: " + cl.getTime() + ", " + cl.getInstrument() + ", " + cl.getOrderCommand() +
                //                        ", " + cl.getInOut() + ", " + cl.getAmount() + ", " + cl.getPrice());
            }
        }
        bufRdr.close();

        for (Map.Entry<Instrument, ArrayList<CSVLine>> entry : csvMap.entrySet()) {
            Collections.sort(entry.getValue(), new Comparator<CSVLine>() {
                @Override
                public int compare(CSVLine o1, CSVLine o2) {
                    return o1.getTime() > o2.getTime() ? -1 : 1;
                }
            });
        }

        return csvMap;
    }


    public static InOut convertToInOut(String inOut) {
        if (inOut.equals("In")) {
            return InOut.IN;
        } else if (inOut.equals("Out")) {
            return InOut.OUT;
        }
        return null;
    }


    private static IEngine.OrderCommand convertToOrderCommand(String command) {
        if (command.equals("Buy")) {
            return IEngine.OrderCommand.BUY;
        } else if (command.equals("Sell")) {
            return IEngine.OrderCommand.SELL;
        }
        return null;
    }

    private static Instrument convertToInstrument(String instrument) {
        if (instrument.equals("EURUSD")) {
            return Instrument.EURUSD;
        } else if (instrument.equals("AUDUSD")) {
            return Instrument.AUDUSD;
        } else if (instrument.equals("GBPUSD")) {
            return Instrument.GBPUSD;
        } else if (instrument.equals("GBPCAD")) {
            return Instrument.GBPCAD;
        } else if (instrument.equals("EURJPY")) {
            return Instrument.EURJPY;
        } else if (instrument.equals("CADJPY")) {
            return Instrument.CADJPY;
        }
        return null;
    }

    public class CSVLine {
        private long time;
        private Instrument instrument;
        private IEngine.OrderCommand orderCommand;
        private InOut inOut;
        private double amount;
        private double price;
        private String date;

        public CSVLine() {

        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public Instrument getInstrument() {
            return instrument;
        }

        public void setInstrument(Instrument instrument) {
            this.instrument = instrument;
        }

        public IEngine.OrderCommand getOrderCommand() {
            return orderCommand;
        }

        public void setOrderCommand(IEngine.OrderCommand orderCommand) {
            this.orderCommand = orderCommand;
        }

        public InOut getInOut() {
            return inOut;
        }

        public void setInOut(InOut inOut) {
            this.inOut = inOut;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

    enum InOut {
        IN,
        OUT
    }
}