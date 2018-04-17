package ch.imetrica.mdfa.examples;

import javax.swing.plaf.multi.MultiListUI;

import ch.imetrica.mdfa.datafeeds.CsvFeed;
import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.mdfa.MDFAFactory;
import ch.imetrica.mdfa.mdfa.MDFASolver;
import ch.imetrica.mdfa.prefilter.WhiteNoiseFilter;
import ch.imetrica.mdfa.series.MultivariateSeries;
import ch.imetrica.mdfa.series.SignalSeries;
import ch.imetrica.mdfa.series.TargetSeries;
import ch.imetrica.mdfa.series.TimeSeriesEntry;

public class ExamplePrefilterSeries {

	private static final int MAX_OBS = 400;

	public static void main(String[] args) throws Exception {

		
		
		
		int nobs 				= 300;
		int nseries 			= 1;
		int f_length			= 4;
		int i1					= 0;
		int i2					= 0;
		double lag				= -2.0;		
		double cutoff			= Math.PI/9;	
		double alpha			= 1.0;	
		double lambda			= 1.0;
		double smooth			= 0.10;		
		double decayStrength	= 0.01;
		double decayStart		= 0.01;
		double crossCorr		= 0.9;
		double shift_const		= 0.0;
		
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
		
		/* Create empty target series and add a prefilter using the White
		 * Noise library 
		 * */
		SignalSeries aaplSignal = new SignalSeries(new TargetSeries(1.0, true, "AAPL"), "yyyy-MM-dd")
				                     .setPrefilter(new WhiteNoiseFilter(cutoff, lag, 50).getFilterCoefficients());

		
		MultivariateSeries multiSeries = new MultivariateSeries(mySolver).setDateFormat("yyyy-MM-dd");
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
        
        multiSeries.chopFirstObservations(200);
        multiSeries.plotAggregateSignal("With prefilter");
		
        System.out.println(((SignalSeries)multiSeries.getSeries(0)).coeffsToString());
		
	}
	
}
