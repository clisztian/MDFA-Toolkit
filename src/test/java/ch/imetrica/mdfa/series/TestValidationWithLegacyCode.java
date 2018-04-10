package ch.imetrica.mdfa.series;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import ch.imetrica.mdfa.mdfa.MDFABase;

public class TestValidationWithLegacyCode {

	@Test
	public void testValidation() throws Exception {
		
		double[] validationCoeffs = new double[]{-0.002245,
		-0.006644,
		-0.012360,
		-0.018759,
		-0.025867,
		-0.033461,
		-0.040972,
		-0.047984,
		-0.053755,
		-0.057421,
		-0.058148,
		-0.055126,
		-0.048189,
		-0.037652,
		-0.023923};
		
		
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
			      .setLambda(5)
			      .setBandPassCutoff(0.0)
			      .setLowpassCutoff(.22)
			      .setCrossCorr(0.0)
			      .setDecayStart(0)
			      .setDecayStrength(0)
			      .setSmooth(0.5)
			      .setFilterLength(15)
			      .setI1(0)
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
		
		assertEquals(validationCoeffs[0], myCoeffs[0], .000001);
		assertEquals(validationCoeffs[4], myCoeffs[4], .000001);
		assertEquals(validationCoeffs[10], myCoeffs[10], .000001);
		assertEquals(validationCoeffs[14], myCoeffs[14], .000001);
		
	}

}
