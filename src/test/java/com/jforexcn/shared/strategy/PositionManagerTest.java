package com.jforexcn.shared.strategy;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;
import com.jforexcn.shared.TestHelper.BarTestHelper;
import com.jforexcn.shared.TestHelper.EngineTestHelper;
import com.jforexcn.shared.TestHelper.OrderTestHelper;
import com.jforexcn.shared.TestHelper.BaseTestHelper;
import com.jforexcn.shared.TestHelper.ContextTestHelper;
import com.jforexcn.shared.strategy.PositionManager;

import org.junit.Assert;
import org.junit.Test;



/**
 * Created by simple on 12/12/16.
 */

public class PositionManagerTest {

    @Test
    // 没有止损的独立仓位必须设置止损,
    public void testSetMaxStopLoss() throws Exception {
        ContextTestHelper context = new ContextTestHelper();
        EngineTestHelper engine = new EngineTestHelper();
        context.setEngine(engine);
        IStrategy positionManager = new PositionManager();
        BaseTestHelper.onStart(positionManager, context);

        BarTestHelper.BarBuilder barBuilder = new BarTestHelper.BarBuilder();

        IOrder order = engine.submitOrder("label1", Instrument.EURUSD, IEngine.OrderCommand.BUY, 0.02, 1.06000);



        BarTestHelper askBar = barBuilder
                .setClose(1.06020)
                .setOpen(1.05020)
                .setHigh(1.06120)
                .setLow(1.05020)
                .create();
        BarTestHelper bidBar = barBuilder
                .setClose(1.06000)
                .setOpen(1.05000)
                .setHigh(1.06100)
                .setLow(1.05000)
                .create();
        positionManager.onBar(Instrument.EURUSD, Period.TEN_SECS, askBar, bidBar);

        Assert.assertTrue(order.getStopLossPrice() > 0);
    }

    @Test
    // 没有止损的独立仓位必须设置止损,
    public void testCloseHalf() throws Exception {
        ContextTestHelper context = new ContextTestHelper();
        EngineTestHelper engine = new EngineTestHelper();
        context.setEngine(engine);
        IStrategy positionManager = new PositionManager();
        BaseTestHelper.onStart(positionManager, context);

        BarTestHelper.BarBuilder barBuilder = new BarTestHelper.BarBuilder();

        IOrder order = engine.submitOrder("label1", Instrument.EURUSD, IEngine.OrderCommand.BUY, 0.02, 1.06000, 0, 1.05700, 0);



        BarTestHelper askBar = barBuilder
                .setClose(1.06420)
                .setOpen(1.06020)
                .setHigh(1.06520)
                .setLow(1.05920)
                .create();
        BarTestHelper bidBar = barBuilder
                .setClose(1.06400)
                .setOpen(1.06000)
                .setHigh(1.06500)
                .setLow(1.05900)
                .create();

        positionManager.onBar(Instrument.EURUSD, Period.TEN_SECS, askBar, bidBar);
        Assert.assertTrue(order.getStopLossPrice() == 1.06000);

        ((OrderTestHelper) order).setProfitLossInPips(10);
        positionManager.onBar(Instrument.EURUSD, Period.TEN_SECS, askBar, bidBar);
        Assert.assertTrue(order.getAmount() == 0.01);
    }

    @Test
    // 没有止损的独立仓位必须设置止损,
    public void testOrderGroup() throws Exception {
        ContextTestHelper context = new ContextTestHelper();
        EngineTestHelper engine = new EngineTestHelper();
        context.setEngine(engine);
        IStrategy positionManager = new PositionManager();
        BaseTestHelper.onStart(positionManager, context);
        IOrder buyOrder = engine.submitOrder("label1", Instrument.EURUSD, IEngine.OrderCommand.BUY, 0.02, 1.06000, 0, 1.05700, 0);

        BarTestHelper.BarBuilder barBuilder = new BarTestHelper.BarBuilder();


        BarTestHelper askBar = barBuilder
                .setClose(1.06420)
                .setOpen(1.06020)
                .setHigh(1.06520)
                .setLow(1.05920)
                .create();
        BarTestHelper bidBar = barBuilder
                .setClose(1.06400)
                .setOpen(1.06000)
                .setHigh(1.06500)
                .setLow(1.05900)
                .create();

        engine.onBar(Instrument.EURUSD, Period.TEN_SECS, askBar, bidBar);
        Assert.assertTrue(buyOrder.getStopLossPrice() == 1.06000);

        engine.onBar(Instrument.EURUSD, Period.TEN_SECS, askBar, bidBar);
        Assert.assertTrue(buyOrder.getAmount() == 0.01);


        BarTestHelper askBar2 = barBuilder
                .setClose(1.06920)
                .setOpen(1.06520)
                .setHigh(1.06920)
                .setLow(1.06420)
                .create();
        BarTestHelper bidBar2 = barBuilder
                .setClose(1.06900)
                .setOpen(1.06500)
                .setHigh(1.06900)
                .setLow(1.06400)
                .create();

        engine.onBar(Instrument.EURUSD, Period.FIVE_MINS, askBar2, bidBar2);
        Assert.assertTrue(engine.getOrders().size() == 2);

        engine.onBar(Instrument.EURUSD, Period.TEN_SECS, askBar2, bidBar2);
        Assert.assertTrue(engine.getOrders().get(0).getStopLossPrice() == 1.0656);
        Assert.assertTrue(engine.getOrders().get(1).getStopLossPrice() == 1.0656);




        IOrder sellOrder = engine.submitOrder("label1", Instrument.EURUSD, IEngine.OrderCommand.SELL, 0.02, 1.06000, 0, 1.06300, 0);

        BarTestHelper askBar3 = barBuilder
                .setClose(1.05420)
                .setOpen(1.05020)
                .setHigh(1.05520)
                .setLow(1.04920)
                .create();
        BarTestHelper bidBar3 = barBuilder
                .setClose(1.05400)
                .setOpen(1.05000)
                .setHigh(1.05500)
                .setLow(1.04900)
                .create();

        engine.onBar(Instrument.EURUSD, Period.TEN_SECS, askBar3, bidBar3);
        Assert.assertTrue(sellOrder.getStopLossPrice() == 1.06000);

        engine.onBar(Instrument.EURUSD, Period.TEN_SECS, askBar3, bidBar3);
        Assert.assertTrue(sellOrder.getAmount() == 0.01);


        BarTestHelper askBar4 = barBuilder
                .setClose(1.04920)
                .setOpen(1.04520)
                .setHigh(1.04920)
                .setLow(1.04420)
                .create();
        BarTestHelper bidBar4 = barBuilder
                .setClose(1.04900)
                .setOpen(1.04500)
                .setHigh(1.04900)
                .setLow(1.04400)
                .create();


        engine.onBar(Instrument.EURUSD, Period.FIVE_MINS, askBar4, bidBar4);
        Assert.assertTrue(engine.getOrders().size() == 4);

        engine.onBar(Instrument.EURUSD, Period.TEN_SECS, askBar4, bidBar4);
//        assertTrue(engine.getOrders().get(0).getStopLossPrice() == 1.0656);
//        assertTrue(engine.getOrders().get(1).getStopLossPrice() == 1.0656);
    }


}
