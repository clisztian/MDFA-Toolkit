package ch.imetrica.mdfatrading.mdfa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.imetrica.mdfatrader.customization.Customization;
import ch.imetrica.mdfatrader.customization.SmoothingWeight;
import ch.imetrica.mdfatrader.datafeeds.CsvFeed;
import ch.imetrica.mdfatrader.matrix.MdfaMatrix;
import ch.imetrica.mdfatrader.regularization.Regularization;
import ch.imetrica.mdfatrader.series.TargetSeries;
import ch.imetrica.mdfatrader.series.TimeSeries;
import ch.imetrica.mdfatrader.spectraldensity.SpectralBase;
import ch.imetrica.mdfatrader.targetfilter.TargetFilter;

public class TestMdfaSolver {

	@Test
	public void testMdfaSolver() throws Exception {
		
		int nobs 				= 100;
		int nseries 			= 1;
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
		SmoothingWeight myWeight = new SmoothingWeight(anyMDFA);
		TargetFilter myTarget = new TargetFilter(anyMDFA);	
		Regularization anyReg = new Regularization(anyMDFA);
		
		SpectralBase mySpectral = new SpectralBase(anyMDFA);
		mySpectral.addSeries(new TargetSeries(appleSeries, .4, true));
		
		Customization anyCustom = new Customization(anyMDFA, myWeight, myTarget, mySpectral);	
		MDFASolver mySolver = new MDFASolver(anyCustom, anyReg);
		
		MdfaMatrix b = mySolver.solver();
		
		assertEquals(1.0, b.sum(), .000001); 
		assertEquals(1.0, b.expectation(), .0000001);
		
	}
	
	
	
}
