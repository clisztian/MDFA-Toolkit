package ch.imetrica.mdfatrading.mdfa;

public class MDFABase {

	
	/**
	 * 
	 * The common shared MDFA object that contains the basic 
	 * Meta information of computing the in-sample MDFA 
	 * filtering process 
	 * 
	 */
	
	/**
	 *  N insample length of time series 
	 */
	private int N;     
	
	/**
	 *  L length of the filter
	 */
	private int L;       
	
	/**
	 *  Number of series for estimating filter 
	 *  Target + explanatory series
	 */
	private int nseries; /* number of series used for signal estimation */
	
	/**
	 *  lag Forecasting/smoothing lag
	 */
	private double lag;			
	
	/**
	 *  Frequency low-pass cutoff
	 */
	private double lowpass_cutoff;


	private double alpha;

	private double lambda;

	private double smooth;

	private double decayStrength;

	private double decayStart;

	private double crossCorr;     
	
	private double shift_constraint;
	
	private int i1;
	
	private int i2;
	
	public MDFABase(int N, int nseries, int L, double lag, double cutoff) {
		
		this.N = N;
		this.nseries = nseries;
		this.L = L;
		this.lag = lag; 
		this.lowpass_cutoff = cutoff;		
	}

	
	public MDFABase(int N, 
			        int nseries, 
			        int L, 
			        int i1,
			        int i2,
			        double lag, 
			        double cutoff,
			        double alpha,
			        double lambda,
			        double smooth,
			        double decayStrength,
			        double decayStart,
			        double crossCorr,
			        double shift_const)  {
		
		this.N = N;
		this.nseries = nseries;
		this.L = L;
		this.lag = lag; 
		this.lowpass_cutoff = cutoff;
		
		this.setI1(i1);
		this.setI2(i2);
		this.setAlpha(alpha);
		this.setLambda(lambda);
		this.setSmooth(smooth);
		this.setDecayStrength(decayStrength);
		this.setDecayStart(decayStart);
		this.setCrossCorr(crossCorr);
		this.setShift_constraint(shift_const);
	}
	
	
	public int getFilterLength() {
		return this.L;
	}

	public int getNSeries() {
		return this.nseries;
	}

	public double getLag() {
		return this.lag;
	}

	public int getSeriesLength() {
		return this.N;
	}
	
	public double getLowPassCutoff() {
		return this.lowpass_cutoff;
	}


	public double getLambda() {
		return lambda;
	}


	public void setLambda(double lambda) {
		this.lambda = lambda;
	}


	public double getSmooth() {
		return smooth;
	}


	public void setSmooth(double smooth) {
		this.smooth = smooth;
	}


	public double getAlpha() {
		return alpha;
	}


	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}


	public double getDecayStrength() {
		return decayStrength;
	}


	public void setDecayStrength(double decayStrength) {
		this.decayStrength = decayStrength;
	}


	public double getDecayStart() {
		return decayStart;
	}


	public void setDecayStart(double decayStart) {
		this.decayStart = decayStart;
	}


	public double getCrossCorr() {
		return crossCorr;
	}


	public void setCrossCorr(double crossCorr) {
		this.crossCorr = crossCorr;
	}


	public int getI2() {
		return i2;
	}


	public void setI2(int i2) {
		this.i2 = i2;
	}


	public int getI1() {
		return i1;
	}


	public void setI1(int i1) {
		this.i1 = i1;
	}


	public double getShift_constraint() {
		return shift_constraint;
	}


	public void setShift_constraint(double shift_constraint) {
		this.shift_constraint = shift_constraint;
	}
	
	
}
