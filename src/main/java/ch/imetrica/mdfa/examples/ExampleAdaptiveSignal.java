package ch.imetrica.mdfa.examples;

import org.joda.time.format.DateTimeFormat;

import ch.imetrica.mdfa.datafeeds.CsvFeed;
import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.mdfa.MDFAFactory;
import ch.imetrica.mdfa.mdfa.MDFASolver;
import ch.imetrica.mdfa.series.MultivariateSeries;
import ch.imetrica.mdfa.series.SignalSeries;
import ch.imetrica.mdfa.series.TargetSeries;
import ch.imetrica.mdfa.series.TimeSeriesEntry;

public class ExampleAdaptiveSignal {


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
			
			
			MDFAFactory anyMDFAFactory = new MDFAFactory(anyMDFA);
			MDFASolver mySolver = new MDFASolver(anyMDFAFactory);
			
			
			String[] dataFiles = new String[3];
			dataFiles[0] = "data/AAPL.IB.dat";
			dataFiles[1] = "data/QQQ.IB.dat";
			dataFiles[2] = "data/SPY.IB.dat";
			
			String dataFile = "data/EEM.IB.dat";
			
			CsvFeed marketFeed = new CsvFeed(dataFiles, "dateTime", "close");
			CsvFeed eemMarketFeed = new CsvFeed(dataFile, "dateTime", "close");
			
			/* Create empty target series */
			SignalSeries aaplSignal = new SignalSeries(new TargetSeries(0.9, true, "AAPL"), "yyyy-MM-dd");	
			SignalSeries qqqSignal = new SignalSeries(new TargetSeries(0.9, true, "QQQ"), "yyyy-MM-dd");
			SignalSeries spySignal = new SignalSeries(new TargetSeries(0.9, true, "SPY"), "yyyy-MM-dd");
			SignalSeries eemSignal = new SignalSeries(new TargetSeries(0.9, true, "EEM"), "yyyy-MM-dd");


			
			MultivariateSeries multiSeries = new MultivariateSeries(mySolver);
			multiSeries.setDateFormat("yyyy-MM-dd");

			multiSeries.addSeries(aaplSignal);
			multiSeries.addSeries(qqqSignal);
			multiSeries.addSeries(spySignal);
			
	        for(int i = 0; i < MAX_OBS ; i++) {
				
				TimeSeriesEntry<double[]> observation = marketFeed.getNextMultivariateObservation();
				multiSeries.addValue(observation.getValue(), observation.getDateTime());

				TimeSeriesEntry<Double> eemObs = eemMarketFeed.getNextObservation();
				eemSignal.addValue(eemObs.getValue(), eemObs.getDateTime());
			}
			
			multiSeries.computeFilterCoefficients();
			
			for(int i = 0; i < 200; i++) {
				
				TimeSeriesEntry<double[]> observation = marketFeed.getNextMultivariateObservation();
				multiSeries.addValue(observation.getValue(), observation.getDateTime());
				
				TimeSeriesEntry<Double> eemObs = eemMarketFeed.getNextObservation();
				eemSignal.addValue(eemObs.getValue(), eemObs.getDateTime());
				
			}
			
			multiSeries.chopFirstObservations(100); 
			eemSignal.chopFirstObservations(100);
			multiSeries.plotAggregateSignal("AAPL with QQQ and SPY");
			
			/* Change a few settings in the mdfa factory and add another signal - eem signal*/

			multiSeries.addSeries(eemSignal);
			
			multiSeries.computeFilterCoefficients();
			multiSeries.plotAggregateSignal("AAPL with QQQ, SPY, EEM and i2/shift constraint");
			
		}
		


	
	
	
}
