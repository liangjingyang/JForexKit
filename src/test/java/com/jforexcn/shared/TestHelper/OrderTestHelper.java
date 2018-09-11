package com.jforexcn.shared.TestHelper;

import com.dukascopy.api.ICloseOrder;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IFillOrder;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.instrument.IFinancialInstrument;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by simple on 12/12/16.
 */

public class OrderTestHelper extends BaseTestHelper implements IOrder {

    private Instrument instrument;
    private String label;
    private String id;
    private long creationTime = 0;
    private long closeTime = 0;
    private IEngine.OrderCommand orderCommand;
    private long fillTime = 0;
    private double originalAmount = 0;
    private double amount = 0;
    private double requestAmount = 0;
    private double openPrice = 0;
    private double closePrice = 0;
    private double stopLossPrice = 0;
    private double takeProfitPrice = 0;
    private State state;
    private double profitLossInPips = 0;
    private OfferSide stopLossSide;

    public static class OrderBuilder {
        private Instrument instrument;
        private String label;
        private String id;
        private long creationTime = 0;
        private long closeTime = 0;
        private IEngine.OrderCommand orderCommand;
        private long fillTime = 0;
        private double originalAmount = 0;
        private double amount = 0;
        private double requestAmount = 0;
        private double openPrice = 0;
        private double closePrice = 0;
        private double stopLossPrice = 0;
        private double takeProfitPrice = 0;
        private State state;
        private double profitLossInPips = 0;
        private OfferSide stopLossSide;

        public OrderBuilder reset() {
            this.instrument         = null;
            this.label              = null;
            this.id                 = null;
            this.creationTime       = 0;
            this.closeTime          = 0;
            this.orderCommand       = null;
            this.fillTime           = 0;
            this.originalAmount     = 0;
            this.amount             = 0;
            this.requestAmount      = 0;
            this.openPrice          = 0;
            this.closePrice         = 0;
            this.stopLossPrice      = 0;
            this.takeProfitPrice    = 0;
            this.state              = null;
            this.profitLossInPips   = 0;
            this.stopLossSide       = null;
            return this;
        }

        public OrderBuilder setInstrument(Instrument instrument) {
            this.instrument = instrument;
            return this;
        }

        public OrderBuilder setLabel(String label) {
            this.label = label;
            return this;
        }

        public OrderBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public OrderBuilder setCreationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public OrderBuilder setCloseTime(long closeTime) {
            this.closeTime = closeTime;
            return this;
        }

        public OrderBuilder setOrderCommand(IEngine.OrderCommand orderCommand) {
            this.orderCommand = orderCommand;
            return this;
        }

        public OrderBuilder setFillTime(long fillTime) {
            this.fillTime = fillTime;
            return this;
        }

        public OrderBuilder setOriginalAmount(double originalAmount) {
            this.originalAmount = originalAmount;
            return this;
        }

        public OrderBuilder setAmount(double amount) {
            this.amount = amount;
            return this;
        }

        public OrderBuilder setRequestAmount(double requestAmount) {
            this.requestAmount = requestAmount;
            return this;
        }

        public OrderBuilder setOpenPrice(double openPrice) {
            this.openPrice = openPrice;
            return this;
        }

        public OrderBuilder setClosePrice(double closePrice) {
            this.closePrice = closePrice;
            return this;
        }

        public OrderBuilder setStopLossPrice(double stopLossPrice) {
            this.stopLossPrice = stopLossPrice;
            return this;
        }

        public OrderBuilder setTakeProfitPrice(double takeProfitPrice) {
            this.takeProfitPrice = takeProfitPrice;
            return this;
        }

        public OrderBuilder setState(State state) {
            this.state = state;
            return this;
        }

        public OrderBuilder setProfitLossInPips(double profitLossInPips) {
            this.profitLossInPips = profitLossInPips;
            return this;
        }

        public OrderBuilder setStopLossSide(OfferSide stopLossSide) {
            this.stopLossSide = stopLossSide;
            return this;
        }

        public OrderTestHelper create() {
            return new OrderTestHelper(
                    this.instrument,
                    this.label,
                    this.id,
                    this.creationTime,
                    this.closeTime,
                    this.orderCommand,
                    this.fillTime,
                    this.originalAmount,
                    this.amount,
                    this.requestAmount,
                    this.openPrice,
                    this.closePrice,
                    this.stopLossPrice,
                    this.takeProfitPrice,
                    this.state,
                    this.profitLossInPips,
                    this.stopLossSide
            );
        }

    }


    private OrderTestHelper(
            Instrument instrument,
            String label,
            String id,
            long creationTime,
            long closeTime,
            IEngine.OrderCommand orderCommand,
            long fillTime,
            double originalAmount,
            double amount,
            double requestAmount,
            double openPrice,
            double closePrice,
            double stopLossPrice,
            double takeProfitPrice,
            State state,
            double profitLossInPips,
            OfferSide stopLossSide
) {
        this.instrument         = instrument;
        this.label              = label;
        this.id                 = id;
        this.creationTime       = creationTime;
        this.closeTime          = closeTime;
        this.orderCommand       = orderCommand;
        this.fillTime           = fillTime;
        this.originalAmount     = originalAmount;
        this.amount             = amount;
        this.requestAmount      = requestAmount;
        this.openPrice          = openPrice;
        this.closePrice         = closePrice;
        this.stopLossPrice      = stopLossPrice;
        this.takeProfitPrice    = takeProfitPrice;
        this.state              = state;
        this.profitLossInPips   = profitLossInPips;
        this.stopLossSide       = stopLossSide;
    }



    @Override
    public Instrument getInstrument() {
        return instrument;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public long getCloseTime() {
        return closeTime;
    }

    @Override
    public IEngine.OrderCommand getOrderCommand() {
        return orderCommand;
    }

    @Override
    public boolean isLong() {
        return orderCommand.isLong();
    }

    @Override
    public long getFillTime() {
        return fillTime;
    }

    @Override
    public double getOriginalAmount() {
        return originalAmount;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public double getRequestedAmount() {
        return requestAmount;
    }

    @Override
    public double getOpenPrice() {
        return openPrice;
    }

    @Override
    public double getClosePrice() {
        return closePrice;
    }

    @Override
    public double getStopLossPrice() {
        return stopLossPrice;
    }

    @Override
    public double getTakeProfitPrice() {
        return takeProfitPrice;
    }

    @Override
    public void setStopLossPrice(double price) throws JFException {
        setStopLossPrice(price, OfferSide.ASK, 0);
    }

    @Override
    public void setStopLossPrice(double price, OfferSide side) throws JFException {
        setStopLossPrice(price, side, 0);
    }

    @Override
    public void setStopLossPrice(double price, OfferSide side, double trailingStep) throws JFException {
        this.stopLossPrice = price;
        this.stopLossSide = side;
        MessageTestHelper messageTestHelper = getMessageBuilder()
                .setType(IMessage.Type.ORDER_CHANGED_OK)
                .setCreationTime(System.currentTimeMillis())
                .setOrder(this)
                .create();
        getStrategy().onMessage(messageTestHelper);
    }

    @Override
    public void setLabel(String label) throws JFException {

    }

    @Override
    public OfferSide getStopLossSide() {
        return stopLossSide;
    }

    @Override
    public double getTrailingStep() {
        return 0;
    }

    @Override
    public void setTakeProfitPrice(double price) throws JFException {

    }

    @Override
    public String getComment() {
        return null;
    }

    @Override
    public void setComment(String comment) throws JFException {

    }

    @Override
    public void setRequestedAmount(double amount) throws JFException {

    }

    @Override
    public void setOpenPrice(double price) throws JFException {

    }

    @Override
    public void setOpenPrice(double price, double slippage) throws JFException {

    }

    @Override
    public void close(double amount, double price, double slippage) throws JFException {
        if (amount > this.amount) {
            System.out.println("!!! Close order amount error");
            return;
        }
        calcClosePriceAndState(amount, price);
        MessageTestHelper messageTestHelper = getMessageBuilder()
                .setType(IMessage.Type.ORDER_CLOSE_OK)
                .setCreationTime(System.currentTimeMillis())
                .setOrder(this)
                .create();
        getStrategy().onMessage(messageTestHelper);
    }

    @Override
    public void close(double amount, double price) throws JFException {
        close(amount, 0, 0);
    }

    @Override
    public void close(double amount) throws JFException {
        close(amount, 0, 0);
    }

    @Override
    public void close() throws JFException {
        close(getAmount(), 0, 0);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setGoodTillTime(long goodTillTime) throws JFException {

    }

    @Override
    public long getGoodTillTime() {
        return 0;
    }

    @Override
    public void waitForUpdate(long timeoutMills) {

    }

    @Override
    public IMessage waitForUpdate(long timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public IMessage waitForUpdate(State... states) throws JFException {
        return null;
    }

    @Override
    public IMessage waitForUpdate(long timeoutMills, State... states) throws JFException {
        return null;
    }

    @Override
    public IMessage waitForUpdate(long timeout, TimeUnit unit, State... states) throws JFException {
        return null;
    }

    @Override
    public double getProfitLossInPips() {
        return profitLossInPips;
    }

    public void setProfitLossInPips(double profitLossInPips) {
        BigDecimal b = new BigDecimal(profitLossInPips);
        this.profitLossInPips = b.setScale(1, BigDecimal.ROUND_FLOOR).doubleValue();
    }

    @Override
    public double getProfitLossInUSD() {
        return 0;
    }

    @Override
    public double getProfitLossInAccountCurrency() {
        return 0;
    }

    @Override
    public double getCommission() {
        return 0;
    }

    @Override
    public double getCommissionInUSD() {
        return 0;
    }

    @Override
    public List<IFillOrder> getFillHistory() {
        return null;
    }

    @Override
    public List<ICloseOrder> getCloseHistory() {
        return null;
    }

    @Override
    public boolean isOCO() {
        return false;
    }

    @Override
    public boolean compare(IOrder order) {
        return false;
    }

    @Override
    public IFinancialInstrument getFinancialInstrument() {
        return null;
    }


    private void calcClosePriceAndState(double amount, double price) {
        this.closePrice = (amount * price + (this.originalAmount - this.amount) * this.closePrice) / (amount + (this.originalAmount - this.amount));
        this.amount = Math.max(0, this.amount - amount);
        if (this.amount <= 0) {
            this.state = State.CLOSED;
        }
    }
}
