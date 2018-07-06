package com.jforexcn.inbox.strategy;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IEngine;
import com.jforexcn.shared.TestHelper.BarTestHelper;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by simple(simple.continue@gmail.com) on 17/12/2017.
 */

public class HammerOrBreakSignalTest {
    @Test
    public void testMergedBar() {
        HammerOrBreakSignal.MergedBar mergedBar = new HammerOrBreakSignal.MergedBar();
        BarTestHelper.BarBuilder barBuilder = new BarTestHelper.BarBuilder();
        IBar bar = barBuilder.setHigh(5).setOpen(4).setLow(2).setClose(3).setTime(1).setVolume(1).create();
        IBar bar2 = barBuilder.setHigh(3).setOpen(3).setLow(0.5).setClose(2).setTime(2).setVolume(1).create();
        IBar bar3 = barBuilder.setHigh(6).setOpen(2).setLow(2).setClose(6).setTime(3).setVolume(1).create();
        mergedBar.merge(bar);
        mergedBar.merge(bar2);
        mergedBar.merge(bar3);

        Assert.assertEquals(mergedBar.getOpen(), bar.getOpen(), 0);
        Assert.assertEquals(mergedBar.getClose(), bar3.getClose(), 0);
        Assert.assertEquals(mergedBar.getHigh(), 6, 0);
        Assert.assertEquals(mergedBar.getLow(), 0.5, 0);
        Assert.assertEquals(mergedBar.getTop(), 6, 0);
        Assert.assertEquals(mergedBar.getBottom(), 2, 0);
        Assert.assertEquals(mergedBar.getTime(), bar.getTime(), 0);
        Assert.assertEquals(mergedBar.getVolume(), 3, 0);
    }

    @Test
    public void testIsHammer() {
        BarTestHelper.BarBuilder barBuilder = new BarTestHelper.BarBuilder();
        IBar bar = barBuilder.setHigh(5).setOpen(4).setLow(1.9).setClose(3).setTime(1).setVolume(1).create();
        HammerOrBreakSignal.MergedBar mergedBar = new HammerOrBreakSignal.MergedBar();
        mergedBar.merge(bar);
        HammerOrBreakSignal signal = new HammerOrBreakSignal();
        Assert.assertFalse(signal.isHammer(mergedBar, IEngine.OrderCommand.SELL));

        IBar bar2 = barBuilder.setHigh(5).setOpen(3).setLow(3).setClose(5).setTime(2).setVolume(1).create();
        mergedBar.merge(bar2);
        Assert.assertTrue(signal.isHammer(mergedBar, IEngine.OrderCommand.BUY));
        Assert.assertFalse(signal.isHammer(mergedBar, IEngine.OrderCommand.SELL));
    }

//    @Test
//    public void testIsBreak() {
//        BarTestHelper.BarBuilder barBuilder = new BarTestHelper.BarBuilder();
//        IBar bar = barBuilder.setHigh(5).setOpen(4).setLow(2).setClose(3).setTime(1).setVolume(1).create();
//        IBar bar2 = barBuilder.setHigh(4).setOpen(3).setLow(3).setClose(4).setTime(2).setVolume(1).create();
//        IBar bar3 = barBuilder.setHigh(8).setOpen(4).setLow(2).setClose(8).setTime(3).setVolume(1).create();
//        HammerOrBreakSignal.MergedBar mergedBar = new HammerOrBreakSignal.MergedBar();
//        mergedBar.merge(bar2);
//        mergedBar.merge(bar);
//        System.out.println("high: " + mergedBar.getHigh() + ", open: " + mergedBar.getOpen() + ", low: " + mergedBar.getLow() + ", close: " + mergedBar.getClose() +
//        ", top: " + mergedBar.getTop() + ", bottom: " + mergedBar.getBottom());
//        System.out.println("high: " + bar3.getHigh() + ", open: " + bar3.getOpen() + ", low: " + bar3.getLow() + ", close: " + bar3.getClose());
//        HammerOrBreakSignal signal = new HammerOrBreakSignal();
//        Assert.assertTrue(signal.isBreak(mergedBar, bar3, IEngine.OrderCommand.BUY));
//    }
}
