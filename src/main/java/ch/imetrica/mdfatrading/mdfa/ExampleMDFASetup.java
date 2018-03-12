package ch.imetrica.mdfatrading.mdfa;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.csvreader.CsvReader;

import ch.imetrica.mdfatrader.customization.Customization;
import ch.imetrica.mdfatrader.customization.SmoothingWeight;
import ch.imetrica.mdfatrader.matrix.MdfaMatrix;
import ch.imetrica.mdfatrader.regularization.Regularization;
import ch.imetrica.mdfatrader.series.MultivariateSeries;
import ch.imetrica.mdfatrader.series.SignalSeries;
import ch.imetrica.mdfatrader.series.TargetSeries;
import ch.imetrica.mdfatrader.series.TimeSeries;
import ch.imetrica.mdfatrader.targetfilter.TargetFilter;

public class ExampleMDFASetup {

	
	public static void main(String[] args) throws Exception {
		
		
		
		int nobs 				= 300;
		int nseries 			= 1;
		int f_length			= 20;
		int i1					= 1;
		int i2					= 0;
		double lag				= 0.0;		
		double cutoff			= Math.PI/6;	
		double alpha			= 1.0;	
		double lambda			= 1.0;
		double smooth			= 0.01;		
		double decayStrength	= 0.1;
		double decayStart		= 0.1;
		double crossCorr		= 0.0;
		double shift_const		= 1.0;
		
		MDFABase anyMDFA = new MDFABase(nobs, 
		        nseries, 
		        f_length, 
		        i1,
		        i2,
		        lag, 
		        cutoff,
		        alpha,
		        lambda,
		        smooth,
		        decayStrength,
		        decayStart,
		        crossCorr,
		        shift_const);
		
		
		
		
		
		/*Create raw price time series*/
		TimeSeries<Double> appleSeries = getChunkOfData(0, 500, "data/AAPL.IB.dat", "close");
		TimeSeries<Double> qqqSeries   = getChunkOfData(0, 500, "data/QQQ.IB.dat", "close");
		
		SmoothingWeight myWeight = new SmoothingWeight(anyMDFA);
		TargetFilter myTarget = new TargetFilter(anyMDFA);	
		Regularization anyReg = new Regularization(anyMDFA);
		Customization anyCustom = new Customization(anyMDFA, myWeight, myTarget);	
		MDFASolver mySolver = new MDFASolver(anyCustom, anyReg);
		
		MultivariateSeries multiSeries = new MultivariateSeries(anyMDFA, mySolver);
		
		SignalSeries signal = new SignalSeries(new TargetSeries(appleSeries, 0.4, true), "AAPL");		
		multiSeries.addSeries(signal);
		
		
	

		
	
	}
	
	
	public static TimeSeries<Double> getChunkOfData(int start, int MAX_OBS, String dataFile, String priceName) {
		
		TimeSeries<Double> rawSeries = new TimeSeries<Double>();
		CsvReader marketDataFeed;
		
		int nObs = 0;
		
		try{
			
			 /* Read data market feed from CSV filer and it's headers*/	
			 marketDataFeed = new CsvReader(dataFile);
			 marketDataFeed.readHeaders();

			 while (marketDataFeed.readRecord()) {
				 
				double price = (new Double(marketDataFeed.get(priceName))).doubleValue();
				String date_stamp = marketDataFeed.get("dateTime");				
				rawSeries.add(date_stamp, price);
				
				nObs++;
				
				if(nObs == MAX_OBS) break;
			 }			 
		}
		catch (FileNotFoundException e) { e.printStackTrace(); throw new RuntimeException(e); } 
		catch (IOException e) { e.printStackTrace(); throw new RuntimeException(e);} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rawSeries;		
	}
	
	
}
