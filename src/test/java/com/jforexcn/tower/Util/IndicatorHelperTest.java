package com.jforexcn.tower.Util;

import com.dukascopy.api.*;
import com.jforexcn.shared.TestHelper.*;
import com.jforexcn.shared.strategy.PositionManager;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by simple on 2018/9/9
 */

public class IndicatorHelperTest {
    @Test
    public void testGetRect() throws JFException {
        ContextTestHelper context = new ContextTestHelper();
        HistoryTestHelper history = new HistoryTestHelper();
        context.setHistory(history);
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
}
