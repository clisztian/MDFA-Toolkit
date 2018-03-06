package ch.imetrica.mdfatrader.series;

import java.io.Serializable;

import org.joda.time.format.DateTimeFormatter;



public interface MdfaSeries extends Serializable {

	
	/**
	 * 
	 * Basic interface to hold time series and 
	 * time series meta-information
	 
	 * Meta-information can include  
	 * I. Type of series
	 * 
	 *  1) price
	 *  2) target series (stationary) 
	 *  3) volatility
	 *  5) explanatory series (stationary) 
	 *  6) MdfaSignal 
	 *  7) Other technical signals
	 * 
	 * II. Mdfa Filter coefficients
	 *  
	 * III. Transform/Differencing  
	 *  
	 * IV. DateTime formatter common formats
	 *     Format("yyyy-MM-dd HH:mm:ss");
	 *     Format("yyyy-MM-dd_HH:mm:ss");
	 *     Format("dd.MM.yyyy"); 
	 *  
	 */
	
	public enum SeriesType {
		
		PRICE,				/* Price of traded asset */
		TARGET,				/* Target series that generates signal, needs coeffs */
		EXPLANATORY,		/* Any explanatory series used to compute target signal, needs coeffs */
		SIGNAL,				/* A filtered series, coeffs are optional */
		VOLA,				/* A price volatility series - implied volatility */
		TECHNICAL;			/* Any other technical indicator */
	}
	

	
	/**
     * Adds a raw time series value in the form of a 
     * double value and a date
     * 
     * @param val
     *            A raw TimeSeries value
     * @param date
     *  		  The date of the entry
     */
	void addValue(double val, String date);
	
	/**
     * Gets the latest time series value
     * 
     * @returns TimeSeriesEntry<Double>
     */
	TimeSeriesEntry<Double> getLatest();
	
	
	/**
     * Returns the entire processed time series held
     * in this MdfaSeries
     * 
     * @returns TimeSeriesEntry<Double>
     */
	TimeSeries<Double> getTimeSeries();
	
	/**
     * Returns the latest n processed time series held
     * in this MdfaSeries. If n is greater than 
     * the time series length then will return entire 
     * series. Future
     * 
     * @param TimeSeries<Double>
     * 
     * @returns TimeSeriesEntry<Double>
     */
	TimeSeries<Double> getLatestValues(int n);
	SeriesType getSeriesType();
	void setDateFormat(DateTimeFormatter anyformat);
	DateTimeFormatter getDateFormat() throws Exception;
	
	

	
}
