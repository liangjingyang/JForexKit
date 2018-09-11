package com.jforexcn.shared.TestHelper;

import com.dukascopy.api.*;
import com.dukascopy.api.feed.*;
import com.dukascopy.api.instrument.IFinancialInstrument;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by simple on 2018/9/9
 */

public class HistoryTestHelper implements IHistory {

    private List<IBar> bars = new ArrayList<>();

    @Override
    public long getTimeOfLastTick(Instrument instrument) throws JFException {
        return 0;
    }

    @Override
    public ITick getLastTick(Instrument instrument) throws JFException {
        return null;
    }

    @Override
    public long getStartTimeOfCurrentBar(Instrument instrument, Period period) throws JFException {
        return 0;
    }

    @Override
    public IBar getBar(Instrument instrument, Period period, OfferSide side, int shift) throws JFException {
        return null;
    }

    @Override
    public void readTicks(Instrument instrument, long from, long to, LoadingDataListener tickListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public void readTicks(Instrument instrument, int numberOfOneSecondIntervalsBefore, long time, int numberOfOneSecondIntervalsAfter, LoadingDataListener tickListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public void readBars(Instrument instrument, Period period, OfferSide side, long from, long to, LoadingDataListener barListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public void readBars(Instrument instrument, Period period, OfferSide side, Filter filter, long from, long to, LoadingDataListener barListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public void readBars(Instrument instrument, Period period, OfferSide side, Filter filter, int numberOfCandlesBefore, long time, int numberOfCandlesAfter, LoadingDataListener barListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public List<ITick> getTicks(Instrument instrument, long from, long to) throws JFException {
        return null;
    }

    @Override
    public List<ITick> getTicks(Instrument instrument, int numberOfOneSecondIntervalsBefore, long time, int numberOfOneSecondIntervalsAfter) throws JFException {
        return null;
    }

    @Override
    public List<IBar> getBars(Instrument instrument, Period period, OfferSide side, long from, long to) throws JFException {
        return bars;
    }

    @Override
    public List<IBar> getBars(Instrument instrument, Period period, OfferSide side, Filter filter, long from, long to) throws JFException {
        return bars;
    }

    @Override
    public List<IBar> getBars(Instrument instrument, Period period, OfferSide side, Filter filter, int numberOfCandlesBefore, long time, int numberOfCandlesAfter) throws JFException {
        return bars;
    }

    @Override
    public long getBarStart(Period period, long time) throws JFException {
        return 0;
    }

    @Override
    public long getNextBarStart(Period period, long barTime) throws JFException {
        return 0;
    }

    @Override
    public long getPreviousBarStart(Period period, long barTime) throws JFException {
        return 0;
    }

    @Override
    public long getTimeForNBarsBack(Period period, long to, int numberOfBars) throws JFException {
        return 0;
    }

    @Override
    public long getTimeForNBarsForward(Period period, long from, int numberOfBars) throws JFException {
        return 0;
    }

    @Override
    public void readOrdersHistory(Instrument instrument, long from, long to, LoadingOrdersListener ordersListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public List<IOrder> getOrdersHistory(Instrument instrument, long from, long to) throws JFException {
        return null;
    }

    @Override
    public IOrder getHistoricalOrderById(String id) throws JFException {
        return null;
    }

    @Override
    public List<IOrder> getOpenOrders(Instrument instrument, long from, long to) {
        return null;
    }

    @Override
    public double getEquity() {
        return 0;
    }

    @Override
    public ITick getTick(Instrument instrument, int shift) throws JFException {
        return null;
    }

    @Override
    public ITimedData getFeedData(IFeedDescriptor feedDescriptor, int shift) throws JFException {
        return null;
    }

    @Override
    public <T extends ITimedData> T getFeedData(ITailoredFeedDescriptor<T> feedDescriptor, int shift) throws JFException {
        return null;
    }

    @Override
    public <T extends ITimedData> T getFeedData(ITailoredFeedInfo<T> feedInfo, int shift) throws JFException {
        return null;
    }

    @Override
    public List<ITimedData> getFeedData(IFeedDescriptor feedDescriptor, long from, long to) throws JFException {
        return null;
    }

    @Override
    public <T extends ITimedData> List<T> getFeedData(ITailoredFeedDescriptor<T> feedDescriptor, long from, long to) throws JFException {
        return null;
    }

    @Override
    public List<ITimedData> getFeedData(IFeedDescriptor feedDescriptor, int numberOfFeedBarsBefore, long time, int numberOfFeedBarsAfter) throws JFException {
        return null;
    }

    @Override
    public <T extends ITimedData> List<T> getFeedData(ITailoredFeedDescriptor<T> feedDescriptor, int numberOfFeedBarsBefore, long time, int numberOfFeedBarsAfter) throws JFException {
        return null;
    }

    @Override
    public void readFeedData(IFeedDescriptor feedDescriptor, long from, long to, IFeedListener feedListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public <T extends ITimedData> void readFeedData(ITailoredFeedDescriptor<T> feedDescriptor, long from, long to, ITailoredFeedListener<T> feedListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public void readFeedData(IFeedDescriptor feedDescriptor, int numberOfFeedDataBefore, long time, int numberOfFeedDataAfter, IFeedListener feedListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public <T extends ITimedData> void readFeedData(ITailoredFeedDescriptor<T> feedDescriptor, int numberOfFeedDataBefore, long time, int numberOfFeedDataAfter, ITailoredFeedListener<T> feedListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public <T extends ITimedData> Stream<T> readFeedData(ITailoredFeedDescriptor<T> feedDescriptor, int numberOfFeedDataBefore, long time, int numberOfFeedDataAfter, LoadingProgressListener loadingProgressListener) throws JFException {
        return null;
    }

    @Override
    public <T extends ITimedData> Stream<T> readFeedData(ITailoredFeedDescriptor<T> feedDescriptorParam, long from, long to, LoadingProgressListener loadingProgressListener) throws JFException {
        return null;
    }

    @Override
    public void readTicks(IFinancialInstrument financialInstrument, int numberOfOneSecondIntervalsBefore, long time, int numberOfOneSecondIntervalsAfter, LoadingDataListener tickListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public List<IBar> getBars(IFinancialInstrument financialInstrument, Period period, OfferSide side, Filter filter, long from, long to) throws JFException {
        return null;
    }

    @Override
    public List<ITimedData> getFeedData(IFeedInfo feedInfo, long from, long to) throws JFException {
        return null;
    }

    @Override
    public <T extends ITimedData> List<T> getFeedData(ITailoredFeedInfo<T> feedInfo, long from, long to) throws JFException {
        return null;
    }

    @Override
    public long getTimeOfLastTick(IFinancialInstrument financialInstrument) throws JFException {
        return 0;
    }

    @Override
    public ITick getLastTick(IFinancialInstrument financialInstrument) throws JFException {
        return null;
    }

    @Override
    public long getStartTimeOfCurrentBar(IFinancialInstrument financialInstrument, Period period) throws JFException {
        return 0;
    }

    @Override
    public IBar getBar(IFinancialInstrument financialInstrument, Period period, OfferSide side, int shift) throws JFException {
        return null;
    }

    @Override
    public void readTicks(IFinancialInstrument financialInstrument, long from, long to, DataLoadingListener tickListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public void readBars(IFinancialInstrument financialInstrument, Period period, OfferSide side, long from, long to, DataLoadingListener barListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public void readBars(IFinancialInstrument financialInstrument, Period period, OfferSide side, Filter filter, long from, long to, DataLoadingListener barListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public void readBars(IFinancialInstrument financialInstrument, Period period, OfferSide side, Filter filter, int numberOfCandlesBefore, long time, int numberOfCandlesAfter, DataLoadingListener barListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public List<ITick> getTicks(IFinancialInstrument financialInstrument, long from, long to) throws JFException {
        return null;
    }

    @Override
    public List<ITick> getTicks(IFinancialInstrument financialInstrument, int numberOfOneSecondIntervalsBefore, long time, int numberOfOneSecondIntervalsAfter) throws JFException {
        return null;
    }

    @Override
    public List<IBar> getBars(IFinancialInstrument financialInstrument, Period period, OfferSide side, long from, long to) throws JFException {
        return null;
    }

    @Override
    public List<IBar> getBars(IFinancialInstrument financialInstrument, Period period, OfferSide side, Filter filter, int numberOfCandlesBefore, long time, int numberOfCandlesAfter) throws JFException {
        return null;
    }

    @Override
    public void readOrdersHistory(IFinancialInstrument financialInstrument, long from, long to, OrdersLoadingListener ordersListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public List<IOrder> getOrdersHistory(IFinancialInstrument financialInstrument, long from, long to) throws JFException {
        return null;
    }

    @Override
    public List<IOrder> getOpenOrders(IFinancialInstrument financialInstrument, long from, long to) {
        return null;
    }

    @Override
    public ITick getTick(IFinancialInstrument financialInstrument, int shift) throws JFException {
        return null;
    }

    @Override
    public ITimedData getFeedData(IFeedInfo feedInfo, int shift) throws JFException {
        return null;
    }

    @Override
    public List<ITimedData> getFeedData(IFeedInfo feedInfo, int numberOfFeedBarsBefore, long time, int numberOfFeedBarsAfter) throws JFException {
        return null;
    }

    @Override
    public <T extends ITimedData> List<T> getFeedData(ITailoredFeedInfo<T> feedInfo, int numberOfFeedBarsBefore, long time, int numberOfFeedBarsAfter) throws JFException {
        return null;
    }

    @Override
    public void readFeedData(IFeedInfo feedInfo, long from, long to, IFinancialFeedListener feedListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public <T extends ITimedData> void readFeedData(ITailoredFeedInfo<T> feedInfo, long from, long to, ITailoredFinancialFeedListener<T> tailoredFinancialFeedListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public <T extends ITimedData> void readFeedData(IFeedInfo feedInfo, int numberOfFeedDataBefore, long time, int numberOfFeedDataAfter, IFinancialFeedListener financialFeedListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public <T extends ITimedData> void readFeedData(ITailoredFeedInfo<T> feedInfo, int numberOfFeedDataBefore, long time, int numberOfFeedDataAfter, ITailoredFinancialFeedListener<T> tailoredFinancialFeedListener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public IPointAndFigure getPointAndFigure(Instrument instrument, OfferSide offerSide, PriceRange boxSize, ReversalAmount reversalAmount, int shift) throws JFException {
        return null;
    }

    @Override
    public List<IPointAndFigure> getPointAndFigures(Instrument instrument, OfferSide offerSide, PriceRange boxSize, ReversalAmount reversalAmount, long from, long to) throws JFException {
        return null;
    }

    @Override
    public List<IPointAndFigure> getPointAndFigures(Instrument instrument, OfferSide offerSide, PriceRange boxSize, ReversalAmount reversalAmount, int numberOfBarsBefore, long time, int numberOfBarsAfter) throws JFException {
        return null;
    }

    @Override
    public void readPointAndFigures(Instrument instrument, OfferSide offerSide, PriceRange boxSize, ReversalAmount reversalAmount, long from, long to, IPointAndFigureFeedListener listener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public void readPointAndFigures(Instrument instrument, OfferSide offerSide, PriceRange boxSize, ReversalAmount reversalAmount, int numberOfBarsBefore, long time, int numberOfBarsAfter, IPointAndFigureFeedListener listener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public ITickBar getTickBar(Instrument instrument, OfferSide offerSide, TickBarSize tickBarSize, int shift) throws JFException {
        return null;
    }

    @Override
    public List<ITickBar> getTickBars(Instrument instrument, OfferSide offerSide, TickBarSize tickBarSize, long from, long to) throws JFException {
        return null;
    }

    @Override
    public List<ITickBar> getTickBars(Instrument instrument, OfferSide offerSide, TickBarSize tickBarSize, int numberOfBarsBefore, long time, int numberOfBarsAfter) throws JFException {
        return null;
    }

    @Override
    public void readTickBars(Instrument instrument, OfferSide offerSide, TickBarSize tickBarSize, long from, long to, ITickBarFeedListener listener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public void readTickBars(Instrument instrument, OfferSide offerSide, TickBarSize tickBarSize, int numberOfBarsBefore, long time, int numberOfBarsAfter, ITickBarFeedListener listener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public List<IRangeBar> getRangeBars(Instrument instrument, OfferSide offerSide, PriceRange priceRange, long from, long to) throws JFException {
        return null;
    }

    @Override
    public List<IRangeBar> getRangeBars(Instrument instrument, OfferSide offerSide, PriceRange priceRange, int numberOfBarsBefore, long time, int numberOfBarsAfter) throws JFException {
        return null;
    }

    @Override
    public void readRangeBars(Instrument instrument, OfferSide offerSide, PriceRange priceRange, long from, long to, IRangeBarFeedListener listener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public void readRangeBars(Instrument instrument, OfferSide offerSide, PriceRange priceRange, int numberOfBarsBefore, long time, int numberOfBarsAfter, IRangeBarFeedListener listener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public IRangeBar getRangeBar(Instrument instrument, OfferSide offerSide, PriceRange priceRange, int shift) throws JFException {
        return null;
    }

    @Override
    public IRenkoBar getRenkoBar(Instrument instrument, OfferSide offerSide, PriceRange brickSize, int shift) throws JFException {
        return null;
    }

    @Override
    public List<IRenkoBar> getRenkoBars(Instrument instrument, OfferSide offerSide, PriceRange brickSize, long from, long to) throws JFException {
        return null;
    }

    @Override
    public List<IRenkoBar> getRenkoBars(Instrument instrument, OfferSide offerSide, PriceRange brickSize, int numberOfBarsBefore, long time, int numberOfBarsAfter) throws JFException {
        return null;
    }

    @Override
    public void readRenkoBars(Instrument instrument, OfferSide offerSide, PriceRange brickSize, long from, long to, IRenkoBarFeedListener listener, LoadingProgressListener loadingProgress) throws JFException {

    }

    @Override
    public void readRenkoBars(Instrument instrument, OfferSide offerSide, PriceRange brickSize, int numberOfBarsBefore, long time, int numberOfBarsAfter, IRenkoBarFeedListener listener, LoadingProgressListener loadingProgress) throws JFException {

    }

    public void setBars(ArrayList<IBar> bars) {
        this.bars = bars;
    }
}
