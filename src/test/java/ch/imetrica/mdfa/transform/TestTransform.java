package ch.imetrica.mdfa.transform;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.imetrica.mdfa.series.TargetSeries;
import ch.imetrica.mdfa.series.TimeSeries;
import ch.imetrica.mdfa.series.TimeSeriesEntry;

public class TestTransform {

	final double eps = .000001;
	
	@Test
	public void testTransformChange() {
		
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
		target.addValue("13.03.2018", 10.0);
		target.addValue("14.03.2018", 11.0);
		target.addValue("15.03.2018", 12.0);
		target.addValue("16.03.2018", 13.0);
		target.addValue("17.03.2018", 14.0);
		target.addValue("18.03.2018", 15.0);
		target.addValue("19.03.2018", 16.0);
		target.addValue("20.03.2018", 17.0);
		target.addValue("21.03.2018", 18.0);
		target.addValue("22.03.2018", 19.0);
		target.addValue("23.03.2018", 20.0);
		target.addValue("24.03.2018", 21.0);
				
		assertEquals(1.0, target.getTargetValue(target.size()-1), eps);
		assertEquals(1.0, target.getTargetValue(target.size()-2), eps);
		assertEquals(1.0, target.getTargetValue(target.size()-3), eps);		
		assertEquals("24.03.2018", target.getTargetDate(target.size() -1));
		
		target.adjustFractionalDifferenceData(0);
		
		assertEquals(21.0, target.getTargetValue(target.size()-1), eps);
		assertEquals(20.0, target.getTargetValue(target.size()-2), eps);
		assertEquals(19.0, target.getTargetValue(target.size()-3), eps);
		assertEquals(1.0, target.getTargetValue(1), eps);
		assertEquals(2.0, target.getTargetValue(2), eps);
		assertEquals("03.03.2018", target.getTargetDate(0));
		assertEquals("24.03.2018", target.getTargetDate(target.size()-1));
		
		target.adjustFractionalDifferenceData(.5);
		assertEquals(1.5, target.getTargetValue(2), eps);
		assertEquals(22, target.size());
		
		target.adjustFractionalDifferenceData(1.0);
		target.addValue("25.03.2018", 22.0);
		target.addValue("26.03.2018", 23.0);
		assertEquals(1.0, target.getTargetValue(target.size()-1), eps);
		assertEquals(24, target.size());
		assertEquals("26.03.2018", target.getTargetDate(target.size()-1));
		
	}

}
