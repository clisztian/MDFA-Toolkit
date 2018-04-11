package ch.imetrica.mdfa.series;

import java.util.ArrayList;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.series.MdfaSeries.SeriesType;
import ch.imetrica.mdfa.util.MdfaUtil;

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
	private ArrayList<double[]> preFilterCoeffs = null;
	private String name;
	
	public VectorSignalSeries(TargetSeries anytarget, String anyformat) {
		
		this.coeffs = new ArrayList<double[]>();
		this.preFilterCoeffs = new ArrayList<double[]>();
		this.target = anytarget;	
		this.formatter = DateTimeFormat.forPattern(anyformat);
		this.signalSeries = new TimeSeries<double[]>();
		this.name = anytarget.getName();
		
	}
	
	public VectorSignalSeries(TargetSeries anytarget, MDFABase[] anyMDFAs, String anyformat) {
		
		this.coeffs = new ArrayList<double[]>();
		this.preFilterCoeffs = new ArrayList<double[]>();
		this.target = anytarget;	
		this.formatter = DateTimeFormat.forPattern(anyformat);
		this.signalSeries = new TimeSeries<double[]>();
		this.name = anytarget.getName();
		
	}
	
	public VectorSignalSeries(TargetSeries anytarget) {
		
		this.coeffs = new ArrayList<double[]>();
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimeSeries<Double> getLatestValues(int n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SeriesType getSeriesType() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getTargetValue(int i) {
		return target.getTargetValue(i);
	}

	@Override
	public void chopFirstObservations(int n) {

		target.chopFirstObservations(n);		
		int chopped = Math.min(n, signalSeries.size());
		for(int i = 0; i < chopped; i++) {
			signalSeries.remove(0);
		}	
		
	}

	@Override
	public boolean isPrefiltered() {
		return (preFilterCoeffs.size() > 0);
	}

	public void clearFilters() {
		preFilterCoeffs.clear();
		coeffs.clear();
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

	
	
}
