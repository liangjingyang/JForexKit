package com.jforexcn.wiki.strategy;

/**
 * Created by simple(simple.continue@gmail.com) on 27/04/2018.
 */


import com.dukascopy.api.*;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.feed.util.RenkoFeedDescriptor;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * The strategy demonstrates the usage of each of the
 * strategy parameter types, including multiple ways how
 * one can go about date declaration
 *
 */
public class ConfigOptionsEnum implements IStrategy {

    @Configurable(value = "int param", stepSize = 3)
    public int intParam = 1;
    @Configurable(value = "double param", stepSize = 0.5)
    public double doubleParam = 0.5;
    @Configurable("bool param")
    public boolean boolParam = true;
    @Configurable("text param")
    public String textParam = "some text";
    @Configurable("")
    public File file = new File(".");
    @Configurable(value="current time", description="default is current time")
    public Calendar currentTime = Calendar.getInstance();
    @Configurable("")
    public Color color = new Color(100, 100, 100);
    @Configurable("instrument (enum)")
    public Instrument instrument = Instrument.EURUSD;
    @Configurable("")
    public Set<Instrument> instruments = new HashSet<Instrument>(
            Arrays.asList(new Instrument[] {Instrument.EURUSD, Instrument.AUDCAD})
    );
    @Configurable("")
    public IFeedDescriptor renkoFeedDescriptor = new RenkoFeedDescriptor(Instrument.EURUSD, PriceRange.TWO_PIPS, OfferSide.ASK);

    //date/time usage possibilities

    private static Calendar myCalendar;
    static {
        myCalendar = Calendar.getInstance();
        myCalendar.set(2012, Calendar.JULY, 17, 14, 30, 00);
    }

    @Configurable(value="particular time", description="17th july 14:30")
    public Calendar particularTime = myCalendar;

    private static Calendar calTodayAt5am;
    static {
        calTodayAt5am = Calendar.getInstance();
        calTodayAt5am.set(Calendar.HOUR_OF_DAY, 5);
        calTodayAt5am.set(Calendar.MINUTE, 0);
        calTodayAt5am.set(Calendar.SECOND, 0);
    }

    @Configurable(value="time in millis", description="default is today at 5am", datetimeAsLong=true)
    public long timeInMillis = calTodayAt5am.getTimeInMillis();

    //custom enum

    enum Mode {
        BUY,
        SELL,
        NONE
    }
    @Configurable("mode (enum param)")
    public Mode mode = Mode.BUY;

    //custom class with self-typed constants

    static class Person {

        public static final Person FOO = new Person("foo");

        public static final Person BAR = new Person("bar");

        public final String name;

        public Person(String name){
            this.name = name;
        }

        @Override
        public String toString(){
            return name;
        }
    }

    @Configurable("")
    public Person person;

    @Configurable("")
    public List<Person> persons;


    @Override
    public void onStart(IContext context) throws JFException {}
    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {}
    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {}
    @Override
    public void onMessage(IMessage message) throws JFException {}
    @Override
    public void onAccount(IAccount account) throws JFException {}
    @Override
    public void onStop() throws JFException {}

}

