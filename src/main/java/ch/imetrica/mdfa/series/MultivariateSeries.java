package ch.imetrica.mdfa.series;

import java.util.ArrayList;

import org.jfree.ui.RefineryUtilities;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.imetrica.mdfa.matrix.MdfaMatrix;
import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.mdfa.MDFAFactory;
import ch.imetrica.mdfa.mdfa.MDFASolver;
import ch.imetrica.mdfa.plotutil.TimeSeriesPlot;
import ch.imetrica.mdfa.series.MdfaSeries.SeriesType;
import ch.imetrica.mdfa.spectraldensity.SpectralBase;

/**
 * 
 * A Multivariate time series holds a collection of information with the goal
 * of providing a robust real-time signal or indicator that will be used for several
 * purposes such as:
 * 1) financial trading/investing on a given asset 
 * 2) extracting signals or adjusting time series 
 * 3) detecting turning points
 * 4) classifying regimes in real-time 
 * 
 * The Multivariate time series object possesses several computational 
 * components to serve helping engineer the above signal types. This includes 
 * the following:
 * 1) A list of @MdfaSeries which include MDFA signals, price information, 
 *    target series, explanatory series, or any additional technical indicators
 * 2) An aggregate real-time signal of the entire Multivariate series which 
 *    serves as the aggregate signal of all the MDFA signals 
 * 3) An MDFA solver object which holds the computational engine needed for constructing 
 *    a real-time signal extraction process using MDFA
 * 4) An MDFABase object which holds the meta-information for MDFA
 * 5) A labelling timeSeries which is used for constructing a labeled time series
 *    for the Machine Learning packages (recurrent neural networks, reinforcement learning, 
 *    random forests, Xtreme learning). 
 * 6) (Not yet implemented) A MasterSignal which serves as the head aggregate signal for the entire 
 *    multivariate time series combining the MDFA and machine learning components
 *
 * 
 * @author Christian D. Blakely (clisztian@gmail.com)
 *
 */
public class MultivariateSeries {

	
	TimeSeries<double[]> labeledSignal;
	TimeSeries<Double> aggregateSignal;
	ArrayList<MdfaSeries> anySeries;
	MDFASolver anySolver;
	DateTimeFormatter formatter;
	
	private int numberOfSignals = 0;
	private int targetIndex = 0;
	
	public MultivariateSeries(MDFASolver anySolver) {
		
		this.aggregateSignal = new TimeSeries<Double>();
		this.anySeries = new ArrayList<MdfaSeries>();
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
		if(anySeries.size() > 0 && series.size() > 0) {
			
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
				numberOfSignals++;
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
    	
    	double sigVal = 0;
    	for(int i = 0; i < anySeries.size(); i++) { 		
    		anySeries.get(i).addValue(date, val[i]);
    		
    		if(anySeries.get(i).getSeriesType() == SeriesType.SIGNAL) {
				sigVal += ((SignalSeries) anySeries.get(i)).getLatestSignalValue();
			}		
    	}
    	aggregateSignal.add(new TimeSeriesEntry<Double>(date, sigVal));  	
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
    	
    	double sigVal = 0;
    	for(int i = 0; i < anySeries.size(); i++) { 		
    		anySeries.get(i).addValue(date, val.get(i));
    		
    		if(anySeries.get(i).getSeriesType() == SeriesType.SIGNAL) {
				sigVal += ((SignalSeries) anySeries.get(i)).getLatestSignalValue();
			}		
    	}
    	aggregateSignal.add(new TimeSeriesEntry<Double>(date, sigVal));
    	
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
				sigVal += ((SignalSeries) anySeries.get(i)).getLatestSignalObservation().getValue();
			}
		}
		return sigVal;
	}
    
	
	/**
	 * Returns a timeSeriesEntry with the aggregate signal of
	 * this multivariate time series and the target series that is 
	 * being filtered. By default, the target series is typically 
	 * the first signal/series in the multivariate series
	 * 
	 * @param i index of value
	 * @return TimeSeriesEntry<double[]> 
	 * @throws Exception is thrown if for some reason the time stamps of the 
	 * 		   given value do not match. Should not happen.
	 */
	public TimeSeriesEntry<double[]> getSignalTargetPair(int i) throws Exception {
		
		
		 String datetime = aggregateSignal.get(i).getDateTime();
		 double sigval = aggregateSignal.get(i).getValue();
		 
		 String tsdatetime = ((SignalSeries) anySeries.get(targetIndex)).getTargetDate(i);
		 double val = ((SignalSeries) anySeries.get(targetIndex)).getTargetValue(i);
		 
		 
		 if(!datetime.equals(tsdatetime)) {
			 throw new Exception("Dates do not match: " + datetime + " " + tsdatetime);
		 }
		 
		 double[] vals = new double[]{val, sigval};
		 return (new TimeSeriesEntry<double[]>(datetime, vals));
	}
	
	
	/**
	 * 
	 * Compute the MDFA filter coefficients given the most 
	 * recent N time series observations and the given 
	 * MDFA Base and solver. 
	 * 
	 * This will first set how many series/signals are in the estimation
	 * for the filter coefficients in the MDFABase object and then 
	 * extract the latest N points from each series to send to the spectral 
	 * base for recomputing the spectral information
	 * 
	 * @throws Exception
	 */
	public void computeFilterCoefficients() throws Exception {
		
		anySolver.getMDFAFactory().setNumberOfSeries(numberOfSignals);
		SpectralBase base = new SpectralBase(anySolver.getMDFAFactory().getSeriesLength());
		base.addMultivariateSeries(this);
		anySolver.updateSpectralBase(base);
		MdfaMatrix bcoeffs = anySolver.solver();
		
		int signal = 0;
		int L = anySolver.getMDFAFactory().getFilterLength();
		this.aggregateSignal = new TimeSeries<Double>();
		for(int i = 0; i < anySeries.size(); i++) {
			
			if(anySeries.get(i).getSeriesType() == SeriesType.SIGNAL) {
				
				double[] sig_coeffs = bcoeffs.getSubsetColumn(0, signal*L, signal*L + L);
				((SignalSeries) anySeries.get(i)).setMDFAFilterCoefficients(sig_coeffs);
				
				addSignalToAggregate((SignalSeries) anySeries.get(i));
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
		return numberOfSignals;
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
	
	public void printSignal() {
		
		SignalSeries sig = (SignalSeries) anySeries.get(0);
		
		System.out.println("SigSize: " + sig.signalSize() + ", targetSize: " + sig.size());
	}
	
	
	
	public void plotAggregateSignal(String myTitle) throws Exception {
		
		if(this.aggregateSignal.size() > 10) {	
		   
			
			
	    	final String title = myTitle;
	        final TimeSeriesPlot eurusd = new TimeSeriesPlot(title, this);
	        eurusd.pack();
	        RefineryUtilities.positionFrameRandomly(eurusd);
	        eurusd.setVisible(true);
	     }
	     else {
	   	  System.out.println("Need more than 10 signal observations");
	     }
		
	}
	
	/**
	 * 
	 * Chops off the first n values of all the time series in this
	 * multivariate series. Typically used to free up memeory 
	 * 
	 * @param n
	 *   Number of observations to eliminate from all time series
	 */
	public void chopFirstObservations(int n) {
		
		for(int i = 0; i < anySeries.size(); i++) {
			anySeries.get(i).chopFirstObservations(n);
		}
		if(aggregateSignal != null && aggregateSignal.size() > 0) {
			
			int chopped = Math.min(n, aggregateSignal.size());
			for(int i = 0; i < chopped; i++) {
				aggregateSignal.remove(0);
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
	 * Set the Joda Dateformat that will be used with this 
	 * signal time series. Should be consistent with the other 
	 * time series
	 * 
	 * @param format
	 *    The format of the datetime stamp (usually something like 
	 *    "yyyy-MM-dd HH:mm:ss")
	 */
	public MultivariateSeries setDateFormat(String format) {
		this.formatter = DateTimeFormat.forPattern(format);
		return this;
	}
	
	private void addSignalToAggregate(SignalSeries signal) throws Exception {
		
		if(aggregateSignal.isEmpty()) {				
			for(int i = 0; i < signal.size(); i++) {
				
			      String current = signal.getSignalDate(i);	
			      double val = signal.getSignalValue(i);
			      aggregateSignal.add(new TimeSeriesEntry<Double>(current, val));
			}		
		}
		else {
			
			for(int i = 0; i < signal.size(); i++) {
				
		      String current = signal.getSignalDate(i);	
				
			  if(current.equals(aggregateSignal.get(i).getDateTime())) {
				  
				  double val = aggregateSignal.get(i).getValue() + signal.getSignalValue(i);
				  aggregateSignal.set(i, new TimeSeriesEntry<Double>(current, val));	  
			  }
			  else {
				  throw new Exception("Dates do not match of the signals: " + current + " is not " + aggregateSignal.get(i).getDateTime());
			  }
 			}
			
		}
	}
	
	/**
	 * Gets access to this MDFAFactor for updating/changing dimensions 
	 * or parameters of the MDFA filtering process
	 * 
	 * @return
	 *   This MDFAFactor accessed from the MDFASolver    
	 *
	 */
	public MDFAFactory getMDFAFactory() {
		
		return this.anySolver.getMDFAFactory();
	}
	
    
	
	
	
}
