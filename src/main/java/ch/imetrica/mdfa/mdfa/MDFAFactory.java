package ch.imetrica.mdfa.mdfa;

import ch.imetrica.mdfa.customization.Customization;
import ch.imetrica.mdfa.customization.SmoothingWeight;
import ch.imetrica.mdfa.regularization.Regularization;
import ch.imetrica.mdfa.targetfilter.TargetFilter;

/**
 * 
 * Constructs the components of an MDFA 
 * customization and regularization given the set 
 * of parameters defined in an MDFABase object. 
 *  
 * Provides an interface to all of the parameters 
 * so when changes to any MDFA parameters are made 
 * the dependent components are recomputed right 
 * away
 * 
 * @author lisztian
 *
 */

public class MDFAFactory {

	
	private MDFABase anyMDFA;
	private SmoothingWeight anyWeight;
	private TargetFilter anyTarget;	
	private Regularization anyReg;
	private Customization anyCustom;	
	
	/**
	 * 
	 * An MDFAFactory object holds all the parameter
	 * components of the filter coefficient 
	 * estimation process. 
	 * 
	 * @param anyMDFA
	 */
	public MDFAFactory(MDFABase anyMDFA) {
		
		this.anyMDFA = anyMDFA;
		anyWeight = new SmoothingWeight(anyMDFA);
		anyTarget = new TargetFilter(anyMDFA);	
		anyReg = new Regularization(anyMDFA);
		anyCustom = new Customization(anyMDFA, anyWeight, anyTarget);
	}
	
	
	/**
	 * 
	 * Sets the in-sample series length for computing 
	 * the MDFA coefficients. In effect, the frequency smoothing
	 * weights, target filter, and dimensions in the customization
	 * matrix will be updated as well. 
	 * 
	 * @param N
	 *   The number of time series observations for computing 
	 *   the filter in-sample. Min value is 10 and Max value is
	 *   1000
	 */
	public void setSeriesLength(int N) {
		
		if(N < 10) {
			N = 10;
		}
		if(N > 1000) {
			N = 1000;
		}
		
		anyMDFA.setSeriesLength(N);		
		anyWeight = new SmoothingWeight(anyMDFA);
		anyTarget = new TargetFilter(anyMDFA);	
		anyCustom = new Customization(anyMDFA, anyWeight, anyTarget);		
	}
	
	/**
	 * 
	 * This sets the low-pass cutoff of the filter coefficients.
	 * The smoothing weight, target filter, and customization matrix
	 * will be updated as well.
	 * 
	 * @param cutoff
	 *    A double value between [0, pi) 
	 * @throws Exception
	 */
	public void setLowpassCutoff(double cutoff) throws Exception {
		
		if(cutoff > Math.PI) {
			cutoff = Math.PI;
		}
		if(cutoff < .001) {
			cutoff = .001;
		}
		
		anyMDFA.setLowpassCutoff(cutoff);
		anyWeight.computeSmoothingWeight(anyMDFA);
		anyTarget.adjustTargetFilter(anyMDFA);
		anyCustom.setSmoothWeight(anyWeight);
		anyCustom.setTargetFilter(anyTarget);
		anyCustom.setMDFABase(anyMDFA);
	}
	
	/**
	 * 
	 * Sets the turning-point parameter and recomputes
	 * the customization matrix 
	 * 
	 * @param lambda
	 *   lambda must be 0 or greater. 
	 * @throws Exception
	 */
	public void setLambda(double lambda) throws Exception {
	    if(lambda < 0) {
	    	lambda = 0;
	    }
		
		anyMDFA.setLambda(lambda);
		anyCustom.setMDFABase(anyMDFA);
	}

	/**
	 * 
	 * Sets the default smoothing parameter for the frequency
	 * domain smoothing operator. If the value is negative, 
	 * the smoothing function will be the step function. If positive
	 * will be the exponential function
	 * 
	 * @param alpha
	 *   A real value that determines strength of the smoothing 
	 *   in the stop-band of the frequency domain.
	 * @throws Exception
	 */
	public void setAlpha(double alpha) throws Exception {
		
		anyMDFA.setAlpha(alpha);
		anyWeight.computeSmoothingWeight(anyMDFA);
		anyCustom.setSmoothWeight(anyWeight);
		anyCustom.setMDFABase(anyMDFA);
	}

	/**
	 * 
	 * Sets the smooth regularization parameter. Will constrain 
	 * the MDFA coefficients to be smooth in the sense that lagged
	 * differences in the coefficients are bounded by a constant dependent
	 * on this parameter. The regularization matrix will automatically 
	 * be recomputed. 
	 * 
	 * @param smooth
	 *   A real number greater or equal to zero
	 */
	public void setSmoothRegularization(double smooth) {
		
		if(smooth < 0) {
			smooth = 0;
		}
		
		anyMDFA.setSmooth(smooth);
		anyReg.setMDFABase(anyMDFA);
		anyReg.adjustRegularizationMatrices();
	}
	
	/**
	 * 
	 * A real-number greater or equal to zero. The effect of this decay parameter
	 * will be that the coefficients decay at a rate given by this parameter. 
	 * The higher the value, the faster the decay to 0 of the coefficients.
	 * 
	 * @param decayStrength
	 *   A real number greater or equal to zero
	 */
	public void setDecayStrengthRegularization(double decayStrength) {
		
		if(decayStrength < 0) {
			decayStrength = 0;
		}
		
		anyMDFA.setDecayStrength(decayStrength);
		anyReg.setMDFABase(anyMDFA);
		anyReg.adjustRegularizationMatrices();
	}
	
	/**
	 * 
	 * A real-number greater or equal to zero. The effect of this decay parameter 
	 * will be how soon the coefficients begin to decay. The smaller the value
	 * the quicker the decay will begin.
	 * 
	 * 
	 * @param decayStart
	 *   A real number greater or equal to zero
	 */
	public void setDecayStartRegularization(double decayStart) {
		
		anyMDFA.setDecayStart(decayStart);
		anyReg.setMDFABase(anyMDFA);
		anyReg.adjustRegularizationMatrices();
	}


	public void setCrossRegularization(double crossCorr) {
		
		anyMDFA.setCrossCorr(crossCorr);
		anyReg.setMDFABase(anyMDFA);
		anyReg.adjustRegularizationMatrices();
	}

	/**
	 * 
	 * Sets the filter length of the MDFA coefficients. Must be greater than 2.
	 * If less than 2, will be automatically set to 2. The customization matrix
	 * will be recomputed with new filter length L.
	 * 
	 * @param L
	 *   An integer greater or equal to L and less than time series length
	 * @throws Exception
	 */
	public void setFilterLength(int L) throws Exception {
		
		if(L < 2) {
			L = 2;
		}
		if(L > anyMDFA.getSeriesLength()-10) {
			L = anyMDFA.getSeriesLength()-10;
		}
		
		anyMDFA.setFilterLength(L);
		anyCustom.adjustFilterLength(anyMDFA);
		anyReg = new Regularization(anyMDFA);
	}

	/**
	 * 
	 * Sets the lag value and recomputes the customization and
	 * regularization matrices. No dimension changes are needed.  
	 * For negative values of lag, a forecast is made of the signal.
	 * For positive values of lag, a smoothing of the signal is made.
	 * 
	 * @param lag
	 *   A real number
	 * @throws Exception
	 */
    public void setLag(double lag) throws Exception { 	
    	
    	anyMDFA.setLag(lag);
    	anyCustom.setMDFABase(anyMDFA);
    	anyReg.setMDFABase(anyMDFA);
		anyReg.adjustRegularizationMatrices();
    }

	/**
	 * 
	 * Sets the i2 constraint which constrains the coefficients
	 * to satisfy the condition that the derivative at zero of the 
	 * frequency response function of the filter coefficients is equal 
	 * to the @shift_constraint (zero by default). This value can be any 
	 * integer. When 0, it is false, otherwise true. The regularization 
	 * matrix dimensions change, and thus the regularization object is 
	 * recomputed.
	 * 
	 * @param i2
	 *   When i2 == 0, false. Otherwise true
	 * 
	 */
	public void setI2(int i2) {
		anyMDFA.setI2(i2);
		anyReg = new Regularization(anyMDFA);
	}

	/**
	 * 
	 * Sets the i1 constraint, which constrains the coefficients 
	 * to satisfy the condition that the value of the frequency 
	 * response function of the filter coefficients is equal to one
	 * at zero. In otherwords, the sum of the coefficients will be equal 
	 * zero. 
	 * 
	 * @param i1
	 *   When i1 == 0, false. Otherwise true.
	 */
	public void setI1(int i1) {
		anyMDFA.setI1(i1);
		anyReg = new Regularization(anyMDFA);
	}
	
	/**
	 * 
	 * Sets the shift constraint so that if the i2 constraint is set to true, 
	 * the value of the derivative of the frequency response function 
	 * at zero will be this value. The default value is zero. For negative 
	 * values, anticipative filter can be constructed.   
	 * 
	 * @param shift_constraint
	 *   Any real value
	 */
	public void setShift_constraint(double shift_constraint) {
		anyMDFA.setShift_constraint(shift_constraint);
		anyReg = new Regularization(anyMDFA);
	}
	
	
	public SmoothingWeight getSmoothingWeight() {
		return anyWeight;
	}
	
	public TargetFilter getTargetFilter() {
		return anyTarget;
	}
	
	public Customization getCustomization() {
		return anyCustom;
	}
	
	public Regularization getRegularization() {
		return anyReg;
	}
	
	
	public final int getFilterLength() {
		return anyMDFA.getFilterLength();
	}

	public final int getNSeries() {
		return anyMDFA.getNSeries();
	}

	public final double getLag() {
		return anyMDFA.getLag();
	}

	public final int getSeriesLength() {
		return anyMDFA.getSeriesLength();
	}
	
	public final double getLowPassCutoff() {
		return anyMDFA.getLowPassCutoff();
	}

	public final double getLambda() {
		return anyMDFA.getLambda();
	}

	public final double getSmooth() {
		return anyMDFA.getSmooth();
	}
	
	public final double getAlpha() {
		return anyMDFA.getAlpha();
	}
	
	public final double getDecayStrength() {
		return anyMDFA.getDecayStrength();
	}
		
	public final double getDecayStart() {
		return anyMDFA.getDecayStart();
	}
	
	public final double getCrossCorr() {
		return anyMDFA.getCrossCorr();
	}
	
	public final int getI1() {
		return anyMDFA.getI1();
	}
	
	public final int getI2() {
		return anyMDFA.getI2();
	}
	
	public final double getShift_constraint() {
		return anyMDFA.getShift_constraint();
	}


	public void setCustomization(Customization anyCustom2) {
		this.anyCustom = anyCustom2;
	}
	
}
