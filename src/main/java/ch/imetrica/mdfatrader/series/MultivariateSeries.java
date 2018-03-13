package ch.imetrica.mdfatrader.series;

import java.util.ArrayList;

import ch.imetrica.mdfatrader.matrix.MdfaMatrix;
import ch.imetrica.mdfatrader.series.MdfaSeries.SeriesType;
import ch.imetrica.mdfatrader.spectraldensity.SpectralBase;
import ch.imetrica.mdfatrading.mdfa.MDFABase;
import ch.imetrica.mdfatrading.mdfa.MDFASolver;

/**
 * 
 * A Multivariate time series holds a collection of MdfaSeries
 * including price, target, and explanatory series which act 
 * as input. The output series are the signal(s) which are automatically
 * computed once a set of coefficients for the target series has been 
 * given 
 *
 * 
 * @author Christian D. Blakely (clisztian@gmail.com)
 *
 */
public class MultivariateSeries {

	
	ArrayList<MdfaSeries> anySeries;
	MDFASolver anySolver;
	MDFABase anyMDFA;
	
	private int nSignals = 0;
	
	public MultivariateSeries(MDFABase anyMDFA, MDFASolver anySolver) {
		
		this.anySeries = new ArrayList<MdfaSeries>();
		this.anyMDFA = anyMDFA;
		this.anySolver = anySolver;
		
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
	public boolean addSeries(MdfaSeries series) {
		
		boolean success = true;
		if(anySeries.size() > 0) {
			
			String datetime = series.getLatest().getDateTime();
			
			for(int i = 0; i < anySeries.size(); i++) {
				
				if(!datetime.equals(anySeries.get(i).getLatest().getDateTime())) {
					success = false;
				}
			}		
		}
		if(success) { 
			
			anySeries.add(series);
			if(series.getSeriesType() == SeriesType.SIGNAL) {
				nSignals++;
			}
		}
		return success;
	}
	
	
	/**
	 * 
	 * Adds raw time series values to the multivariate series.
	 * They must all share the same timestamp and size must 
	 * equal number of total series 
	 * 
	 * @param val
	 *    A double[] array of values to be added 
	 * @param date
	 *    The common date among the time series values
	 * @throws Exception 
	 */
    public void addValue(double[] val, String date) throws Exception {
    
    	if(val.length != this.size()) {
    		throw new Exception("Sizes of array and number of time series don't match");
    	}
    	
    	for(int i = 0; i < anySeries.size(); i++) { 		
    		anySeries.get(i).addValue(val[i], date);
    	}
    }

	/**
	 * 
	 * Adds raw time series values to the multivariate series.
	 * They must all share the same timestamp. 
	 * 
	 * @param val
	 *    An ArrayList of raw time series values. Must
	 *    match number of total series 
	 * @param date
	 *    The common date among the time series values
	 * @throws Exception 
	 */
    public void addValue(ArrayList<Double> val, String date) throws Exception {
    
    	if(val.size() != this.size()) {
    		throw new Exception("Sizes of array and number of time series don't match");
    	}
    	
    	for(int i = 0; i < anySeries.size(); i++) { 		
    		anySeries.get(i).addValue(val.get(i), date);
    	}
    }
    
    
	/**
	 * 
	 * Computes the latest aggregate signal given the current signals 
	 * in the multivariate time series
	 *  
	 * @return
	 *    The multivariate signal value
	 */
	public double getSignalValue() {
		
		double sigVal = 0;
		for(int i = 0; i < anySeries.size(); i++) {
			
			if(anySeries.get(i).getSeriesType() == SeriesType.SIGNAL) {
				sigVal += ((SignalSeries) anySeries.get(i)).getSignalValue(i);
			}
		}
		return sigVal;
	}
    
	/**
	 * 
	 * Compute the MDFA filter coefficients given the most 
	 * recent N time series observations and the given 
	 * MDFA Base and solver
	 * 
	 * @throws Exception
	 */
	public void computeFilterCoefficients() throws Exception {
		
		SpectralBase base = new SpectralBase(anyMDFA);
		base.addMultivariateSeries(this);
		anySolver.updateSpectralBase(base);
		MdfaMatrix bcoeffs = anySolver.solver();
		
		int signal = 0;
		int L = anyMDFA.getFilterLength();
		for(int i = 0; i < anySeries.size(); i++) {
			
			if(anySeries.get(i).getSeriesType() == SeriesType.SIGNAL) {
				double[] sig_coeffs = bcoeffs.getSubsetColumn(0, signal*L, signal*L + L);
				((SignalSeries) anySeries.get(i)).setMDFAFilterCoefficients(sig_coeffs);
				signal++;
			}	
		}
	}
	 
	/**
	 * Returns the number of total series in the 
	 * Multivariate list
	 * 
	 * @return size
	 *    The number of series
	 */
	public int size() {
		return anySeries.size();
	}
	
	/**
	 * Returns the total number of series which are 
	 * signals in the multivariate time series list
	 * @return
	 */
	public int getNumberSignal() {
		return nSignals;
	}
	
	/**
	 * 
	 * Returns the ith series in the system. 
	 * Most likely will be a signal series
	 * 
	 * @param i index of series to return
	 * @return
	 *   the MdfaSeries
	 */
	public MdfaSeries getSeries(int i) {
		return anySeries.get(i);
	}
	
	/**
	 * 
	 * Print all the MDFA coefficients for each signal in 
	 * this multivariate series
	 */
	public void printMDFACoeffs() {
		
		for(int i = 0; i < anySeries.size(); i++) {
			
			if(anySeries.get(i).getSeriesType() == SeriesType.SIGNAL) {
				System.out.println(((SignalSeries) anySeries.get(i)).coeffsToString());
			}
		}
	}
	
	public double sumAllCoefficients() {
		
		double sum = 0;
		for(int i = 0; i < anySeries.size(); i++) {
			
			if(anySeries.get(i).getSeriesType() == SeriesType.SIGNAL) {
				sum += ((SignalSeries) anySeries.get(i)).sumCoeffs();
			}
		}
		return sum;
	}
	
	/**
	 * 
	 * Returns an arrayList of the MDFA coefficients for each signal
	 * series in this MultivariateSeries  
	 *  
	 * @return 
	 *   ArrayList<double[]> a list of the coefficients for each 
	 *   signal series in this multivariate series
	 *    
	 */
	public ArrayList<double[]> getMDFACoeffs() {
		
		ArrayList<double[]> allCoeffs = new ArrayList<double[]>();
		
		for(int i = 0; i < anySeries.size(); i++) {
			
			if(anySeries.get(i).getSeriesType() == SeriesType.SIGNAL) {
				allCoeffs.add(((SignalSeries) anySeries.get(i)).getCoeffs());
			}
		}	
		return allCoeffs;
	}
	
	public void plotSignals() throws Exception {
		
         for(int i = 0; i < anySeries.size(); i++) {
			
			if(anySeries.get(i).getSeriesType() == SeriesType.SIGNAL) {
				((SignalSeries) anySeries.get(i)).plotSignal();
			}
		}
	}
	
	
}
