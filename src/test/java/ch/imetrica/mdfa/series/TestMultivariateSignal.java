package ch.imetrica.mdfa.series;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.imetrica.mdfa.datafeeds.CsvFeed;
import ch.imetrica.mdfa.mdfa.MDFABase;

public class TestMultivariateSignal {

	final double eps = .0000000001;
	
	@Test
	public void testBandPassGeneration() throws Exception {
		
		MDFABase[] anyMDFAs = new MDFABase[3];
		
		anyMDFAs[0] = (new MDFABase()).setLowpassCutoff(Math.PI/15.0);
		
		anyMDFAs[1] = (new MDFABase()).setLowpassCutoff(Math.PI/10.0)
				.setBandPassCutoff(Math.PI/15.0);
		
		anyMDFAs[2] = (new MDFABase()).setLowpassCutoff(Math.PI/5.0)
                .setBandPassCutoff(Math.PI/10.0);
				
		anyMDFAs[2].setBandPassCutoff(Math.PI/5.0);
		assertEquals(Math.PI/10.0, anyMDFAs[2].getBandPassCutoff(), eps);
		
		anyMDFAs[1].setLowpassCutoff(Math.PI/16.0);
		assertEquals(Math.PI/10.0, anyMDFAs[1].getLowPassCutoff(), eps);
		
		assertEquals(0, anyMDFAs[0].getBandPassCutoff(), eps);
		assertEquals(Math.PI/15.0, anyMDFAs[1].getBandPassCutoff(), eps);
		assertEquals(Math.PI/10.0, anyMDFAs[1].getLowPassCutoff(), eps);
		
		
		TimeSeries<Double> appleSeries = CsvFeed.getChunkOfData(0, 400, "data/AAPL.IB.dat", "dateTime", "close");	
		
		MultivariateSignalSeries signal = new MultivariateSignalSeries(new TargetSeries(appleSeries, .4, true), 
				anyMDFAs, "yyyy-MM-dd")
				.computeFilterCoefficients()
				.computeSignalsFromTarget();
		
		
		double[] target = signal.getMDFAFactory(1).getTargetFilter().getTargetGamma();
		assertEquals(0.0, target[0], eps);
		
			
		int band0 = (int)((Math.PI/15.0)*150/Math.PI);
		assertEquals(0.0, target[band0-1], eps);
		assertEquals(1.0, target[band0], eps);
		
		int band1 = (int)((Math.PI/10.0)*150/Math.PI);
		assertEquals(1.0, target[band1], eps);
		assertEquals(0.0, target[band1+1], eps);
		
		signal.getMDFAFactory(1).setLowpassCutoff(Math.PI/8.0);
		signal.getMDFAFactory(1).setBandpassCutoff(Math.PI/10.0);
		target = signal.getMDFAFactory(1).getTargetFilter().getTargetGamma();
		
		assertEquals(0.0, target[band0-1], eps);
		assertEquals(0.0, target[band0], eps);
		
	}
	
	
	@Test
	public void testCoefficientGeneration() throws Exception {
		
		MDFABase[] anyMDFAs = new MDFABase[3];
		
		anyMDFAs[0] = (new MDFABase()).setLowpassCutoff(Math.PI/15.0)
				.setI1(1)
				.setI2(1)
				.setShift_constraint(-2.0);
		
		anyMDFAs[1] = (new MDFABase()).setLowpassCutoff(Math.PI/10.0)
				.setBandPassCutoff(Math.PI/15.0);
		
		anyMDFAs[2] = (new MDFABase()).setLowpassCutoff(Math.PI/5.0)
                .setBandPassCutoff(Math.PI/10.0);
		
		
		TimeSeries<Double> appleSeries = CsvFeed.getChunkOfData(0, 400, "data/AAPL.IB.dat", "dateTime", "close");	
		MultivariateSignalSeries signal = new MultivariateSignalSeries(new TargetSeries(appleSeries, .4, true), 
				anyMDFAs, "yyyy-MM-dd")
				.computeFilterCoefficients()
				.computeSignalsFromTarget();
		
		double[] coeffs = signal.getCoefficients(0);
		assertEquals(20, coeffs.length);
		
		double sum = 0; 
		for(int j = 0; j < coeffs.length; j++) {
			sum += coeffs[j];
		}
		assertEquals(1.0, sum, eps);
	
		sum = 0;
		for(int j = 0; j < coeffs.length; j++) {
			sum += coeffs[j]*j;
		}
		assertEquals(-2.0, sum, eps);		
		
		
		signal.getMDFAFactory(0).setFilterLength(40);
		signal.getMDFAFactory(0).setAlpha(1.0);
		signal.getMDFAFactory(0).setShift_constraint(-3.0);
		signal.computeFilterCoefficients().computeSignalsFromTarget();
		
		coeffs = signal.getCoefficients(0);
		assertEquals(40, coeffs.length);
		
		sum = 0.0;
		for(int j = 0; j < coeffs.length; j++) {
			sum += coeffs[j];
		}
		assertEquals(1.0, sum, eps);

		sum = 0;
		for(int j = 0; j < coeffs.length; j++) {
			sum += coeffs[j]*j;
		}
		assertEquals(-3.0, sum, eps);		

	}
	
	
	@Test
	public void testSignalOutput() throws Exception {
		
		MDFABase[] anyMDFAs = new MDFABase[3];
		
		anyMDFAs[0] = (new MDFABase()).setLowpassCutoff(Math.PI/15.0)
				.setI1(1)
				.setI2(1)
				.setShift_constraint(-2.0);
		
		anyMDFAs[1] = (new MDFABase()).setLowpassCutoff(Math.PI/10.0)
				.setBandPassCutoff(Math.PI/15.0);
		
		anyMDFAs[2] = (new MDFABase()).setLowpassCutoff(Math.PI/5.0)
                .setBandPassCutoff(Math.PI/10.0);
		
		
		TimeSeries<Double> appleSeries = CsvFeed.getChunkOfData(0, 400, "data/AAPL.IB.dat", "dateTime", "close");	
		MultivariateSignalSeries signal = new MultivariateSignalSeries(new TargetSeries(appleSeries, .4, true), 
				anyMDFAs, "yyyy-MM-dd")
				.computeFilterCoefficients()
				.computeSignalsFromTarget();
		
		
		double[] signalOutput = signal.getLatestSignalValue();
		assertEquals(3, signalOutput.length);
		assertEquals("2014-08-04", signal.getLatestSignalEntry().getDateTime());
		assertEquals("2014-08-04", signal.getLatestTargetDate());
		assertEquals(0.33971545094165534, signalOutput[0], eps);
		
		
		signal.addValue("2014-08-05", 95.12);
		assertEquals("2014-08-05", signal.getLatestSignalEntry().getDateTime());
		assertEquals("2014-08-05", signal.getLatestTargetDate());
		
		signalOutput = signal.getLatestSignalValue();		
		assertEquals(-0.008502414331387839, signalOutput[2], eps);
		
	}
	
	

}
