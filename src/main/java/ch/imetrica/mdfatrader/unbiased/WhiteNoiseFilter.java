package ch.imetrica.mdfatrader.unbiased;

public class WhiteNoiseFilter {

	
	/*
	 * An Filter that is a perfect 
	 * cutoff of a symmetric target filter.
	 * If time series is purely white noise, 
	 * this is the best possible filter that can be used
	 * 
	 */
	
	
	double freqCutoff; /*frequency cutoff of the filter */
	double lag;        /*lag of the filter for smoothing/forecasting */
	int L_filter;      /*length of the filter */
	double[] wn_filter; /*filter coefficients */
	
	/**
     * Sets a TimeSeries object to this MdfaSeries.
     * TimeSeries data is raw series, with no transformations
     * applied
     * 
     * @param freqCutoff
     *            The frequency cutoff of the pass-band filter
     *            
     * @param lag
     * 			  The forecast/smoothing operator
     * 
     * @param L_filter
     * 			  The length of the filter
     */
	public WhiteNoiseFilter(double freqCutoff, double lag, int L_filter) {
		
		this.freqCutoff = freqCutoff;
		this.lag = lag;
		this.L_filter = L_filter;
		
		computeFilterCoefficients();
	
	}
	
	private void computeFilterCoefficients() {
		

	    double sum;
	    wn_filter = new double[L_filter]; 
	    
	    wn_filter[0] = freqCutoff/Math.PI; 
	    
	    sum = wn_filter[0]; 
	    for(int i = 1; i < L_filter; i++) {
	    	
	    	wn_filter[i] = (1.0/Math.PI)*Math.sin(freqCutoff*i)/i; 
	    	sum = sum + wn_filter[i];
	    }
	    
	}
	
	/**
     * The L_filter white noise filter with cutoff freqCutoff
     * 
     * @return wn_filter
     */
	public double[] getFilterCoefficients() {
		return wn_filter;
	}
	
}
