package ch.imetrica.mdfatrader.transform;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

import ch.imetrica.mdfatrader.series.TimeSeries;
import ch.imetrica.mdfatrader.series.TimeSeriesEntry;




public class Transform {

	double d;          /* Fractional difference (1 - B)^d */
	double[] f_weights;  /* Fractional differencing weights */	
	private boolean logTransform;
	public double weight_threshold = .0001;
	
	public double baseTransform(double v) {
		
		return (logTransform) ? Math.log(v) : v;
	}
	
	public Transform(double d, boolean log) {
		
		logTransform = log;
		this.d = d;
		
		computeFractionalDifferenceWeights(weight_threshold);		
	}
		
	
	private void computeFractionalDifferenceWeights(double thresh) {
		
	  if(d > 0) {
		  
		double wk = 1.0;
		double wk1;
		double k = 1.0;
		boolean overThresh = true;	
		
		Double[] frac_w;
		ArrayList<Double> myWs = new ArrayList<Double>();
		myWs.add(new Double(wk));
		
		while(overThresh) {
			
			wk1 = -wk * (d - k + 1.0)/k;
			
			myWs.add(new Double(wk1));
			wk = wk1;
			k = k + 1.0;
			
			if(Math.abs(wk) < thresh) {
				overThresh = false;
			}			
		}
		
		frac_w = myWs.toArray(new Double[myWs.size()]);
		f_weights = ArrayUtils.toPrimitive(frac_w);
		
	  }
		
	}

	
	
	
	public TimeSeries<double[]> applyTransform(TimeSeries<Double> anyseries) {
		
		TimeSeries<double[]> transformedSeries = new TimeSeries<double[]>();
	
		if(d < 1 && d > 0) {
		
			double sum = 0;
			int filter_length = 0;
			int w_length = f_weights.length;
			
			for(int N = 1; N < anyseries.size(); N++) {
			
				filter_length = Math.min(N+1, w_length);	
				sum = 0;
				for (int l = 0; l < filter_length; l++) {
					sum = sum + f_weights[l]*baseTransform(anyseries.get(N - l).getValue());
				}
				
				double[] values = new double[]{sum, anyseries.get(N).getValue()};
				transformedSeries.add(new TimeSeriesEntry<double[]>(anyseries.get(N).getDateTime(), values));		
			}		
			
		}
		else if(d == 1) {
			
			for(int N = 1; N < anyseries.size(); N++) {
				
				double val = baseTransform(anyseries.get(N).getValue()) - 
						baseTransform(anyseries.get(N-1).getValue());
				
				double[] values = new double[]{val, anyseries.get(N).getValue()};				
				transformedSeries.add(new TimeSeriesEntry<double[]>(anyseries.get(N).getDateTime(), values));	
				
			}			
		}
		else {
			
			for(int N = 0; N < anyseries.size(); N++) {
				double val = baseTransform(anyseries.get(N).getValue());
				double[] values = new double[]{val, anyseries.get(N).getValue()};				
				transformedSeries.add(new TimeSeriesEntry<double[]>(anyseries.get(N).getDateTime(), values));	
				
			}					
		}
		
		return transformedSeries;	
	}

	public void addValue(TimeSeries<double[]> timeSeries, double val, String date) {
		
		
		timeSeries.add(new TimeSeriesEntry<double[]>(date, new double[]{0, val}));
		
		int N = timeSeries.size();
		
		if(d < 1 && d > 0) {
			
			double sum = 0;
			int w_length = f_weights.length;
			int filter_length = Math.min(N, w_length);
			
		    sum = 0;
			for (int l = 0; l < filter_length; l++) {
					sum = sum + f_weights[l]*baseTransform(timeSeries.get(N - l - 1).getValue()[1]);
			}
			
			timeSeries.get(N-1).getValue()[0] = sum;	
			
		}
		else if(d == 1) {
			

				double v = baseTransform(timeSeries.get(N - 1).getValue()[1]) - 
						baseTransform(timeSeries.get(N - 2).getValue()[1]);
				
				timeSeries.get(N-1).getValue()[0] = v;
						
		}
		else {
			
			double v = baseTransform(timeSeries.get(N - 1).getValue()[1]);			
			timeSeries.get(N-1).getValue()[0] = v;
									
		}				
	}

	public TimeSeries<Double> applyLogTransform(TimeSeries<Double> anyseries, boolean log) {
		
		TimeSeries<Double> transformedSeries = new TimeSeries<Double>();
		for(int N = 0; N < anyseries.size(); N++) {
			
			double val = baseTransform(anyseries.get(N).getValue());			
			transformedSeries.add(new TimeSeriesEntry<Double>(anyseries.get(N).getDateTime(), val));	
			
		}
		
		
		return null;
	}
	
	
	public String toString() {
		
		String w = "\n Weights for d = " + d + "\n";
		
		if(d == 0) {return (w += "\n w_0 = 1");}
		
		for(int i = 0; i < f_weights.length; i++) {
			w += f_weights[i] + "\n";
		}
		return w;
	}

	public void addPrice(TimeSeries<Double> timeSeries, double val, String date) {
		timeSeries.add(new TimeSeriesEntry<Double>(date, baseTransform(val)));
	}
		
}
