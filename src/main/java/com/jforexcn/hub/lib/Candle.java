package com.jforexcn.hub.lib;

import com.dukascopy.api.IBar;

import java.util.List;

/**
 * Created by simple(simple.continue@gmail.com) on 2018/5/25.
 */
public class Candle implements IBar {
    
    private double open = 0;
    private double close = 0;
    private double low = 0;
    private double high = 0;
    private double volume = 0;
    private long time = 0;
    
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

    public void setOpen(double open) {
        this.open = open;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public void setTime(long time) {
        this.time = time;
    }
    
    public void mergeBar(IBar bar) {
        if (this.getTime() == 0) {
            this.setTime(bar.getTime());
            this.setVolume(bar.getVolume());
            this.setOpen(bar.getOpen());
            this.setClose(bar.getClose());
            this.setLow(bar.getLow());
            this.setHigh(bar.getHigh());
        } else {
            if (bar.getTime() < this.getTime()) {
                this.setTime(bar.getTime());
                this.setOpen(bar.getOpen());
            } else {
                this.setClose(bar.getClose());
            }
            if (bar.getHigh() > this.getHigh()) {
                this.setHigh(bar.getHigh());
            }
            if (bar.getLow() < this.getLow()) {
                this.setLow(bar.getLow());
            }
            this.setVolume(this.getVolume() + bar.getVolume());
        }
    }

    public void mergeBars(List<IBar> bars) {
        if (bars.size() <= 0) {
            return;
        }
        for (int i = 0; i < bars.size(); i++) {
            this.mergeBar(bars.get(i));
        }
    }
}
