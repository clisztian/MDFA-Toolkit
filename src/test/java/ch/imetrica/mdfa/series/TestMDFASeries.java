package ch.imetrica.mdfa.series;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;

import org.apache.commons.math3.complex.Complex;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import ch.imetrica.mdfa.datafeeds.CsvFeed;
import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.mdfa.MDFAFactory;
import ch.imetrica.mdfa.mdfa.MDFASolver;
import ch.imetrica.mdfa.series.SignalSeries;
import ch.imetrica.mdfa.series.TargetSeries;
import ch.imetrica.mdfa.series.TimeSeries;
import ch.imetrica.mdfa.series.TimeSeriesEntry;

public class TestMDFASeries {

	final double eps = .000001;
	
	
	@Test
	public void testTargetSeries() {
		

		TimeSeries<Double> price = new TimeSeries<Double>();
		price.add(new TimeSeriesEntry<Double>("03.03.2018", 0.0));
		price.add(new TimeSeriesEntry<Double>("04.03.2018", 1.0));
		price.add(new TimeSeriesEntry<Double>("05.03.2018", 2.0));
		price.add(new TimeSeriesEntry<Double>("06.03.2018", 3.0));
		price.add(new TimeSeriesEntry<Double>("07.03.2018", 4.0));
		price.add(new TimeSeriesEntry<Double>("08.03.2018", 5.0));
		price.add(new TimeSeriesEntry<Double>("09.03.2018", 6.0));
		price.add(new TimeSeriesEntry<Double>("10.03.2018", 7.0));
		price.add(new TimeSeriesEntry<Double>("11.03.2018", 8.0));
		price.add(new TimeSeriesEntry<Double>("12.03.2018", 9.0));
		
		
		TargetSeries target = new TargetSeries(price, 1.0, false);
		target.addValue(10.0, "13.03.2018");
		target.addValue(11.0, "14.03.2018");
		
				
		assertEquals(1.0, target.getTargetValue(target.size()-1), eps);
		assertEquals(1.0, target.getTargetValue(target.size()-2), eps);
		assertEquals(1.0, target.getTargetValue(target.size()-3), eps);		
		assertEquals("14.03.2018", target.getTargetDate(target.size() -1));
		
	}
	
	
	@Test
	public void testSignalSeries() {
		

		TimeSeries<Double> price = new TimeSeries<Double>();
		price.add(new TimeSeriesEntry<Double>("03.03.2018", 0.0));
		price.add(new TimeSeriesEntry<Double>("04.03.2018", 1.0));
		price.add(new TimeSeriesEntry<Double>("05.03.2018", 2.0));
		price.add(new TimeSeriesEntry<Double>("06.03.2018", 3.0));
		price.add(new TimeSeriesEntry<Double>("07.03.2018", 4.0));
		price.add(new TimeSeriesEntry<Double>("08.03.2018", 5.0));
		price.add(new TimeSeriesEntry<Double>("09.03.2018", 6.0));
		price.add(new TimeSeriesEntry<Double>("10.03.2018", 7.0));
		price.add(new TimeSeriesEntry<Double>("11.03.2018", 8.0));
		price.add(new TimeSeriesEntry<Double>("12.03.2018", 9.0));
		
		TargetSeries target = new TargetSeries(price, 0.99999, false);
		target.addValue(10.0, "13.03.2018");
		target.addValue(11.0, "14.03.2018");
						
		double[] coeffs = new double[]{.2, .4, .1, .5};
		
		try {
		
			SignalSeries signal = new SignalSeries(coeffs, target);
			
			assertEquals( .2, signal.getSignalValue(0), .0001);
			assertEquals( .6, signal.getSignalValue(1), .0001);
			assertEquals( .7, signal.getSignalValue(2), .0001);
			assertEquals(1.2, signal.getSignalValue(3), .0001);
			assertEquals(1.2, signal.getSignalValue(signal.size()-1), .0001);
			
			assertEquals("04.03.2018", signal.getSignalDate(0));
			assertEquals("14.03.2018", signal.getSignalDate(signal.size()-1));
		
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		TargetSeries targetDiff = new TargetSeries(price, 1.0, false);
		targetDiff.addValue(10.0, "13.03.2018");
		targetDiff.addValue(11.0, "14.03.2018");
						
		
		try {
		
			SignalSeries signal = new SignalSeries(coeffs, targetDiff);
			
			assertEquals( .2, signal.getSignalValue(0), eps);
			assertEquals( .6, signal.getSignalValue(1), eps);
			assertEquals( .7, signal.getSignalValue(2), eps);
			assertEquals(1.2, signal.getSignalValue(3), eps);
			assertEquals(1.2, signal.getSignalValue(signal.size()-1), eps);
			
			assertEquals("04.03.2018", signal.getSignalDate(0));
			assertEquals("14.03.2018", signal.getSignalDate(signal.size()-1));
		
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testDateTimeFormatter() {
		
		
		TimeSeries<Double> price = new TimeSeries<Double>();
		price.add(new TimeSeriesEntry<Double>("2011-11-23 02:00:00", 0.0));
		price.add(new TimeSeriesEntry<Double>("2011-11-23 02:30:00", 1.0));
		price.add(new TimeSeriesEntry<Double>("2011-11-23 03:00:00", 2.0));
		price.add(new TimeSeriesEntry<Double>("2011-11-23 03:30:00", 3.0));
		price.add(new TimeSeriesEntry<Double>("2011-11-23 04:00:00", 4.0));
		price.add(new TimeSeriesEntry<Double>("2011-11-23 04:30:00", 5.0));

		TargetSeries targetDiff = new TargetSeries(price, 1.0, false);
		
		double[] coeffs = new double[]{.2, .4, .1, .5};
		try {
			
			
			SignalSeries signal = new SignalSeries(coeffs, targetDiff, "yyyy-MM-dd HH:mm:ss");
			DateTime dt = signal.getSignalDateTime(1);
			
			assertEquals(23, dt.dayOfMonth().get());
			assertEquals(3, dt.hourOfDay().get());
			assertEquals(0, dt.minuteOfHour().get());
		
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	@Test
	public void testPrefilter() {
		
		
		TimeSeries<Double> price = new TimeSeries<Double>();
		price.add(new TimeSeriesEntry<Double>("03.03.2018", 0.0));
		price.add(new TimeSeriesEntry<Double>("04.03.2018", 1.0));
		price.add(new TimeSeriesEntry<Double>("05.03.2018", 2.0));
		price.add(new TimeSeriesEntry<Double>("06.03.2018", 3.0));
		price.add(new TimeSeriesEntry<Double>("07.03.2018", 4.0));
		price.add(new TimeSeriesEntry<Double>("08.03.2018", 5.0));
		price.add(new TimeSeriesEntry<Double>("09.03.2018", 6.0));
		price.add(new TimeSeriesEntry<Double>("10.03.2018", 7.0));
		price.add(new TimeSeriesEntry<Double>("11.03.2018", 8.0));
		price.add(new TimeSeriesEntry<Double>("12.03.2018", 9.0));
		
		TargetSeries target = new TargetSeries(price, 0.99999, false);
		target.addValue(10.0, "13.03.2018");
		target.addValue(11.0, "14.03.2018");
						
		double[] coeffs = new double[]{.2, .4, .1, .5};
		double[] preFilter = new double[]{1.0, 0.0};
		
		try {
		
			SignalSeries signal = new SignalSeries(coeffs, target).setPrefilter(preFilter);
			signal.setMDFAFilterCoefficients(coeffs);
			
			assertTrue(signal.isPrefiltered());
			
			assertEquals( .2, signal.getSignalValue(0), .0001);
			assertEquals( .6, signal.getSignalValue(1), .0001);
			assertEquals( .7, signal.getSignalValue(2), .0001);
			assertEquals(1.2, signal.getSignalValue(3), .0001);
			assertEquals(1.2, signal.getSignalValue(signal.size()-1), .0001);
			
			assertEquals("04.03.2018", signal.getSignalDate(0));
			assertEquals("14.03.2018", signal.getSignalDate(signal.size()-1));
		
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		TargetSeries targetDiff = new TargetSeries(price, 1.0, false);
		targetDiff.addValue(10.0, "13.03.2018");
		targetDiff.addValue(11.0, "14.03.2018");
						
		
		try {
		
			SignalSeries signal = new SignalSeries(coeffs, targetDiff);
			
			assertEquals( .2, signal.getSignalValue(0), eps);
			assertEquals( .6, signal.getSignalValue(1), eps);
			assertEquals( .7, signal.getSignalValue(2), eps);
			assertEquals(1.2, signal.getSignalValue(3), eps);
			assertEquals(1.2, signal.getSignalValue(signal.size()-1), eps);
			
			assertEquals("04.03.2018", signal.getSignalDate(0));
			assertEquals("14.03.2018", signal.getSignalDate(signal.size()-1));
		
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testPrefilterSeries() throws Exception {
		
		
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
		
		double[] preFilter = new double[]{1.0, 0.0, 0.0, 0.0, 0.0};		
		TimeSeries<Double> appleSeries = CsvFeed.getChunkOfData(0, 200, "data/AAPL.IB.dat", "dateTime", "close");	
		
		PriceSeries applePrice = new PriceSeries(appleSeries, false);
		SignalSeries appleSignal = new SignalSeries(new TargetSeries(appleSeries, .8, true), "yyyy-MM-dd").setPrefilter(preFilter);
		
		MDFAFactory anyMDFAFactory = new MDFAFactory(anyMDFA);
		MDFASolver mySolver = new MDFASolver(anyMDFAFactory);
		
		
		MultivariateSeries multi = new MultivariateSeries(mySolver);
		
		multi.addSeries(applePrice);
		multi.addSeries(appleSignal);
		
		multi.computeFilterCoefficients();
		
		Complex c10 = multi.getMDFAFactory().getCustomization().getSpectralBase().getTargetSpectralDensity(10);
		Complex t10 = multi.getMDFAFactory().getCustomization().getSpectralBase().getSpectralDensity(0)[10];
		
		double diff = c10.getReal() - t10.getReal();
		double diffi = c10.getImaginary() - t10.getImaginary();
		diff -= diffi;
		assertEquals(diff, 0.0, .00000001);						
		
		double[] preFilterBad = new double[]{0.4, 0.2, 0.1, 0.9, 0.1};
		((SignalSeries)multi.getSeries(1)).setPrefilter(preFilterBad);
		
		multi.getMDFAFactory().setFilterLength(30);
		multi.computeFilterCoefficients();
		
		int newL = multi.getMDFACoeffs().get(0).length;
		assertEquals(34, newL);
		
		Complex nc10 = multi.getMDFAFactory().getCustomization().getSpectralBase().getTargetSpectralDensity(10);
		Complex nt10 = multi.getMDFAFactory().getCustomization().getSpectralBase().getSpectralDensity(0)[10];
		
		assertFalse(nc10.equals(nt10));
		
	}
	

}
