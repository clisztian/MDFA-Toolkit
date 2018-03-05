package ch.imetrica.mdfatrader.spectraldensity;

import org.apache.commons.math3.complex.Complex;

import ch.imetrica.mdfatrader.series.TimeSeries;





public class SpectralBase {

	
	Complex[] prdx;
	
	/* 
	 * 
	 * Computes the classical DFT for a snapshot of 
	 * stationary fractionally differenced times series
	 * 
	 */
	
	 public SpectralBase(TimeSeries<Double> tseries) {
		 
		 int n = tseries.size();
		 int K = (int)n/2;
		 int K1 = K+1;
		 final double M_PI = Math.PI;
		 
		 prdx = new Complex[K1];
		 
		 double mean = 0;
		 for(int i = 0; i < tseries.size(); i++) {
			 mean += tseries.get(i).getValue();
		 }
		 mean = mean/n;
		 		 
		 prdx[0] = new Complex(mean,0);
		 Complex ab = new Complex(0,0);
		 
		 for(int j = 1; j < K1; j++) {
			 
			 ab = new Complex(0,0);
			 for(int i = 0; i < n; i++) {
				 
				 double val = tseries.get(i).getValue() - mean;				 
				 Complex z = (new Complex(0, M_PI*(i + 1.0)*j/K)).exp();				 
				 ab = ab.add(z.multiply(val)); 
			 }
			 prdx[j] = ab.divide(Math.sqrt(M_PI*n));			 
		 }		 
	 }
	
	
}
