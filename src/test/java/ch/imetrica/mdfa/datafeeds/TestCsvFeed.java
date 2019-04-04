package ch.imetrica.mdfa.datafeeds;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.Random;

import org.joda.time.DateTime;
import org.junit.Test;

import ch.imetrica.mdfa.series.TimeSeriesEntry;

public class TestCsvFeed {

	double eps = .00000000001;
	
	@Test
	public void testMultivariateFeed() throws Exception {
		
		String dataFile = "data/ohneTimeStamp.csv";
		CsvFeed marketFeed = new CsvFeed(dataFile, new DateTime().withDate(2018, 7, 10));
		
		TimeSeriesEntry<double[]> observation = marketFeed.getNextMultivariateObservation();
				
		assertEquals(0.7757946108877242, observation.getValue()[0], eps);
		assertEquals(0.6019838330223529, observation.getValue()[1], eps);
		assertEquals(0.9977480833573259, observation.getValue()[2], eps);
		

		observation = marketFeed.getNextMultivariateObservation();
		assertEquals(0.34451159883600857, observation.getValue()[0], eps);
		assertEquals(0.8048803816459155, observation.getValue()[1], eps);
		assertEquals(0.17409453201360459, observation.getValue()[2], eps);
		
		assertEquals("2018-07-11", observation.getDateTime());
		
	}

}
