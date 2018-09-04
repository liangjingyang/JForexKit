package com.jforexcn.hub.strategy;

/**
 * Created by simple on 19/06/18.
**/

import com.dukascopy.api.Filter;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Library;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.RequiresFullAccess;

import java.util.HashMap;
import java.util.List;

@RequiresFullAccess
@Library("/data/github/JForexKit/build/libs/JForexKit-3.0.jar")
public class DailyVolatilityWatcher extends SubStrategy {

    private static String STRATEGY_TAG = "DailyVolatilityStrategy";
    private HashMap<Instrument, Double> lastHighestHashMap = new HashMap<>();
    private HashMap<Instrument, Double> lastLowestHashMap = new HashMap<>();
    private HashMap<Instrument, Double> lastEmailHighestHashMap = new HashMap<>();
    private HashMap<Instrument, Double> lastEmailLowestHashMap = new HashMap<>();

    private String getEmail() throws JFException {
        return getConfig("email", String.class);
    }

    private int getReversePips(Instrument instrument) throws JFException {
        return getConfig(instrument.toString(), "reversePips", Integer.class);
    }

    @Override
    public void onStart(IContext context) throws JFException {
        super.onStart(context);
        init(context, STRATEGY_TAG);
        helper.setMailTo(getEmail());
        helper.logDebug(STRATEGY_TAG + " start!");
        for (Instrument instrument : helper.getInstrumentSet()) {
            refreshHighestLowest(instrument);
            lastEmailHighestHashMap.put(instrument, 0.0);
            lastEmailLowestHashMap.put(instrument, 0.0);
        }
    }

    @Override
    public void onAccount(IAccount account) throws JFException {
    }


    @Override
    public void onMessage(IMessage message) throws JFException {
        super.onMessage(message);
    }

    @Override
    public void onStop() throws JFException {
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (helper.isStrategyInstrument(instrument)) {
            if (period.equals(Period.ONE_HOUR)) {
                refreshHighestLowest(instrument);
            } else if (period.equals(Period.ONE_MIN)) {
                double pipValue = instrument.getPipValue();
                double lastHighest = lastHighestHashMap.get(instrument);
                double lastLowest = lastLowestHashMap.get(instrument);
                double lastEmailHighest = lastEmailHighestHashMap.get(instrument);
                double lastEmailLowest = lastEmailLowestHashMap.get(instrument);
//                helper.logDebug(instrument.toString() + ", lastEmailHighest: " + lastEmailHighest + ", lastEmailLowest: " + lastEmailLowest);
//                helper.logDebug(instrument.toString() + ", lastHighest: " + lastHighest + ", LastLowest: " + lastLowest);
                List<IBar> askBars = helper.mHistory.getBars(instrument, period, OfferSide.ASK, Filter.ALL_FLATS, 2, askBar.getTime(), 0);
                if (lastEmailHighest != lastHighest &&
                        askBars.get(0).getClose() + getReversePips(instrument) * pipValue <= lastHighest &&
                        askBars.get(1).getClose() + getReversePips(instrument) * pipValue >= lastHighest) {
                    helper.logDebug(instrument.toString() + " Highest Reverse at " + askBars.get(1).getClose() +
                            "\nLastHighest: " + lastHighest + ", LastLowest: " + lastLowest +
                            "\nlastEmailHighest: " + lastEmailHighest + ", lastEmailLowest: " + lastEmailLowest);
                    helper.sendEmail("DailyVolatility " + instrument.toString() + " " +
                            askBars.get(1).getClose(), "Highest Reverse.LastHighest: " + lastHighest + "\nLastLowest: " +
                            lastLowest + "\n" + "askBars.getClose: " + askBars.get(1).getClose());
                    lastEmailHighestHashMap.put(instrument, lastHighest);
                }
                List<IBar> bidBars = helper.mHistory.getBars(instrument, period, OfferSide.BID, Filter.ALL_FLATS, 2, bidBar.getTime(), 0);
                if (lastEmailLowest != lastLowest &&
                        bidBars.get(0).getClose() - getReversePips(instrument) * pipValue <= lastLowest &&
                        bidBars.get(1).getClose() - getReversePips(instrument) * pipValue >= lastLowest) {
                    helper.logDebug(instrument.toString() + " Lowest Reverse at " + bidBars.get(1).getClose() +
                            "\nLastHighest: " + lastHighest + ", LastLowest: " + lastLowest +
                            "\nlastEmailHighest: " + lastEmailHighest + ", lastEmailLowest: " + lastEmailLowest);
                    helper.sendEmail("DailyVolatility " + instrument.toString() + " " +
                            bidBars.get(1).getClose(), "Lowest Reverse.\nLastHighest: " + lastHighest + "\nLastLowest: " +
                            lastLowest + "\n" + "bidBars.getClose: " + bidBars.get(1).getClose());
                    lastEmailLowestHashMap.put(instrument, lastLowest);
                }
            }
        }
    }

    private void refreshHighestLowest(Instrument instrument) throws JFException {
        long time = helper.mHistory.getStartTimeOfCurrentBar(instrument, Period.ONE_HOUR);
        double[] zigzags = helper.mIndicators.zigzag(instrument, Period.ONE_HOUR, OfferSide.ASK,
                4, 5, 3, Filter.ALL_FLATS, 24, time, 0);
        int counter = 0;
        double lastHighest = 0;
        double lastLowest = 0;
        for (int i = zigzags.length - 1; i >= 0; i--) {
            if (zigzags[i] > 0) {
                if (counter == 0) {
                    lastHighest = zigzags[i];
                    lastLowest = zigzags[i];
                    counter = counter + 1;
                } else if (counter == 1) {
                    if (zigzags[i] < lastLowest) {
                        lastLowest = zigzags[i];
                    } else if (zigzags[i] > lastHighest) {
                        lastHighest = zigzags[i];
                    }
                    if (lastHighest > lastLowest) {
                        break;
                    }
                }
            }
        }
        lastHighestHashMap.put(instrument, lastHighest);
        lastLowestHashMap.put(instrument, lastLowest);
        helper.logDebug(instrument.toString() + " LastHighest: " + lastHighest + ", LastLowest: " + lastLowest);
    }


}