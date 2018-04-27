package com.jforexcn.hub;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.TickBarSize;
import com.dukascopy.api.feed.ITailoredFeedDescriptor;
import com.dukascopy.api.feed.ITailoredFeedListener;
import com.dukascopy.api.feed.ITickBar;
import com.dukascopy.api.feed.util.TickBarFeedDescriptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by simple(simple.continue@gmail.com) on 26/04/2018.
 */

public class FeedDescriptors {

    public static final int TICK_BAR_SIZE_SMALL = 10;
    public static final int TICK_BAR_SIZE_LARGE = 1000;
    private static final Set<ITailoredFeedDescriptor<ITickBar>> TICK_BAR_FEED_DESCRIPTORS = new HashSet<>();
    private static final Set<Instrument> INSTRUMENTS = new HashSet<Instrument>();

    static {
        INSTRUMENTS.add(Instrument.EURUSD);
        INSTRUMENTS.add(Instrument.GBPUSD);
        INSTRUMENTS.add(Instrument.USDCHF);
        INSTRUMENTS.add(Instrument.USDJPY);
        INSTRUMENTS.add(Instrument.USDCAD);
        INSTRUMENTS.add(Instrument.AUDUSD);
        INSTRUMENTS.add(Instrument.NZDUSD);
        INSTRUMENTS.add(Instrument.XAUUSD);
        INSTRUMENTS.add(Instrument.EURGBP);
        INSTRUMENTS.add(Instrument.EURJPY);
        INSTRUMENTS.add(Instrument.GBPJPY);
        INSTRUMENTS.add(Instrument.AUDCAD);
    }

    public static final ITailoredFeedDescriptor<ITickBar> AUDJPY_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> AUDCAD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDCAD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> AUDCHF_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDCHF, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> AUDNZD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDNZD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> AUDSGD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDSGD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> AUDUSD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDUSD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> CADCHF_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.CADCHF, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> CADHKD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.CADHKD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> CADJPY_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.CADJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> CHFJPY_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.CHFJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> CHFPLN_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.CHFPLN, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> CHFSGD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.CHFSGD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURAUD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURAUD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURBRL_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURBRL, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURCAD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURCAD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURCHF_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURCHF, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURCZK_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURCZK, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURDKK_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURDKK, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURGBP_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURGBP, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURHKD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURHKD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURHUF_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURHUF, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURJPY_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURMXN_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURMXN, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURNOK_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURNOK, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURNZD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURNZD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURPLN_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURPLN, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURRUB_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURRUB, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURSEK_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURSEK, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURSGD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURSGD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURTHB_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURTHB, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURTRY_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURTRY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURUSD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURUSD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURZAR_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURZAR, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> GBPAUD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPAUD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> GBPCAD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPCAD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> GBPCHF_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPCHF, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> GBPJPY_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> GBPNZD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPNZD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> GBPUSD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPUSD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> HKDJPY_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.HKDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> HUFJPY_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.HUFJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> MXNJPY_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.MXNJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> NZDCAD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDCAD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> NZDCHF_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDCHF, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> NZDJPY_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> NZDSGD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDSGD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> NZDUSD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDUSD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> SGDJPY_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.SGDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDBRL_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDBRL, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDCAD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDCAD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDCHF_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDCHF, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDCNH_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDCNH, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDCZK_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDCZK, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDDKK_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDDKK, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDHKD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDHKD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDHUF_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDHUF, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDILS_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDILS, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDJPY_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDMXN_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDMXN, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDNOK_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDNOK, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDPLN_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDPLN, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDRON_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDRON, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDRUB_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDRUB, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDSEK_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDSEK, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDSGD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDSGD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDTHB_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDTHB, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDTRY_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDTRY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDZAR_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDZAR, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> XAGUSD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.XAGUSD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> XAUUSD_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.XAUUSD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> ZARJPY_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.ZARJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> TRYJPY_100_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.TRYJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);

    public static final ITailoredFeedDescriptor<ITickBar> AUDJPY_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> AUDCAD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDCAD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> AUDCHF_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDCHF, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> AUDNZD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDNZD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> AUDSGD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDSGD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> AUDUSD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDUSD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> CADCHF_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.CADCHF, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> CADHKD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.CADHKD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> CADJPY_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.CADJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> CHFJPY_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.CHFJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> CHFPLN_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.CHFPLN, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> CHFSGD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.CHFSGD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURAUD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURAUD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURBRL_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURBRL, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURCAD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURCAD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURCHF_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURCHF, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURCZK_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURCZK, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURDKK_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURDKK, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURGBP_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURGBP, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURHKD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURHKD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURHUF_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURHUF, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURJPY_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURMXN_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURMXN, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURNOK_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURNOK, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURNZD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURNZD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURPLN_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURPLN, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURRUB_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURRUB, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURSEK_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURSEK, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURSGD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURSGD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURTHB_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURTHB, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURTRY_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURTRY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURUSD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURUSD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> EURZAR_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURZAR, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> GBPAUD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPAUD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> GBPCAD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPCAD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> GBPCHF_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPCHF, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> GBPJPY_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> GBPNZD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPNZD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> GBPUSD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPUSD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> HKDJPY_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.HKDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> HUFJPY_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.HUFJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> MXNJPY_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.MXNJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> NZDCAD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDCAD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> NZDCHF_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDCHF, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> NZDJPY_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> NZDSGD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDSGD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> NZDUSD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDUSD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> SGDJPY_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.SGDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDBRL_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDBRL, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDCAD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDCAD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDCHF_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDCHF, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDCNH_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDCNH, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDCZK_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDCZK, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDDKK_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDDKK, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDHKD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDHKD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDHUF_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDHUF, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDILS_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDILS, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDJPY_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDMXN_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDMXN, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDNOK_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDNOK, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDPLN_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDPLN, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDRON_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDRON, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDRUB_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDRUB, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDSEK_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDSEK, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDSGD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDSGD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDTHB_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDTHB, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDTRY_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDTRY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> USDZAR_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDZAR, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> XAGUSD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.XAGUSD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> XAUUSD_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.XAUUSD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> ZARJPY_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.ZARJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
    public static final ITailoredFeedDescriptor<ITickBar> TRYJPY_1000_BID_TICK_BAR = createTickBarFeedDescriptor(Instrument.TRYJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);


    public static final ITailoredFeedDescriptor<ITickBar> AUDJPY_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> AUDCAD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDCAD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> AUDCHF_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDCHF, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> AUDNZD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDNZD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> AUDSGD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDSGD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> AUDUSD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDUSD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> CADCHF_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.CADCHF, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> CADHKD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.CADHKD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> CADJPY_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.CADJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> CHFJPY_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.CHFJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> CHFPLN_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.CHFPLN, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> CHFSGD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.CHFSGD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURAUD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURAUD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURBRL_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURBRL, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURCAD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURCAD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURCHF_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURCHF, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURCZK_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURCZK, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURDKK_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURDKK, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURGBP_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURGBP, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURHKD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURHKD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURHUF_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURHUF, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURJPY_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURMXN_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURMXN, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURNOK_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURNOK, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURNZD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURNZD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURPLN_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURPLN, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURRUB_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURRUB, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURSEK_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURSEK, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURSGD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURSGD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURTHB_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURTHB, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURTRY_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURTRY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURUSD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURUSD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURZAR_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURZAR, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> GBPAUD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPAUD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> GBPCAD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPCAD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> GBPCHF_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPCHF, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> GBPJPY_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> GBPNZD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPNZD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> GBPUSD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPUSD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> HKDJPY_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.HKDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> HUFJPY_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.HUFJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> MXNJPY_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.MXNJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> NZDCAD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDCAD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> NZDCHF_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDCHF, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> NZDJPY_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> NZDSGD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDSGD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> NZDUSD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDUSD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> SGDJPY_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.SGDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDBRL_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDBRL, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDCAD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDCAD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDCHF_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDCHF, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDCNH_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDCNH, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDCZK_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDCZK, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDDKK_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDDKK, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDHKD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDHKD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDHUF_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDHUF, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDILS_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDILS, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDJPY_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDMXN_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDMXN, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDNOK_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDNOK, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDPLN_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDPLN, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDRON_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDRON, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDRUB_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDRUB, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDSEK_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDSEK, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDSGD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDSGD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDTHB_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDTHB, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDTRY_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDTRY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDZAR_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDZAR, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> XAGUSD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.XAGUSD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> XAUUSD_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.XAUUSD, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> ZARJPY_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.ZARJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> TRYJPY_100_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.TRYJPY, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);

    public static final ITailoredFeedDescriptor<ITickBar> AUDJPY_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> AUDCAD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDCAD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> AUDCHF_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDCHF, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> AUDNZD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDNZD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> AUDSGD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDSGD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> AUDUSD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.AUDUSD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> CADCHF_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.CADCHF, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> CADHKD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.CADHKD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> CADJPY_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.CADJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> CHFJPY_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.CHFJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> CHFPLN_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.CHFPLN, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> CHFSGD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.CHFSGD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURAUD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURAUD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURBRL_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURBRL, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURCAD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURCAD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURCHF_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURCHF, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURCZK_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURCZK, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURDKK_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURDKK, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURGBP_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURGBP, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURHKD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURHKD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURHUF_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURHUF, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURJPY_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURMXN_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURMXN, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURNOK_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURNOK, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURNZD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURNZD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURPLN_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURPLN, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURRUB_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURRUB, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURSEK_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURSEK, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURSGD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURSGD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURTHB_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURTHB, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURTRY_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURTRY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURUSD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURUSD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> EURZAR_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.EURZAR, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> GBPAUD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPAUD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> GBPCAD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPCAD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> GBPCHF_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPCHF, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> GBPJPY_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> GBPNZD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPNZD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> GBPUSD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.GBPUSD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> HKDJPY_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.HKDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> HUFJPY_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.HUFJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> MXNJPY_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.MXNJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> NZDCAD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDCAD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> NZDCHF_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDCHF, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> NZDJPY_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> NZDSGD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDSGD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> NZDUSD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.NZDUSD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> SGDJPY_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.SGDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDBRL_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDBRL, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDCAD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDCAD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDCHF_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDCHF, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDCNH_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDCNH, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDCZK_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDCZK, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDDKK_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDDKK, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDHKD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDHKD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDHUF_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDHUF, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDILS_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDILS, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDJPY_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDMXN_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDMXN, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDNOK_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDNOK, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDPLN_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDPLN, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDRON_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDRON, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDRUB_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDRUB, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDSEK_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDSEK, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDSGD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDSGD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDTHB_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDTHB, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDTRY_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDTRY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> USDZAR_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.USDZAR, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> XAGUSD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.XAGUSD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> XAUUSD_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.XAUUSD, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> ZARJPY_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.ZARJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
    public static final ITailoredFeedDescriptor<ITickBar> TRYJPY_1000_ASK_TICK_BAR = createTickBarFeedDescriptor(Instrument.TRYJPY, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);



    private static ITailoredFeedDescriptor<ITickBar> createTickBarFeedDescriptor(Instrument instrument, TickBarSize tickBarSize, OfferSide offerSide) {
        ITailoredFeedDescriptor<ITickBar> descriptor = new TickBarFeedDescriptor(
                instrument,
                tickBarSize,
                offerSide
        );
        TICK_BAR_FEED_DESCRIPTORS.add(descriptor);
        return descriptor;
    }

    public static boolean containsDescriptor(ITailoredFeedDescriptor<ITickBar> descriptor) {
        return TICK_BAR_FEED_DESCRIPTORS.contains(descriptor);
    }

    public static boolean containsInstrument(Instrument instrument) {
        return INSTRUMENTS.contains(instrument);
    }

    public static Set<ITailoredFeedDescriptor<ITickBar>> descriptors() {
        return TICK_BAR_FEED_DESCRIPTORS;
    }

    public static Set<Instrument> instruments() {
        return INSTRUMENTS;
    }

    public static Set<ITailoredFeedDescriptor<ITickBar>> getDescriptorsByInstruments(Set<Instrument> instruments) {
        Set<ITailoredFeedDescriptor<ITickBar>> descriptors = new HashSet<>();
        for(ITailoredFeedDescriptor<ITickBar> descriptor : TICK_BAR_FEED_DESCRIPTORS) {
            if (instruments.contains(descriptor.getInstrument())) {
                descriptors.add(descriptor);
            }
        }
        return descriptors;
    }

    public static Set<ITailoredFeedDescriptor<ITickBar>> generateDescriptorsByInstruments(Set<Instrument> instruments) {
        Set<ITailoredFeedDescriptor<ITickBar>> descriptors = new HashSet<>();
        for (Instrument instrument : instruments) {
            ITailoredFeedDescriptor<ITickBar> smallAskDescriptor = createTickBarFeedDescriptor(instrument, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.ASK);
            ITailoredFeedDescriptor<ITickBar> smallBidDescriptor = createTickBarFeedDescriptor(instrument, TickBarSize.valueOf(TICK_BAR_SIZE_SMALL), OfferSide.BID);
            ITailoredFeedDescriptor<ITickBar> largeAskDescriptor = createTickBarFeedDescriptor(instrument, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.ASK);
            ITailoredFeedDescriptor<ITickBar> largeBidDescriptor = createTickBarFeedDescriptor(instrument, TickBarSize.valueOf(TICK_BAR_SIZE_LARGE), OfferSide.BID);
            descriptors.add(smallAskDescriptor);
            descriptors.add(smallBidDescriptor);
            descriptors.add(largeAskDescriptor);
            descriptors.add(largeBidDescriptor);
        }
        return descriptors;
    }
}
