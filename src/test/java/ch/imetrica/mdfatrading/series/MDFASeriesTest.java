package ch.imetrica.mdfatrading.series;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import ch.imetrica.mdfatrader.series.SignalSeries;
import ch.imetrica.mdfatrader.series.TargetSeries;
import ch.imetrica.mdfatrader.series.TimeSeries;
import ch.imetrica.mdfatrader.series.TimeSeriesEntry;

public class MDFASeriesTest {

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
			
			
			SignalSeries signal = new SignalSeries(coeffs, targetDiff, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
			DateTime dt = signal.getSignalDateTime(1);
			
			assertEquals(23, dt.dayOfMonth().get());
			assertEquals(3, dt.hourOfDay().get());
			assertEquals(0, dt.minuteOfHour().get());
		
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

}
