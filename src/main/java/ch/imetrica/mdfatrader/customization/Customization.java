package ch.imetrica.mdfatrader.customization;

import org.apache.commons.math3.complex.Complex;

import ch.imetrica.mdfatrader.matrix.MdfaMatrixGPU;
import ch.imetrica.mdfatrader.targetfilter.TargetFilter;
import ch.imetrica.mdfatrading.mdfabase.MDFABase;

public class Customization {

	
	final double M_PI = Math.PI; 
	double lambda; 
	
	SmoothingWeight freqSmoother;
	TargetFilter gamma;
 
	MdfaMatrixGPU REX;
	MdfaMatrixGPU IMX;
	
	
	public Customization(double lambda) {
		this.lambda = Math.abs(lambda);
	}
	
	public void setSmoothWeight(SmoothingWeight freqSmoother) {
		this.freqSmoother = freqSmoother;
	}
	
	public void setTargetFilter(TargetFilter gamma) {
		this.gamma = gamma;
	}
	
	
	void initiateCustomization(MDFABase anyMDFA) {
		
		int L       = anyMDFA.getFilterLength();
		int nseries = anyMDFA.getNSeries();		
		int N = anyMDFA.getSeriesLength();
		int K = (int)Math.ceil(N/2.0);
		int K1 = K+1;
		
		REX = new MdfaMatrixGPU(K1, nseries*L);
		IMX = new MdfaMatrixGPU(K1, nseries*L);
	}
	
	
	void computeCustomizationMatrix(MDFABase anyMDFA) {
	
		
		int L       = anyMDFA.getFilterLength();
		int nseries = anyMDFA.getNSeries();
		double lag  = anyMDFA.getLag();
		
		int N = anyMDFA.getSeriesLength();
		int K = (int)Math.ceil(N/2.0);
		int K1 = K+1;
		
		for(int i = 0; i < nseries; i++) {
	
	      for(int l = 1; l <= L; l++) {      
	         
	    	  for(int j = 0; j < K1; j++) {
	    		  
	    		  double lambdaWeight = Math.sqrt(1.0 + gamma.getValue(j)*lambda);	    		  
	    		  Complex phi = new Complex(0, (l-1-lag)*M_PI*j/K);	    		  
	    		  Complex base = phi.exp().multiply(freqSmoother.getComplex(j,i));
	    		  
	    		  REX.mdfaMatrixSet(j, L*i +(l-1), base.getReal());  
	    		  IMX.mdfaMatrixSet(j, L*i +(l-1), lambdaWeight*base.getImaginary()); 
	        
	          }
	      }
	    }
		
	
	}
	
}
