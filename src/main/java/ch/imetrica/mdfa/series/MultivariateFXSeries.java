package ch.imetrica.mdfa.series;

import java.util.ArrayList;

import org.jfree.ui.RefineryUtilities;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.imetrica.mdfa.matrix.MdfaMatrix;
import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.mdfa.MDFAFactory;
import ch.imetrica.mdfa.mdfa.MDFASolver;
import ch.imetrica.mdfa.plotutil.TimeSeriesPlot;
import ch.imetrica.mdfa.spectraldensity.SpectralBase;
import ch.imetrica.mdfa.unbiased.WhiteNoiseFilter;
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
	 * 
	 * Adds a new multivariate series value for the given date. 
	 * The new multivariate signal will be computed automatically 
	 * at the given date. This assumes that all coefficients are
	 * defined for each series
	 * 
	 * 
	 * @param val array of newest observations at given date
	 * @param date Most curent date
	 * @throws Exception if the multivariate size of the new observation
	 * 		   does not equal the number of series
	 */
    public void addValue(double[] val, String date) throws Exception {
        
    	if(val.length != anySignals.size()) {
    		throw new Exception("Sizes of array and number of time series don't match");
    	}
    	
    	double[] sigVal = new double[anySolvers.size()];
    	for(int m = 0; m < anySignals.size(); m++) { 		
    		
    		anySignals.get(m).addValue(date, val[m]);
    		
    		if(anySignals.get(m).hasFilter()) {
    			sigVal = MdfaUtil.plus(sigVal, anySignals.get(m).getLatestSignalValue());
    		}
		}
		fxSignals.add(new TimeSeriesEntry<double[]>(date, sigVal));	 	
    }
	
    
	/**
	 * 
	 * Adds a new multivariate series value for the given date. 
	 * The new multivariate signal will be computed automatically 
	 * at the given date. This assumes that all coefficients are
	 * defined for each series
	 * 
	 * 
	 * @param val arrayList of newest observations at given date
	 * @param date Most current date
	 * @throws Exception if the multivariate size of the new observation
	 * 		   does not equal the number of series
	 */
    public void addValue(ArrayList<Double> val, String date) throws Exception {
        
    	if(val.size() != anySignals.size()) {
    		throw new Exception("Sizes of array and number of time series don't match");
    	}
    	
    	double[] sigVal = new double[anySolvers.size()];
    	for(int m = 0; m < anySignals.size(); m++) { 		
    		
    		anySignals.get(m).addValue(date, val.get(m));
    		
    		if(anySignals.get(m).hasFilter()) {
    			sigVal = MdfaUtil.plus(sigVal, anySignals.get(m).getLatestSignalValue());
    		}		
		}
		fxSignals.add(new TimeSeriesEntry<double[]>(date, sigVal));	 	
    }
	
	
	
	/**
	 * Adds a new series to the collection of the multivariate 
	 * time series stream. Will be added if the two conditions hold:
	 * 
	 * - Has at least MDFABase.N observations
	 * - The latest date corresponds to the latest date of all 
	 *   series in the collection
	 * 
	 * @param series
	 *        Nonempty time series to be added for use as an explanatory 
	 *        series that is filtered or a technical for trading
	 *        
	 */
	public boolean addSeries(TargetSeries series) {
		
		boolean success = true;
		if(anySignals.size() > 0 && series.size() > 0) {
			
			String datetime = series.getLatest().getDateTime();
			
			for(int i = 0; i < anySignals.size(); i++) {
				
				if(!datetime.equals(anySignals.get(i).getLatestDate())) {
					success = false;
				}
			}		
		}
		if(success) { 	
			
			VectorSignalSeries vecSeries = new VectorSignalSeries(series);
			anySignals.add(vecSeries);
		}
		return success;
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
		
		int sigIndex = 0;
		for(VectorSignalSeries series : anySignals) {
			series.clearFilters();
		}

		
		SpectralBase base = new SpectralBase(anySolvers.get(0).getMDFAFactory().getSeriesLength());
		base.addVectorSeries(anySignals);
		
		for(MDFASolver anySolver : anySolvers) {
			
			anySolver.getMDFAFactory().setNumberOfSeries(anySignals.size());
			anySolver.updateSpectralBase(base);
			
			int L = anySolver.getMDFAFactory().getFilterLength();
			MdfaMatrix bcoeffs = anySolver.solver();
			
			for(int i = 0; i < anySignals.size(); i++) {
				
				double[] sig_coeffs = bcoeffs.getSubsetColumn(0, i*L, i*L + L);
				anySignals.get(i).setMDFAFilterCoefficients(sigIndex, sig_coeffs);
			}
			sigIndex++;
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
			
			MDFASolver anySolver = anySolvers.get(n);
			
			anySolver.getMDFAFactory().setNumberOfSeries(anySignals.size());
			
			SpectralBase base = new SpectralBase(anySolver.getMDFAFactory().getSeriesLength());
			base.addVectorSeries(anySignals);
			anySolver.updateSpectralBase(base);
			
			int L = anySolver.getMDFAFactory().getFilterLength();
			MdfaMatrix bcoeffs = anySolver.solver();
			
			for(int i = 0; i < anySignals.size(); i++) {
				
				double[] sig_coeffs = bcoeffs.getSubsetColumn(0, i*L, i*L + L);
				anySignals.get(i).setMDFAFilterCoefficients(n, sig_coeffs);
			}	
		}
		computeAggregateSignal();
	}
	
	
	private void computeAggregateSignal() throws Exception {
		
		fxSignals.clear();
		int N = anySignals.get(0).size();
		for(int i = 0; i < N; i++) {
			
			double[] val = anySignals.get(0).getSignalValue(i);
			String current = anySignals.get(0).getSignalDate(i);
			
			
			for(int m = 1; m < anySignals.size(); m++) {
				
				if(current.equals(anySignals.get(m).getSignalDate(i))) {					
					
					val = MdfaUtil.plus(val, anySignals.get(m).getSignalValue(i));
				
				}
				else {
					  throw new Exception("Dates do not match of the signals: " + current + " is not " + anySignals.get(m).getSignalDate(i));
				}
			}
			fxSignals.add(new TimeSeriesEntry<double[]>(current, val));	
		}
	}
	
	
	
	/**
	 * 
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
	 * Creates unbiased white noise filters on the 
	 * @param L
	 */
	public void setWhiteNoisePrefilters(int L) {
					
		if(anySolvers.size() > 0) {			
			for(int m = 0; m < anySignals.size(); m++) {			
				anySignals.get(m).clearFilters();
				for(int i = 0; i < anySolvers.size(); i++) {

					double[] whiteFilter = (new WhiteNoiseFilter(anySolvers.get(i).getMDFAFactory().getLowPassCutoff(), 0, L))
			                  .getFilterCoefficients();
					anySignals.get(m).addPrefilter(whiteFilter);
				}
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

	public void chopFirstObservations(int obs) {
		
		int chopped = Math.min(obs, fxSignals.size());
		for(int i = 0; i < chopped; i++) {
			fxSignals.remove(0);
		}	
		
		for(VectorSignalSeries signal : anySignals) {
			signal.chopFirstObservations(obs);
		}
	}
	
	
	public void plotSignals(String myTitle) throws Exception {
		
		final String title = myTitle;
        final TimeSeriesPlot eurusd = new TimeSeriesPlot(title, this);
        eurusd.pack();
        RefineryUtilities.positionFrameRandomly(eurusd);
        eurusd.setVisible(true);
	}

	public DateTimeFormatter getDateFormat() {
		
		return formatter;
	}

	public int getNumberSignals() {
		return anySolvers.size();
	}

	public int size() {	
		return fxSignals.size();
	}

	public String getTargetDate(int i) {
		return anySignals.get(0).getTargetDate(i);
		
	}

	public double getTargetValue(int i) {
		return anySignals.get(0).getTargetValue(i);
	}

	public double[] getSignalValue(int i) {
		return fxSignals.get(i).getValue();
	}
	
	
}
