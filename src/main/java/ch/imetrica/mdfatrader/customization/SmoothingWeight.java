package ch.imetrica.mdfatrader.customization;

import org.apache.commons.math3.complex.Complex;

import ch.imetrica.mdfatrading.mdfa.MDFABase;

public class SmoothingWeight {

	/**
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
	

	double[] smoothingWeight;

	public SmoothingWeight(MDFABase anyMDFA) {
		 
		int K = (int)(anyMDFA.getSeriesLength()/2);
		this.smoothingWeight = new double[K+1];
		
		computeSmoothingWeight(anyMDFA);
	}
	
	
	
	public void computeSmoothingWeight(MDFABase anyMDFA) {
		
		int K = smoothingWeight.length-1;
		double cutoff = anyMDFA.getLowPassCutoff();
		double alpha = anyMDFA.getAlpha();
		
	    int omega_Gamma = (int)(cutoff*K/Math.PI);
		
		for(int i = 0; i <= K; i++) {
			
			if(i <= omega_Gamma) {
				smoothingWeight[i] = 1.0;
			}
			else {
				smoothingWeight[i] = Math.pow((i-omega_Gamma)*Math.PI/K + 1.0, alpha/10.0);
			}
		}
	}
	
	public void updateSmoothingWeight(MDFABase anyMDFA) {
		
		int K = (int)(anyMDFA.getSeriesLength()/2);
		this.smoothingWeight = new double[K+1];
		double cutoff = anyMDFA.getLowPassCutoff();
		double alpha = anyMDFA.getAlpha();
		
	    int omega_Gamma = (int)(cutoff*K/Math.PI);
		
		for(int i = 0; i <= K; i++) {
			
			if(i <= omega_Gamma) {
				smoothingWeight[i] = 1.0;
			}
			else {
				smoothingWeight[i] = Math.pow((i-omega_Gamma)*Math.PI/K + 1.0, alpha/10.0);
			}
		}		
	}
	
	public Complex getComplex(int j, int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public double getWeight(int k) {

		return smoothingWeight[k];
	} 
	
	
}
