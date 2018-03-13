package ch.imetrica.mdfatrader.examples;

import org.joda.time.format.DateTimeFormat;

import ch.imetrica.mdfatrader.customization.Customization;
import ch.imetrica.mdfatrader.customization.SmoothingWeight;
import ch.imetrica.mdfatrader.datafeeds.CsvFeed;
import ch.imetrica.mdfatrader.regularization.Regularization;
import ch.imetrica.mdfatrader.series.MultivariateSeries;
import ch.imetrica.mdfatrader.series.SignalSeries;
import ch.imetrica.mdfatrader.series.TargetSeries;
import ch.imetrica.mdfatrader.series.TimeSeriesEntry;
import ch.imetrica.mdfatrader.targetfilter.TargetFilter;
import ch.imetrica.mdfatrading.mdfa.MDFABase;
import ch.imetrica.mdfatrading.mdfa.MDFASolver;

public class ExampleSignalSeries {
	
	
	
	private static final int MAX_OBS = 400;

	public static void main(String[] args) throws Exception {

		
		
		
		int nobs 				= 300;
		int nseries 			= 1;
		int f_length			= 20;
		int i1					= 0;
		int i2					= 0;
		double lag				= 0.0;		
		double cutoff			= Math.PI/6;	
		double alpha			= 1.0;	
		double lambda			= 1.0;
		double smooth			= 0.01;		
		double decayStrength	= 0.1;
		double decayStart		= 0.1;
		double crossCorr		= 0.9;
		double shift_const		= 1.0;
		
		MDFABase anyMDFA = new MDFABase(nobs, 
		        nseries, 
		        f_length, 
		        i1,
		        i2,
		        lag, 
		        cutoff,
		        alpha,
		        lambda,
		        smooth,
		        decayStrength,
		        decayStart,
		        crossCorr,
		        shift_const);
		
		
		SmoothingWeight myWeight = new SmoothingWeight(anyMDFA);
		TargetFilter myTarget = new TargetFilter(anyMDFA);	
		Regularization anyReg = new Regularization(anyMDFA);
		Customization anyCustom = new Customization(anyMDFA, myWeight, myTarget);	
		MDFASolver mySolver = new MDFASolver(anyCustom, anyReg);
		
		
		/* Create market data feed */
		CsvFeed marketDataFeed = new CsvFeed("data/AAPL.IB.dat", "dateTime", "close");
		
		/* Create empty target series */
		SignalSeries aaplSignal = new SignalSeries(new TargetSeries(0.1, true, "AAPL"), "AAPL");	
		aaplSignal.setDateFormat(DateTimeFormat.forPattern("yyyy-MM-dd"));

		MultivariateSeries multiSeries = new MultivariateSeries(anyMDFA, mySolver);
		multiSeries.addSeries(aaplSignal);
		
		for(int i = 0; i < MAX_OBS; i++) {
			
			TimeSeriesEntry<Double> observation = marketDataFeed.getNextObservation();
			multiSeries.addValue(new double[]{observation.getValue()}, observation.getDateTime());
			
		}
		
		multiSeries.computeFilterCoefficients();
		multiSeries.plotSignals();
		
        for(int i = 0; i < 300; i++) {
			
			TimeSeriesEntry<Double> observation = marketDataFeed.getNextObservation();
			multiSeries.addValue(new double[]{observation.getValue()}, observation.getDateTime());
			
		}
        
        //multiSeries.plotSignals();
		


		
		
	
	}
	
}
