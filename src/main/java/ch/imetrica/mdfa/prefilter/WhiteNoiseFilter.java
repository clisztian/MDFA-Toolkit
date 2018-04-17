package ch.imetrica.mdfa.prefilter;

public class WhiteNoiseFilter {

	
	/*
	 * An Filter that is a perfect 
	 * cutoff of a symmetric target filter.
	 * If time series is purely white noise, 
	 * this is the best possible filter that can be used
	 * 
	 */
	
	double bandpassCutoff = 0;
	double freqCutoff; /*frequency cutoff of the filter */
	double lag;        /*lag of the filter for smoothing/forecasting */
	int L_filter;      /*length of the filter */
	double[] wn_filter; /*filter coefficients */
	private double[] Gamma;
	
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
	
    public WhiteNoiseFilter(double bandpass, double freqCutoff, double lag, int L_filter) {
		
    	this.bandpassCutoff = bandpass;
		this.freqCutoff = freqCutoff;
		this.lag = lag;
		this.L_filter = L_filter;
		
		computeFilterCoefficients();
	}
	
    public WhiteNoiseFilter(double[] Gamma, double lag, int L_filter) {
		
        this.Gamma = Gamma; 
		this.lag = lag;
		this.L_filter = L_filter;
		
		computeLagFilterCoefficients();
	}
    
	private void computeFilterCoefficients() {
		

	    double sum;
	    wn_filter = new double[L_filter]; 
	    
	    if(bandpassCutoff == 0) {
	    	
		    wn_filter[0] = freqCutoff/Math.PI; 	    
		    sum = wn_filter[0]; 
		    for(int i = 1; i < L_filter; i++) {
		    	
		    	wn_filter[i] = (1.0/Math.PI)*Math.sin(freqCutoff*i)/i; 
		    	sum = sum + wn_filter[i];
		    }
		    for(int l=0; l < L_filter; l++) {
				wn_filter[l] = wn_filter[l]/(sum-wn_filter[0]/2.0);
			}
	    }
	    else {
	    	
	    	double coeff0 = (freqCutoff - bandpassCutoff)/Math.PI; 
	    	wn_filter[0] = coeff0;  
	    	
		    for(int i=1; i < L_filter; i++) {
		    	wn_filter[i] = (Math.sin(freqCutoff*i) - Math.sin(bandpassCutoff*i))/(Math.PI*i);
		    }

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
	
	
	
	private void computeLagFilterCoefficients() {
		
		double sum2=0;
		int K = Gamma.length;
		wn_filter = new double[L_filter]; 
		
		for(int l=0;l < L_filter; l++) {
			
			double sum = 0.0; 
			double sumi = 0.0;
			
			for(int n=0; n < K; n++) {
				sum = sum + Gamma[n]*Math.cos(-lag*Math.PI*n/K)*Math.cos(Math.PI*n*l/K);
				sumi = sumi + Gamma[n]*Math.sin(-lag*Math.PI*n/K)*Math.sin(Math.PI*n*l/K);
			}     
			if(l==0) wn_filter[0] = sum/2;
			else wn_filter[l] = sum; //(sum*sum + sumi*sumi);} 
			sum2 = sum2 + wn_filter[l];
		} 
	}
	
}
