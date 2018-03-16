package ch.imetrica.mdfa.series;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.imetrica.mdfa.transform.Transform;

public class PriceSeries implements MdfaSeries {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final SeriesType seriesType = SeriesType.PRICE;
	private DateTimeFormatter formatter;
	TimeSeries<Double> timeSeries;
	Transform seriesTransform;
	boolean logTransform = false;
	private String name;
	
	/**
     * PriceSeries is a type of time series which has only one component:
     * The original price series.  
     * 
     * @param anyseries
     *            A raw univariate TimeSeries 
     *            
     * @param log 
     * 			Log values applied to raw time series 
     */
	
	public PriceSeries(TimeSeries<Double> anyseries, boolean log) {
		
		seriesTransform = new Transform(0, log);
		timeSeries = seriesTransform.applyLogTransform(anyseries);
		logTransform = log;
				
	}

	
	@Override
	public void addValue(double val, String date) {		
		seriesTransform.addPrice(timeSeries, val, date);		
	}

	@Override
	public TimeSeriesEntry<Double> getLatest() {
		return timeSeries.get(timeSeries.size() - 1);
	}

	@Override
	public TimeSeries<Double> getTimeSeries() {
		return timeSeries;
	}

	@Override
	public TimeSeries<Double> getLatestValues(int n) {
		
		TimeSeries<Double> series = new TimeSeries<Double>();
		int mySize = Math.min(timeSeries.size(), n);
		int start = Math.max(timeSeries.size() - mySize,0);
		
		for(int i = start; i < timeSeries.size(); i++) {
			series.add(timeSeries.get(i));
		}		
		return series;
	}

	@Override
	public SeriesType getSeriesType() {
		return seriesType;
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


	@Override
	public int size() {
		// TODO Auto-generated method stub
		return timeSeries.size();
	}


	@Override
	public double getTargetValue(int i) {
		return 0;
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
}
