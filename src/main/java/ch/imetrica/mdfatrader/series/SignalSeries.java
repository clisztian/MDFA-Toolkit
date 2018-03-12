package ch.imetrica.mdfatrader.series;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.jfree.ui.RefineryUtilities;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.csvreader.CsvReader;

import ch.imetrica.mdfatrader.unbiased.WhiteNoiseFilter;
import ch.imetrica.mdfatrading.plotutil.TimeSeriesPlot;

public class SignalSeries implements MdfaSeries {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final SeriesType seriesType = SeriesType.SIGNAL;
	private DateTimeFormatter formatter;
	private TimeSeries<Double> signalSeries;
	private TargetSeries target;
	private double[] coeffs;

	private String name;
	
	
	public SignalSeries(double[] coeffs) {			
		this.coeffs = coeffs;			
	}

	/**
     * Constructs a SignalSeries from a reference target time series
     * and set of MDFA coefficients. The signal time series will be 
     * automatically computed once the coefficients are set 
     *            
     * @param anytarget 
     * 			A target TimeSeries object holding historical price/target data
     * 
     */
	public SignalSeries(TargetSeries anytarget) {
		
		this.coeffs = null;
		this.target = anytarget;		
	}
	
	
	/**
	 * 
	 * Constructs a SignalSeries from a reference target time series
     * and set of MDFA coefficients. The signal time series will be 
     * automatically computed once the coefficients are set  
	 * 
	 * @param anytarget
	 *   A target TimeSeries object holding historical price/target data
	 * @param name
	 *   Name of series such that it can be uniquely identified
	 */
	public SignalSeries(TargetSeries anytarget, String name) {
		
		this.coeffs = null;
		this.target = anytarget;	
		this.name = name;
	}
	
	
	/**
     * Constructs a SignalSeries from a reference target time series
     * and set of MDFA coefficients. The signal time series will be 
     * automatically computed 
     * 
     * @param coeffs
     *            A set of MDFA or other filter coefficients
     *            
     * @param anytarget 
     * 			A target TimeSeries object holding historical price/target data
     * 
     */
	
	public SignalSeries(double[] coeffs, TargetSeries anytarget) throws Exception {		
		
		this.coeffs = coeffs;		
		this.target = anytarget;
		this.computeSignalFromTarget();
	}
	
		
	
	/**
     * Constructs a SignalSeries from a reference target time series
     * and set of MDFA coefficients. The signal time series will be 
     * automatically computed 
     * 
     * @param coeffs
     *            A set of MDFA or other filter coefficients
     *            
     * @param anytarget 
     * 			A target TimeSeries object holding historical price/target data
     * 
     * @param format 
     * 			A format for the DateTime representation
     * 
     */
	
	public SignalSeries(double[] coeffs, TargetSeries anytarget, DateTimeFormatter formatter) throws Exception {		
		
		this.coeffs = coeffs;		
		this.target = anytarget;
		this.formatter = formatter;
		this.target.setDateFormat(formatter);
		this.computeSignalFromTarget();
	}
	
	
	
	/**
     * Stores the latest filter coefficients
     * coefficients. Recomputes a new signal series based on (new)
     * coefficients. 
     * 
     * @param b 
     *     The filter coefficients to store
	 * @throws Exception 
	 * 		if target not defined, won't recompute signal
     * 
     */
	public void setMDFAFilterCoefficients(double[] b) throws Exception {
		
		this.coeffs = b;
		if(target != null) {
			this.computeSignalFromTarget();
		}
	}
	
	
	/**
     * Constructs a SignalSeries from a reference target time series
     * and set of MDFA coefficients. The signal time series will be 
     * automatically computed if/once the coefficients are set. 
     * 
     * For the first L values of signal series, the values are computed
     * using the truncated coefficients, so the signal series and underlying
     * target series match in datetime values
     *            
     * @throws Exception 
     * 			Both the coefficients and the 
     * 
     */
	public void computeSignalFromTarget() throws Exception {
		
		if(coeffs == null) {
			throw new Exception("No MDFA coefficients yet computed for this target series");
		}
		
		if(target == null) {
			throw new Exception("No target series has been defined yet");
		}
		
		signalSeries = new TimeSeries<Double>();
		
		int N = target.size();		
		for(int i = 0; i < N; i++) {
			
			int filter_length = Math.min(i+1, coeffs.length);
			double sum = 0;
			for (int l = 0; l < filter_length; l++) {
				sum = sum + coeffs[l]*target.getTargetValue(i - l);
			}
			signalSeries.add(new TimeSeriesEntry<Double>(target.getTargetDate(i), sum));
			
		}		
	}
	
	/**
     * Adds a raw time series value in the form of a 
     * double value and a date. Once the raw value
     * is added to the target the target series 
     * is updated and then the latest signal value 
     * is added to the signal series
     * 
     * @param val
     *            A raw TimeSeries value
     * @param date
     *  		  The date of the entry
     */
	@Override
	public void addValue(double val, String date) {
		
		if(coeffs != null && target != null) {
			
			target.addValue(val, date);
			
			int N = target.size();
			int filter_length = Math.min(N, coeffs.length);
			double sum = 0;
			for (int l = 0; l < filter_length; l++) {
				sum = sum + coeffs[l]*target.getTargetValue(N - l - 1);
			}
			signalSeries.add(new TimeSeriesEntry<Double>(target.getTargetDate(N - 1), sum));
			
		}		
	}

	@Override
	public TimeSeriesEntry<Double> getLatest() {
		return signalSeries.get(signalSeries.size() - 1);
	}

	@Override
	public TimeSeries<Double> getTimeSeries() {
		return signalSeries;
	}

	@Override
	public TimeSeries<Double> getLatestValues(int n) {
		
		TimeSeries<Double> series = new TimeSeries<Double>();
		
		int mySize = Math.min(signalSeries.size(), n);
		int start = Math.max(signalSeries.size() - mySize,0);
		
		for(int i = start; i < signalSeries.size(); i++) {
			series.add(signalSeries.get(i));
		}		
		return series;
	}

	/**
     * Returns the size of the underlying time series
     * @return 
     *          int the current size of the time series
     */
	public int size() {
		return signalSeries.size();
	}

	/**
     * Returns the value of the target series at index i
     * 
     * @param i index at i 
     * 
     * @return 
     *          double value at index i
     */
	public double getSignalValue(int i) {
		return signalSeries.get(i).getValue();
	}

	/**
     * Returns the date at index i
     *
     * @param i index at i 
     * 
     * @return 
     *          String the datetime at index i
     */
	public String getSignalDate(int i) {
		return signalSeries.get(i).getDateTime();
	}
	
	
	
	@Override
	public SeriesType getSeriesType() {
		return seriesType;
	}
	
	
	@Override
	public void setDateFormat(DateTimeFormatter anyformat) {
		formatter = anyformat;
	}



	@Override
	public DateTimeFormatter getDateFormat() throws Exception {
        if(formatter == null) {
        	throw new Exception("No DateTime format defined yet");
        }
		
		return formatter;
	}

	public DateTime getSignalDateTime(int i) {
		// TODO Auto-generated method stub
		return formatter.parseDateTime(getSignalDate(i));
	}
	
	
    public static void plotSignal(SignalSeries signal) {
		
		final String title = "EURUSD frac diff";
        final TimeSeriesPlot eurusd = new TimeSeriesPlot(title, signal);
        eurusd.pack();
        RefineryUtilities.positionFrameRandomly(eurusd);
        eurusd.setVisible(true);
		
	}
    
    public String toString() {
    	
    	String tostring = "";
		for(int i = 0; i < signalSeries.size(); i++) {
			
			tostring += signalSeries.get(i).getDateTime() + " " + signalSeries.get(i).getValue() + " " + target.getTargetDate(i) + " " + target.getTargetValue(i) + "\n";
		}
		return tostring;
    }
	
	public double getTargetValue(int i) {
		return target.getTargetValue(i);
	}
	
	
	
	public static void main(String[] args) {
		
		
		String dataFile = "data/EURUSD.30min.csv";
		TimeSeries<Double> rawSeries = new TimeSeries<Double>();
		CsvReader marketDataFeed;
		
		int nObs = 0;
		int MAX_OBS = 1200;
		
		try{
			
			 /* Read data market feed from CSV filer and it's headers*/	
			 marketDataFeed = new CsvReader(dataFile);
			 marketDataFeed.readHeaders();

			 while (marketDataFeed.readRecord()) {
				 
				double price = (new Double(marketDataFeed.get("bid"))).doubleValue();
				String date_stamp = marketDataFeed.get("dateTime");				
				rawSeries.add(date_stamp, price);
				
				nObs++;
				
				if(nObs == MAX_OBS) break;
			 }
			 
			 double[] filter = (new WhiteNoiseFilter(Math.PI/6.0, 0, 50)).getFilterCoefficients();
			 TargetSeries myTarget = new TargetSeries(rawSeries, 0.2, true);
			 
			 
			 SignalSeries signal = new SignalSeries(filter, myTarget, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
		
		     
			 SignalSeries.plotSignal(signal);
			 
			 
			 while (marketDataFeed.readRecord()) {
				 
					double price = (new Double(marketDataFeed.get("bid"))).doubleValue();
					String date_stamp = marketDataFeed.get("dateTime");				
					signal.addValue(price, date_stamp);
					
					nObs++;
					
					if(nObs == MAX_OBS + 200) break;
					
			 }
			 
			 System.out.println(signal.toString());
			 SignalSeries.plotSignal(signal);
			 
		
		}
		catch (FileNotFoundException e) { e.printStackTrace(); throw new RuntimeException(e); } 
		catch (IOException e) { e.printStackTrace(); throw new RuntimeException(e);} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}


	@Override
	public String getName() {
		return name;
	}

	

}
