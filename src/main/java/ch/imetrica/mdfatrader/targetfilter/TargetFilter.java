package ch.imetrica.mdfatrader.targetfilter;

import ch.imetrica.mdfatrading.mdfabase.MDFABase;

public class TargetFilter {

	
	double cutoff;
	double[] targetGamma;
	
	public TargetFilter(double cutoff) {
		this.cutoff = cutoff;
	}
	
	void computeTargetFilter(MDFABase anyMDFA) {
		
		int N = anyMDFA.getSeriesLength();
		int K = (int)Math.ceil(N/2.0);
		int K1 = K+1;
		
		targetGamma = new double[K1];
		
		
	}

	public double[] getTargetGamma() {
		return targetGamma;
	}
	
	public double getValue(int j) {
		
		return targetGamma[j];
	}
	
}
