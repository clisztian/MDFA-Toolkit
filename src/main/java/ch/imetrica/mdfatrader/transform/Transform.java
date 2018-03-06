package ch.imetrica.mdfatrader.transform;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

import ch.imetrica.mdfatrader.series.TimeSeries;
import ch.imetrica.mdfatrader.series.TimeSeriesEntry;


/**
 * 
 * The basic class for transforming
 * raw nonstationary (financial) time-series
 * data into a stationary (log-transformed)
 * time series for real-time filtering that still 
 * retains memory.
 * 
 * The series can be fractionally differenced with order d < 0 
 * as opposed to order 1 log-differenced so as to (ideally) 
 * render stationary for the purpose of computing the MDF
 * coefficients 
 * 
 */

public class Transform {

	double d;          /* Fractional difference (1 - B)^d */
	double[] f_weights;  /* Fractional differencing weights */	
	private boolean logTransform;
	public double weight_threshold = .0001;
	
	private double baseTransform(double v) {
		
		return (logTransform) ? Math.log(v) : v;
	}
	
	/**
     * Creates a transformation protocol with difference d
     * and sets log-transformation. The difference weights
     * are computed and stored
     * 
     * @param d
     *            The fractional differencing value 0 <= d <= 1
     *            
     * @param log
     * 			  Perform log transform on raw data
     * 
     */
	
	public Transform(double d, boolean log) {
		
		logTransform = log;
		this.d = d;
		
		computeFractionalDifferenceWeights(weight_threshold);		
	}
		
	/**
     * Computes the fractional difference weights for a given value
     * 0 < d <= 1. The threshold determines when to truncate the weight. 
     * 1e-4 is typically a 'good enough' truncation value. 
     * 
     * @param thesh
     *            Threshold for determining truncation
     * 
     */
	private void computeFractionalDifferenceWeights(double thresh) {
		
	  if(d > 0) {
		  
		double wk = 1.0;
		double wk1;
		double k = 1.0;
		boolean overThresh = true;	
		
		Double[] frac_w;
		ArrayList<Double> myWs = new ArrayList<Double>();
		myWs.add(new Double(wk));
		
		while(overThresh) {
			
			wk1 = -wk * (d - k + 1.0)/k;
			
			myWs.add(new Double(wk1));
			wk = wk1;
			k = k + 1.0;
			
			if(Math.abs(wk) < thresh) {
				overThresh = false;
			}			
		}
		
		frac_w = myWs.toArray(new Double[myWs.size()]);
		f_weights = ArrayUtils.toPrimitive(frac_w);
		
	  }
		
	}

	
	/**
     * Applies the transform to a given raw TimeSeries<Double> object and returns
     * the transformed data. The beginning of the time series will contain less 
     * precise differenced data due to truncation used in the weight filtering. 
     * In the case d = 0 or d = 1, no differencing or classic (log)-differencing 
     * is applied.
     * 
     * @param anyseries
     *            Any TimeSeries<Double> raw nonstationary time series 
     * 
     * @return TimeSeries<double[]>
     * 			  A TimeSeries object with double[] values where the first index 
     *            contains the transformed data, and the second index contains the 
     *            original raw time series data, which is used as reference 
     *            to compute future transformed values
     *            
     */
	
	public TimeSeries<double[]> applyTransform(TimeSeries<Double> anyseries) {
		
		TimeSeries<double[]> transformedSeries = new TimeSeries<double[]>();
	
		if(d < 1 && d > 0) {
		
			double sum = 0;
			int filter_length = 0;
			int w_length = f_weights.length;
			
			for(int N = 1; N < anyseries.size(); N++) {
			
				filter_length = Math.min(N+1, w_length);	
				sum = 0;
				for (int l = 0; l < filter_length; l++) {
					sum = sum + f_weights[l]*baseTransform(anyseries.get(N - l).getValue());
				}
				
				double[] values = new double[]{sum, anyseries.get(N).getValue()};
				transformedSeries.add(new TimeSeriesEntry<double[]>(anyseries.get(N).getDateTime(), values));		
			}		
			
		}
		else if(d == 1) {
			
			for(int N = 1; N < anyseries.size(); N++) {
				
				double val = baseTransform(anyseries.get(N).getValue()) - 
						baseTransform(anyseries.get(N-1).getValue());
				
				double[] values = new double[]{val, anyseries.get(N).getValue()};				
				transformedSeries.add(new TimeSeriesEntry<double[]>(anyseries.get(N).getDateTime(), values));	
				
			}			
		}
		else {
			
			for(int N = 0; N < anyseries.size(); N++) {
				double val = baseTransform(anyseries.get(N).getValue());
				double[] values = new double[]{val, anyseries.get(N).getValue()};				
				transformedSeries.add(new TimeSeriesEntry<double[]>(anyseries.get(N).getDateTime(), values));	
				
			}					
		}
		
		return transformedSeries;	
	}

	/**
     * Adds a new raw and transformed data to the referenced TimeSeries. The 
     * new value in index 0 will be the new transformed data at the given datetime
     * stamp
     * 
     * @param timeSeries
     *            The historical TimeSeries<double[]> transformed and raw 
     *            nonstationary time series. The new transformed value and raw value 
     *            are added to this time series
     *            
     * @param val
     *           The new raw time series value
     *           
     * @param data
     *           The datetime stamp
     *            
     */
	
	public void addValue(TimeSeries<double[]> timeSeries, double val, String date) {
		
		
		timeSeries.add(new TimeSeriesEntry<double[]>(date, new double[]{0, val}));
		
		int N = timeSeries.size();
		
		if(d < 1 && d > 0) {
			
			double sum = 0;
			int w_length = f_weights.length;
			int filter_length = Math.min(N, w_length);
			
		    sum = 0;
			for (int l = 0; l < filter_length; l++) {
					sum = sum + f_weights[l]*baseTransform(timeSeries.get(N - l - 1).getValue()[1]);
			}			
			timeSeries.get(N-1).getValue()[0] = sum;				
		}
		else if(d == 1) {
			

				double v = baseTransform(timeSeries.get(N - 1).getValue()[1]) - 
						baseTransform(timeSeries.get(N - 2).getValue()[1]);
				
				timeSeries.get(N-1).getValue()[0] = v;			
		}
		else {
			
			double v = baseTransform(timeSeries.get(N - 1).getValue()[1]);			
			timeSeries.get(N-1).getValue()[0] = v;
									
		}				
	}

	/**
     * Adds a new raw and transformed data to the referenced TimeSeries. The 
     * new value in index 0 will be the new transformed data at the given datetime
     * stamp
     * 
     * @param anyseries
     *            The historical TimeSeries<Double> we wish to apply transform on
     *            
     *           
     * @return TimeSeries<Double>
     *           The (log)transformed time series
     *            
     */
	public TimeSeries<Double> applyLogTransform(TimeSeries<Double> anyseries) {
		
		TimeSeries<Double> transformedSeries = new TimeSeries<Double>();
		for(int N = 0; N < anyseries.size(); N++) {
			
			double val = baseTransform(anyseries.get(N).getValue());			
			transformedSeries.add(new TimeSeriesEntry<Double>(anyseries.get(N).getDateTime(), val));	
			
		}
		return transformedSeries;
	}
	
	/**
     * Outputs the weights in String format for testing
     *            
     *           
     * @return String weights
     *           The weights used for differencing
     *            
     */
	public String toString() {
		
		String w = "\n Weights for d = " + d + "\n";
		
		if(d == 0) {return (w += "\n w_0 = 1");}
		
		for(int i = 0; i < f_weights.length; i++) {
			w += f_weights[i] + "\n";
		}
		return w;
	}

	
	/**
     * Adds a new Price value to the given time series using the 
     * defined (log) transform. No differencing is made since this 
     * is uniquely for Price time series
     * 
     * @param timeSeries
     *            The historical TimeSeries<Double> already transformed
     *                      
     * @param val
     *           The new raw Price value
     *           
     * @param data
     *           The datetime stamp
     *            
     */
	public void addPrice(TimeSeries<Double> timeSeries, double val, String date) {
		timeSeries.add(new TimeSeriesEntry<Double>(date, baseTransform(val)));
	}
		
}
