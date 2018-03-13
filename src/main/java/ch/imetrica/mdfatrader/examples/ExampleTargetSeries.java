package ch.imetrica.mdfatrader.examples;

import org.joda.time.format.DateTimeFormat;

import ch.imetrica.mdfatrader.datafeeds.CsvFeed;
import ch.imetrica.mdfatrader.series.TargetSeries;
import ch.imetrica.mdfatrader.series.TimeSeriesEntry;

public class ExampleTargetSeries {

	
	private static final int MAX_OBS = 300;

	public static void main(String[] args) throws Exception {
		
		/* Create market data feed */
		CsvFeed marketDataFeed = new CsvFeed("data/AAPL.IB.dat", "dateTime", "close");
		
		/* Create empty target series */
		TargetSeries target = new TargetSeries(0.99, true, "test");
		target.setDateFormat(DateTimeFormat.forPattern("yyyy-MM-dd"));
		
		for(int i = 0; i < MAX_OBS; i++) {
			
			TimeSeriesEntry<Double> observation = marketDataFeed.getNextObservation();
			target.addValue(observation.getValue(), observation.getDateTime());
			
		}
		
		target.plotSeries();

	}
	
	
}
