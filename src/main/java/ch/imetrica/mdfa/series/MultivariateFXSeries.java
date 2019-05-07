package ch.imetrica.mdfa.series;

import java.util.ArrayList;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.imetrica.mdfa.market.Side;
import ch.imetrica.mdfa.matrix.MdfaMatrix;
import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.mdfa.MDFAFactory;
import ch.imetrica.mdfa.mdfa.MDFASolver;
import ch.imetrica.mdfa.plotutil.TimeSeriesPlot;
import ch.imetrica.mdfa.prefilter.WhiteNoiseFilter;
import ch.imetrica.mdfa.spectraldensity.SpectralBase;
import ch.imetrica.mdfa.util.MdfaUtil;

/**
 * 
 * A MultivariateFX time series signal extraction 
 * process.
 * 
 * We consider a multivariate time series of M (related)
 * series all observed on the same timestamps. This class
 * applies K independent MDFA solvers to the M multivariate 
 * series to produce K different signals
 * 
 * 
 * @author Christian D. Blakely (clisztian@gmail.com)
 *
 */
public class MultivariateFXSeries {

	
	
	private ArrayList<VectorSignalSeries> anySignals;  /* Multivariate Series */
	private ArrayList<MDFASolver> anySolvers;   /* All the solvers */		
	private TimeSeries<double[]> fxSignals;     /* Aggregate Signals */	
	private DateTimeFormatter formatter;
	private int targetSeriesIndex = 0;         
	private boolean prefilterAll = false;
		
	private double minValue = Double.MAX_VALUE;
	private double maxValue = -Double.MAX_VALUE;
	private double latest = 0;
	private double previous = 0;
	private double filterMultiplier = 1.0;
	/**
	 * A MultivariateFX series is instantiated with an array of 
	 * MDFABase objects, each object defining a real-time signal 
	 * specification. Each MDFABase object will instantiate a MDFASolver.
	 * 
	 * A format for the DateTime is also required
	 * 
	 * @param anyMDFAs
	 * @param anyformat
	 */
	public MultivariateFXSeries(MDFABase[] anyMDFAs, String anyformat) {
		
		this.formatter = DateTimeFormat.forPattern(anyformat);
		
		anySolvers = new ArrayList<MDFASolver>();
		for(int i = 0; i < anyMDFAs.length; i++) {
			anySolvers.add(new MDFASolver(new MDFAFactory(anyMDFAs[i])));
		}	
		
		anySignals = new ArrayList<VectorSignalSeries>();
		fxSignals = new TimeSeries<double[]>();
	}
	
	
	/**
	 * A MultivariateFX series is instantiated with an ArrayList of 
	 * MDFABase objects, each object defining a real-time signal 
	 * specification. Each MDFABase object will instantiate a MDFASolver.
	 * 
	 * A format for the DateTime is also required
	 * 
	 * @param anyMDFAs
	 * @param anyformat
	 */
	public MultivariateFXSeries(ArrayList<MDFABase> anyMDFAs, String anyformat) {
		
		this.formatter = DateTimeFormat.forPattern(anyformat);
		
		anySolvers = new ArrayList<MDFASolver>();
		for(int i = 0; i < anyMDFAs.size(); i++) {
			anySolvers.add(new MDFASolver(new MDFAFactory(anyMDFAs.get(i))));
		}	
		
		anySignals = new ArrayList<VectorSignalSeries>();
		fxSignals = new TimeSeries<double[]>();
	}
	
	
	/**
	 * Adds a new multivariate series value for the given date. 
	 * The new multivariate signal will be computed automatically 
	 * at the given date. This assumes that all coefficients are
	 * defined for each series. If they are not defined yet, a zero
	 * vector will be added to the signal at the observation time
	 * 
	 * 
	 * @param val array of newest observations at given date
	 * @param date Current date
	 * @throws Exception if the multivariate size of the new observation
	 * 		   does not equal the number of series
	 */
    public void addValue(String date, double[] val) throws Exception {
        
    	if(val.length != anySignals.size()) {
    		throw new Exception("Sizes of array and number of time series don't match");
    	}
    	

    	previous = latest;
    	double[] sigVal = new double[anySolvers.size()];
    	
    	for(int m = 0; m < anySignals.size(); m++) { 		
    		
    		anySignals.get(m).addValue(date, val[m]);
    		
    		if(anySignals.get(m).hasFilter()) {
    			sigVal = MdfaUtil.plus(sigVal, anySignals.get(m).getLatestSignalValue(), filterMultiplier);
    		}    	
		}

    	if(prefilterAll && anySignals.get(targetSeriesIndex).hasFilter()) {
    		sigVal = anySignals.get(targetSeriesIndex).getLatestSignalValue();
    	}
    	else {
	    	for(int m = 0; m < anySignals.size(); m++) { 		
	    		  			
	    		anySignals.get(m).addValue(date, val[m]);
	    		
	    		if(anySignals.get(m).hasFilter()) {
	    			sigVal = MdfaUtil.plus(sigVal, anySignals.get(m).getLatestSignalValue(), filterMultiplier);
	    		}    	
			}
    	}

    	   	
		fxSignals.add(new TimeSeriesEntry<double[]>(date, sigVal));	 
		latest = sigVal[0]; 
		

		
    }
	
    
	/**
	 * 
	 * Adds a new multivariate series value for the given date. 
	 * The new multivariate signal will be computed automatically 
	 * at the given date. This assumes that all coefficients are
	 * defined for each series. If they are not defined yet, a zero
	 * vector will be added to the signal at the observation time
	 * 
	 * 
	 * @param val arrayList of newest observations at given date
	 * @param date Current date
	 * @throws Exception if the multivariate size of the new observation
	 * 		   does not equal the number of series
	 */
    public void addValue(String date, ArrayList<Double> val) throws Exception {
        
    	if(val.size() != anySignals.size()) {
    		throw new Exception("Sizes of array and number of time series don't match");
    	}
    	
    	previous = latest;
    	double[] sigVal = new double[anySolvers.size()];
    	for(int m = 0; m < anySignals.size(); m++) { 		
    		
    		anySignals.get(m).addValue(date, val.get(m));
    		
    		if(anySignals.get(m).hasFilter()) {
    			sigVal = MdfaUtil.plus(sigVal, anySignals.get(m).getLatestSignalValue(), filterMultiplier);
    		}		
		}
		fxSignals.add(new TimeSeriesEntry<double[]>(date, sigVal));
		latest = sigVal[0]; 
		
		double value = getTargetValue(size()-1);
		if(value > maxValue) maxValue = value;
		else if(value < minValue) minValue = value;
    }
	
	
	
	/**
	 * Adds a new series to the collection of the multivariate 
	 * time series stream. Will be added if:
	 * 
	 * - The latest date corresponds to the latest date of all 
	 *   series in the collection
	 * 
	 * @param series
	 *        A time series to be added for use as an explanatory 
	 *        series that is filtered or a technical for trading
	 * @throws Exception 
	 *        
	 */
	public MultivariateFXSeries addSeries(TargetSeries series) throws Exception {
		
		boolean success = true;
		if(anySignals.size() > 0 && series.size() > 0) {
			
			String datetime = series.getLatest().getDateTime();
			
			for(int i = 0; i < anySignals.size(); i++) {
				
				if(!datetime.equals(anySignals.get(i).getLatestDate())) {
					throw new Exception("Dates do not match of the signals: " + 
				         datetime + " is not " + anySignals.get(i).getLatestDate());
				}
			}		
		}
		if(success) { 	
			
			VectorSignalSeries vecSeries = new VectorSignalSeries(series);
			anySignals.add(vecSeries);
		}
		return this;
	}
	
	
	/**
	 * 
	 * Computes all the filter coefficients for each series 
	 * Compute the spectral base for the multivariate series using 
	 * insample length of first target. This is then shared 
	 * among all solvers
	 * 
	 * 
	 * @throws Exception
	 */
	
	public void computeAllFilterCoefficients() throws Exception {
		
		for(VectorSignalSeries series : anySignals) {
			series.clearFilters();
		}
				
		for(int n = 0; n < anySolvers.size(); n++) {
			computeFilterCoefficients(n);
		}
		computeAggregateSignal();
	}	
	
	
	/**
	 * 
	 * Computes the filter coefficients for each series for the nth 
	 * MDFASolver 
	 * 
	 * A new spectral base is computed for each MDFASolver. 
	 * More generalized approach but much more memory intensive
	 * 
	 * @param n Computes the coefficients for each series for the nth solver
	 * @throws Exception
	 */
	public void computeFilterCoefficients(int n) throws Exception {
		
		if(n < anySolvers.size()) {
			
			if(prefilterAll) {
				setWhiteNoisePrefilters(n, 50);
			}
			
			MDFASolver anySolver = anySolvers.get(n);
			
			anySolver.getMDFAFactory().setNumberOfSeries(anySignals.size());
			
			SpectralBase base = new SpectralBase(anySolver.getMDFAFactory().getSeriesLength())
					                            .setTargetIndex(targetSeriesIndex);
			base.addVectorSeries(anySignals, n);
			anySolver.updateSpectralBase(base);
			
			int L = anySolver.getMDFAFactory().getFilterLength();
			MdfaMatrix bcoeffs = anySolver.solver();
			
			for(int i = 0; i < anySignals.size(); i++) {
				
				double[] sig_coeffs = bcoeffs.getSubsetColumn(0, i*L, i*L + L);
				anySignals.get(i).setMDFAFilterCoefficients(n, sig_coeffs);
			}	
		}
		else {
			throw new Exception("The Solver at index " + n + " is not defined. Only " + 
					anySolvers.size() + " are currently defined");
		}	
	}
	
	/**
	 * Computes the aggregate signal from all the series
	 * for each MDFASolver
	 * 
	 * @throws Exception
	 */
	public void computeAggregateSignal() throws Exception {
		
		fxSignals.clear();
		int N = anySignals.get(0).size();
		
		minValue = Double.MAX_VALUE;
		maxValue = -Double.MAX_VALUE;
		
		for(int i = 0; i < N; i++) {
			
			double[] val = anySignals.get(0).getSignalValue(i);
			String current = anySignals.get(0).getSignalDate(i);
			
			
			for(int m = 1; m < anySignals.size(); m++) {
				
				if(current.equals(anySignals.get(m).getSignalDate(i))) {					
					
					val = MdfaUtil.plus(val, anySignals.get(m).getSignalValue(i), filterMultiplier);
				}
				else {
					  throw new Exception("Dates do not match of the signals: " + current + " is not " + anySignals.get(m).getSignalDate(i));
				}
			}
			fxSignals.add(new TimeSeriesEntry<double[]>(current, val));	
			
			if(i > N - 300) {
				double value = getTargetValue(i);
				if(value > maxValue) maxValue = value;
				else if(value < minValue) minValue = value;	
			}
		}
	}
	
	/**
	 * Returns access to the ith MDFAFactory
	 * to adapt/change MDFA parameters for 
	 * the ith signal in this multivariate signal
	 * 
	 * @param i the ith MDFAFactory of the ith signal 
	 * @return the MDFAFactor object for the ith signal
	 */
	public MDFAFactory getMDFAFactory(int i) {
		
		if(i < 0 || i >= anySolvers.size()) {
			return null;
		}		
		return anySolvers.get(i).getMDFAFactory();
	}
	
	/**
	 * Creates unbiased white noise filters for 
	 * each series and each signal. The target 
	 * information from the MDFAsolvers will be used
	 * to compute the white noise filters. The resulting
	 * signals when applied will be the optimal filters
	 * if assuming future value returns are white noise.
	 * 
	 * After prefiltering, the MDFA routine will then 
	 * update these prefilters to create a more timely 
	 * MDFA signal. A great way to reduce number of effective 
	 * degrees of freedom from the hyperparameters
	 * 
	 * @param L Length of prefilter. Longer the better
	 */
	public void setWhiteNoisePrefilters(int L) {
					
		if(anySolvers.size() > 0) {			
			for(int m = 0; m < anySignals.size(); m++) {
				
				anySignals.get(m).clearPreFilters();
				for(int i = 0; i < anySolvers.size(); i++) {

					double[] whiteFilter = (new WhiteNoiseFilter(anySolvers.get(i).getMDFAFactory().getBandPassCutoff(),
																 anySolvers.get(i).getMDFAFactory().getLowPassCutoff(), 0, L))
			                  				.getFilterCoefficients();
					anySignals.get(m).addPrefilter(whiteFilter);
				}
			}		
		}		
	}
	
	
	/**
	 * Set the white noise filter for the ith 
	 * signal definition 
	 * 
	 * @param i for the ith signal definition
	 * @param L number of coefficients
	 */
	
	public void setWhiteNoisePrefilters(int i, int L) {
		
		if(anySolvers.size() > 0) {			
			for(int m = 0; m < anySignals.size(); m++) {
				
				double[] whiteFilter = (new WhiteNoiseFilter(anySolvers.get(i).getMDFAFactory().getBandPassCutoff(),
																 anySolvers.get(i).getMDFAFactory().getLowPassCutoff(), 0, L))
			                  			.getFilterCoefficients();
				anySignals.get(m).setPrefilter(i, whiteFilter);
			}		
		}		
	}
	
	
	/**
	 * Gets this joda DateTimeFormatter
	 * @return
	 *   DateTimeFormatter
	 */
	public DateTimeFormatter getFormatter() {
		return formatter;
	}

	/**
	 * Deletes the first n number of observations in
	 * all the time series associated with this multivariateFX signal
	 * @param n Number of observations to delete
	 */
	public void chopFirstObservations(int n) {
		
		int chopped = Math.min(n, fxSignals.size());
		for(int i = 0; i < chopped; i++) {
			fxSignals.remove(0);
		}	
		
		for(VectorSignalSeries signal : anySignals) {
			signal.chopFirstObservations(n);
		}
	}
	
	
	public void plotSignals(String myTitle) throws Exception {
		
		final String title = myTitle;
        final TimeSeriesPlot eurusd = new TimeSeriesPlot(title, this);
        eurusd.pack();
        eurusd.setVisible(true);
	}

	public DateTimeFormatter getDateFormat() {
		
		return formatter;
	}

	/**
	 * The number of total signals defined 
	 * @return
	 */
	public int getNumberSignals() {
		return anySolvers.size();
	}

	/**
	 * The number of time series in the multivariate
	 * time series used for signal estimation
	 * @return Number of series
	 */
	public int getNumberSeries() {
		return anySignals.size();
	}
	
	/**
	 * The number of observations in the time series for
	 * the output signal
	 * @return int Number of time series observations
	 */
	public int size() {	
		return fxSignals.size();
	}

	public String getTargetDate(int i) {
		return anySignals.get(targetSeriesIndex).getTargetDate(i);
		
	}

	/**
	 * Get the target value for the target series for 
	 * this multivariate signal extraction,  
	 * which is the first VectorSeries in the arrayList
	 * @param i index ith date
	 * @return TargetValue at the ith date
	 */
	public double getTargetValue(int i) {
		return anySignals.get(targetSeriesIndex).getTargetValue(i);
	}

	/**
	 * 
	 * Get the signal value at index i as a double array
	 * 
	 * @param i
	 * @return A double array
	 */
	public double[] getSignalValue(int i) {
		return fxSignals.get(i).getValue();
	}

	
	/**
	 * Get the latest TimeSeriesEntry containing multivariate
	 * signal and timestamp
	 * @return TimeSeriesEntry<double[]>
	 */
	public TimeSeriesEntry<double[]> getSignal(int i) {
		return fxSignals.get(i);
	}
	
	/**
	 * Get the latest TimeSeriesEntry containing multivariate
	 * signal and timestamp
	 * @return TimeSeriesEntry<double[]>
	 */
	public TimeSeriesEntry<double[]> getLatestSignalEntry() {
		return fxSignals.last();
	}

	/**
	 * 
	 * Adds a new MDFA signal extraction definition to the 
	 * list of outputed signals. All the filter coefficients 
	 * for all the other signals will be recomputed. 
	 * 
	 * @param newbase An MDFABase object defining the new MDFA signal 
	 * 
	 * @throws Exception
	 */
	public void addMDFABase(MDFABase newbase) throws Exception {		
		
		anySolvers.add(new MDFASolver(new MDFAFactory(newbase)));
		setWhiteNoisePrefilters(50);
		computeAllFilterCoefficients();
	}
	
	/**
	 * 
	 * Adjusts the fractional difference to the 
	 * all of the signal series in the multivariate 
	 * time series
	 * 
	 * @param d The new fractional difference exponent
	 */
	public void adjustFractionalDifferenceData(double d) {
	
		double value;
		for(int i = 0; i < anySignals.size(); i++) {	
				anySignals.get(i).getTargetSeries().adjustFractionalDifferenceData(d);			
		}
		
		minValue = Double.MAX_VALUE;
		maxValue = -Double.MAX_VALUE;
		for(int i = size() - 300; i < size(); i++) {
			value = getTargetValue(i);
			if(value > maxValue) maxValue = value;
			else if(value < minValue) minValue = value;	
		}
	}

    /**
     * Set the date format for joda timedate. Typically yyyy-MM-dd
     * for daily data  or yyyy-MM-dd HH:mm:ss for second data
     * @param anyformat String of format
     */
	public void setDateFormat(String anyformat) {
		this.formatter = DateTimeFormat.forPattern(anyformat);	
	}


	/**
	 * Gets a handle on the ith vector series
	 * @param i
	 * @return VectorSignalSeris
	 */
	public VectorSignalSeries getSeries(int i) {
		return anySignals.get(i);
	}


	/**
	 * 
	 * Returns the name of the target series
	 * 
	 * @return Name of target series
	 */
	public String getTargetName() {
		return anySignals.get(targetSeriesIndex).getName();
	}
	
	/**
	 * Sets the target series in the multivariate
	 * series collection. The target is the series
	 * from which the target signal is based
	 * 
	 * @param index New index of target
	 * @throws Exception 
	 */
	public void setTargetSeriesIndex(int index) throws Exception {
		
		if(index < getNumberSeries()) {
			this.targetSeriesIndex = index;
			this.computeAllFilterCoefficients();
		}		
	}
	
	/**
	 * Gets the target index
	 * @return int Target index
	 */
	public int getTargetSeriesIndex() {
		return this.targetSeriesIndex;
	}
 	
	/**
	 * 
	 * Activate prefiltering for each signal
	 * 
	 * @param prefilter
	 */
	public void prefilterActivate(boolean prefilter) {
		
		prefilterAll = prefilter;
		for(VectorSignalSeries series : anySignals) {
			series.prefilterActivate(prefilter);
		}
	}
	
	public boolean isPrefiltered() {
		
		for(VectorSignalSeries series : anySignals) {
			if(!series.isPrefiltered()) {
				return false;
			}
		}
		return true;
	}
	
	public double getMaxValue() {
		return maxValue;
	}
	
	public double getMinValue() {
		return minValue;
	}
	
	public int getSize() {	
		return fxSignals.size();
	}
	
	public long getLongtime() {
		return (formatter.parseDateTime(fxSignals
				         .last().getDateTime()))
				         .getMillis()/1000;
	}


	public double getPrevious() {
		return previous;
	}
	
	public double getLatest() {
		return latest;
	}


	public double getSymmetricSignal(int i) {
		// TODO Auto-generated method stub
		return 0;
	}


	public double getFilterMultiplier() {
		return filterMultiplier;
	}


	public void setFilterMultiplier(double filterMultiplier) {
		this.filterMultiplier = filterMultiplier;
	}


	public double updatePnl(String time, double val) {
		return anySignals.get(0).updatePnl(time, val);
	}


	public Side getCurrentSide() {
		return anySignals.get(0).getCurrentSide();
	}


	public double computeSignal(String time, double bid, double ask, int hour, double bid2) {	
		
		anySignals.get(0).addValue(time,  bid);		
		return anySignals.get(0).getCurrentPnL();
	}
	
	public double getCurrentPnl() {
		return anySignals.get(0).getCurrentPnL();
	}
	
	public double getRealizedPnl() {
		return anySignals.get(0).getRealizedPnl();
	}
	

}
