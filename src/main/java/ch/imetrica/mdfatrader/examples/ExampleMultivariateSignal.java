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
		
		
		String[] dataFiles = new String[3];
		dataFiles[0] = "data/AAPL.IB.dat";
		dataFiles[1] = "data/QQQ.IB.dat";
		dataFiles[2] = "data/SPY.IB.dat";
		
		CsvFeed marketFeed = new CsvFeed(dataFiles, "dateTime", "close");
		
		/* Create empty target series */
		SignalSeries aaplSignal = new SignalSeries(new TargetSeries(0.9, true, "AAPL"), "AAPL");	
		SignalSeries qqqSignal = new SignalSeries(new TargetSeries(0.9, true, "QQQ"), "QQQ");
		SignalSeries spySignal = new SignalSeries(new TargetSeries(0.9, true, "SPY"), "SPY");
		aaplSignal.setDateFormat(DateTimeFormat.forPattern("yyyy-MM-dd"));
		qqqSignal.setDateFormat(DateTimeFormat.forPattern("yyyy-MM-dd"));
		spySignal.setDateFormat(DateTimeFormat.forPattern("yyyy-MM-dd"));

		
		MultivariateSeries multiSeries = new MultivariateSeries(anyMDFA, mySolver);
		multiSeries.setDateFormat(DateTimeFormat.forPattern("yyyy-MM-dd"));
		
		
		
		multiSeries.addSeries(aaplSignal);
		multiSeries.addSeries(qqqSignal);
		multiSeries.addSeries(spySignal);
		
        for(int i = 0; i < MAX_OBS ; i++) {
			
			TimeSeriesEntry<double[]> observation = marketFeed.getNextMultivariateObservation();
			multiSeries.addValue(observation.getValue(), observation.getDateTime());
			
		}
		
		multiSeries.computeFilterCoefficients();
		
		for(int i = 0; i < 500; i++) {
			
			TimeSeriesEntry<double[]> observation = marketFeed.getNextMultivariateObservation();
			multiSeries.addValue(observation.getValue(), observation.getDateTime());
			
		}
		
		multiSeries.chopFirstObservations(100);
		multiSeries.plotAggregateSignal();
		
	}
	
	
}
