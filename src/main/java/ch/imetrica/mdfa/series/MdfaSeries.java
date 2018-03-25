package ch.imetrica.mdfa.series;

import java.io.Serializable;

import org.joda.time.format.DateTimeFormatter;


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
 * 
 * 
 * @author Christian D. Blakely (clisztian@gmail.com)
 *
 */

public interface MdfaSeries extends Serializable {

	
	public enum SeriesType {
		
		PRICE,				/** Price of traded asset */
		TARGET,				/** Target series that generates signal, needs coeffs */
		LABEL,				/** Any labeled series used for machine learning apps */
		SIGNAL,				/** A filtered series, coeffs are optional */
		MULTISIGNAL,		/** A MultivariateSignal */
		VOLA,				/** A price volatility series - implied volatility */
		TECHNICAL;			/** Any other technical indicator */
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
	void addValue(String date, double val);
	
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
	
	/**
	 * Get the series type of the MdfaSeries 
	 * 
	 * @return SeriesType
	 */
	SeriesType getSeriesType();
	
	/**
	 * Set a  string format to datetime objects
	 * 
	 * @param anyformat 
	 */
	void setDateFormat(String anyformat);
	
	/**
	 * Throws an exception if format doesn't exist
	 * 
	 * @return
	 * @throws Exception
	 */
	DateTimeFormatter getDateFormat() throws Exception;
	
	/**
	 * Current size of this time series
	 * @return size
	 */
	int size();
	
	/**
	 * Sets the name of this series from which it can 
	 * be uniquely identified
	 * 
	 * @param name
	 *   Any name 
	 */
	void setName(String name);
	
	/**
	 * Gets the name of this series
	 * 
	 * @return
	 *   Name of series which should be unique
	 */
	String getName();

	/**
	 * Gets the latest value in this time series 
	 * without the String date. For a signalSeries, this will 
	 * be the underlying target series that this signal is 
	 * connected to. For a priceseries, this will simply be the price
	 * @param i
	 * @return Value at i
	 */
	double getTargetValue(int i);
	
	/**
	 * Eliminates the first n observations 
	 * in the time series, which are typically not 
	 * needed. A typical n value is L, the length 
	 * of the filter
	 * 
	 * @param n
	 *   The number of beginning observations to 
	 *   eliminate
	 */
	void chopFirstObservations(int n);

	/**
	 * In the case that the MDFASeries is 
	 * a Signal series AND is a prefiltered series, 
	 * returns true, otherwise returns false
	 * 
	 * @return
	 */
	boolean isPrefiltered();

	
}
