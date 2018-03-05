package ch.imetrica.mdfatrader.customization;

import org.apache.commons.math3.complex.Complex;

public class SmoothingWeight {

	/*
	 * Class for generating frequency domain smoother of spectral density
	 * 
	 * Input N - length of in-sample time-series
	 * 
	 * Choices are 1_ Classical ExpWeight
	 * 			   2_ Cutoff weight 
	 *             3_ ARMA Model 
	 *             4_ ZPC filter
	 *             5_ ... 
	 * 
	 * 
	 */
	
	double alpha;

	public Complex getComplex(int j, int i) {
		// TODO Auto-generated method stub
		return null;
	} 
	
	
}
