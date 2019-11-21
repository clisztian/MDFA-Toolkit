package ch.imetrica.mdfa.market;

import java.util.ArrayList;

import ch.imetrica.mdfa.market.BeneficialOwner.Position;
import ch.imetrica.mdfa.series.TimeSeries;
import ch.imetrica.mdfa.series.TimeSeriesEntry;

public class TradingPerformance {

	private double current_signal = 0;
	private double prev_signal = 0;
	private TimeSeries<Double> unrealized;
	private TimeSeries<Double> realized;
	private TimeSeries<double[]> trades;	
	private Position myPosition;
	private BeneficialOwner[] strategies;
	private TradeReport report;	
	private int n_strategies;
	
	
	public TradingPerformance(int n_strategies) {
		
		this.n_strategies = n_strategies;
		report = new TradeReport();
		strategies = new BeneficialOwner[n_strategies];
		
		unrealized = new TimeSeries<Double>();
		realized = new TimeSeries<Double>();
		
		for(int i = 0; i < strategies.length; i++) {
			strategies[i] = new BeneficialOwner();
		}		
	}
	
	public void addEvent(String timestamp, double price, double close, int[] signal) {
		
	
		double myUnrealized = 0;
		double myRealized = 0;
		
		for(int i = 0; i < n_strategies; i++) {			
			
			strategies[i].addEvent(timestamp, price, close, (double)signal[i]);
				
			myUnrealized += strategies[i].getUnrealized().last().getValue();
			myRealized += strategies[i].getRealized().last().getValue();
			
			if(strategies[i].getTrades().last().getValue()[0] != 0) {
				report.add(timestamp, new Trade(timestamp, i, strategies[i].getTrades().last().getValue()[0], strategies[i].getTrades().last().getValue()[1]));
			}	
		}
		
		unrealized.add(new TimeSeriesEntry<Double>(timestamp, myUnrealized));		
		realized.add(new TimeSeriesEntry<Double>(timestamp, myRealized));				
	}
	
	public TimeSeries<Double> getRealized() {
		return realized;
	}
		
	public TimeSeries<Double> getUnrealized() {
		return unrealized;
	}

	
	public static void main(String[] args) {
		
		
		
		
	}
	
	
	
}
