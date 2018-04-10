package ch.imetrica.mdfa.series;
import org.jfree.ui.RefineryUtilities;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.imetrica.mdfa.plotutil.TimeSeriesPlot;
import ch.imetrica.mdfa.transform.Transform;

public class TargetSeries implements MdfaSeries {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final SeriesType seriesType = SeriesType.TARGET;
	private DateTimeFormatter formatter;// = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
	private TimeSeries<double[]> timeSeries;
	private Transform seriesTransform;
    private String name;
	
    
    /**
     * 
     * A TargetSeries with an empty TimeSeries<double[]> and a 
     * initiated fractional differencing weight set. Raw time series
     * values are ready to be added
     * 
     * @param d A differencing operater d such that 0 <= d < =1.
     * 			In the case that d=0 no differencing is applied. In the case d=1 is 
     * 			just standard backward differencing.
     * 
     * @param log Log values applied to raw time series
     * 
     * @param name The name of the target series
     */
    public TargetSeries(double d, boolean log, String name) {
    	
    	seriesTransform = new Transform(d, log);
    	timeSeries = new TimeSeries<double[]>();
    	this.name = name;
    }
    
    
	/**
     * TargetSeries is a type of time series which has two components:
     * The original time series, and the transformed time series.
     * TimeSeries data is raw series, with no transformations
     * applied. After transformation, the raw series is kept as the 
     * second index of the double[2] value. The first (zeroth) index
     * contains the transformed data
     * 
     * @param anyseries
     *            A raw univariate TimeSeries 
     *            
     * @param d 
     * 			A differencing operater d such that 0 <= d < =1
     * In the case that d=0 no differencing is applied. In the case d=1 is 
     * just standard backward differencing. 
     * 
     * @param log 
     * 			Log values applied to raw time series 
     */
	
	public TargetSeries(TimeSeries<Double> anyseries, double d, boolean log) {
		
		seriesTransform = new Transform(d, log);
		timeSeries = seriesTransform.applyTransform(anyseries);
				
	}


	/**
     * Adds a new TimeSeriesEntry observation to the timeSeries.
     * The raw value is kept in the second index of the timeSeries.
     * The transformation value is stored in the first index.
     * 
     * @param val
     *            A raw time series value
     *            
     * @param date 
     * 			The timedate stamp of this value
     * 
     */
	@Override
	public void addValue(String date, double val) {
		
		seriesTransform.addValue(timeSeries, val, date);
	}



	@Override
	public TimeSeriesEntry<Double> getLatest() {
		
		return new TimeSeriesEntry<Double>(timeSeries.get(timeSeries.size() - 1).getDateTime(),
				                   timeSeries.get(timeSeries.size() - 1).getValue()[0]);
	}



	@Override
	public TimeSeries<Double> getTimeSeries() {
		
		TimeSeries<Double> series = new TimeSeries<Double>();
		
		for(int i = 0; i < timeSeries.size(); i++) {
			series.add(new TimeSeriesEntry<Double>(timeSeries.get(i).getDateTime(),
				                   timeSeries.get(i).getValue()[0]));
		}
		
		return series;
	}


	@Override
	public TimeSeries<Double> getLatestValues(int n) {

		TimeSeries<Double> series = new TimeSeries<Double>();
		int mySize = Math.min(timeSeries.size(), n);
		int start = Math.max(timeSeries.size() - mySize,0);
		
		for(int i = start; i < timeSeries.size(); i++) {
			series.add(new TimeSeriesEntry<Double>(timeSeries.get(i).getDateTime(),
				                   timeSeries.get(i).getValue()[0]));
		}		
		return series;
	}


	@Override
	public SeriesType getSeriesType() {
		return seriesType;
	}

	
	
	/**
     * Returns the size of the underlying time series
     * @return 
     *          int the current size of the time series
     */
	public int size() {
		return timeSeries.size();
	}

	/**
     * Returns the value of the target series at index i
     * @return 
     *          double value at index i
     */
	public double getTargetValue(int i) {
		return timeSeries.get(i).getValue()[0];
	}

	/**
     * Returns the date at index i
     * @return 
     *          String the datetime at index i
     */
	public String getTargetDate(int i) {
		return timeSeries.get(i).getDateTime();
	}
	
	/**
     * Returns in string format the current target in
     * comma separate form 

     * @return 
     *          String with datetime, target series value, raw series value
     */
	public String toString() {
		
		String tostring = "";
		for(TimeSeriesEntry<double[]> entry : timeSeries) {
			
			tostring += entry.getDateTime() + ", " + entry.getValue()[0] + ", " + entry.getValue()[1] + "\n";
		}
		return tostring;
	}
	
	/**
	 * Adjusts the fractional difference component  
	 * of the target time series using the raw series
	 * that is stored in the time series 
	 * 
	 * @param d New fractional difference exponent
	 */
	public void adjustFractionalDifferenceData(double d) {
		
		seriesTransform.adjustFractionalDifference(this.timeSeries, d);		
	}
	
	
	
	public void plotSeries() {
		
		final String title = "EURUSD frac diff";
        final TimeSeriesPlot eurusd = new TimeSeriesPlot(title, this);
        eurusd.pack();
        RefineryUtilities.positionFrameRandomly(eurusd);
        eurusd.setVisible(true);
		
	}
	
	
	public static void plotMultipleSeries(TargetSeries[] collection) {
		
		final String title = "EURUSD frac diff";
        final TimeSeriesPlot eurusd = new TimeSeriesPlot(title, collection);
        eurusd.pack();
        RefineryUtilities.positionFrameRandomly(eurusd);
        eurusd.setVisible(true);
		
	}
	
	public void printFracDiffWeights() {
		
		System.out.println(seriesTransform.toString());	
	}
	
	


	@Override
	public void setDateFormat(String anyformat) {
		formatter = DateTimeFormat.forPattern(anyformat);
	}

	@Override
	public DateTimeFormatter getDateFormat() throws Exception {
		
        if(formatter == null) {
        	throw new Exception("No DateTime format defined yet");
        }		
		return formatter;
	}
	
	public DateTime getDateTime(int i) {
		return formatter.parseDateTime(timeSeries.get(i).getDateTime());
	}


	@Override
	public void setName(String name) {
		this.name = name;
	}


	@Override
	public String getName() {
		return name;
	}


	@Override
	public void chopFirstObservations(int n) {
		
		int chopped = Math.min(n, timeSeries.size());
		for(int i = 0; i < chopped; i++) {
			timeSeries.remove(0);
		}	
	}


	@Override
	public boolean isPrefiltered() {
		return false;
	}



	

	
	
}
