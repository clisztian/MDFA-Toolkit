package ch.imetrica.mdfa.label;

import java.io.Serializable;

import ch.imetrica.mdfa.series.TimeSeries;

/**
 * 
 * Interface to create a labeled series for machine learning 
 * applications. 
 * 
 * There are three categories of labeling processes that this interface
 * will offer:
 * 
 * 1) Observational labeling: every time series observation is labeled
 * 2) Fixed Period labeling: every period (day, week, etc) is labeled
 * 3) Regime labeling: every regime change is labeled
 * 
 * Additionally, there are two types of labels that can be c
 * 
 * 
 * 
 * @author Christian D. Blakely (clisztian@gmail.com)
 *
 */
public interface Labelizer extends Serializable {

	public enum LabelType {
		
		OBSERVATIONAL,  /* Every time series observation is labeled */
		FIXED_PERIOD,   /* Certain fixed-period length labeled */
		REGIME;		    /* Undetermined length of time labeled */
	}
	
	public TimeSeries<double[]> labelTimeSeries(TimeSeries<double[]> series);
	

	
}
