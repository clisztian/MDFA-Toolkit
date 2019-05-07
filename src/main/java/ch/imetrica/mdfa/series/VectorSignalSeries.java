package ch.imetrica.mdfa.series;

import java.util.ArrayList;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.imetrica.mdfa.market.Side;
import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.series.MdfaSeries.SeriesType;
import ch.imetrica.mdfa.util.MdfaUtil;
import lombok.Getter;

public class VectorSignalSeries implements MdfaSeries {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final SeriesType seriesType = SeriesType.MULTISIGNAL;
	private DateTimeFormatter formatter;
	private TimeSeries<double[]> signalSeries;
	private TargetSeries target;
	private ArrayList<double[]> coeffs;
	private ArrayList<double[]> original_coeffs;
	private ArrayList<double[]> preFilterCoeffs = null;
	private String name;
	private boolean preFilteringActivated = true;
	
	
	/**
	 * Financial info
	 */
	@Getter
	private double TakeProfit = 30.0;
	@Getter
	private double StopLoss = -30.0;
	@Getter
	private boolean MeanRevert = false;
	@Getter
	private double MeanRevertPips;
	@Getter
	private double LastFillPrice = 0;
	@Getter
	private double CurrentBid = 0;
	@Getter
	private double CurrentAsk = 0;
	@Getter
	private double CurrentMid = 0;
	@Getter
	private double CurrentSpread = 100;
	@Getter
	private Side CurrentSide = Side.NEUTRAL;
	@Getter
	private int CurrentUnitValue = 0;
	@Getter
	private double CurrentPnL = 0;
	@Getter
	private double MeanRevertPrice = 0;
	@Getter
	private double PseudoPnL = 0;
	@Getter
	private boolean WaitingMeanRevert = false;
	private boolean printDebug = false;
	@Getter
	private double CurrentSignal = 0;
	@Getter
	private double PreviousSignal = 0;
	@Getter
	private double RealizedPnl = 0;
	
	
	
	
	public VectorSignalSeries(TargetSeries anytarget, String anyformat) {
		
		this.coeffs = new ArrayList<double[]>();
		this.original_coeffs = new ArrayList<double[]>();
		this.preFilterCoeffs = new ArrayList<double[]>();
		this.target = anytarget;	
		this.formatter = DateTimeFormat.forPattern(anyformat);
		this.signalSeries = new TimeSeries<double[]>();
		this.name = anytarget.getName();
		
	}
	
	public VectorSignalSeries(TargetSeries anytarget, MDFABase[] anyMDFAs, String anyformat) {
		
		this.coeffs = new ArrayList<double[]>();
		this.preFilterCoeffs = new ArrayList<double[]>();
		this.original_coeffs = new ArrayList<double[]>();
		this.target = anytarget;	
		this.formatter = DateTimeFormat.forPattern(anyformat);
		this.signalSeries = new TimeSeries<double[]>();
		this.name = anytarget.getName();
		
	}
	
	public VectorSignalSeries(TargetSeries anytarget) {
		
		this.coeffs = new ArrayList<double[]>();
		this.original_coeffs = new ArrayList<double[]>();
		this.preFilterCoeffs = new ArrayList<double[]>();
		this.target = anytarget;	

		this.signalSeries = new TimeSeries<double[]>();
		this.name = anytarget.getName();
		
	}
	
	/**
     * Replaces the latest filter coefficients
     * coefficients. Recomputes a new signal series based on (new)
     * coefficients. 
     * 
     * If a preFilter exists for this signal, the coefficients will be 
     * convolved with the prefilter to produce the aggregate filter
     * 
     * @param b 
     *     The filter coefficients to store
	 * @throws Exception 
	 * 		if target not defined, won't recompute signal
     * 
     */
	public void setMDFAFilterCoefficients(int i, double[] b) throws Exception {
		
		if(i >= coeffs.size()) {
			addMDFAFilterCoefficients(b);
		}
		
		if(isPrefiltered()) {
			coeffs.set(i, MdfaUtil.convolve(preFilterCoeffs.get(i), b));
			original_coeffs.set(i,b);
		}
		else { 
			coeffs.set(i,b);
		}
		
		if(target != null) {
			this.computeSignalFromTarget();
		}
	}
	
	/**
	 * Add a new set of coefficients to the list of mDFA 
	 * coefficients
	 * 
	 * @param b
	 * @throws Exception
	 */
    public void addMDFAFilterCoefficients(double[] b) throws Exception {
		
		if(isPrefiltered()) {
			
			coeffs.add(MdfaUtil.convolve(preFilterCoeffs.get(coeffs.size()), b));
			original_coeffs.add(b);
		}
		else { 
			coeffs.add(b);
		}
		
		if(target != null) {
			this.computeSignalFromTarget();
		}
	}
	
	
	
	private void computeSignalFromTarget() throws Exception {
		
		if(coeffs.size() == 0) {
			throw new Exception("No MDFA coefficients yet computed for this target series");
		}
		
		if(target == null) {
			throw new Exception("No target series has been defined yet");
		}
		
		signalSeries = new TimeSeries<double[]>();
		
		int N = target.size();
		
		for(int i = 0; i < N; i++) {
			
			double[] sigvec = new double[coeffs.size()];
			for(int m = 0; m < coeffs.size(); m++) {
				
				int filter_length = Math.min(i+1, coeffs.get(m).length);
				for (int l = 0; l < filter_length; l++) {
					sigvec[m] += coeffs.get(m)[l]*target.getTargetValue(i - l);
				}
			}
			signalSeries.add(new TimeSeriesEntry<double[]>(target.getTargetDate(i), sigvec));	
		}	
	}


	@Override
	public void addValue(String date, double val) {
		
		target.addValue(date, val);
		
		CurrentBid = val; 
		CurrentAsk = val; 
		CurrentSpread = 0; 
		CurrentMid = val;
		
		
		if(coeffs.size() > 0) {
			
			int N = target.size();
			double[] sigvec = new double[coeffs.size()];
			for(int m = 0; m < coeffs.size(); m++) {
				
				int filter_length = Math.min(N, coeffs.get(m).length);
				for (int l = 0; l < filter_length; l++) {
					sigvec[m] += coeffs.get(m)[l]*target.getTargetValue(N - l - 1);
				}
			}
			signalSeries.add(new TimeSeriesEntry<double[]>(date, sigvec));	
		
			/**
			 * Compute financial signals
			 */
			
			CurrentSignal = sigvec[0];
			
			if(CurrentSide == Side.LONG) {
				CurrentPnL = CurrentBid - LastFillPrice;
			}
			else if(CurrentSide == Side.SHORT) {
				CurrentPnL = LastFillPrice - CurrentAsk;
			}
			
			
        	if((PreviousSignal > 0 && CurrentSignal < 0) || (PreviousSignal < 0 && CurrentSignal > 0)) {
        	    
        		if(!MeanRevert)  {
        			
        			if(CurrentSide != Side.NEUTRAL) {
        				RealizedPnl = CurrentPnL;
        			}
        			
        			CurrentUnitValue = (CurrentSignal > 0) ? 1 : -1;
        			CurrentSide = (CurrentSignal > 0) ? Side.LONG : Side.SHORT;
        			LastFillPrice = val;
        		}
        		else {
        			
        			if(CurrentSide != Side.NEUTRAL) {
        			
        				RealizedPnl = CurrentPnL; 
        			    
        				if(CurrentSide == Side.LONG) {
        			    	if(printDebug) {
        			    		System.out.println(date + " CurrentPnl = " + CurrentPnL + ", bid = " + CurrentBid + ", lastFill" + LastFillPrice);
        			    	}
        			    }
        			    else {
        			    	if(printDebug) {
        			    		System.out.println(date + " CurrentPnl = " + CurrentPnL + ", ask = " + CurrentAsk + ", lastFill" + LastFillPrice + ", RealizedPnl = " + RealizedPnl);
        			    	}
        			    }
        			}
        			
 
        			CurrentUnitValue = 0; 
        			CurrentSide = Side.NEUTRAL;     			
        			setMeanRevertPrice(val);
        			
        			if(printDebug) {
        				
        				System.out.println("Setting mean-revert at " + date + " for signal " + name 
        				  + ", ask, bid = " + val + ", " + val + ", MeanRevertPrice = " + MeanRevertPrice);
        			}
        		}	
        	}
        	PreviousSignal = CurrentSignal;
		}	
	}

	public String getLatestDate() {
		
		return target.getLatest().getDateTime();
	}
	
	public String getTargetDate(int i) {
		return target.getTargetDate(i);
	}
	
	@Override
	public TimeSeriesEntry<Double> getLatest() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimeSeries<Double> getTimeSeries() {
		return null;
	}

	@Override
	public TimeSeries<Double> getLatestValues(int n) {
		return null;
	}

	@Override
	public SeriesType getSeriesType() {
		return seriesType;
	}

	@Override
	public void setDateFormat(String anyformat) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DateTimeFormatter getDateFormat() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		return target.size();
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public double getTargetValue(int i) {
		return target.getTargetValue(i);
	}

	/**
	 * Get the original underlying value
	 * @param i
	 * @return
	 */
	public double getOriginalValue(int i) {
		return target.getOriginalValue(i);
	}
	
	@Override
	public void chopFirstObservations(int n) {

		target.chopFirstObservations(n);		
		int chopped = Math.min(n, signalSeries.size());
		for(int i = 0; i < chopped; i++) {
			signalSeries.remove(0);
		}	
		
	}

	/**
	 * Returns the prefiltered series at index i
	 * if prefiltering is turned on. Else it 
	 * will return 0
	 * 
	 * @param i
	 * @return
	 *    Prefiltered series at index i, if exists.
	 *    Otherwise 0
	 * @throws Exception 
	 */
	public double getPrefilteredValue(int m, int i) throws Exception {
		
		if(m >= preFilterCoeffs.size()) {
			throw new Exception("Prefilter hasn't been defined for signal " + m);
		}
		
		if(!isPrefiltered()) {
			return 0;
		}
		
		double val = 0;
		int filter_length = Math.min(i+1, preFilterCoeffs.get(m).length);

		for (int l = 0; l < filter_length; l++) {
			val = val + preFilterCoeffs.get(m)[l]*target.getTargetValue(i - l);
		}
		return val;
	}
	
	
	@Override
	public boolean isPrefiltered() {
		return (preFilterCoeffs.size() > 0 && preFilteringActivated);
	}

	public void prefilterActivate(boolean prefilter) {
		preFilteringActivated = prefilter;
	}
	
	public void clearFilters() {
		coeffs.clear();
	}
	
	public void clearPreFilters() {
		preFilterCoeffs.clear();
	}
	
	public void addPrefilter(double[] whiteFilter) {
		preFilterCoeffs.add(whiteFilter);
	}

	public String getSignalDate(int i) {
		
		return signalSeries.get(i).getDateTime();
	}

	public double[] getSignalValue(int i) {

		return signalSeries.get(i).getValue();
	}

	public double[] getLatestSignalValue() {

		return signalSeries.last().getValue();
	}

	public boolean hasFilter() {
		return (coeffs.size() > 0);
	}

	public TargetSeries getTargetSeries() {
		return target;
	}

	public double[] getCoefficientSet(int n) {
		
		if(coeffs.size() == 0) {
			return preFilterCoeffs.get(n);
		}
		return coeffs.get(n);
	}
	
	public double[] getOriginalCoefficients(int n) {
		if(!preFilteringActivated) {
			return coeffs.get(n);
		}
		return original_coeffs.get(n);
	}
	
	public int getNumberPrefilterCoefficientSets() {
		return preFilterCoeffs.size();
	}
	
	public double[] getPrefilterSet(int n) {
		return preFilterCoeffs.get(n);
	}

	public void setPrefilter(int i, double[] whiteFilter) {
		preFilterCoeffs.set(i, whiteFilter);
	}
	
	
	/**
	 * Financial functionality
	 */
	
	/**
	 * Set the current mean-revert price of this signal
	 * @param price
	 */
	public void setMeanRevertPrice(double price) {
		
		if(this.MeanRevert) {

			this.MeanRevertPrice = price;
			this.WaitingMeanRevert = true;
			this.PseudoPnL = 0;
		}
	}
	
	//--- Market Functions -------------------------------
	/**
	 * Sets the fill price
	 * @param fill
	 */
	public void setFillPrice(double fill) {
		this.LastFillPrice = fill;
	}
	
	/**
	 * Sets the current side
	 * @param side
	 */
	public void setSide(Side side) {
		this.CurrentSide = side;
	}	
	
	
	/**
	 * Update the current Pnl
	 * @param bid
	 * @param ask
	 * @param hour
	 * @param time
	 * @return
	 */
	public double updatePnl(String time, double val) {
		
		
		CurrentBid = val; 
		CurrentAsk = val; 
		CurrentSpread = 0; 
		CurrentMid = val;
		CurrentPnL = 0;
		double realizedPnl = 0;
		

		//--- Check PnL for positions open---------
		if(CurrentSide == Side.LONG) {
			CurrentPnL = CurrentBid - LastFillPrice;
		}
		else if(CurrentSide == Side.SHORT) {
			CurrentPnL = LastFillPrice - CurrentAsk;
		}

		
		if(CurrentPnL > TakeProfit)  {

			CurrentUnitValue = 0;
			CurrentSide = Side.NEUTRAL;
			
			if(printDebug) {
				System.out.println("Take-profit at " + time + " for signal " + name + ", with CurrentPnL/TakeProfit " 
			  + CurrentPnL + "/" + TakeProfit  + ", ask, bid = " + val + ", " + val + ", FillPrice = " + LastFillPrice);
			}
			realizedPnl = CurrentPnL;
		}
		else if(CurrentPnL < StopLoss)  {
			
			CurrentUnitValue = 0;
			CurrentSide = Side.NEUTRAL;
			
			if(printDebug) {
				System.out.println("Stop-loss at " + time + " for signal " + name + ", with CurrentPnL/StopLoss " 
			  + CurrentPnL + "/" + StopLoss  + ", ask, bid = " + val + ", " + val + ", FillPrice = " + LastFillPrice);
			}
			realizedPnl = CurrentPnL;
		}
		

		
		if(this.MeanRevert && this.WaitingMeanRevert) {
			
			if(CurrentSignal > 0) {
				PseudoPnL = CurrentMid - MeanRevertPrice;
			}
			else if(CurrentSignal < 0) {
				PseudoPnL = MeanRevertPrice - CurrentMid;
			}
		
			if(printDebug) {
				System.out.println("Mean-revert pseudoPnl " + time + " for signal " + name + ", with PseudoPnL/MeanRevertPips " 
			  + PseudoPnL + "/" + MeanRevertPips  + ", ask, bid = " + val + ", " + val + ", MeanRevertPrice = " + MeanRevertPrice);
			}
			
		    if(PseudoPnL < MeanRevertPips) {
		    	
    			CurrentUnitValue = (CurrentSignal > 0) ? 1 : -1;
    			CurrentSide = (CurrentSignal > 0) ? Side.LONG : Side.SHORT;		
    			WaitingMeanRevert = false;
    			LastFillPrice = val;
    			
    			if(printDebug) {
    				
    				System.out.println("Mean-revert satisfied " + time + " for signal " + name + ", with MeanRevertPips " + MeanRevertPips 
    				  + ", ask, bid = " + val + ", " + val + ", FillPrice = " + LastFillPrice + ", " + CurrentUnitValue);
    			}
		    }
		}
		
		RealizedPnl = realizedPnl;
		return CurrentPnL;
	}		
	
	public double getRealizedPnl() {
		return RealizedPnl;
	}
	
	public double getUnrealizedPnl() {
		return CurrentPnL;
	}
}
