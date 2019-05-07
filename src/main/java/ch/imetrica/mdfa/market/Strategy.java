package ch.imetrica.mdfa.market;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.series.MultivariateFXSeries;
import ch.imetrica.mdfa.series.TargetSeries;
import lombok.Getter;

public class Strategy {

	private DecimalFormat df = new DecimalFormat("#.##");
	private DateTimeFormatter myFormatter; 
	private MultivariateFXSeries[] UnitSignals;
	private ArrayList<MDFABase> anyMDFAs;
	
	private String stringFormatter;
	private String targetName;
	

	@Getter
	private double CurrentBid;
	@Getter
	private double CurrentAsk;
	@Getter
	private double CurrentMid;
	@Getter
	private int NumberOfUnits;
	@Getter
	private double CurrentPnl;
	@Getter
	private double RealizedPnl;
	@Getter
	private String current_date;
	
	private int current_signal = 0;
	
	/**
	 * Instantiates a collection of MDFA signals 
	 * for the strategy
	 * @param n_strategies
	 */
	public Strategy() {	
		
		anyMDFAs = new ArrayList<MDFABase>();		
		stringFormatter = "yyyy-MM-dd";
	}
	
	/*
	 * Adds a signal strategy
	 */
	public Strategy addStrategy(MDFABase anyStrategy) {
		
		anyMDFAs.add(anyStrategy);
		return this;
	}
	
	/**
	 * Sets the target name of the asset being traded
	 * @param name
	 */
	public void setTargetSeriesName(String name) {
		this.targetName = name;
	}
	
	/**
	 * Builds the final strategy
	 * @return
	 * @throws Exception
	 */
	public Strategy build() throws Exception {
				
		UnitSignals = new MultivariateFXSeries[anyMDFAs.size()];
		
		NumberOfUnits = anyMDFAs.size();
		
		for(int i = 0; i < NumberOfUnits; i++) {
			
			UnitSignals[i] = new MultivariateFXSeries(new MDFABase[] {anyMDFAs.get(i)}, stringFormatter);
			UnitSignals[i].addSeries(new TargetSeries(1.0, true, targetName));
			
		}
		return this;
	}
	
	public void setDateTimeFormatter(String formatter) {
		
		this.stringFormatter = formatter;
		this.myFormatter = DateTimeFormat.forPattern(formatter);
	
	}

	public int getNStrategies() {
		return anyMDFAs.size();
	}
	
	
	
	public int add(String time, double val, double close) throws Exception {
		
		double signalPnl = 0;
		double tpPnl = 0;
		
		CurrentBid = val;
		CurrentAsk = val; 
		CurrentMid = val;
		current_date = time;
	
		
		for(int i=0; i < NumberOfUnits; i++) {	
			
			UnitSignals[i].addValue(time, new double[] {val});
			signalPnl += UnitSignals[i].getRealizedPnl();
		}

		for(int i=0; i < NumberOfUnits; i++) {	
			
		  tpPnl = tpPnl + UnitSignals[i].updatePnl(time, close);          
		
		}
	
		int total = 0;
		for(int i=0; i < NumberOfUnits; i++) {
			
			if(UnitSignals[i].getCurrentSide() == Side.LONG) {total = total + 1;}
			else if(UnitSignals[i].getCurrentSide() == Side.SHORT) {total = total - 1;}
		
		}
		
		RealizedPnl = signalPnl;		
		CurrentPnl =  tpPnl;
		current_signal = total;
		
		return total;
	}	
	
	
	
	public int computePnL(String time, double val) {
		
		double newSignalVal = 0;
	
		CurrentBid = val;
		CurrentAsk = val; 
		CurrentMid = val;
		
		newSignalVal = 0;
		for(int i=0; i < NumberOfUnits; i++) {
			
			newSignalVal = newSignalVal + UnitSignals[i].updatePnl(time, val);				
		}
		
		
		int total = 0;
		for(int i=0; i < NumberOfUnits; i++) {
			
			if(UnitSignals[i].getCurrentSide() == Side.LONG) {total = total + 1;}
			else if(UnitSignals[i].getCurrentSide() == Side.SHORT) {total = total - 1;}
		}		
		
		CurrentPnl = newSignalVal;
		return total;
	}

	public void computeCoefficients() throws Exception {
		
		for(int i=0; i < NumberOfUnits; i++) {			
			UnitSignals[i].computeAllFilterCoefficients();
		}
	}
	
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(current_date + " ");
		sb.append(current_signal + " " + df.format(CurrentPnl) + " " + df.format(RealizedPnl));
		sb.append("\n");
		
		return sb.toString();
	}
	
	
	
	
//	anyMDFAs[0] = (new MDFABase()).setLowpassCutoff(Math.PI/20.0)
//			.setI1(1)
//			.setHybridForecast(.01)
//			.setSmooth(.3)
//			.setDecayStart(.1)
//			.setDecayStrength(.2)
//			.setLag(-2.0)
//			.setLambda(2.0)
//			.setAlpha(2.0)
//			.setSeriesLength(400);
//	
//	anyMDFAs[1] = (new MDFABase()).setLowpassCutoff(Math.PI/10.0)
//			.setBandPassCutoff(Math.PI/15.0)
//			.setSmooth(.1)
//			.setSeriesLength(400);
//	
//	anyMDFAs[2] = (new MDFABase()).setLowpassCutoff(Math.PI/5.0)
//            .setBandPassCutoff(Math.PI/10.0)
//            .setSmooth(.1)
//            .setSeriesLength(400);
	
	
	
	
	
	
	
	
}
