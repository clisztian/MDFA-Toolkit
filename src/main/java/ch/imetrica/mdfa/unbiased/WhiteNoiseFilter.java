package ch.imetrica.mdfa.unbiased;

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
	
	
	
//	private void computeLagFilterCoefficients() {
//		
//		for(int k=0;k < L_filter; k++) {
//			
//			double sum=0.0; 
//			double sumi = 0.0;
//			
//			for(int n=0;n<=K;n++) {
//				sum = sum + Gamma[n]*Math.cos(-lag*Math.PI*n/K)*Math.cos(Math.PI*n*k/K);
//				sumi = sumi + Gamma[n]*Math.sin(-lag*Math.PI*n/K)*Math.sin(Math.PI*n*k/K);
//			}     
//			if(k==0) {h0b0[0] = sum*sum;}
//			else
//			{h0b0[k] = 2.0*sum;} //(sum*sum + sumi*sumi);} 
//			sum2 = sum2 + h0b0[k];
//		}
//		for(i=0;i<L;i++)
//		{h0b0[i] = h0b0[i]/(sum2-h0b0[0]/2.0);}    
//     
//		for(i=0;i<L;i++)
//		{
//			System.out.println("0.0 " + h0b0[i] + " 0.0 0.0");    
//		}
//	}
	
}
