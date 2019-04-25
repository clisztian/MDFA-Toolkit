package ch.imetrica.mdfa.market;

import ch.imetrica.mdfa.series.TimeSeries;
import ch.imetrica.mdfa.series.TimeSeriesEntry;

public class BenificialOwner {

	enum Position {NEUTRAL, SHORT, LONG};
	
	private Strategy myStrategy;
	
	private double current_position;
	private double last_realized;
	private int current_volume;
	private double current_price;
	private int shares_per_lot;
	
	private int market_price;
	
	private TimeSeries<Double> unrealized;
	private TimeSeries<Double> realized;
	private TimeSeries<double[]> trades;
	
	private Position myPosition;
	
	private double current_signal;
	private double prev_signal;
	
	public BenificialOwner() {
		
		setCurrent_volume(0);
		current_price = 0;
		last_realized = 0;
		current_signal = 0;
		prev_signal = 0;
		shares_per_lot = 1;
		market_price = 0;
		setMyPosition(Position.NEUTRAL);
		
		
		unrealized = new TimeSeries<Double>();
		realized = new TimeSeries<Double>();
		trades = new TimeSeries<double[]>();
	}

	
	public void addCrossStrategy(String timestamp, double price, double close, double signal) {
		
		double myUnrealized = 0;
		current_signal = signal;
		
		boolean signalChange = current_signal*prev_signal < 0 ? true : false; 
		

		double[] myTrade = new double[2];
		
		
		if(signalChange) {
			
			if(myPosition == Position.NEUTRAL) {
				
				if(signal > 0) {					
					
					myPosition = Position.LONG;
					current_position = -shares_per_lot*price;	
					current_volume = -shares_per_lot;
					current_price = price;
					//System.out.println("Long: " + current_position + " " + current_volume + " " + current_price);
				}
				else if(signal < 0) {
				
					myPosition = Position.SHORT;
					current_position = shares_per_lot*price;	
					current_volume = shares_per_lot;
					current_price = price;
					//System.out.println("Short: " + current_position + " " + current_volume + " " + current_price);
				}	
			}
			else if(myPosition == Position.LONG) {
				
				myPosition = Position.SHORT;				
				last_realized += shares_per_lot*price + current_position;
				current_position = shares_per_lot*price;
				current_volume = shares_per_lot;
				current_price = price;
				//System.out.println("Sell: " + current_position + " " + current_volume + " " + current_price);
			
			}
			else if(myPosition == Position.SHORT) {
				
				myPosition = Position.LONG;			
				last_realized += current_position - shares_per_lot*price; 
				current_position = -shares_per_lot*price;
				current_volume = -shares_per_lot;
				current_price = price;
				//System.out.println("Buy: " + current_position + " " + current_volume + " " + current_price);
			
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
	
	
	public void addSignalStrengthStrategy(String timestamp, double price, double close, double signal) {
		
		
		current_signal = signal;
		
		double myUnrealized = 0;
		boolean signalChange = current_signal != prev_signal  ? true : false; 
		boolean signChange = current_signal*prev_signal < 0 ? true : false; 		
		double signal_diff = current_signal - prev_signal;

		double[] myTrade = new double[2];
		
		
		if(signalChange) {				
			
			
			if(myPosition == Position.NEUTRAL) {
				
				if(signal_diff > 0) {					
					
					myPosition = Position.LONG;
					current_position += -shares_per_lot*price*signal_diff;	
					current_volume += -(int)shares_per_lot*signal_diff;
					current_price = price;
					//System.out.println("Long: " + current_position + " " + current_volume + " " + current_price);
				}
				else if(signal_diff < 0) {
				
					myPosition = Position.SHORT;
					current_position += -shares_per_lot*price*signal_diff;	
					current_volume += -(int)shares_per_lot*signal_diff;
					current_price = price;
					//System.out.println("Short: " + current_position + " " + current_volume + " " + current_price);
				}	
			}
			else if(myPosition == Position.LONG) {

				
				if(current_signal < prev_signal) {
					
					last_realized += -shares_per_lot*price*signal_diff + current_position;
					current_position += -shares_per_lot*price*signal_diff;
					current_volume += -(int)shares_per_lot*signal_diff;
					current_price = price;
					
					if(signChange) {
						myPosition = Position.SHORT;
						System.out.println("Changed to Short: price " + close);
					}
				}
				else {
					
					current_position += -shares_per_lot*price*signal_diff;
					current_volume += -(int)shares_per_lot*signal_diff;
					current_price = price;
				}
				
			}
			else if(myPosition == Position.SHORT) {
				
				if(current_signal > prev_signal) {
							
					last_realized += current_position - shares_per_lot*price*signal_diff;
					current_position += -shares_per_lot*price*signal_diff;
					current_volume += -shares_per_lot*signal_diff;
					current_price = price;
					
					if(signChange) {
						myPosition = Position.LONG;
					}
					
				}
				else {
					current_position += -shares_per_lot*price*signal_diff;
					current_volume += -shares_per_lot*signal_diff;
					current_price = price;
				}

				//System.out.println("Buy: " + current_position + " " + current_volume + " " + current_price);
			
			}
			myTrade = new double[] {current_volume, price};
		}
		
		if(myPosition == Position.LONG) {
			//System.out.println(current_volume + " " + close + " " + current_position);
			myUnrealized = -current_volume*close + current_position ;
		}
		else if(myPosition == Position.SHORT) {
			System.out.println(current_volume + " " + close + " " + current_position);
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
	
	public TimeSeries<Double> getUnrealized() {
		return unrealized;
	}
	
	public void setSharesPerLot(int shares) {
		shares_per_lot = shares;
	}
	
}

