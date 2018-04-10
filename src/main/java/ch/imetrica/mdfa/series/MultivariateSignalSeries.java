package ch.imetrica.mdfa.series;

import java.util.ArrayList;

import javax.swing.plaf.multi.MultiListUI;

import org.jfree.ui.RefineryUtilities;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.imetrica.mdfa.matrix.MdfaMatrix;
import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.mdfa.MDFAFactory;
import ch.imetrica.mdfa.mdfa.MDFASolver;
import ch.imetrica.mdfa.plotutil.TimeSeriesPlot;
import ch.imetrica.mdfa.series.MdfaSeries.SeriesType;
import ch.imetrica.mdfa.spectraldensity.SpectralBase;
import ch.imetrica.mdfa.unbiased.WhiteNoiseFilter;

/**
 * 
 * A MultivariateSignal series is a collection of M MDFA
 * signals that are governed by M independent signal extraction 
 * processes. They are defined by a collection on M {@link MDFASolver}
 * objects and one {@link TargetSeries}.
 * 
 * This class could also be replicated with M {@link MultivariateSeries}, 
 * in a more generalized setting, but at the cost of having M 
 * replications of the TargetSeries.
 * 
 * The motivation behind the MultivariateSignal is to create an "easier"
 * interface for our Machine Learning applications
 * 
 * 
 * @author Christian D. Blakely (clisztian@gmail.com)
 *
 */
public class MultivariateSignalSeries implements MdfaSeries {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final SeriesType seriesType = SeriesType.MULTISIGNAL;
	private TargetSeries myTarget;	
	private ArrayList<double[]> bcoeffs;
	private ArrayList<double[]> preFilterCoeffs;
	
	private TimeSeries<double[]> multiSignalSeries;
	private String name;
	private DateTimeFormatter formatter;
	private MDFASolver[] anySolvers;
	
	
	/**
	 * 
	 * Instantiate a MutivariateSignalSeries with a handle on a TargetSeries, 
	 * a collection of M MDFABase objects in an array, and the format for the datetime stamps.
	 * 
	 * M independent MDFA solvers will then be created 
	 * 
	 * @param anytarget A target time series that will be decomposed into M signals
	 * @param anyMDFAs Collection of M independent MDFABase objects
	 * @param anyformat Format for datetime strings
	 */
	public MultivariateSignalSeries(TargetSeries anytarget, MDFABase[] anyMDFAs, String anyformat) {
		
		this.myTarget = anytarget;
		this.formatter = DateTimeFormat.forPattern(anyformat);
		this.bcoeffs = new ArrayList<double[]>();
		this.preFilterCoeffs = new ArrayList<double[]>();
		
		
		anySolvers = new MDFASolver[anyMDFAs.length];
		for(int i = 0; i < anyMDFAs.length; i++) {
			anySolvers[i] = new MDFASolver(new MDFAFactory(anyMDFAs[i]));
		}		
	}

	/**
	 * 
	 * Instantiate a MutivariateSignalSeries with a handle on a TargetSeries, 
	 * a collection of M MDFABase objects in an ArrayList, and the format for the datetime stamps.
	 * 
	 * M independent MDFA solvers will then be created 
	 * 
	 * @param anytarget A target time series that will be decomposed into M signals
	 * @param anyMDFAs Collection of M independent MDFABase objects
	 * @param anyformat Format for datetime strings
	 */
	public MultivariateSignalSeries(TargetSeries anytarget, ArrayList<MDFABase> anyMDFAs, String anyformat) {
		
		this.myTarget = anytarget;
		this.formatter = DateTimeFormat.forPattern(anyformat);
		this.bcoeffs = new ArrayList<double[]>();
		this.preFilterCoeffs = new ArrayList<double[]>();
		
		int count = 0;	
		anySolvers = new MDFASolver[anyMDFAs.size()];		
		for(MDFABase anyMDFA : anyMDFAs) {
			anySolvers[count] = new MDFASolver(new MDFAFactory(anyMDFA));
			count++;
		}
	}
	
	/**
	 * With the target data set and a sufficient amount of 
	 * observations, this computes the filter coefficients 
	 * for all the MDFABase definitions
	 * 
	 * @throws Exception
	 */
	public MultivariateSignalSeries computeFilterCoefficients() throws Exception {
		
		int M = anySolvers.length;
		bcoeffs.clear();
		
		for(int m = 0; m < M; m++) {
			
			SpectralBase base = new SpectralBase(anySolvers[m].getMDFAFactory().getSeriesLength());
			base.addSeries(this.myTarget);
			anySolvers[m].updateSpectralBase(base);
			MdfaMatrix myCoeffs = anySolvers[m].solver();
			
			int L = anySolvers[m].getMDFAFactory().getFilterLength();
			double[] sig_coeffs = myCoeffs.getSubsetColumn(0, 0, L);
			bcoeffs.add(sig_coeffs);			
		}	
		return this;
	}
	
	/**
	 * 
	 * Adds a new value to the target series and recomputes the new
	 * signal values for all M signals if the coefficients have
	 * been computed 
	 * 
	 * 
	 * @param val new raw time series value for the target series
	 * @param date new raw time series date value
	 */
	@Override
	public void addValue(String date, double val) {
		
		myTarget.addValue(date, val);
		
		if(bcoeffs.size() == anySolvers.length) {
			
			int M = anySolvers.length;
			int N = myTarget.size();
			
			double[] sums = new double[M];
			for(int m = 0; m < M; m++) {
				
				int filter_length = Math.min(N, bcoeffs.get(m).length);
				for (int l = 0; l < filter_length; l++) {
					sums[m] += bcoeffs.get(m)[l]*myTarget.getTargetValue(N - l - 1);
				}		
			}
			multiSignalSeries.add(new TimeSeriesEntry<double[]>(date, sums));			
		}		
	}
	

	/**
	 * 
	 * Once the filter coefficients have been computed, the historical 
	 * (and latest) signal values from be computed from the target. 
	 * 
	 * @throws Exception if the number of coefficient sets does not equal 
	 * number of MDFASolvers
	 */
	public MultivariateSignalSeries computeSignalsFromTarget() throws Exception {
		
		if(bcoeffs.size() != anySolvers.length) {
			throw new Exception("No MDFA coefficients yet computed for this target series");
		}
		
		if(myTarget == null) {
			throw new Exception("No target series has been defined yet");
		}
		
		multiSignalSeries = new TimeSeries<double[]>();
		
		int M = anySolvers.length;
		int N = myTarget.size();
		for(int i = 0; i < N; i++) {
			
			double[] sums = new double[M];
			for(int m = 0; m < M; m++) {
				
				int filter_length = Math.min(i+1, bcoeffs.get(m).length);
				for (int l = 0; l < filter_length; l++) {
					sums[m] += bcoeffs.get(m)[l]*myTarget.getTargetValue(i - l);
				}		
			}
			multiSignalSeries.add(new TimeSeriesEntry<double[]>(myTarget.getTargetDate(i), sums));
		}
		return this;
	} 
	
	/**
	 * Get the signal value at index i. The 
	 * signal will be M values for each signal
	 * defined by the given MDFABase
	 * @param i ith index in the signal series
	 * @return
	 *    A double array of the M signal values
	 */
	public double[] getSignalValue(int i) {
		
		return multiSignalSeries.get(i).getValue();
	}
	
	/**
	 * Get the signal date/value pair at index i. The 
	 * signal will be M values for each signal
	 * defined by the given MDFABase
	 * @param i
	 * @return
	 *   A TimeSeriesEntry at index i
	 */
	public TimeSeriesEntry<double[]> getSignalEntry(int i) {
		
		return multiSignalSeries.get(i);
	}
	
	/**
	 * Get the latest signal value.
	 * signal will be M values for each signal
	 * defined by the given MDFABase

	 * @return
	 *    A double array of the M signal values
	 */
	public double[] getLatestSignalValue() {
		return multiSignalSeries.last().getValue();
	}
	
	/**
	 * Get the latest signal date/value pair. The 
	 * signal will be M values for each signal
	 * defined by the given MDFABase
	 * 
	 * @return
	 *   A TimeSeriesEntry at index i
	 */
	public TimeSeriesEntry<double[]> getLatestSignalEntry() {	
		return multiSignalSeries.last();
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
		
		if(i < 0 || i >= anySolvers.length) {
			return null;
		}		
		return anySolvers[i].getMDFAFactory();
	}
	
	
	/**
	 * Set the name of the multisignal generating process
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the target value at index i
	 * @param i
	 * @return Target time series value
	 */
	public double getTargetValue(int i) {	
		return this.myTarget.getTargetValue(i);
	}
	
	/**
	 * Get the target date at index i
	 * @param i
	 * @return Target time series date
	 */
	public String getTargetDate(int i) {
		return this.myTarget.getTargetDate(i);
	}
	
	/**
	 * Get the latest value in the target series
	 * @return Target time series latest value
	 */
	public double getLatestTargetValue() {
		return this.myTarget.getLatest().getValue();
	}
	
	/**
	 * Get the latest date in the target series
	 * @return Target time series latest date
	 */
	public String getLatestTargetDate() {
		return this.myTarget.getLatest().getDateTime();
	}

	
	/**
	 * Creates unbiased white noise filters on the 
	 * @param L
	 */
	public void setWhiteNoisePrefilters(int L) {
		
		preFilterCoeffs.clear();		
		if(anySolvers.length > 0) {
			
			for(int i = 0; i < anySolvers.length; i++) {
				
				double[] whiteFilter = (new WhiteNoiseFilter(anySolvers[i].getMDFAFactory().getLowPassCutoff(), 0, L))
						                  .getFilterCoefficients();
			   
				preFilterCoeffs.add(whiteFilter);
			}		
		}		
	}

	
	
	
	
	public void plotSignals(String myTitle) throws Exception {
		
		final String title = myTitle;
        final TimeSeriesPlot eurusd = new TimeSeriesPlot(title, this);
        eurusd.pack();
        RefineryUtilities.positionFrameRandomly(eurusd);
        eurusd.setVisible(true);
	}
	
	

	@Override
	public SeriesType getSeriesType() {
		return this.seriesType;
	}

	@Override
	public void setDateFormat(String anyformat) {
		this.formatter = DateTimeFormat.forPattern(anyformat);
	}

	@Override
	public DateTimeFormatter getDateFormat() throws Exception {
		
		if(formatter == null) {
        	throw new Exception("No DateTime format defined yet");
        }	
		return formatter;
	}

	@Override
	public int size() {
		return myTarget.size();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void chopFirstObservations(int n) {
		
		myTarget.chopFirstObservations(n);
		
		int chopped = Math.min(n, multiSignalSeries.size());
		for(int i = 0; i < chopped; i++) {
			multiSignalSeries.remove(0);
		}
	}

	@Override
	public boolean isPrefiltered() {
		return (preFilterCoeffs.size() > 0);
	}

	/**
	 * Gets the filter coefficients for the 
	 * ith signal in the multisignal object
	 * @param i
	 * @return
	 */
	public double[] getCoefficients(int i) {
		
		return bcoeffs.get(i);
	}


	/**
	 * Returns the number of signals in 
	 * this multisignal object. Determined 
	 * by number of MDFASolvers
	 * 
	 * @return Numbero of signals 
	 */
	public int getNumberSignals() {
		
		return bcoeffs.size();
	}

	
	
	@Override
	public TimeSeriesEntry<Double> getLatest() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimeSeries<Double> getTimeSeries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimeSeries<Double> getLatestValues(int n) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
