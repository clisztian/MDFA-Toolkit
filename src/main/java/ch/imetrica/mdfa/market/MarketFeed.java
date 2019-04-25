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
		marketFeed = new CsvFeed(dataFile, "Index", "Open", "High", "Low");
	}
	
	public TimeSeriesEntry<double[]>  getNextPrice() throws NumberFormatException, IOException {
		return marketFeed.getNextBar();
	}
	
	
	
}
