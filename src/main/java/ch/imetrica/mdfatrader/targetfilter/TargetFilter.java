package ch.imetrica.mdfatrader.targetfilter;

import ch.imetrica.mdfatrading.mdfa.MDFABase;

/**
 * 
 * The Target filter class constructs and holds a 
 * real double array that is the target filter defined 
 * on the frequency domain. The MDFA seeks to find filter
 * coefficients the minimized the error of its frequency 
 * response function and the target (symmetric) filter.
 *  
 * The simplest target filter is defined by a pass-band 
 * with a defined cutoff frequency. All values from 0 to frequency 
 * omega0 cutoff are 1 and then rest are zero. 
 * 
 * Other more exotic target filters are certainly possible as well, such as
 * 
 * -Band-pass defined by two frequencies such that lower frequencies 
 * less than omega0 and greater than omega1 are set to zero. 
 * 
 * -Any positive function f on [0, pi] that is bounded by 1. 
 * 
 * @author lisztian
 *
 */
public class TargetFilter {

	
	private double cutoff;
	private double[] targetGamma;
	
	/**
	 * Retrieves the frequency cutoff for the pass-band 
	 * (default) target filter and the 
	 * 
	 * @param anyMDFA
	 *     The global anyMDFA object with given cutoff 
	 *     and resolution in the frequency domain
	 */
	
	public TargetFilter(MDFABase anyMDFA) {
		
		this.cutoff = anyMDFA.getLowPassCutoff();
		computeTargetFilter(anyMDFA);
	}
	
	void computeTargetFilter(MDFABase anyMDFA) {
		
		int N = anyMDFA.getSeriesLength();
		int K = (int)Math.ceil(N/2.0);
		int K1 = K+1;
		
		targetGamma = new double[K1];
				
	    int omega_Gamma = (int)(cutoff*K/Math.PI);
		
		for(int i = 0; i <= K; i++) {
			
			if(i <= omega_Gamma) {
				targetGamma[i] = 1.0;
			}
		}		
	}

	/**
	 * Returns or sets the entire target filter
	 * 
	 * @return targetGamma
	 *   
	 */
	public double[] getTargetGamma() {
		return targetGamma;
	}
	
	/**
	 * Returns a single value of the target filter
	 * 
	 * @param j
	 *    The index
	 * @return
	 *    The values of the target filter at j
	 */
	public double getValue(int j) {
		
		return targetGamma[j];
	}


	
}
