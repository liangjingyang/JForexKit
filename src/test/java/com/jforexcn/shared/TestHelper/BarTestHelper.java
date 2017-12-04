package com.jforexcn.shared.TestHelper;

import com.dukascopy.api.IBar;

/**
 * Created by simple on 12/12/16.
 */

public class BarTestHelper implements IBar {

    private double open = 0;
    private double close = 0;
    private double low = 0;
    private double high = 0;
    private double volume = 0;
    private long time = 0;

    public static class BarBuilder {
        private double open = 0;
        private double close = 0;
        private double low = 0;
        private double high = 0;
        private double volume = 0;
        private long time = 0;

        public BarBuilder setOpen(double open) {
            this.open = open;
            return this;
        }

        public BarBuilder setClose(double close) {
            this.close = close;
            return this;
        }

        public BarBuilder setLow(double low) {
            this.low = low;
            return this;
        }

        public BarBuilder setHigh(double high) {
            this.high = high;
            return this;
        }

        public BarBuilder setVolume(double volume) {
            this.volume = volume;
            return this;
        }

        public BarBuilder setTime(long time) {
            this.time = time;
            return this;
        }

        public BarTestHelper create() {
            return new BarTestHelper(
                    this.open,
                    this.close,
                    this.low,
                    this.high,
                    this.volume,
                    this.time
            );
        }
    }

    public BarTestHelper(
            double open,
            double close,
            double low,
            double high,
            double volume,
            long time) {
        this.open = open;
        this.close = close;
        this.low = low;
        this.high = high;
        this.volume = volume;
        this.time = time;
    }




    @Override
    public double getOpen() {
        return open;
    }

    @Override
    public double getClose() {
        return close;
    }

    @Override
    public double getLow() {
        return low;
    }

    @Override
    public double getHigh() {
        return high;
    }

    @Override
    public double getVolume() {
        return volume;
    }

    @Override
    public long getTime() {
        return time;
    }
}
