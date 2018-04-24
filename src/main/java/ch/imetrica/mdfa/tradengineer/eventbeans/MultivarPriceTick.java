package ch.imetrica.mdfa.tradengineer.eventbeans;

import org.joda.time.DateTime;

public class MultivarPriceTick {
	
    private String symbol;
    private double[] bid;
    private double[] ask;
    private DateTime timestamp;
	private long longtime;
	
	public MultivarPriceTick(String stockSymbol, DateTime timestamp, double[] bid, double[] ask) {
        
		this.symbol = stockSymbol;
        this.bid = bid;
        this.ask = ask;
        this.timestamp = timestamp;
        this.longtime = timestamp.getMillis()/1000;
    }

    public String getSymbol() {
        return symbol;
    }

    public double[] getBid() {
        return bid;
    }
    
    public double[] getAsk() {
        return ask;
    }
    
    
    public long getLongtime() {
    	return longtime;
    }
    
    public DateTime getTimestamp() {
    	return timestamp;
    }

    public double[] getMid() {
    	
    	double[] mid = new double[bid.length];
    	for(int i = 0; i < mid.length; i++) {
    		mid[i] = (ask[i] + bid[i])/2.0;
    	}
    	return mid;
    }
    
    public String toString() {
        return timestamp.toString() + ", Symbol=" + symbol +
                "  bid=" + bid[0] + " ask=" + ask[0];
    }
}
