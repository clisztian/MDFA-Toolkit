package ch.imetrica.mdfa.examples;

import org.joda.time.format.DateTimeFormat;

import ch.imetrica.mdfa.customization.Customization;
import ch.imetrica.mdfa.customization.SmoothingWeight;
import ch.imetrica.mdfa.datafeeds.CsvFeed;
import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.mdfa.MDFAFactory;
import ch.imetrica.mdfa.mdfa.MDFASolver;
import ch.imetrica.mdfa.regularization.Regularization;
import ch.imetrica.mdfa.series.MultivariateSeries;
import ch.imetrica.mdfa.series.SignalSeries;
import ch.imetrica.mdfa.series.TargetSeries;
import ch.imetrica.mdfa.series.TimeSeriesEntry;
import ch.imetrica.mdfa.targetfilter.TargetFilter;

public class ExampleUnivariateSeries {
	
	
	
	private static final int MAX_OBS = 400;

	public static void main(String[] args) throws Exception {

		
		
		
		int nobs 				= 300;
		int nseries 			= 1;
		int f_length			= 20;
		int i1					= 0;
		int i2					= 0;
		double lag				= 0.0;		
		double cutoff			= Math.PI/5;	
		double alpha			= 10.0;	
		double lambda			= 1.0;
		double smooth			= 0.10;		
		double decayStrength	= 0.01;
		double decayStart		= 0.01;
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
		
		
		MDFAFactory anyMDFAFactory = new MDFAFactory(anyMDFA);
		MDFASolver mySolver = new MDFASolver(anyMDFAFactory);
		
		
		/* Create market data feed */
		CsvFeed marketDataFeed = new CsvFeed("data/AAPL.IB.dat", "dateTime", "close");
		
		/* Create empty target series */
		SignalSeries aaplSignal = new SignalSeries(new TargetSeries(1.0, true, "AAPL"), "AAPL");	
		aaplSignal.setDateFormat("yyyy-MM-dd");

		MultivariateSeries multiSeries = new MultivariateSeries(mySolver);
		multiSeries.setDateFormat("yyyy-MM-dd");
		
		
		
		multiSeries.addSeries(aaplSignal);
		
		for(int i = 0; i < MAX_OBS; i++) {
			
			TimeSeriesEntry<Double> observation = marketDataFeed.getNextObservation();
			multiSeries.addValue(new double[]{observation.getValue()}, observation.getDateTime());
			
		}
		
		multiSeries.computeFilterCoefficients();
		
        for(int i = 0; i < 500; i++) {
			
			TimeSeriesEntry<Double> observation = marketDataFeed.getNextObservation();
			multiSeries.addValue(new double[]{observation.getValue()}, observation.getDateTime());
			
		}
        
        multiSeries.printSignal();
        multiSeries.chopFirstObservations(50);
        multiSeries.plotAggregateSignal("AAPL");

		


		
		
	
	}
	
}
