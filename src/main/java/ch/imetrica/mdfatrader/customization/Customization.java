package ch.imetrica.mdfatrader.customization;

import org.apache.commons.math3.complex.Complex;

import ch.imetrica.mdfatrader.matrix.MdfaMatrix;
import ch.imetrica.mdfatrader.spectraldensity.SpectralBase;
import ch.imetrica.mdfatrader.targetfilter.TargetFilter;
import ch.imetrica.mdfatrading.mdfa.MDFABase;


/**
 * 
 * The Customization control center which computes the 
 * Grammian matrices of the complex exponential basis 
 * functions and incorporates the effects of the filter speed
 * parameter lambda and the frequency smoother. 
 * 
 * A customization also needs the most recent information on
 * the in-sample time series given by the spectral information 
 * in the SpectralBase object. The target filter is also needed 
 * to complete the Customization object
 * 
 * The customization is automatically computed once the 
 * Spectral information of the time series has been given 
 * and agrees with the number of series in MDFABase
 * 
 * @author lisztian
 *
 */
public class Customization {

	
	final double M_PI = Math.PI; 

	private MDFABase anyMDFA;
	private SpectralBase anySpectralDensity;
	private SmoothingWeight freqSmoother;
	private TargetFilter gamma;

	private MdfaMatrix REX;
	private MdfaMatrix IMX;
	private MdfaMatrix rh_gamma;
	
	/**
	 * Initiate customization with anyMDFA MDFABase object
	 * 
	 * @param anyMDFA
	 *    Customization based on anyMDFA
	 */
	public Customization(MDFABase anyMDFA) {
		
		this.anyMDFA = anyMDFA;
		initiateCustomization();
	}
	
	public Customization(MDFABase anyMDFA, 
            SmoothingWeight freqSmoother,
            TargetFilter gamma) {
		
		this.anyMDFA = anyMDFA;
		this.freqSmoother = freqSmoother;
		this.gamma = gamma;
		initiateCustomization();
		
	}
	
	/**
	 *
	 * Initiate customization with anyMDFA MDFABase object
	 * along with the frequency smoother, gamma, and a
	 * list of spectral density estimates
	 * 
	 * @param anyMDFA
	 * @param freqSmoother
	 * @param gamma
	 * @param anySpectralDensity
	 * @throws Exception 
	 */
	public Customization(MDFABase anyMDFA, 
			             SmoothingWeight freqSmoother,
			             TargetFilter gamma, 
			             SpectralBase anySpectralDensity) throws Exception {
		
		this.anyMDFA = anyMDFA;
		this.freqSmoother = freqSmoother;
		this.gamma = gamma;
		this.anySpectralDensity = anySpectralDensity;
		initiateCustomization();
		computeWeightFunction();
	}
	
	/**
	 * Set the updated Smoothing weight on frequency domain
	 * 
	 * @param freqSmoother
	 *        The updated frequency smoother
	 */
	public void setSmoothWeight(SmoothingWeight freqSmoother) {
		this.freqSmoother = freqSmoother;
	}
	
	/**
	 * 
	 * Set the target filter
	 * 
	 * @param gamma
	 *       The updated target filter
	 */
	public void setTargetFilter(TargetFilter gamma) {
		this.gamma = gamma;
	}
	
	/**
	 * 
	 * Set the updated spectral information from the 
	 * time series in-sample. 
	 * 
	 * @param anySpectralDensity
	 *     object containing a list of spectral densities
	 * @throws Exception 
	 */
	public void setSpectralBase(SpectralBase anySpectralDensity) throws Exception {
		
		this.anySpectralDensity = anySpectralDensity;
		initiateCustomization();
		computeWeightFunction();
		
	}
	
	private void initiateCustomization() {
		
		int L       = anyMDFA.getFilterLength();
		int nseries = anyMDFA.getNSeries();		
		int N = anyMDFA.getSeriesLength();
		int K = (int)(N/2.0);
		int K1 = K+1;
		
		REX = new MdfaMatrix(K1, nseries*L);
		IMX = new MdfaMatrix(K1, nseries*L);
		rh_gamma = new MdfaMatrix(K1, 1);
	}
	
	/**
	 *  Compute the basic MDFA weight function which is the 
	 *  dft or any other frequency information estimation 
	 *  of each time series times the smoothing weight function 
	 *  and any other frequency domain weights
	 * 
	 *      
	 *  @throws Exception
	 *      The number of time series in MDFABase must equal number of 
	 *      time series 
	 */
	void computeWeightFunction() throws Exception {
		
		
		if(anySpectralDensity.size() != anyMDFA.getNSeries()) {
			throw new <NonconformativeDataException>Exception("Number of time series does not equal number of DFTs. Must have " + 
		                         anyMDFA.getNSeries() + " instead of " + anySpectralDensity.size()); 
		}
		
		double lambda = anyMDFA.getLambda();
		double lag    = anyMDFA.getLag();
		int nseries   = anyMDFA.getNSeries();	
		int N         = anyMDFA.getSeriesLength();
		int L         = anyMDFA.getFilterLength();
		
		
		int K = (int)(N/2.0);
		int K1 = K+1;
		int targetIndx = 0;
		
		Complex[][] mdfaWeight = new Complex[nseries][K1];

		for(int i = 0; i < nseries; i++) {
			
			Complex[] dft = anySpectralDensity.getSpectralDensity(i);
			for(int k = 0; k < K1; k++) {				
				mdfaWeight[i][k] = dft[k].multiply(freqSmoother.getWeight(k));			
			}		
		}
		
		Complex[] args = new Complex[K1];
		for(int k = 0; k < K1; k++) {
			
			double val = gamma.getValue(k)*mdfaWeight[targetIndx][k].abs();
			rh_gamma.mdfaMatrixSet(k, 0, val); 
			args[k] = (new Complex(0, -mdfaWeight[targetIndx][k].getArgument())).exp();
		}
		
		for(int i = 0; i < nseries; i++) {
			for(int k = 0; k < K1; k++) {
				
				mdfaWeight[i][k] = mdfaWeight[i][k].multiply(args[k]);	
			}		
		}
		
		for(int i = 0; i < nseries; i++) {
			
		      for(int l = 1; l <= L; l++) {      
		         
		    	  for(int j = 0; j < K1; j++) {
		    		  
		    		  double lambdaWeight = Math.sqrt(1.0 + gamma.getValue(j)*lambda);	    		  
		    		  Complex phi = new Complex(0, (l-1.0-lag)*M_PI*j/K);	    		  
		    		  Complex base = (phi.exp()).multiply(mdfaWeight[i][j]);
		    		  
		    		  REX.mdfaMatrixSet(j, L*i +(l-1), base.getReal());  
		    		  IMX.mdfaMatrixSet(j, L*i +(l-1), lambdaWeight*base.getImaginary()); 	        
		          }
		      }
		}
	}
	
	
	public MdfaMatrix getREX() {
		return this.REX;
	}
	
	public MdfaMatrix getIMX() {
		return this.IMX;
	}
	
	public MdfaMatrix getGamma() {
		return this.rh_gamma;
	}
}
