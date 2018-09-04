package com.jforexcn.inbox.strategy.competition;

import java.util.*;
import com.dukascopy.api.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.CopyOnWriteArrayList;
import java.lang.reflect.*;
import java.math.BigDecimal;


/*
 * Created by VisualJForex Generator, version 2.40
 * Date: 21.06.2018 22:11
 */
public class rsiadxsmallprofitforGBP implements IStrategy {

    private CopyOnWriteArrayList<TradeEventAction> tradeEventActions = new CopyOnWriteArrayList<TradeEventAction>();
    private static final String DATE_FORMAT_NOW = "yyyyMMdd_HHmmss";
    private IEngine engine;
    private IConsole console;
    private IHistory history;
    private IContext context;
    private IIndicators indicators;
    private IUserInterface userInterface;

    @Configurable("defaultTakeProfit:")
    public int defaultTakeProfit = 300;
    @Configurable("_tptarget:")
    public double _tptarget = 1.0;
    @Configurable("defaultInstrument:")
    public Instrument defaultInstrument = Instrument.GBPUSD;
    @Configurable("defaultSlippage:")
    public int defaultSlippage = 1;
    @Configurable("defaultTradeAmount:")
    public double defaultTradeAmount = 7.0;
    @Configurable("defaultStopLoss:")
    public int defaultStopLoss = 200;
    @Configurable("defaultPeriod:")
    public Period defaultPeriod = Period.TEN_SECS;

    private Candle LastBidCandle =  null ;
    private String AccountId = "";
    private double _out32;
    private double _amount3 = 4.0;
    private double _amount2 = 5.0;
    private double Equity;
    private Tick LastTick =  null ;
    private String AccountCurrency = "";
    private int OverWeekendEndLeverage;
    private double _tempVar174 = 5.0E-5;
    private double Leverage;
    private boolean _bLong = true;
    private List<IOrder> PendingPositions =  null ;
    private List<IOrder> OpenPositions =  null ;
    private double UseofLeverage;
    private IMessage LastTradeEvent =  null ;
    private boolean GlobalAccount;
    private double _rsi0;
    private Candle LastAskCandle =  null ;
    private double _rsi1;
    private int MarginCutLevel;
    private List<IOrder> AllPositions =  null ;
    private IOrder _pos =  null ;
    private double _adx35;
    private boolean _bTrade = false;


    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.history = context.getHistory();
        this.context = context;
        this.indicators = context.getIndicators();
        this.userInterface = context.getUserInterface();

        subscriptionInstrumentCheck(defaultInstrument);

        ITick lastITick = context.getHistory().getLastTick(defaultInstrument);
        LastTick = new Tick(lastITick, defaultInstrument);

        IBar bidBar = context.getHistory().getBar(defaultInstrument, defaultPeriod, OfferSide.BID, 1);
        IBar askBar = context.getHistory().getBar(defaultInstrument, defaultPeriod, OfferSide.ASK, 1);
        LastAskCandle = new Candle(askBar, defaultPeriod, defaultInstrument, OfferSide.ASK);
        LastBidCandle = new Candle(bidBar, defaultPeriod, defaultInstrument, OfferSide.BID);

        if (indicators.getIndicator("RSI") == null) {
            indicators.registerDownloadableIndicator("1286","RSI");
        }
        if (indicators.getIndicator("ADX") == null) {
            indicators.registerDownloadableIndicator("1267","ADX");
        }
        if (indicators.getIndicator("RSI") == null) {
            indicators.registerDownloadableIndicator("1286","RSI");
        }
        if (indicators.getIndicator("RSI") == null) {
            indicators.registerDownloadableIndicator("1286","RSI");
        }
        subscriptionInstrumentCheck(Instrument.fromString("GBP/USD"));

    }

    public void onAccount(IAccount account) throws JFException {
        AccountCurrency = account.getCurrency().toString();
        Leverage = account.getLeverage();
        AccountId= account.getAccountId();
        Equity = account.getEquity();
        UseofLeverage = account.getUseOfLeverage();
        OverWeekendEndLeverage = account.getOverWeekEndLeverage();
        MarginCutLevel = account.getMarginCutLevel();
        GlobalAccount = account.isGlobal();
    }

    private void updateVariables(Instrument instrument) {
        try {
            AllPositions = engine.getOrders();
            List<IOrder> listMarket = new ArrayList<IOrder>();
            for (IOrder order: AllPositions) {
                if (order.getState().equals(IOrder.State.FILLED)){
                    listMarket.add(order);
                }
            }
            List<IOrder> listPending = new ArrayList<IOrder>();
            for (IOrder order: AllPositions) {
                if (order.getState().equals(IOrder.State.OPENED)){
                    listPending.add(order);
                }
            }
            OpenPositions = listMarket;
            PendingPositions = listPending;
        } catch(JFException e) {
            e.printStackTrace();
        }
    }

    public void onMessage(IMessage message) throws JFException {
        if (message.getOrder() != null) {
            updateVariables(message.getOrder().getInstrument());
            LastTradeEvent = message;

            If_block_39(2);
        }
    }

    public void onStop() throws JFException {
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {
        LastTick = new Tick(tick, instrument);
        updateVariables(instrument);

        If_block_30(0);

    }

    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        LastAskCandle = new Candle(askBar, period, instrument, OfferSide.ASK);
        LastBidCandle = new Candle(bidBar, period, instrument, OfferSide.BID);
        updateVariables(instrument);

    }

    public void subscriptionInstrumentCheck(Instrument instrument) {
        try {
            if (!context.getSubscribedInstruments().contains(instrument)) {
                Set<Instrument> instruments = new HashSet<Instrument>();
                instruments.add(instrument);
                context.setSubscribedInstruments(instruments, true);
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public double round(double price, Instrument instrument) {
        BigDecimal big = new BigDecimal("" + price);
        big = big.setScale(instrument.getPipScale() + 1, BigDecimal.ROUND_HALF_UP);
        return big.doubleValue();
    }

    public ITick getLastTick(Instrument instrument) {
        try {
            return (context.getHistory().getTick(instrument, 0));
        } catch (JFException e) {
            e.printStackTrace();
        }
        return null;
    }

    // _rsi0 > 50, _rsi0 < 50
    private  void If_block_15(Integer flow) {
        double argument_1 = _rsi0;
        double argument_2 = 50.0;
        if (argument_1< argument_2) {
            If_block_16(flow);
        }
        else if (argument_1> argument_2) {
            If_block_17(flow);
        }
        else if (argument_1== argument_2) {
        }
    }

    // _rsi1 < 50
    private  void If_block_16(Integer flow) {
        double argument_1 = _rsi1;
        double argument_2 = 50.0;
        if (argument_1< argument_2) {
        }
        else if (argument_1> argument_2) {
            If_block_20(flow);
        }
        else if (argument_1== argument_2) {
        }
    }

    // _rsi1 > 50
    private  void If_block_17(Integer flow) {
        double argument_1 = _rsi1;
        double argument_2 = 50.0;
        if (argument_1< argument_2) {
            If_block_25(flow);
        }
        else if (argument_1> argument_2) {
        }
        else if (argument_1== argument_2) {
        }
    }

    private  void OpenatMarket_block_18(Integer flow) {
        Instrument argument_1 = defaultInstrument;
        double argument_2 = _amount2;
        int argument_3 = defaultSlippage;
        int argument_4 = defaultStopLoss;
        int argument_5 = defaultTakeProfit;
        String argument_6 = "";
        ITick tick = getLastTick(argument_1);

        IEngine.OrderCommand command = IEngine.OrderCommand.BUY;

        double stopLoss = tick.getBid() - argument_1.getPipValue() * argument_4;
        double takeProfit = round(tick.getBid() + argument_1.getPipValue() * argument_5, argument_1);

        try {
            String label = getLabel();
            IOrder order = context.getEngine().submitOrder(label, argument_1, command, argument_2, 0, argument_3,  stopLoss, takeProfit, 0, argument_6);
        } catch (JFException e) {
            e.printStackTrace();
        }
    }

    private  void OpenatMarket_block_19(Integer flow) {
        Instrument argument_1 = defaultInstrument;
        double argument_2 = _amount2;
        int argument_3 = defaultSlippage;
        int argument_4 = defaultStopLoss;
        int argument_5 = defaultTakeProfit;
        String argument_6 = "";
        ITick tick = getLastTick(argument_1);

        IEngine.OrderCommand command = IEngine.OrderCommand.SELL;

        double stopLoss = tick.getAsk() + argument_1.getPipValue() * argument_4;
        double takeProfit = round(tick.getAsk() - argument_1.getPipValue() * argument_5, argument_1);

        try {
            String label = getLabel();
            IOrder order = context.getEngine().submitOrder(label, argument_1, command, argument_2, 0, argument_3,  stopLoss, takeProfit, 0, argument_6);
        } catch (JFException e) {
            e.printStackTrace();
        }
    }

    private  void If_block_20(Integer flow) {
        int argument_1 = OpenPositions.size();
        int argument_2 = 1;
        if (argument_1< argument_2) {
            If_block_33(flow);
        }
        else if (argument_1> argument_2) {
            PositionsViewer_block_22(flow);
        }
        else if (argument_1== argument_2) {
            PositionsViewer_block_22(flow);
        }
    }

    private  void If_block_21(Integer flow) {
        double argument_1 = _pos.getProfitLossInPips();
        double argument_2 = _tptarget;
        if (argument_1< argument_2) {
        }
        else if (argument_1> argument_2) {
            CloseandCancelPosition_block_55(flow);
        }
        else if (argument_1== argument_2) {
        }
    }

    private  void PositionsViewer_block_22(Integer flow) {
        List<IOrder> argument_1 = OpenPositions;
        for (IOrder order : argument_1){
            if (order.getState() == IOrder.State.OPENED||order.getState() == IOrder.State.FILLED){
                _pos = order;
                If_block_23(flow);
            }
        }
    }

    private  void If_block_23(Integer flow) {
        boolean argument_1 = _pos.isLong();
        boolean argument_2 = true;
        if (argument_1!= argument_2) {
        }
        else if (argument_1 == argument_2) {
            If_block_54(flow);
        }
    }

    private  void CloseandCancelPosition_block_24(Integer flow) {
        try {
            if (_pos != null && (_pos.getState() == IOrder.State.OPENED||_pos.getState() == IOrder.State.FILLED)){
                _pos.close();
            }
        } catch (JFException e)  {
            e.printStackTrace();
        }
    }

    private  void If_block_25(Integer flow) {
        int argument_1 = OpenPositions.size();
        int argument_2 = 1;
        if (argument_1< argument_2) {
            If_block_34(flow);
        }
        else if (argument_1> argument_2) {
            PositionsViewer_block_26(flow);
        }
        else if (argument_1== argument_2) {
            PositionsViewer_block_26(flow);
        }
    }

    private  void PositionsViewer_block_26(Integer flow) {
        List<IOrder> argument_1 = OpenPositions;
        for (IOrder order : argument_1){
            if (order.getState() == IOrder.State.OPENED||order.getState() == IOrder.State.FILLED){
                _pos = order;
                If_block_28(flow);
            }
        }
    }

    private  void If_block_27(Integer flow) {
        double argument_1 = _pos.getProfitLossInPips();
        double argument_2 = _tptarget;
        if (argument_1< argument_2) {
        }
        else if (argument_1> argument_2) {
            CloseandCancelPosition_block_53(flow);
        }
        else if (argument_1== argument_2) {
        }
    }

    private  void If_block_28(Integer flow) {
        boolean argument_1 = _pos.isLong();
        boolean argument_2 = true;
        if (argument_1!= argument_2) {
            If_block_51(flow);
        }
        else if (argument_1 == argument_2) {
        }
    }

    private  void CloseandCancelPosition_block_29(Integer flow) {
        try {
            if (_pos != null && (_pos.getState() == IOrder.State.OPENED||_pos.getState() == IOrder.State.FILLED)){
                _pos.close();
            }
        } catch (JFException e)  {
            e.printStackTrace();
        }
    }

    // period.equal(Period.TEN_SECS);
    private  void If_block_30(Integer flow) {
        Period argument_1 = LastAskCandle.getPeriod();
        Period argument_2 = Period.TEN_SECS;
        if (argument_1 == null && argument_2 !=null || (argument_1!= null && !argument_1.equals(argument_2))) {
        }
        else if (argument_1!= null && argument_1.equals(argument_2)) {
            If_block_31(flow);
        }
    }

    // instrument.equal(defaultInstrument);
    private  void If_block_31(Integer flow) {
        Instrument argument_1 = defaultInstrument;
        Instrument argument_2 = LastAskCandle.getInstrument();
        if (argument_1 == null && argument_2 !=null || (argument_1!= null && !argument_1.equals(argument_2))) {
        }
        else if (argument_1!= null && argument_1.equals(argument_2)) {
            RSI_block_32(flow);
        }
    }

    // this._out32 = RSI(FOUR_HOURS, 7)
    private void RSI_block_32(Integer flow) {
        Instrument argument_1 = defaultInstrument;
        Period argument_2 = Period.FOUR_HOURS;
        int argument_3 = 0;
        int argument_4 = 7;
        OfferSide[] offerside = new OfferSide[1];
        IIndicators.AppliedPrice[] appliedPrice = new IIndicators.AppliedPrice[1];
        offerside[0] = OfferSide.BID;
        appliedPrice[0] = IIndicators.AppliedPrice.CLOSE;
        Object[] params = new Object[1];
        params[0] = 7;
        try {
            subscriptionInstrumentCheck(argument_1);
            long time = context.getHistory().getBar(argument_1, argument_2, OfferSide.BID, argument_3).getTime();
            Object[] indicatorResult = context.getIndicators().calculateIndicator(argument_1, argument_2, offerside,
                    "RSI", appliedPrice, params, Filter.WEEKENDS, 1, time, 0);
            if ((new Double(((double [])indicatorResult[0])[0])) == null) {
                this._out32 = Double.NaN;
            } else {
                this._out32 = (((double [])indicatorResult[0])[0]);
            }
            Calculation_block_38(flow);
        } catch (JFException e) {
            e.printStackTrace();
            console.getErr().println(e);
            this._out32 = Double.NaN;
        }
    }

    private  void If_block_33(Integer flow) {
        double argument_1 = _out32;
        double argument_2 = 50.0;
        if (argument_1< argument_2) {
            If_block_36(flow);
        }
        else if (argument_1> argument_2) {
        }
        else if (argument_1== argument_2) {
        }
    }

    private  void If_block_34(Integer flow) {
        double argument_1 = _out32;
        double argument_2 = 50.0;
        if (argument_1< argument_2) {
        }
        else if (argument_1> argument_2) {
            If_block_37(flow);
        }
        else if (argument_1== argument_2) {
        }
    }

    // this._adx35 = ADX(FIFTEEN_MINS, 14)
    private void ADX_block_35(Integer flow) {
        Instrument argument_1 = defaultInstrument;
        Period argument_2 = Period.FIFTEEN_MINS;
        int argument_3 = 0;
        int argument_4 = 14;
        OfferSide[] offerside = new OfferSide[1];
        IIndicators.AppliedPrice[] appliedPrice = new IIndicators.AppliedPrice[1];
        offerside[0] = OfferSide.BID;
        appliedPrice[0] = IIndicators.AppliedPrice.CLOSE;
        Object[] params = new Object[1];
        params[0] = 14;
        try {
            subscriptionInstrumentCheck(argument_1);
            long time = context.getHistory().getBar(argument_1, argument_2, OfferSide.BID, argument_3).getTime();
            Object[] indicatorResult = context.getIndicators().calculateIndicator(argument_1, argument_2, offerside,
                    "ADX", appliedPrice, params, Filter.WEEKENDS, 1, time, 0);
            if ((new Double(((double [])indicatorResult[0])[0])) == null) {
                this._adx35 = Double.NaN;
            } else {
                this._adx35 = (((double [])indicatorResult[0])[0]);
            }
            RSI_block_69(flow);
        } catch (JFException e) {
            e.printStackTrace();
            console.getErr().println(e);
            this._adx35 = Double.NaN;
        }
    }

    private  void If_block_36(Integer flow) {
        double argument_1 = _adx35;
        double argument_2 = 20.0;
        if (argument_1< argument_2) {
        }
        else if (argument_1> argument_2) {
            If_block_47(flow);
        }
        else if (argument_1== argument_2) {
        }
    }

    private  void If_block_37(Integer flow) {
        double argument_1 = _adx35;
        double argument_2 = 20.0;
        if (argument_1< argument_2) {
        }
        else if (argument_1> argument_2) {
            If_block_49(flow);
        }
        else if (argument_1== argument_2) {
        }
    }

    // set amount = Equity * 0.005
    private void Calculation_block_38(Integer flow) {
        double argument_1 = Equity;
        double argument_2 = _tempVar174;
        _amount3 = argument_1 * argument_2;
        ADX_block_35(flow);
    }

    private  void If_block_39(Integer flow) {
        IMessage argument_1 = LastTradeEvent;
        Object argument_2 = null;
        if (argument_1 != null) {
            Assign_block_41(flow);
        }
        if (argument_1 == null) {
        }
    }

    private  void Assign_block_40(Integer flow) {
        boolean argument_1 = true;
        _bTrade =  argument_1;
        If_block_56(flow);
    }

    private  void Assign_block_41(Integer flow) {
        boolean argument_1 = false;
        _bTrade =  argument_1;
        If_block_42(flow);
    }

    private  void If_block_42(Integer flow) {
        IOrder.State argument_1 = LastTradeEvent.getOrder().getState();
        IOrder.State argument_2 = IOrder.State.CLOSED;
        if (argument_1 == null && argument_2 !=null || (argument_1!= null && !argument_1.equals(argument_2))) {
        }
        else if (argument_1!= null && argument_1.equals(argument_2)) {
            If_block_43(flow);
        }
    }

    private  void If_block_43(Integer flow) {
        double argument_1 = LastTradeEvent.getOrder().getProfitLossInPips();
        double argument_2 = -3.0;
        if (argument_1< argument_2) {
            If_block_44(flow);
        }
        else if (argument_1> argument_2) {
        }
        else if (argument_1== argument_2) {
        }
    }

    private  void If_block_44(Integer flow) {
        double argument_1 = LastTradeEvent.getOrder().getAmount();
        double argument_2 = 0.1;
        if (argument_1< argument_2) {
            Assign_block_40(flow);
        }
        else if (argument_1> argument_2) {
        }
        else if (argument_1== argument_2) {
        }
    }

    private  void If_block_47(Integer flow) {
        boolean argument_1 = _bTrade;
        boolean argument_2 = true;
        if (argument_1!= argument_2) {
            OpenatMarket_block_48(flow);
        }
        else if (argument_1 == argument_2) {
            If_block_59(flow);
        }
    }

    private  void OpenatMarket_block_48(Integer flow) {
        Instrument argument_1 = defaultInstrument;
        double argument_2 = 0.001;
        int argument_3 = defaultSlippage;
        int argument_4 = defaultStopLoss;
        int argument_5 = defaultTakeProfit;
        String argument_6 = "";
        ITick tick = getLastTick(argument_1);

        IEngine.OrderCommand command = IEngine.OrderCommand.SELL;

        double stopLoss = tick.getAsk() + argument_1.getPipValue() * argument_4;
        double takeProfit = round(tick.getAsk() - argument_1.getPipValue() * argument_5, argument_1);

        try {
            String label = getLabel();
            IOrder order = context.getEngine().submitOrder(label, argument_1, command, argument_2, 0, argument_3,  stopLoss, takeProfit, 0, argument_6);
        } catch (JFException e) {
            e.printStackTrace();
        }
    }

    private  void If_block_49(Integer flow) {
        boolean argument_1 = _bTrade;
        boolean argument_2 = true;
        if (argument_1!= argument_2) {
            OpenatMarket_block_50(flow);
        }
        else if (argument_1 == argument_2) {
            If_block_60(flow);
        }
    }

    private  void OpenatMarket_block_50(Integer flow) {
        Instrument argument_1 = defaultInstrument;
        double argument_2 = 0.001;
        int argument_3 = defaultSlippage;
        int argument_4 = defaultStopLoss;
        int argument_5 = defaultTakeProfit;
        String argument_6 = "";
        ITick tick = getLastTick(argument_1);

        IEngine.OrderCommand command = IEngine.OrderCommand.BUY;

        double stopLoss = tick.getBid() - argument_1.getPipValue() * argument_4;
        double takeProfit = round(tick.getBid() + argument_1.getPipValue() * argument_5, argument_1);

        try {
            String label = getLabel();
            IOrder order = context.getEngine().submitOrder(label, argument_1, command, argument_2, 0, argument_3,  stopLoss, takeProfit, 0, argument_6);
        } catch (JFException e) {
            e.printStackTrace();
        }
    }

    private  void If_block_51(Integer flow) {
        double argument_1 = _pos.getAmount();
        double argument_2 = 0.1;
        if (argument_1< argument_2) {
            CloseandCancelPosition_block_29(flow);
        }
        else if (argument_1> argument_2) {
            MultipleAction_block_61(flow);
        }
        else if (argument_1== argument_2) {
            CloseandCancelPosition_block_29(flow);
        }
    }

    private  void CloseandCancelPosition_block_53(Integer flow) {
        try {
            if (_pos != null && (_pos.getState() == IOrder.State.OPENED||_pos.getState() == IOrder.State.FILLED)){
                _pos.close();
            }
        } catch (JFException e)  {
            e.printStackTrace();
        }
    }

    private  void If_block_54(Integer flow) {
        double argument_1 = _pos.getAmount();
        double argument_2 = 0.1;
        if (argument_1< argument_2) {
            CloseandCancelPosition_block_24(flow);
        }
        else if (argument_1> argument_2) {
            MultipleAction_block_64(flow);
        }
        else if (argument_1== argument_2) {
            CloseandCancelPosition_block_24(flow);
        }
    }

    private  void CloseandCancelPosition_block_55(Integer flow) {
        try {
            if (_pos != null && (_pos.getState() == IOrder.State.OPENED||_pos.getState() == IOrder.State.FILLED)){
                _pos.close();
            }
        } catch (JFException e)  {
            e.printStackTrace();
        }
    }

    private  void If_block_56(Integer flow) {
        boolean argument_1 = LastTradeEvent.getOrder().isLong();
        boolean argument_2 = true;
        if (argument_1!= argument_2) {
            Assign_block_58(flow);
        }
        else if (argument_1 == argument_2) {
            Assign_block_57(flow);
        }
    }

    private  void Assign_block_57(Integer flow) {
        boolean argument_1 = true;
        _bLong =  argument_1;
    }

    private  void Assign_block_58(Integer flow) {
        boolean argument_1 = false;
        _bLong =  argument_1;
    }

    private  void If_block_59(Integer flow) {
        boolean argument_1 = _bLong;
        boolean argument_2 = true;
        if (argument_1!= argument_2) {
            OpenatMarket_block_19(flow);
        }
        else if (argument_1 == argument_2) {
            OpenatMarket_block_48(flow);
        }
    }

    private  void If_block_60(Integer flow) {
        boolean argument_1 = _bLong;
        boolean argument_2 = true;
        if (argument_1!= argument_2) {
            OpenatMarket_block_50(flow);
        }
        else if (argument_1 == argument_2) {
            OpenatMarket_block_18(flow);
        }
    }

    private  void MultipleAction_block_61(Integer flow) {
        If_block_27(flow);
    }

    private  void MultipleAction_block_64(Integer flow) {
        If_block_21(flow);
    }

    // this._rsi1 = RSI(ONE_MIN, 7), shift = 1
    private void RSI_block_68(Integer flow) {
        Instrument argument_1 = defaultInstrument;
        Period argument_2 = Period.ONE_MIN;
        int argument_3 = 1;
        int argument_4 = 7;
        OfferSide[] offerside = new OfferSide[1];
        IIndicators.AppliedPrice[] appliedPrice = new IIndicators.AppliedPrice[1];
        offerside[0] = OfferSide.BID;
        appliedPrice[0] = IIndicators.AppliedPrice.CLOSE;
        Object[] params = new Object[1];
        params[0] = 7;
        try {
            subscriptionInstrumentCheck(argument_1);
            long time = context.getHistory().getBar(argument_1, argument_2, OfferSide.BID, argument_3).getTime();
            Object[] indicatorResult = context.getIndicators().calculateIndicator(argument_1, argument_2, offerside,
                    "RSI", appliedPrice, params, Filter.WEEKENDS, 1, time, 0);
            if ((new Double(((double [])indicatorResult[0])[0])) == null) {
                this._rsi1 = Double.NaN;
            } else {
                this._rsi1 = (((double [])indicatorResult[0])[0]);
            }
            If_block_15(flow);
        } catch (JFException e) {
            e.printStackTrace();
            console.getErr().println(e);
            this._rsi1 = Double.NaN;
        }
    }

    // this._rsi0 = RSI(ONE_MIN, 7);
    private void RSI_block_69(Integer flow) {
        Instrument argument_1 = defaultInstrument;
        Period argument_2 = Period.ONE_MIN;
        int argument_3 = 0;
        int argument_4 = 7;
        OfferSide[] offerside = new OfferSide[1];
        IIndicators.AppliedPrice[] appliedPrice = new IIndicators.AppliedPrice[1];
        offerside[0] = OfferSide.BID;
        appliedPrice[0] = IIndicators.AppliedPrice.CLOSE;
        Object[] params = new Object[1];
        params[0] = 7;
        try {
            subscriptionInstrumentCheck(argument_1);
            long time = context.getHistory().getBar(argument_1, argument_2, OfferSide.BID, argument_3).getTime();
            Object[] indicatorResult = context.getIndicators().calculateIndicator(argument_1, argument_2, offerside,
                    "RSI", appliedPrice, params, Filter.WEEKENDS, 1, time, 0);
            if ((new Double(((double [])indicatorResult[0])[0])) == null) {
                this._rsi0 = Double.NaN;
            } else {
                this._rsi0 = (((double [])indicatorResult[0])[0]);
            }
            RSI_block_68(flow);
        } catch (JFException e) {
            e.printStackTrace();
            console.getErr().println(e);
            this._rsi0 = Double.NaN;
        }
    }

    class Candle  {

        IBar bar;
        Period period;
        Instrument instrument;
        OfferSide offerSide;

        public Candle(IBar bar, Period period, Instrument instrument, OfferSide offerSide) {
            this.bar = bar;
            this.period = period;
            this.instrument = instrument;
            this.offerSide = offerSide;
        }

        public Period getPeriod() {
            return period;
        }

        public void setPeriod(Period period) {
            this.period = period;
        }

        public Instrument getInstrument() {
            return instrument;
        }

        public void setInstrument(Instrument instrument) {
            this.instrument = instrument;
        }

        public OfferSide getOfferSide() {
            return offerSide;
        }

        public void setOfferSide(OfferSide offerSide) {
            this.offerSide = offerSide;
        }

        public IBar getBar() {
            return bar;
        }

        public void setBar(IBar bar) {
            this.bar = bar;
        }

        public long getTime() {
            return bar.getTime();
        }

        public double getOpen() {
            return bar.getOpen();
        }

        public double getClose() {
            return bar.getClose();
        }

        public double getLow() {
            return bar.getLow();
        }

        public double getHigh() {
            return bar.getHigh();
        }

        public double getVolume() {
            return bar.getVolume();
        }
    }
    class Tick {

        private ITick tick;
        private Instrument instrument;

        public Tick(ITick tick, Instrument instrument){
            this.instrument = instrument;
            this.tick = tick;
        }

        public Instrument getInstrument(){
            return  instrument;
        }

        public double getAsk(){
            return  tick.getAsk();
        }

        public double getBid(){
            return  tick.getBid();
        }

        public double getAskVolume(){
            return  tick.getAskVolume();
        }

        public double getBidVolume(){
            return tick.getBidVolume();
        }

        public long getTime(){
            return  tick.getTime();
        }

        public ITick getTick(){
            return  tick;
        }
    }

    protected String getLabel() {
        String label;
        label = "IVF" + getCurrentTime(LastTick.getTime()) + generateRandom(10000) + generateRandom(10000);
        return label;
    }

    private String getCurrentTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(time);
    }

    private static String generateRandom(int n) {
        int randomNumber = (int) (Math.random() * n);
        String answer = "" + randomNumber;
        if (answer.length() > 3) {
            answer = answer.substring(0, 4);
        }
        return answer;
    }

    class TradeEventAction {
        private IMessage.Type messageType;
        private String nextBlockId = "";
        private String positionLabel = "";
        private int flowId = 0;

        public IMessage.Type getMessageType() {
            return messageType;
        }

        public void setMessageType(IMessage.Type messageType) {
            this.messageType = messageType;
        }

        public String getNextBlockId() {
            return nextBlockId;
        }

        public void setNextBlockId(String nextBlockId) {
            this.nextBlockId = nextBlockId;
        }
        public String getPositionLabel() {
            return positionLabel;
        }

        public void setPositionLabel(String positionLabel) {
            this.positionLabel = positionLabel;
        }
        public int getFlowId() {
            return flowId;
        }
        public void setFlowId(int flowId) {
            this.flowId = flowId;
        }
    }
}