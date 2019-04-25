package ch.imetrica.mdfa.datafeeds;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.csvreader.CsvReader;

import ch.imetrica.mdfa.series.TimeSeries;
import ch.imetrica.mdfa.series.TimeSeriesEntry;


/**
 * 
 * A utility class for reading and streaming market data files in 
 * csv format. 
 * 
 * The data file must be standard comma separated files with a datetime
 * stamp column and at least one "price" value column
 * 
 * 
 * @author Christian D. Blakely (clisztian@gmail.com)
 *
 */
public class CsvFeed {

		
	private CsvReader[] marketDataFeeds;
	private CsvReader marketDataFeed;
	private String dateColumnName;
	private String priceColumnName;
	private String highColumnName;
	private String lowColumnName;
	
	private DateTime dt;
	private DateTimeFormatter dtfOut;
	private String[] headers;
	
	
	public CsvFeed(String dataFile, String dateName, String priceName) throws IOException {
		
		this.marketDataFeed = new CsvReader(dataFile);
		this.marketDataFeed.readHeaders();
		this.dateColumnName = dateName;
		this.priceColumnName = priceName;
	}
	
	public CsvFeed(String dataFile, String dateName, String priceName,
			String highName, String lowName) throws IOException {
		
		this.marketDataFeed = new CsvReader(dataFile);
		this.marketDataFeed.readHeaders();
		this.dateColumnName = dateName;
		this.priceColumnName = priceName;
		this.highColumnName = highName;
		this.lowColumnName = lowName;
	}
	
	public CsvFeed(String[] dataFiles, String dateName, String priceName) throws IOException {
		
		this.marketDataFeeds = new CsvReader[dataFiles.length]; 
		
		for(int i = 0; i < dataFiles.length; i++) {
			this.marketDataFeeds[i] = new CsvReader(dataFiles[i]);
			this.marketDataFeeds[i].readHeaders();
		}
		
		this.dateColumnName = dateName;
		this.priceColumnName = priceName;
	}
	
	public CsvFeed(ArrayList<String> dataFiles, String dateName, String priceName) throws IOException {
		
		this.marketDataFeeds = new CsvReader[dataFiles.size()]; 
		
		for(int i = 0; i < dataFiles.size(); i++) {
			this.marketDataFeeds[i] = new CsvReader(dataFiles.get(i));
			this.marketDataFeeds[i].readHeaders();
		}
		
		this.dateColumnName = dateName;
		this.priceColumnName = priceName;
	}
	
	/**
	 * Create a CsvFeed for data files that do not have a header or timestamp values
	 * The timestamp will be created to be the daily stamp from the day of creation of the 
	 * CsvFeed object. 
	 * 
	 * 
	 * @param dataFiles
	 * @param startDate First date to start the time series
	 * @throws Exception 
	 */
	public CsvFeed(String dataFile, DateTime startDate) throws Exception {
		
		dt = startDate;
		dtfOut = DateTimeFormat.forPattern("yyyy-MM-dd");

		this.marketDataFeed = new CsvReader(dataFile);

		if(this.marketDataFeed.readRecord()) {
			
			int nCols = this.marketDataFeed.getColumnCount();
			headers = new String[nCols];
			
			for(int i = 0; i < nCols; i++) {
				headers[i] = "DataColumn_" + i;
			}
			this.marketDataFeed.setHeaders(headers);
		}
		else {
			throw new Exception("No data or not in any .csv format");
		}
	}
	
	/**
	 * 
	 * Gets a chunk of MAX_OBS time series data from a csv file from the given start.
	 * The file must have a header with the dateTime as the date time columns. The value 
	 * will be extracted from the priceName column (for example "bid", "ask", "close", "high", etc.)
	 * 
	 * @param start
	 *    The first observation in the csv file that will be used
	 * @param MAX_OBS
	 *    The total number of observations
	 * @param dataFile
	 *    The csv data file name/location
	 * @param dateName
	 *    The column name of the raw time series to extract    
	 * @param priceName
	 *    The column name of the raw time series to extract
	 * @return
	 */
	public static TimeSeries<Double> getChunkOfData(int start, int MAX_OBS, String dataFile, String dateName, String priceName) {
		
		TimeSeries<Double> rawSeries = new TimeSeries<Double>();
		CsvReader marketDataFeed;
		
		int nObs = 0;
		
		try{
			
			 /* Read data market feed from CSV filer and it's headers*/	
			 marketDataFeed = new CsvReader(dataFile);
			 marketDataFeed.readHeaders();

			 while (marketDataFeed.readRecord()) {
				 
				double price = (new Double(marketDataFeed.get(priceName))).doubleValue();
				String date_stamp = marketDataFeed.get(dateName);				
				rawSeries.add(date_stamp, price);
				
				nObs++;
				
				if(nObs == MAX_OBS) break;
			 }			 
		}
		catch (FileNotFoundException e) { e.printStackTrace(); throw new RuntimeException(e); } 
		catch (IOException e) { e.printStackTrace(); throw new RuntimeException(e);} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rawSeries;		
	}
	
	/**
	 * 
	 * Returns the next raw time series observation in the csv file
	 * in the form of a TimeSeriesEntry<Double>. If none left the 
	 * observation will be null.
	 * 
	 * @return TimeSeriesEntry<Double> the next observation in the given 
	 * 		   csv market feed
	 * 
	 * @throws NumberFormatException
	 * 
	 * @throws IOException
	 */
	public TimeSeriesEntry<Double> getNextObservation() throws NumberFormatException, IOException {
		
		if(!marketDataFeed.readRecord()) {
			return null;
		}
		
		double price = (new Double(marketDataFeed.get(priceColumnName))).doubleValue();
		String date_stamp = marketDataFeed.get(dateColumnName);		
			
		return (new TimeSeriesEntry<Double>(date_stamp, price));
	}
	
	
	public TimeSeriesEntry<double[]> getNextBar() throws NumberFormatException, IOException {
		
		if(!marketDataFeed.readRecord()) {
			return null;
		}
		
		double price = (new Double(marketDataFeed.get(priceColumnName))).doubleValue();
		double high = (new Double(marketDataFeed.get(highColumnName))).doubleValue();
		double low = (new Double(marketDataFeed.get(lowColumnName))).doubleValue();		
		String date_stamp = marketDataFeed.get(dateColumnName);		
		
		double[] myBar = new double[] {low, high, price};
		
		return (new TimeSeriesEntry<double[]>(date_stamp, myBar));
	}
	
	
	public TimeSeriesEntry<double[]> getNextMultivariateObservation() throws Exception {
		
		
		if(dt != null && headers != null) {
		
			if(marketDataFeed == null) {
				throw new Exception();
			}
			
			String date_stamp;
			double[] prices = new double[headers.length];
			
			if(marketDataFeed.readRecord()) {
				
				for(int i = 0; i < headers.length; i++) {					
					prices[i] = (new Double(marketDataFeed.get(headers[i]))).doubleValue();
				}
				date_stamp = dt.toString(dtfOut);
				dt = dt.plusDays(1);
			}
			else {
				return null;
			}
			return (new TimeSeriesEntry<double[]>(date_stamp, prices));
		}
		else {
		
		 if(marketDataFeeds == null) {
		 	throw new Exception();
		 }

		 double price;
		 String date_stamp;
		 double[] prices = new double[marketDataFeeds.length];
		 String[] timestamps = new String[marketDataFeeds.length];
		
		 if(marketDataFeeds[0].readRecord()) {
		
			price = (new Double(marketDataFeeds[0].get(priceColumnName))).doubleValue();
			date_stamp = marketDataFeeds[0].get(dateColumnName);	
			
			prices[0] = price;
			timestamps[0] = date_stamp;
		 }
		 else {
			return null;
		 }
			
		 for(int i = 1; i < marketDataFeeds.length; i++) {
			
			if(marketDataFeeds[i].readRecord()) {
				
				price = (new Double(marketDataFeeds[i].get(priceColumnName))).doubleValue();
				date_stamp = marketDataFeeds[i].get(dateColumnName);		
				if(!timestamps[0].equals(date_stamp)) {
					price = -1.0;
				}
				prices[i] = price;
			}
			else {
				return null;
			}		
		 }		
		 return (new TimeSeriesEntry<double[]>(timestamps[0], prices));
		}
	}
	
	public CsvReader[] getMarketDataFeeds() {
		return marketDataFeeds;
	}
}
