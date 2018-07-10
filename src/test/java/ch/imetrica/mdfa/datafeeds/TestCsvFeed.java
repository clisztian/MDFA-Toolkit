package ch.imetrica.mdfa.datafeeds;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.Random;

import org.junit.Test;

import ch.imetrica.mdfa.series.TimeSeriesEntry;

public class TestCsvFeed {

	double eps = .00000000001;
	
	@Test
	public void testMultivariateFeed() throws Exception {
		
		String dataFile = "data/ohneTimeStamp.csv";
		CsvFeed marketFeed = new CsvFeed(dataFile);
		
		TimeSeriesEntry<double[]> observation = marketFeed.getNextMultivariateObservation();
		
		System.out.println(observation.toString());
		
		assertEquals(0.5077886109183235, observation.getValue()[0], eps);
		assertEquals(0.7280528437659037, observation.getValue()[1], eps);
		assertEquals(0.5185897922388323, observation.getValue()[2], eps);
		
		observation = marketFeed.getNextMultivariateObservation();
		assertEquals(0.7757946108877242, observation.getValue()[0], eps);
		assertEquals(0.7757946108877242, observation.getValue()[1], eps);
		assertEquals(0.9977480833573259, observation.getValue()[2], eps);
		

	}

}
