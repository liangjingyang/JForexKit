package com.jforexcn.shared.TestHelper;

import com.dukascopy.api.ConfigurableChangeListener;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IChart;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IDataService;
import com.dukascopy.api.IDownloadableStrategies;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IReportService;
import com.dukascopy.api.IStrategies;
import com.dukascopy.api.ITimedData;
import com.dukascopy.api.IUserInterface;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFUtils;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.PriceRange;
import com.dukascopy.api.ReversalAmount;
import com.dukascopy.api.TickBarSize;
import com.dukascopy.api.feed.IBarFeedListener;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.feed.IFeedInfo;
import com.dukascopy.api.feed.IFeedInfoProvider;
import com.dukascopy.api.feed.IFeedListener;
import com.dukascopy.api.feed.IFinancialBarFeedListener;
import com.dukascopy.api.feed.IFinancialFeedListener;
import com.dukascopy.api.feed.IFinancialTickFeedListener;
import com.dukascopy.api.feed.IPointAndFigureFeedListener;
import com.dukascopy.api.feed.IRangeBarFeedListener;
import com.dukascopy.api.feed.IRenkoBarFeedListener;
import com.dukascopy.api.feed.ITailoredFeedDescriptor;
import com.dukascopy.api.feed.ITailoredFeedListener;
import com.dukascopy.api.feed.ITickBarFeedListener;
import com.dukascopy.api.feed.ITickFeedListener;
import com.dukascopy.api.instrument.IFinancialInstrument;
import com.dukascopy.api.instrument.IFinancialInstrumentProvider;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by simple on 12/12/16.
 */

public class ContextTestHelper implements IContext {

    private EngineTestHelper engine;
    private HistoryTestHelper history;
    private IndicatorsTestHelper indicators;
    private Set<Instrument> subscribedInstruments = new HashSet<>();

    public void setEngine(EngineTestHelper engine) {
        this.engine = engine;
    }

    public void setHistory(HistoryTestHelper history) {
        this.history = history;
    }

    @Override
    public EngineTestHelper getEngine() {
        return this.engine;
    }

    @Override
    public IChart getChart(Instrument instrument) {
        return null;
    }

    @Override
    public Set<IChart> getCharts(Instrument instrument) {
        return null;
    }

    @Override
    public Set<IChart> getCharts() {
        return null;
    }

    @Override
    public IChart getLastActiveChart() {
        return null;
    }

    @Override
    public IChart openChart(IFeedDescriptor feedDescriptor) {
        return null;
    }

    @Override
    public void closeChart(IChart chart) {

    }

    @Override
    public IUserInterface getUserInterface() {
        return null;
    }

    @Override
    public IHistory getHistory() {
        return history;
    }

    @Override
    public IConsole getConsole() {
        return null;
    }

    @Override
    public IIndicators getIndicators() {
        return null;
    }

    @Override
    public IAccount getAccount() {
        return null;
    }

    @Override
    public IDownloadableStrategies getDownloadableStrategies() {
        return null;
    }

    @Override
    public JFUtils getUtils() {
        return null;
    }

    @Override
    public IDataService getDataService() {
        return null;
    }

    @Override
    public IReportService getReportService() {
        return null;
    }

    @Override
    public void setSubscribedInstruments(Set<Instrument> instruments) {

    }

    @Override
    public void setSubscribedInstruments(Set<Instrument> instruments, boolean lock) {
        this.subscribedInstruments = instruments;
    }

    @Override
    public void unsubscribeInstruments(Set<Instrument> instruments) {

    }

    @Override
    public void setSubscribedFinancialInstruments(Set<IFinancialInstrument> financialInstruments, boolean lock) {

    }

    @Override
    public Set<Instrument> getSubscribedInstruments() {
        return this.subscribedInstruments;
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public boolean isFullAccessGranted() {
        return false;
    }

    @Override
    public <T> Future<T> executeTask(Callable<T> callable) {
        return null;
    }

    @Override
    public File getFilesDir() {
        return null;
    }

    @Override
    public void pause() {

    }

    @Override
    public <T extends ITimedData> void subscribeToFeed(ITailoredFeedDescriptor<T> feedDescriptor, ITailoredFeedListener<T> feedListener) {

    }

    @Override
    public <T extends ITimedData> void unsubscribeFromFeed(ITailoredFeedListener<T> feedListener) {

    }

    @Override
    public <T extends ITimedData> void unsubscribeFromFeed(ITailoredFeedListener<T> feedListener, ITailoredFeedDescriptor<T> feedDescriptor) {

    }

    @Override
    public void subscribeToFeed(IFeedDescriptor feedDescriptor, IFeedListener feedListener) {

    }

    @Override
    public void unsubscribeFromFeed(IFeedListener feedListener) {

    }

    @Override
    public void unsubscribeFromFeed(IFeedListener feedListener, IFeedDescriptor feedDescriptor) {

    }

    @Override
    public void subscribeToTicksFeed(Instrument instrument, ITickFeedListener listener) {

    }

    @Override
    public void unsubscribeFromTicksFeed(IFinancialTickFeedListener listener) {

    }

    @Override
    public void unsubscribeFromTicksFeed(ITickFeedListener listener) {

    }

    @Override
    public void subscribeToBarsFeed(Instrument instrument, Period period, OfferSide offerSide, IBarFeedListener listener) {

    }

    @Override
    public void unsubscribeFromBarsFeed(IFinancialBarFeedListener listener) {

    }

    @Override
    public void unsubscribeFromBarsFeed(IBarFeedListener listener) {

    }

    @Override
    public void addConfigurationChangeListener(String parameter, PropertyChangeListener listener) {

    }

    @Override
    public void removeConfigurationChangeListener(String parameter, PropertyChangeListener listener) {

    }

    @Override
    public void setConfigurableChangeListener(ConfigurableChangeListener listener) {

    }

    @Override
    public long getTime() {
        return System.currentTimeMillis();
    }

    @Override
    public void subscribeToFeed(IFeedInfo feedInfo, IFinancialFeedListener financialfeedListener) {

    }

    @Override
    public void setSubscribedFinancialInstruments(Set<IFinancialInstrument> financialInstruments) {

    }

    @Override
    public IChart openChart(IFeedInfo feedInfo) {
        return null;
    }

    @Override
    public IChart getChart(IFinancialInstrument financialInstrument) {
        return null;
    }

    @Override
    public Set<IChart> getCharts(IFinancialInstrument financialInstrument) {
        return null;
    }

    @Override
    public IFinancialInstrumentProvider getFinancialInstrumentProvider() {
        return null;
    }

    @Override
    public IFeedInfoProvider getFeedInfoProvider() {
        return null;
    }

    @Override
    public Set<IFinancialInstrument> getSubscribedFinancialInstruments() {
        return null;
    }

    @Override
    public void subscribeToTicksFeed(IFinancialInstrument financialInstrument, IFinancialTickFeedListener listener) {

    }

    @Override
    public void subscribeToBarsFeed(IFinancialInstrument financialInstrument, Period period, OfferSide offerSide, IFinancialBarFeedListener listener) {

    }

    @Override
    public void subscribeToRangeBarFeed(Instrument instrument, OfferSide offerSide, PriceRange priceRange, IRangeBarFeedListener listener) {

    }

    @Override
    public void unsubscribeFromRangeBarFeed(IRangeBarFeedListener listener) {

    }

    @Override
    public void subscribeToPointAndFigureFeed(Instrument instrument, OfferSide offerSide, PriceRange priceRange, ReversalAmount reversalAmount, IPointAndFigureFeedListener listener) {

    }

    @Override
    public void unsubscribeFromPointAndFigureFeed(IPointAndFigureFeedListener listener) {

    }

    @Override
    public void subscribeToTickBarFeed(Instrument instrument, OfferSide offerSide, TickBarSize tickBarSize, ITickBarFeedListener listener) {

    }

    @Override
    public void unsubscribeFromTickBarFeed(ITickBarFeedListener listener) {

    }

    @Override
    public void subscribeToRenkoBarFeed(Instrument instrument, OfferSide offerSide, PriceRange brickSize, IRenkoBarFeedListener listener) {

    }

    @Override
    public void unsubscribeFromRenkoBarFeed(IRenkoBarFeedListener listener) {

    }

    @Override
    public IStrategies getStrategies() {
        return null;
    }

}
