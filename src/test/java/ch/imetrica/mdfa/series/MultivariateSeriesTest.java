package ch.imetrica.mdfa.series;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import ch.imetrica.mdfa.customization.Customization;
import ch.imetrica.mdfa.customization.SmoothingWeight;
import ch.imetrica.mdfa.datafeeds.CsvFeed;
import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.mdfa.MDFASolver;
import ch.imetrica.mdfa.regularization.Regularization;
import ch.imetrica.mdfa.series.MultivariateSeries;
import ch.imetrica.mdfa.series.PriceSeries;
import ch.imetrica.mdfa.series.SignalSeries;
import ch.imetrica.mdfa.series.TargetSeries;
import ch.imetrica.mdfa.series.TimeSeries;
import ch.imetrica.mdfa.targetfilter.TargetFilter;

public class MultivariateSeriesTest {

	@Test
	public void testAddingSignalSeries() throws Exception {
		

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
		
		
		
		TimeSeries<Double> appleSeries = CsvFeed.getChunkOfData(0, 200, "data/AAPL.IB.dat", "dateTime", "close");	
		TimeSeries<Double> qqqSeries = CsvFeed.getChunkOfData(0, 200, "data/QQQ.IB.dat", "dateTime", "close");	
		TimeSeries<Double> spySeries = CsvFeed.getChunkOfData(0, 200, "data/SPY.IB.dat", "dateTime", "close");	
		
		SignalSeries apple = new SignalSeries(new TargetSeries(appleSeries, .4, true), "AAPL");
		SignalSeries qqq = new SignalSeries(new TargetSeries(qqqSeries, .4, true), "QQQ");
		SignalSeries spy = new SignalSeries(new TargetSeries(spySeries, .4, true), "SPY");
		PriceSeries applePrice = new PriceSeries(appleSeries, false);
		
		
		
		SmoothingWeight myWeight = new SmoothingWeight(anyMDFA);
		TargetFilter myTarget = new TargetFilter(anyMDFA);	
		Regularization anyReg = new Regularization(anyMDFA);
		Customization anyCustom = new Customization(anyMDFA, myWeight, myTarget);	
		MDFASolver mySolver = new MDFASolver(anyCustom, anyReg);
		
		
		MultivariateSeries multi = new MultivariateSeries(anyMDFA, mySolver);
		
		
		multi.addSeries(applePrice);
		multi.addSeries(apple);
		multi.addSeries(qqq);
		multi.addSeries(spy);
		
		assertEquals(3, multi.getNumberSignal());

		TimeSeries<Double> eemSeries = CsvFeed.getChunkOfData(0, 205, "data/EEM.IB.dat", "dateTime", "close");	
		SignalSeries eem = new SignalSeries(new TargetSeries(eemSeries, .4, true), "GOOG");
		
		
		assertFalse(multi.addSeries(eem));
		
		multi.computeFilterCoefficients();
		assertEquals(1.0, multi.sumAllCoefficients(), .0000001);
	}
	
	
	@Test
	public void testCrossRegularizaton() throws Exception {
		

		int nobs 				= 151;
		int nseries 			= 3;
		int f_length			= 40;
		int i1					= 0;
		int i2					= 0;
		double lag				= -1.0;		
		double cutoff			= Math.PI/6;	
		double alpha			= 1.0;	
		double lambda			= 1.0;
		double smooth			= 0.01;		
		double decayStrength	= 0.1;
		double decayStart		= 0.1;
		double crossCorr		= 0.99;
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
		
		
		
		TimeSeries<Double> appleSeries = CsvFeed.getChunkOfData(0, 200, "data/AAPL.IB.dat", "dateTime", "close");	
		TimeSeries<Double> qqqSeries = CsvFeed.getChunkOfData(0, 200, "data/QQQ.IB.dat", "dateTime", "close");	
		TimeSeries<Double> spySeries = CsvFeed.getChunkOfData(0, 200, "data/SPY.IB.dat", "dateTime", "close");	
		
		SignalSeries apple = new SignalSeries(new TargetSeries(appleSeries, .4, true), "AAPL");
		SignalSeries qqq = new SignalSeries(new TargetSeries(qqqSeries, .4, true), "QQQ");
		SignalSeries spy = new SignalSeries(new TargetSeries(spySeries, .4, true), "SPY");
		PriceSeries applePrice = new PriceSeries(appleSeries, false);
		
		
		
		SmoothingWeight myWeight = new SmoothingWeight(anyMDFA);
		TargetFilter myTarget = new TargetFilter(anyMDFA);	
		Regularization anyReg = new Regularization(anyMDFA);
		Customization anyCustom = new Customization(anyMDFA, myWeight, myTarget);	
		MDFASolver mySolver = new MDFASolver(anyCustom, anyReg);
		
		
		MultivariateSeries multi = new MultivariateSeries(anyMDFA, mySolver);
		
		
		multi.addSeries(applePrice);
		multi.addSeries(apple);
		multi.addSeries(qqq);
		multi.addSeries(spy);
				
		multi.computeFilterCoefficients();
		
		ArrayList<double[]> listcoeffs = multi.getMDFACoeffs();
		
		double[] aggregate = listcoeffs.get(0);
		double[] setone = listcoeffs.get(1);
		double[] settwo = listcoeffs.get(2);
		
		double[] testone = new double[aggregate.length];
		double[] testtwo = new double[aggregate.length];
		for(int j = 0; j < aggregate.length; j++) {
			testone[j] = aggregate[j] - setone[j];
		}
		
		for(int j = 0; j < aggregate.length; j++) {
			testtwo[j] = aggregate[j] - settwo[j];
		}
		
		double[] zeros = new double[aggregate.length];
		assertArrayEquals(zeros, testone, .00001);
		assertArrayEquals(zeros, testtwo, .00001);
	}
	
	
	
}
