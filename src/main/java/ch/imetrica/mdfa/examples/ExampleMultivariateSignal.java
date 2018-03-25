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

public class ExampleMultivariateSignal {

	
	private static int MAX_OBS = 400;

	public static void main(String[] args) throws Exception {
		
		
		int nobs 				= 300;
		int nseries 			= 3;
		int f_length			= 50;
		int i1					= 1;
		int i2					= 0;
		double lag				= -2.0;		
		double cutoff			= .33;	
		double alpha			= 10.0;	
		double lambda			= 1.0;
		double smooth			= 0.10;		
		double decayStrength	= 0.01;
		double decayStart		= 0.01;
		double crossCorr		= 0.1;
		double shift_const		= -1.0;
		
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
		
		
		MDFASolver mySolver = new MDFASolver(anyMDFA);
		
		
		String[] dataFiles = new String[3];
		dataFiles[0] = "data/AAPL.IB.dat";
		dataFiles[1] = "data/QQQ.IB.dat";
		dataFiles[2] = "data/SPY.IB.dat";
		
		CsvFeed marketFeed = new CsvFeed(dataFiles, "dateTime", "close");
		
		/* Create empty target series */
		SignalSeries aaplSignal = new SignalSeries(new TargetSeries(0.9, true, "AAPL"), "yyyy-MM-dd");	
		SignalSeries qqqSignal = new SignalSeries(new TargetSeries(0.9, true, "QQQ"), "yyyy-MM-dd");
		SignalSeries spySignal = new SignalSeries(new TargetSeries(0.9, true, "SPY"), "yyyy-MM-dd");


		
		MultivariateSeries multiSeries = new MultivariateSeries(mySolver);
		multiSeries.setDateFormat("yyyy-MM-dd");
		
		
		
		multiSeries.addSeries(aaplSignal);
		multiSeries.addSeries(qqqSignal);
		multiSeries.addSeries(spySignal);
		
        for(int i = 0; i < MAX_OBS ; i++) {
			
			TimeSeriesEntry<double[]> observation = marketFeed.getNextMultivariateObservation();
			multiSeries.addValue(observation.getValue(), observation.getDateTime());
			
		}
		
		multiSeries.computeFilterCoefficients();
		
		for(int i = 0; i < 200; i++) {
			
			TimeSeriesEntry<double[]> observation = marketFeed.getNextMultivariateObservation();
			multiSeries.addValue(observation.getValue(), observation.getDateTime());
			
		}
		
		multiSeries.chopFirstObservations(100);
		multiSeries.plotAggregateSignal("After data chop");
		
		multiSeries.getMDFAFactory().setI2(1);
		multiSeries.getMDFAFactory().setShift_constraint(-1.0);
				
		multiSeries.computeFilterCoefficients();
		multiSeries.plotAggregateSignal("With i2 and phase shift");
	}
	
	
}
