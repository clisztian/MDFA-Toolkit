package ch.imetrica.mdfa.market;

import java.text.DecimalFormat;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.imetrica.mdfa.datafeeds.CsvFeed;
import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.series.TimeSeries;
import ch.imetrica.mdfa.series.TimeSeriesEntry;

public class Trader {

	static private DecimalFormat df = new DecimalFormat("#.##");
	private DateTimeFormatter dtfOut;
	private Strategy myStrategy;
	private DateTime dt;
	private Random rng; 
	private TimeSeries<Double> unrealized;
	private TimeSeries<Double> realized;
	private double cumRealized = 0;
	
	public Trader() {		
		
		myStrategy = new Strategy();
		rng = new Random(123);
		
		unrealized = new TimeSeries<Double>();
		realized = new TimeSeries<Double>();
	}
	
	public Trader addStrategy(MDFABase anyMdfa) {		
		myStrategy.addStrategy(anyMdfa);
		return this;
	}
	
	public Trader build() throws Exception {		
		myStrategy.build();
		return this;
	}
	
	public final Strategy getMyStrategy() {
		return myStrategy;
	}
	
	/**
	 * Initiate nValues from a Gaussian distribution to generate 
	 * enough values for a signal generator
	 * @param nValues
	 * @param startDate
	 * @param mean
	 * @param stdev
	 * @throws Exception
	 */
	public void initiate(int nValues, DateTime startDate, double mean, double stdev) throws Exception {
		
		dt = startDate;
		dtfOut = DateTimeFormat.forPattern("yyyy-MM-dd");
		
		dt = dt.minusDays(nValues+1);
		
		for(int i = 0; i < nValues; i++) {
			
			String date_stamp = dt.toString(dtfOut);
			double val = rng.nextGaussian()*stdev + mean;
			
			myStrategy.add(date_stamp, val, val);
			dt = dt.plusDays(1);			
		}
		myStrategy.computeCoefficients();
				
	}
	
	/**
	 * Add a new value
	 * Position is computed 
	 * @param date
	 * @param val
	 * @throws Exception
	 */
	public void add(String date, double val, double close) throws Exception {
		
		myStrategy.add(date, val, close);
	}
	
	public void updatePortfolio(String time, double unreal, double real) {
		
		cumRealized += real;
		
		unrealized.add(new TimeSeriesEntry<Double>(time, unreal));
		realized.add(new TimeSeriesEntry<Double>(time, cumRealized));
		
	}
	

	public void printPortfolio() {
		
		for(int i = 0; i < unrealized.size(); i++) {
			
			System.out.println(unrealized.get(i).getTimeStamp() + ", " + df.format(unrealized.get(i).getValue()) 
			+ " " + df.format(realized.get(i).getValue()));
		} 
		
	}
	
	@Override
	public String toString() {		
		return myStrategy.toString();			
	}
	
	public static void main(String[] args) throws Exception {

		Random rng = new Random(123);
		
		CsvFeed marketFeed = new CsvFeed("data/CEVA.SW.daily.csv", "Index", "Close", "High", "Low");
		
		MDFABase anyMdfa = (new MDFABase()).setLowpassCutoff(Math.PI/20.0)
				.setI1(1)
				.setHybridForecast(.01)
				.setSmooth(.3)
				.setDecayStart(.1)
				.setDecayStrength(.2)
				.setLag(-2.0)
				.setLambda(2.0)
				.setAlpha(2.0)
				.setSeriesLength(300);
		
		
		Trader trader = new Trader().addStrategy(anyMdfa).build();
		
		trader.initiate(300, new DateTime().withDate(2018, 5, 4), 27.45, 1.0);
		
		
		System.out.println("Starting market feed");
		while(true) {
			
			TimeSeriesEntry<double[]> newBar = marketFeed.getNextBar();
			
			if(newBar == null) {
				break;
			}
			
			
			double[] price = newBar.getValue();
			double v = rng.nextDouble();
			double mktPrice = price[0]*(1.0 - v) + price[1]*v; 
			
			

			trader.add(newBar.getTimeStamp(), mktPrice, price[2]);
			
			trader.updatePortfolio(newBar.getTimeStamp(), 
					               trader.getMyStrategy().getCurrentPnl(), 
					               trader.getMyStrategy().getRealizedPnl());
			
			System.out.println(newBar.getTimeStamp() + " " + df.format(price[0]) + " " + 
				    df.format(price[1]) + " " + df.format(price[2]) + " | " + df.format(mktPrice) + " " + trader.toString());
		
			
		}
		
		trader.printPortfolio();
		
		
	}
	
	
	
	
}
