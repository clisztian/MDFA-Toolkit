package ch.imetrica.mdfa.market;

import ch.imetrica.mdfa.series.TimeSeries;
import ch.imetrica.mdfa.series.TimeSeriesEntry;

public class BeneficialOwner {

	enum Position {NEUTRAL, SHORT, LONG};
	
	public double trans_price;
	private double current_position;
	private double last_realized;
	private int current_volume;
	private int shares_per_lot;
	
	private TimeSeries<Double> unrealized;
	private TimeSeries<Double> realized;
	private TimeSeries<double[]> trades;
	
	private Position myPosition;
	
	private double current_signal;
	private double prev_signal;
	
	public BeneficialOwner() {
		
		setCurrent_volume(0);
		last_realized = 0;
		current_signal = 0;
		prev_signal = 0;
		shares_per_lot = 1;
		setMyPosition(Position.NEUTRAL);
		
		
		unrealized = new TimeSeries<Double>();
		realized = new TimeSeries<Double>();
		trades = new TimeSeries<double[]>();
	}

	/**
	 * 
	 * @param timestamp
	 * @param price
	 * @param close
	 * @param signal
	 */
	public void addEvent(String timestamp, double price, double close, double signal) {
		
		double myUnrealized = 0;
		current_signal = signal;
		
		boolean signalChange = false;
		if(current_signal > prev_signal || current_signal < prev_signal) {
			signalChange = true;
		}
				
		double[] myTrade = new double[2];
				
		
		if(current_signal == 0) { //liquidate
			
			if(myPosition == Position.LONG) {		
				last_realized += shares_per_lot*price + current_position;	
				myTrade = new double[] {current_volume, price};
			}
			else if(myPosition == Position.SHORT) {
				last_realized += current_position - shares_per_lot*price; 
				myTrade = new double[] {current_volume, price};
			}
			myPosition = Position.NEUTRAL;	
			current_position = 0;
			current_volume = 0;		
		}	
		else if(signalChange) {
			
			if(myPosition == Position.NEUTRAL) {
				
				if(signal > 0) {					
					
					myPosition = Position.LONG;
					current_position = -shares_per_lot*price;	
					current_volume = -shares_per_lot;
				}
				else if(signal < 0) {
				
					myPosition = Position.SHORT;
					current_position = shares_per_lot*price;	
					current_volume = shares_per_lot;
				}	
			}
			else if(myPosition == Position.LONG) {
				
				myPosition = Position.SHORT;				
				last_realized += shares_per_lot*price + current_position;
				current_position = shares_per_lot*price;
				current_volume = shares_per_lot;
			
			}
			else if(myPosition == Position.SHORT) {
				
				myPosition = Position.LONG;			
				last_realized += current_position - shares_per_lot*price; 
				current_position = -shares_per_lot*price;
				current_volume = -shares_per_lot;
			
			}
			myTrade = new double[] {current_volume, price};
		}
		
		
		if(myPosition == Position.LONG) {
			//System.out.println(current_volume + " " + close + " " + current_position);
			myUnrealized = -current_volume*close + current_position ;
		}
		else if(myPosition == Position.SHORT) {
			//System.out.println(current_volume + " " + close + " " + current_position);
			myUnrealized = current_position - current_volume*close;
		}
	

		unrealized.add(new TimeSeriesEntry<Double>(timestamp, myUnrealized));		
		realized.add(new TimeSeriesEntry<Double>(timestamp, last_realized));		
		trades.add(new TimeSeriesEntry<double[]>(timestamp, myTrade)); 
		
		prev_signal = current_signal;
	
	}
	
	
	
	public Position getMyPosition() {
		return myPosition;
	}

	public void setMyPosition(Position myPosition) {
		this.myPosition = myPosition;
	}


	public int getCurrent_volume() {
		return current_volume;
	}

	

	public void setCurrent_volume(int current_volume) {
		this.current_volume = current_volume;
	}
	
	public TimeSeries<Double> getRealized() {
		return realized;
	}
	
	public TimeSeries<double[]> getTrades() {
		return trades;
	}
	
	public TimeSeries<Double> getUnrealized() {
		return unrealized;
	}
	
	public void setSharesPerLot(int shares) {
		shares_per_lot = shares;
	}
	
}

