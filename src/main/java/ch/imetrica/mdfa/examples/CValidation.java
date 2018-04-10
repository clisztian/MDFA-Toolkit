package ch.imetrica.mdfa.examples;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.series.MultivariateSignalSeries;
import ch.imetrica.mdfa.series.TargetSeries;
import ch.imetrica.mdfa.series.TimeSeries;
import ch.imetrica.mdfa.series.TimeSeriesEntry;

public class CValidation {

	
	
	public static void main(String[] args) throws Exception {
		
		String strline;
		DateTime dt = new DateTime(2018,4,4,15,24);
		ArrayList<Double> myData = new ArrayList<Double>();
		FileInputStream fin = new FileInputStream(new File("data/series0.txt"));
		DataInputStream din = new DataInputStream(fin);
		BufferedReader br = new BufferedReader(new InputStreamReader(din));
		

        while((strline = br.readLine()) != null) {
        	myData.add(new Double(strline));
        }
		  
		
		
		MDFABase[] anyMDFAs = new MDFABase[1];
		
		anyMDFAs[0] = (new MDFABase())
				  .setAlpha(20)
			      .setLag(-1.0)
			      .setLambda(40)
			      .setBandPassCutoff(0.0)
			      .setLowpassCutoff(.56)
			      .setCrossCorr(0.0)
			      .setDecayStart(0.5)
			      .setDecayStrength(0.5)
			      .setSmooth(0.5)
			      .setFilterLength(30)
			      .setI1(1)
			      .setI2(1)
			      .setShift_constraint(-5.0)
			      .setSeriesLength(300);	
		
		
		TimeSeries<Double> series0 = new TimeSeries<Double>();
		
		for(int i = 0; i < myData.size(); i++) {
			series0.add(new TimeSeriesEntry<Double>(dt.toString(DateTimeFormat.forPattern("yyyy-MM-dd")), myData.get(i)));
			dt = dt.plusDays(1);
		}
		
		MultivariateSignalSeries signal = new MultivariateSignalSeries(new TargetSeries(series0, 0, false), anyMDFAs, "yyyy-MM-dd")
				.computeFilterCoefficients()
				.computeSignalsFromTarget();
				
		double[] myCoeffs = signal.getCoefficients(0);
		for(int i = 0; i < myCoeffs.length; i++) {
			System.out.println(i + ", " + myCoeffs[i]);		
		}

		
	}
	
	
}
