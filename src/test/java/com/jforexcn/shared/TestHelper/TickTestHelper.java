package com.jforexcn.shared.TestHelper;

import com.dukascopy.api.ITick;

/**
 * Created by simple on 12/12/16.
 */

public class TickTestHelper implements ITick {
    private double ask;
    private double bid;
    private long time;

    public static class TickBuilder {
        private double ask;
        private double bid;
        private long time;

        public TickBuilder setAsk(double ask) {
            this.ask = ask;
            return this;
        }

        public TickBuilder setBid(double bid) {
            this.bid = bid;
            return this;
        }

        public TickBuilder setTime(long time) {
            this.time = time;
            return this;
        }

        public ITick create() {
            return new TickTestHelper(
                    this.ask,
                    this.bid,
                    this.time
            );
        }
    }

    public TickTestHelper(double ask, double bid, long time) {
        this.ask = ask;
        this.bid = bid;
        this.time = time;
    }

    @Override
    public double getAsk() {
        return ask;
    }

    @Override
    public double getBid() {
        return bid;
    }

    @Override
    public double getAskVolume() {
        return 0;
    }

    @Override
    public double getBidVolume() {
        return 0;
    }

    @Override
    public double[] getAsks() {
        return new double[0];
    }

    @Override
    public double[] getBids() {
        return new double[0];
    }

    @Override
    public double[] getAskVolumes() {
        return new double[0];
    }

    @Override
    public double[] getBidVolumes() {
        return new double[0];
    }

    @Override
    public double getTotalAskVolume() {
        return 0;
    }

    @Override
    public double getTotalBidVolume() {
        return 0;
    }

    @Override
    public long getTime() {
        return time;
    }
}
