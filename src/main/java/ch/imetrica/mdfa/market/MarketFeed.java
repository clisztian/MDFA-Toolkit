package ch.imetrica.mdfa.market;

import java.io.IOException;
import java.util.Random;

import ch.imetrica.mdfa.datafeeds.CsvFeed;
import ch.imetrica.mdfa.series.TimeSeriesEntry;

public class MarketFeed {

	
	CsvFeed marketFeed;
	Random rng;
	boolean randomPrice;
	
	
	public MarketFeed(String dataFile) throws IOException {		
		
		rng = new Random();
		marketFeed = new CsvFeed(dataFile, "Index", "Open", "High", "Low", "Close");
	}
	
	public TimeSeriesEntry<double[]> getNextPrice() throws NumberFormatException, IOException {
		
		TimeSeriesEntry<double[]> myEntry = marketFeed.getNextBar();	
		
		if(myEntry == null) {
			return null;
		}
		
		String timestamp = myEntry.getDateTime();
		
		double[] bar = myEntry.getValue();
		
		double high = bar[1];
		double low = bar[0];
		double close = bar[3];
		double open = bar[2];
		
		double trade_price = (high - low)*rng.nextDouble() + low;		
		double[] newbar = new double[] {trade_price, close, open};
		
		return new TimeSeriesEntry<double[]>(timestamp, newbar); 				
	}

	public void close() {
		marketFeed.close();	
	}


	
	
	
}
