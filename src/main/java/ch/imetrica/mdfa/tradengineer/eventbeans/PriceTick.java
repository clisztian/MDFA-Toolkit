package ch.imetrica.mdfa.tradengineer.eventbeans;

import org.joda.time.DateTime;


/**
 * 
 * A PriceTick eventbean that will be used mostly for 
 * automatically generating events based on certain time patterns
 * 
 * 
 * @author lisztian
 *
 */
public class PriceTick {
    private String symbol;
    private double bid;
    private double ask;
    private DateTime timestamp;
    private int myMinute;
    private int mySecond;
	private long longtime;

	/**
	 * Any priceTick takes a symbol (Eg. EURCHF), current bid, ask, 
	 * and a Joda DateTime stamp
	 * @param stockSymbol
	 * @param timestamp
	 * @param bid
	 * @param ask
	 */
    public PriceTick(String stockSymbol, DateTime timestamp, double bid, double ask) {
        this.symbol = stockSymbol;
        this.bid = bid;
        this.ask = ask;
        this.timestamp = timestamp;
        this.myMinute = timestamp.getMinuteOfHour();
        this.mySecond = timestamp.getSecondOfMinute();
        this.longtime = timestamp.getMillis()/1000;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getBid() {
        return bid;
    }
    
    public double getAsk() {
        return ask;
    }
    
    public int getMyMinute() {
    	return myMinute;
    }
    
    public long getLongtime() {
    	return longtime;
    }
    
    public DateTime getTimestamp() {
    	return timestamp;
    }

    public double getMid() {
    	return (ask + bid)/2.0;
    }
    
    public String toString() {
        return timestamp.toString() + ", Symbol=" + symbol +
                "  bid=" + bid + " ask=" + ask;
    }
}