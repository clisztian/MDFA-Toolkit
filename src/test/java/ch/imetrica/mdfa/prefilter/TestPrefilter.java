package ch.imetrica.mdfa.prefilter;

import static org.junit.Assert.*;

import java.text.DecimalFormat;

import org.junit.Test;

public class TestPrefilter {

	@Test
	public void testPrefilter() {
		
		
		DecimalFormat decimalFormat = new DecimalFormat("#.#####");
		
		int K = 200;
		double omega = Math.PI/6.0;
		double omega0 = Math.PI/10.0;
		double[] Gamma = new double[K+1];
		
		double omegak;
		for(int i = 0; i <= 200; i++) {
			
			omegak = Math.PI*i/K;
			
			if(omegak <= omega) {
				Gamma[i] = 1.0;
			}
		}
		
		double lag = 0;
		int L_filter = 50;
		WhiteNoiseFilter wn = new WhiteNoiseFilter(0, omega, 0, L_filter);
		
		double[] prefilter = wn.getFilterCoefficients();
		

		WhiteNoiseFilter wn2 = new WhiteNoiseFilter(omega0, omega, 0, L_filter);
		double[] prefilter2 = wn2.getFilterCoefficients();
		
		
	}

}
