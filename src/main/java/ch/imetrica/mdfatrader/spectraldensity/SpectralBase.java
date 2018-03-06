package ch.imetrica.mdfatrader.spectraldensity;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.math3.complex.Complex;
import org.jfree.ui.RefineryUtilities;

import com.csvreader.CsvReader;

import ch.imetrica.mdfatrader.series.TargetSeries;
import ch.imetrica.mdfatrader.series.TimeSeries;
import ch.imetrica.mdfatrading.plotutil.TimeSeriesPlot;



/**
 * Spectral information of the time series. Used as the default 
 * unbiased weighting spectral density function for computing 
 * the MDFA coefficients 
 * 
 * Other possibilities of spectral density estimates are also 
 * possible and will be included
 * 
 * 1) ARMA model FR function * DFT phase
 * 2) Pre-filtered DFT using ZPC filter coefficients
 * 
 * 
 * 
 */

public class SpectralBase {

	
	Complex[] prdx;
	
	/**
     * Computes the basic Fourier coefficients
     * for a given target series that has been 
     * fractionally differenced.
     * 
     * @param tseries
     *            A target series fractionally differenced
     */
	
	 public SpectralBase(TargetSeries tseries) {
		 
		 int n = tseries.size();
		 int K = (int)n/2;
		 int K1 = K+1;
		 final double M_PI = Math.PI;
		 
		 prdx = new Complex[K1];
		 
		 double mean = 0;
		 for(int i = 0; i < tseries.size(); i++) {
			 mean += tseries.getTargetValue(i);
		 }
		 mean = mean/n;
		 		 
		 prdx[0] = new Complex(mean,0);
		 Complex ab = new Complex(0,0);
		 
		 for(int j = 1; j < K1; j++) {
			 
			 ab = new Complex(0,0);
			 for(int i = 0; i < n; i++) {
				 
				 double val = tseries.getTargetValue(i) - mean;				 
				 Complex z = (new Complex(0, M_PI*(i + 1.0)*j/K)).exp();				 
				 ab = ab.add(z.multiply(val)); 
			 }
			 prdx[j] = ab.divide(Math.sqrt(M_PI*n));			 
		 }		 
	}
	 
	/**
	* Returns the DFT information
	* 
	* @return prdx
	*      Complex array of DFT
	*     
	*/
	 
	public Complex[] getDFT() {
		return prdx;
	}
	

	public static void plotPeriodogram(double[] prdx) {
			
			final String title = "EURUSD frac diff";
	        final TimeSeriesPlot eurusd = new TimeSeriesPlot(title, prdx);
	        eurusd.pack();
	        RefineryUtilities.positionFrameRandomly(eurusd);
	        eurusd.setVisible(true);
			
	}
	 
	 
	 public static void main(String[] args) {
		 
		 
		    String dataFile = "data/EUR.USD.csv";
			TimeSeries<Double> rawSeries = new TimeSeries<Double>();
			CsvReader marketDataFeed;
			
			int nObs = 0;
			int MAX_OBS = 1000;
			
			try{
				
				 /* Read data market feed from CSV filer and it's headers*/	
				 marketDataFeed = new CsvReader(dataFile);
				 marketDataFeed.readHeaders();

				 while (marketDataFeed.readRecord()) {
					 
					double price = (new Double(marketDataFeed.get("close"))).doubleValue();
					String date_stamp = marketDataFeed.get("dateTime");
					
					rawSeries.add(date_stamp, price);
					nObs++;
					
					if(nObs == MAX_OBS) break;
				 }
				 
				 TargetSeries tseries = new TargetSeries(rawSeries, 0.7, true);
		 
				 SpectralBase spec = new SpectralBase(tseries);
				 
				 double[] abs = new double[spec.prdx.length];
				 for(int i = 0; i < abs.length; i++) {
					 
					 abs[i] = spec.prdx[i].abs();
					 System.out.println(abs[i]);
				 }
				 
				 SpectralBase.plotPeriodogram(abs);
				 
				 
	      }
		  catch (FileNotFoundException e) { e.printStackTrace(); throw new RuntimeException(e); } 
		  catch (IOException e) { e.printStackTrace(); throw new RuntimeException(e);}
		
	 }
	
}
 