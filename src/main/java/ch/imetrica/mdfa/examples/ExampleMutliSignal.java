package ch.imetrica.mdfa.examples;

import ch.imetrica.mdfa.datafeeds.CsvFeed;
import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.series.MultivariateSignalSeries;
import ch.imetrica.mdfa.series.TargetSeries;
import ch.imetrica.mdfa.series.TimeSeries;

public class ExampleMutliSignal {

	
	public static void main(String[] args) throws Exception {
		
		
		
		MDFABase[] anyMDFAs = new MDFABase[3];
		
		anyMDFAs[0] = (new MDFABase()).setLowpassCutoff(Math.PI/20.0)
				.setI1(1)
				.setHybridForecast(.01)
				.setSmooth(.3)
				.setDecayStart(.1)
				.setDecayStrength(.2)
				.setLag(-2.0)
				.setLambda(2.0)
				.setAlpha(2.0)
				.setSeriesLength(400);
		
		anyMDFAs[1] = (new MDFABase()).setLowpassCutoff(Math.PI/10.0)
				.setBandPassCutoff(Math.PI/15.0)
				.setSmooth(.1)
				.setSeriesLength(400);
		
		anyMDFAs[2] = (new MDFABase()).setLowpassCutoff(Math.PI/5.0)
                .setBandPassCutoff(Math.PI/10.0)
                .setSmooth(.1)
                .setSeriesLength(400);
		
		
		TimeSeries<Double> appleSeries = CsvFeed.getChunkOfData(0, 600, "data/AAPL.IB.dat", "dateTime", "close");	
		MultivariateSignalSeries signal = new MultivariateSignalSeries(new TargetSeries(appleSeries, .6, true), 
				anyMDFAs, "yyyy-MM-dd")
				.computeFilterCoefficients()
				.computeSignalsFromTarget();
		
		signal.chopFirstObservations(70);
		signal.plotSignals("Tres amigos");
		
		
	}
	
	
	
}
