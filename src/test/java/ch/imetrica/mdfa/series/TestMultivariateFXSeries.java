package ch.imetrica.mdfa.series;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.imetrica.mdfa.datafeeds.CsvFeed;
import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.mdfa.MDFAFactory;
import ch.imetrica.mdfa.mdfa.MDFASolver;

public class TestMultivariateFXSeries {

	static double eps = .000001;

	@Test
	public void testAddSeriesSignal() throws Exception {
		
		String[] dataFiles = new String[3];
		dataFiles[0] = "data/AAPL.daily.csv";
		dataFiles[1] = "data/QQQ.daily.csv";
		dataFiles[2] = "data/SPY.daily.csv";
		
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
        
        fxSeries.chopFirstObservations(70);	
        fxSeries.computeAllFilterCoefficients();
	
		
        for(int i = 0; i < 600; i++) {
			
			TimeSeriesEntry<double[]> observation = marketFeed.getNextMultivariateObservation();
			fxSeries.addValue(observation.getDateTime(), observation.getValue());
		}
        fxSeries.chopFirstObservations(400);	

        fxSeries.getMDFAFactory(0).setLowpassCutoff(Math.PI/6.0);
        fxSeries.getMDFAFactory(0).setLag(-3.0);
        fxSeries.computeFilterCoefficients(0);
        fxSeries.computeAggregateSignal();

        MDFABase newbase = (new MDFABase()).setLowpassCutoff(Math.PI/8.0)
        .setBandPassCutoff(Math.PI/10.0)
        .setSmooth(.2)
        .setSeriesLength(400);
		
		fxSeries.addMDFABase(newbase);
		assertEquals(4, fxSeries.getNumberSignals());
		
        for(int i = 0; i < 200; i++) {
			
			TimeSeriesEntry<double[]> observation = marketFeed.getNextMultivariateObservation();
			fxSeries.addValue(observation.getDateTime(), observation.getValue());
		}
        
        TimeSeriesEntry<double[]> latest = fxSeries.getLatestSignalEntry();
		
        assertEquals(4, latest.getValue().length);
        assertEquals("2017-12-11", latest.getDateTime());
        assertTrue(latest.getValue()[0] != 0);
        assertTrue(latest.getValue()[1] != 0);
        assertTrue(latest.getValue()[2] != 0);
        assertTrue(latest.getValue()[3] != 0);
        
	}

	@Test
	public void testSignalVerificationMultipleSignal() throws Exception {
		
		
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
		
		
		TimeSeries<Double> appleSeries = CsvFeed.getChunkOfData(0, 600, "data/AAPL.daily.csv", "Index", "Open");	
		MultivariateSignalSeries signal = new MultivariateSignalSeries(new TargetSeries(appleSeries, .6, true), 
				anyMDFAs, "yyyy-MM-dd")
				.computeFilterCoefficients()
				.computeSignalsFromTarget();
		
		signal.chopFirstObservations(70);
		
		
		MultivariateFXSeries fxSeries = new MultivariateFXSeries(anyMDFAs, "yyyy-MM-dd");
		fxSeries.addSeries(new TargetSeries(appleSeries, .6, true));
		fxSeries.computeAllFilterCoefficients();
		fxSeries.chopFirstObservations(70);
		
		TimeSeriesEntry<double[]> sig0 = signal.getLatestSignalEntry();
		TimeSeriesEntry<double[]> sig1 = fxSeries.getLatestSignalEntry();
	
		double sigdiff = sig0.getValue()[0] - sig1.getValue()[0];
		double sigdiff1 = sig0.getValue()[1] - sig1.getValue()[1];
		double sigdiff2 = sig0.getValue()[2] - sig1.getValue()[2];
		
		assertEquals(0.0, sigdiff, eps );
		assertEquals(0.0, sigdiff1, eps);
		assertEquals(0.0, sigdiff2, eps);

	}
	
	@Test
	public void testMultipleSeriesUnisignal() throws Exception {
		
		
		int nobs 				= 100;
		int nseries 			= 3;
		int f_length			= 20;
		int i1					= 1;
		int i2					= 1;
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
		
		TimeSeries<Double> appleSeries = CsvFeed.getChunkOfData(0, 200, "data/AAPL.daily.csv", "Index", "Close");	
		TimeSeries<Double> qqqSeries = CsvFeed.getChunkOfData(0, 200, "data/QQQ.daily.csv", "Index", "Close");	
		TimeSeries<Double> spySeries = CsvFeed.getChunkOfData(0, 200, "data/SPY.daily.csv", "Index", "Close");	
		
		SignalSeries apple = new SignalSeries(new TargetSeries(appleSeries, .4, true), "yyyy-MM-dd");
		SignalSeries qqq = new SignalSeries(new TargetSeries(qqqSeries, .4, true), "yyyy-MM-dd");
		SignalSeries spy = new SignalSeries(new TargetSeries(spySeries, .4, true), "yyyy-MM-dd");
		PriceSeries applePrice = new PriceSeries(appleSeries, false);	
		
		MDFAFactory anyMDFAFactory = new MDFAFactory(anyMDFA);
		MDFASolver mySolver = new MDFASolver(anyMDFAFactory);
		MultivariateSeries multi = new MultivariateSeries(mySolver);
				
		multi.addSeries(applePrice);
		multi.addSeries(apple);
		multi.addSeries(qqq);
		multi.addSeries(spy);
		
		multi.computeFilterCoefficients();
		assertEquals(1.0, multi.sumAllCoefficients(), .0000001);
		
		
		MDFABase[] anyMDFAs = new MDFABase[1];
		anyMDFAs[0] = anyMDFA;
		
		MultivariateFXSeries fxSeries = new MultivariateFXSeries(anyMDFAs, "yyyy-MM-dd")
											.addSeries(new TargetSeries(appleSeries, .4, true))
											.addSeries(new TargetSeries(qqqSeries, .4, true))
											.addSeries(new TargetSeries(spySeries, .4, true));
		
		fxSeries.computeFilterCoefficients(0);
		fxSeries.computeAggregateSignal();

		for(int i = 0; i < multi.getSignalSize(); i++) {
			
			TimeSeriesEntry<Double> sig0 = multi.getAggregateSignal(i);
			double[] sig1 = fxSeries.getSignalValue(i);
			double sigdiff = sig0.getValue() - sig1[0];
			assertEquals(0.0, sigdiff, eps );
			System.out.println(sig0.getDateTime() + " " + sig0.getValue() + " " + sig1[0]);
		}
		

		
	}
	
}
