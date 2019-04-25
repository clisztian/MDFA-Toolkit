package ch.imetrica.mdfa.market;

import java.util.ArrayList;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.series.MultivariateFXSeries;
import ch.imetrica.mdfa.series.TargetSeries;

public class Strategy {

	private DateTimeFormatter myFormatter; 
	private MultivariateFXSeries fxSeries;
	private ArrayList<MDFABase> anyMDFAs;
	
	private String stringFormatter;
	private String targetName;
	
	/**
	 * Instantiates a collection of MDFA signals 
	 * for the strategy
	 * @param n_strategies
	 */
	public Strategy(int n_strategies) {
		
		anyMDFAs = new ArrayList<MDFABase>();
		
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
				
		fxSeries = new MultivariateFXSeries(anyMDFAs, stringFormatter);
		fxSeries.addSeries(new TargetSeries(1.0, true, targetName));
		return this;
	}
	
	public void setDateTimeFormatter(String formatter) {
		
		this.stringFormatter = formatter;
		this.myFormatter = DateTimeFormat.forPattern(formatter);
	
	}

	public int getNStrategies() {
		return anyMDFAs.size();
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
