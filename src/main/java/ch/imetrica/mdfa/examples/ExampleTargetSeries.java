package ch.imetrica.mdfa.examples;

import ch.imetrica.mdfa.datafeeds.CsvFeed;
import ch.imetrica.mdfa.series.TargetSeries;
import ch.imetrica.mdfa.series.TimeSeriesEntry;

public class ExampleTargetSeries {

	
	private static final int MAX_OBS = 300;

	public static void main(String[] args) throws Exception {
		
		/* Create market data feed */
		CsvFeed marketDataFeed = new CsvFeed("data/AAPL.IB.dat", "dateTime", "close");
		
		/* Create empty target series */
		TargetSeries target = new TargetSeries(0.99, true, "test");
		target.setDateFormat("yyyy-MM-dd");
		
		for(int i = 0; i < MAX_OBS; i++) {
			
			TimeSeriesEntry<Double> observation = marketDataFeed.getNextObservation();
			target.addValue(observation.getDateTime(), observation.getValue());
			
		}
		
		target.plotSeries();

	}
	
	
}
