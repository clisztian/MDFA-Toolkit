package ch.imetrica.mdfa.mdfa;

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
	private int N = 300;     
	
	/**
	 *  L length of the filter
	 */
	private int L = 20;       
	
	/**
	 *  Number of series for estimating filter 
	 *  Target + explanatory series
	 */
	private int nseries; /* number of series used for signal estimation */
	
	/**
	 *  lag Forecasting/smoothing lag
	 */
	private double lag = 0.0;			
	
	/**
	 *  Frequency low-pass cutoff
	 */
	private double lowpass_cutoff = Math.PI/10.0;


	private double alpha = 0.0;

	private double lambda = 0.0;

	private double smooth = 0.0;

	private double decayStrength = 0.0;

	private double decayStart = 0.0;

	private double crossCorr = 0.0;     
	
	private double shift_constraint = 0.0;
	
	private int i1 = 0;
	
	private int i2 = 0;

	private double omega0 = 0.0;
	
	public MDFABase() {
		
	}
	
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
	
	public MDFABase setFilterLength(int L) {
		this.L = L;
		return this;
	}

	public int getNSeries() {
		return this.nseries;
	}

	public MDFABase setNumberOfSeries(int nseries) {
		this.nseries = nseries;
		return this;
	}
	
	public double getLag() {
		return this.lag;
	}
	
	public MDFABase setLag(double lag) {
		this.lag = lag;
		return this;
	}

	public int getSeriesLength() {
		return this.N;
	}
	
	public MDFABase setSeriesLength(int N) {
		this.N = N;
		return this;
	}
	
	
	public double getLowPassCutoff() {
		return this.lowpass_cutoff;
	}


	public double getLambda() {
		return lambda;
	}


	public MDFABase setLambda(double lambda) {
		this.lambda = lambda;
		return this;
	}


	public double getSmooth() {
		return smooth;
	}


	public MDFABase setSmooth(double smooth) {
		this.smooth = smooth;
		return this;
	}


	public double getAlpha() {
		return alpha;
	}


	public MDFABase setAlpha(double alpha) {
		this.alpha = alpha;
		return this;
	}


	public double getDecayStrength() {
		return decayStrength;
	}


	public MDFABase setDecayStrength(double decayStrength) {
		this.decayStrength = decayStrength;
		return this;
	}


	public double getDecayStart() {
		return decayStart;
	}


	public MDFABase setDecayStart(double decayStart) {
		this.decayStart = decayStart;
		return this;
	}


	public double getCrossCorr() {
		return crossCorr;
	}


	public MDFABase setCrossCorr(double crossCorr) {
		this.crossCorr = crossCorr;
		return this;
	}


	public int getI2() {
		return i2;
	}


	public MDFABase setI2(int i2) {
		this.i2 = i2;
		return this;
	}


	public int getI1() {
		return i1;
	}


	public MDFABase setI1(int i1) {
		this.i1 = i1;
		return this;
	}


	public double getShift_constraint() {
		return shift_constraint;
	}


	public MDFABase setShift_constraint(double shift_constraint) {
		this.shift_constraint = shift_constraint;
		return this;
	}
	
	public MDFABase setLowpassCutoff(double cutoff) {
		
		if(cutoff > this.omega0) {
			this.lowpass_cutoff = cutoff;
		}
		return this;
	}

	public double getBandPassCutoff() {
		return this.omega0 ;
	}
	
	public MDFABase setBandPassCutoff(double omega) {
		
		if(omega < this.lowpass_cutoff) {
			this.omega0 = omega;
		}
		return this;	
	}
}
