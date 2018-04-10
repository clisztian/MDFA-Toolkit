package ch.imetrica.mdfa.spectraldensity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.complex.Complex;
import org.jfree.ui.RefineryUtilities;

import com.csvreader.CsvReader;

import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.mdfa.MDFAFactory;
import ch.imetrica.mdfa.plotutil.TimeSeriesPlot;
import ch.imetrica.mdfa.series.MdfaSeries;
import ch.imetrica.mdfa.series.MultivariateSeries;
import ch.imetrica.mdfa.series.MultivariateSignalSeries;
import ch.imetrica.mdfa.series.SignalSeries;
import ch.imetrica.mdfa.series.TargetSeries;
import ch.imetrica.mdfa.series.TimeSeries;
import ch.imetrica.mdfa.series.VectorSignalSeries;
import ch.imetrica.mdfa.series.MdfaSeries.SeriesType;



/**
 * Spectral information of the time series. Used as the default 
 * unbiased weighting spectral density function for computing 
 * the MDFA coefficients. All series added in the Mutlivariate
 * signal estimation will be added to an ArrayList. One of the 
 * series in the ArrayList must be a target series. By default, 
 * the first series will be the target series and the rest explanatory
 * series. 
 * 
 * Other possibilities of spectral density estimates are also 
 * possible and will be included
 * 
 * 1) ARMA model FR function * DFT phase
 * 2) Pre-filtered DFT using ZPC filter coefficients
 * 3) Tapered/Windowed DFTs
 * 
 * 
 */

public class SpectralBase {

	

	private ArrayList<Complex[]> dfts;
	private int in_sample_size;
	private int myTarget;
	private Complex[] targetDFTs;
	
	/**
     * Sets the in_sample_size for the SpectralDensity estimation
     * and the default value of myTarget index 
     * which is the target series that is being filtered
     * 
     * @param anyMdfa
     *            An Mdfa object holding number of observations
     */
	
	 public SpectralBase(int nObservations) {
		 	 
		 this.in_sample_size = nObservations;
		 this.myTarget = 0;
		 this.dfts = new ArrayList<Complex[]>();
	 }
	 
	 
	 /** 
	  * 
	  * Uses an MDFAFactor object to extract 
	  * MDFA computational parameters. The default 
	  * myTarget index is set to 0
	  * 
	  * @param anyMDFAFactory
	  */
	 public SpectralBase(MDFAFactory anyMDFAFactory) {
		 
		 this.in_sample_size = anyMDFAFactory.getSeriesLength();
		 this.myTarget = 0;
		 this.dfts = new ArrayList<Complex[]>();
		 
	 }
	 
	 
	 /**
	  * Computes the basic Fourier coefficients
	  * for a given target series that has been 
	  * fractionally differenced. Only the final 
	  * in_sample_size values will be used
	  * 
	  * @param anySeries
	  *            And Mdfa series of at least size
	  *            in_sample_size. The stationary 
	  *            target/signal values will be transformed
	  *            to the frequency domain and added to 
	  * @throws Exception 
	  *            
	  */
	 public void addSeries(MdfaSeries anySeries) throws Exception {
		
		 if(anySeries.size() < in_sample_size) {
			 throw new Exception("Size of anySeries must be at least " + in_sample_size);
		 }
	
		 if(dfts.size() == myTarget) {			
			 targetDFTs = computeDFT(anySeries, true);			 
		 }
		 
		 /* Set the target DFTs to this DFT */
		 if(anySeries.isPrefiltered()) {			 
	
			 dfts.add(computeDFT(anySeries, false));
		 }
		 else {
			 
			 dfts.add(computeDFT(anySeries, true));
		 }
	}
	 
	 
	private Complex[] computeDFT(MdfaSeries anySeries, boolean target) {
		
		int K = (int)in_sample_size/2;
		int K1 = K+1;
		final double M_PI = Math.PI;
		double mean = 0;
		double val;
		double normalizer = Math.sqrt(M_PI*in_sample_size);
		
		
		Complex[] prdx = new Complex[K1];
		int start = anySeries.size() - in_sample_size;
		 
		for(int i = start; i < anySeries.size(); i++) {
			
			if(target) 
				mean += anySeries.getTargetValue(i);
			else if(anySeries.getSeriesType() == SeriesType.SIGNAL)
				mean += ((SignalSeries)anySeries).getPrefilteredValue(i);
			
		}
		mean = mean/normalizer;
		 		 
		prdx[0] = new Complex(mean,0);
		Complex ab = new Complex(0,0);
		 
		for(int j = 1; j < K1; j++) {
			 
			 ab = new Complex(0,0);
			 for(int i = start; i < anySeries.size(); i++) {
				 
				 if(target) 
					 val = anySeries.getTargetValue(i);				 
				 else
					 val = ((SignalSeries)anySeries).getPrefilteredValue(i);
				 
				 Complex z = (new Complex(0, M_PI*(i - start + 1.0)*j/K)).exp();				 
				 ab = ab.add(z.multiply(val)); 
			 }
			 prdx[j] = ab.divide(normalizer);			 
		}
		return prdx;		
	}
	 
	 
	/**
	 * 
	 * Extracts any signal series that requires filter coefficients and 
	 * adds them to the list of series to be included in the multivariate
	 * filtering system
	 *  
	 *  
	 * @param anySeries
	 *     A multivariate series with at least one signal series with target
	 *     to be used for the computing the signal
	 * @throws Exception 
	 */
	public void addMultivariateSeries(MultivariateSeries signals) throws Exception {
		
		int M = signals.size();

		for(int i = 0; i < M; i++) {
			if(signals.getSeries(i).getSeriesType() == SeriesType.SIGNAL) {
				addSeries(((SignalSeries)signals.getSeries(i)));
			}
		}
	}
	
	/**
	 * 
	 * Extracts any signal series that requires filter coefficients and 
	 * adds them to the list of series to be included in the multivariate
	 * filtering system
	 *  
	 *  
	 * @param signals
	 *     A multivariate series with at least one signal series with target
	 *     to be used for the computing the signal
	 * @throws Exception 
	 */
	public void addSignalSeries(ArrayList<SignalSeries> signals) throws Exception {
		
		int M = signals.size();
		for(int i = 0; i < M; i++) {
			addSeries(signals.get(i));
		}
	}
	
	
	public void addVectorSeries(ArrayList<VectorSignalSeries> signals) throws Exception {
		
		int M = signals.size();
		for(int i = 0; i < M; i++) {
			addSeries(signals.get(i));
		}
	}
	
	public void addMdfaSeries(ArrayList<MdfaSeries> signals) throws Exception {
		
		int M = signals.size();
		for(int i = 0; i < M; i++) {
			addSeries(signals.get(i));
		}
	}	

	/**
	* Returns the DFT information
	* 
	* @return dfts
	*      ArrayList of Complex[] of the spectral
	*      density estimates for each series in 
	*      the estimation     
	 * @throws Exception 
	*/
	public ArrayList<Complex[]> getSpectralBase() throws Exception {
		
		if(dfts.size() == 0) {
			 throw new Exception("At least one spectral density estimate for a series is needed");
		}	
		return dfts;
	}
	
	
	/**
	* Sets the index of the target series used 
	* for the real-time signal estimation
	* 
	* @param target
	*      Sets the target index. By default the target
	*      index is 0.     
	*/
	public void setTargetIndex(int target) {
		
		if(target < dfts.size()) {
			this.myTarget = target;
			targetDFTs = dfts.get(target);
		}	
	}

	/**
	* Gets the index of the target series used 
	* for the real-time signal estimation
	* 
	* @return target
	*      Gets the target index. By default the target
	*      index is 0.     
	*/
	public int getTargetIndex() {
		return this.myTarget;
	}
	
	/**
		* Returns number of series that have been added
		* for estimation of real-time signal
		* @return 
		*      Number of series      
		*/
	public int size() {
		return dfts.size();
	}
	
	
	/**
	* Returns the dft (or spectral density estimate) 
	* associated with index i
	* @return 
	*      The dft at ith index      
	*/
	public Complex[] getSpectralDensity(int i) {
		return dfts.get(i);
	}
	
	/**
	 * 
	 * Clears the list of spectral dfts
	 * 
	 */
	public void clearSpectralBase() {
		dfts.clear();
	}
	
	
	
	public static void plotPeriodogram(double[] prdx) {
			
			final String title = "EURUSD frac diff";
	        final TimeSeriesPlot eurusd = new TimeSeriesPlot(title, prdx);
	        eurusd.pack();
	        RefineryUtilities.positionFrameRandomly(eurusd);
	        eurusd.setVisible(true);
			
	}


	public Complex getTargetSpectralDensity(int k) {
		return targetDFTs[k];
	}
	
	
	
	 






	
}
 