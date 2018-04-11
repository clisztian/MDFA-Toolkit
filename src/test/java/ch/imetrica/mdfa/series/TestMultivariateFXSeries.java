package ch.imetrica.mdfa.series;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.imetrica.mdfa.datafeeds.CsvFeed;
import ch.imetrica.mdfa.mdfa.MDFABase;

public class TestMultivariateFXSeries {

	@Test
	public void testAddSeries() throws Exception {
		
		String[] dataFiles = new String[3];
		dataFiles[0] = "/home/lisztian/mdfaData/AAPL.daily.csv";
		dataFiles[1] = "/home/lisztian/mdfaData/QQQ.daily.csv";
		dataFiles[2] = "/home/lisztian/mdfaData/SPY.daily.csv";
		
		CsvFeed marketFeed = new CsvFeed(dataFiles, "Index", "Open");
		

		/* Create some MDFA sigEx processes */
		MDFABase[] anyMDFAs = new MDFABase[3];
		
		anyMDFAs[0] = (new MDFABase()).setLowpassCutoff(Math.PI/20.0)
				.setI1(1)
				.setHybridForecast(.01)
				.setSmooth(.3)
				.setDecayStart(.1)
				.setDecayStrength(.2)
				.setLag(-2.0)
				.setLambda(2.0)
				.setAlpha(2.0)
				.setSeriesLength(400);
		
		anyMDFAs[1] = (new MDFABase()).setLowpassCutoff(Math.PI/10.0)
				.setBandPassCutoff(Math.PI/15.0)
				.setSmooth(.1)
				.setSeriesLength(400);
		
		anyMDFAs[2] = (new MDFABase()).setLowpassCutoff(Math.PI/5.0)
                .setBandPassCutoff(Math.PI/10.0)
                .setSmooth(.1)
                .setSeriesLength(400);
		
		
		MultivariateFXSeries fxSeries = new MultivariateFXSeries(anyMDFAs, "yyyy-MM-dd");	
		fxSeries.addSeries(new TargetSeries(0.6, true, "AAPL"));
		fxSeries.addSeries(new TargetSeries(0.6, true, "QQQ"));
		TargetSeries spy = new TargetSeries(0.6, true, "SPY");
		
        for(int i = 0; i < 600; i++) {
			
			TimeSeriesEntry<double[]> observation = marketFeed.getNextMultivariateObservation();
			double[] obs = observation.getValue();
			double[] realObs = new double[2];
			realObs[0] = obs[0]; realObs[1] = obs[1];
			
			spy.addValue(observation.getDateTime(), obs[2]);
			fxSeries.addValue(observation.getDateTime(), realObs);
		}
        
        fxSeries.computeAllFilterCoefficients();	
        fxSeries.addSeries(spy);
        
        for(int i = 0; i < 600; i++) {
			
			TimeSeriesEntry<double[]> observation = marketFeed.getNextMultivariateObservation();
			fxSeries.addValue(observation.getDateTime(), observation.getValue());
		}
        
        assertEquals(3, fxSeries.getNumberSeries());
//        
//        fxSeries.chopFirstObservations(70);	
//	
//		
//        for(int i = 0; i < 600; i++) {
//			
//			TimeSeriesEntry<double[]> observation = marketFeed.getNextMultivariateObservation();
//			fxSeries.addValue(observation.getDateTime(), observation.getValue());
//		}
//        fxSeries.chopFirstObservations(400);	
//
//        
//        
//        fxSeries.getMDFAFactory(0).setLowpassCutoff(Math.PI/6.0);
//        fxSeries.getMDFAFactory(0).setLag(-3.0);
//        fxSeries.computeFilterCoefficients(0);
   
		
		
		
		
		
	}

}
