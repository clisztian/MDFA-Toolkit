package ch.imetrica.mdfa.market;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.joda.time.format.DateTimeFormatter;

import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.series.MultivariateFXSeries;
import ch.imetrica.mdfa.series.TimeSeries;
import ch.imetrica.mdfa.series.TimeSeriesEntry;

public class SimulateStrategy {

	
	private TradingPerformance trader; 
	private MarketFeed market;
	private KeyPerformanceIndicator kpi;
	private DecimalFormat df = new DecimalFormat("#.##");
	private DateTimeFormatter myFormatter; 
	private MultivariateFXSeries strategy;
	private ArrayList<MDFABase> anyMDFAs;
	private String strategyName;
	private HashMap<String, Integer> insideInformation;
	
	private int n_strategies;
	private int[] current_state;
	private boolean print_results;
	private float tradeFreq;
	private int mytrade;
	private int[] signal;
	private boolean insider = false;
	
	public SimulateStrategy(String market_name, String strategyName, int n_strategies, float tradeFreq, boolean print) throws IOException {
		
		this.setStrategyName(strategyName);
		market = new MarketFeed(market_name);
		this.n_strategies = n_strategies;
		current_state = new int[n_strategies];
		trader = new TradingPerformance(n_strategies);
		this.tradeFreq = tradeFreq;
		insideInformation = new HashMap<String, Integer>();
		print_results = print;
		signal = new int[n_strategies];
	}
	
	
	public void simulate(float[] prob) throws Exception {
		
		Random rng = new Random(23);
		ArrayList<String> history = new ArrayList<String>();
		
		if(prob.length != n_strategies) {
			throw new Exception("State changer length must be equal to n_strategies");
		}
		
		
		while(true) {
			
			TimeSeriesEntry<double[]> entry = market.getNextPrice();
			
			if(entry == null) {
				break;
			}
			
			String timestamp = entry.getTimeStamp();
			double price = entry.getValue()[0];
			double close = entry.getValue()[1];
			double open = entry.getValue()[2];
			
			double noNews = entry.getValue()[3];
			double good = entry.getValue()[4];
			double bad = entry.getValue()[5];
			
			//System.out.println(timestamp + ", " + noNews + ", " + good + ", " + bad);
			
			
			
			mytrade = 0;
			for(int i = 0; i < n_strategies; i++) {
				
				signal[i] = rng.nextFloat() < prob[i] ? -1 : 1;
				signal[i] = rng.nextFloat() < tradeFreq ? signal[i] : 0;
				
				if(insider ) {
					if(rng.nextFloat() < good) signal[i] = -1;
					else if(rng.nextFloat() < bad) signal[i] = 1;
				}
				
				
				mytrade += signal[i];	
			}

			history.add(timestamp + ", price: " + df.format(price) + ", close: " + df.format(open) + ", position: (" + mytrade + ") ");			
			trader.addEvent(timestamp, Math.log(price), Math.log(open), signal);
			
		}
				
		if(print_results) {
			
			TimeSeries<Double> myPerformance = trader.getRealized();
			
			for(int i = 0; i < myPerformance.size(); i++) {
				
				String time = myPerformance.get(i).getDateTime();
				double real = myPerformance.get(i).getValue();
				
			    System.out.println(history.get(i) + " " + df.format(real) + "   " + time);			
			}
		}
				
		kpi = new KeyPerformanceIndicator(trader);
		kpi.computeKPIs();	
		//System.out.println(kpi.toString());
	}
	
	public KeyPerformanceIndicator getKPI() {
		return kpi;
	}

	public int getN_strategies() {
		return n_strategies;
	}

	public void setN_strategies(int n_strategies) {
		this.n_strategies = n_strategies;
	}

	public String getStrategyName() {
		return strategyName;
	}

	public void setStrategyName(String strategyName) {
		this.strategyName = strategyName;
	}
	
	public boolean getInsider() {
		return insider;
	}
	
	public void setInsider(boolean insider) {
		this.insider = insider;
	}
	
	public void closeMarket() {
		market.close();
	}
	
	
	/**
	 * Adds inside information event (eq, News, GreenDays)
	 * signal (-1, 1) determines whether to short/buy
	 * @param date
	 * @param signal
	 */
	public void addInsideInformation(String date, int signal) {
		
		if(!insideInformation.containsKey(date)) {
			insideInformation.put(date, signal);
		}		
	}
	
	
	public static void main(String[] args) throws Exception {
		
		DecimalFormat df = new DecimalFormat("#.##");
		
		int n_simulations = 1;
		double[] sharpeDistribution = new double[n_simulations];
		
		for(int i = 0; i < n_simulations; i++) {
			
			SimulateStrategy simulator = new SimulateStrategy("data/AVEC.all.csv", "Bobo", 3, 0f, true);
			
			float[] strategy = new float[] {.5f, .3f, .1f};
			simulator.simulate(strategy);	
			
			sharpeDistribution[i] = simulator.getKPI().getSharpeRatio();			
			simulator.closeMarket();
			
			System.out.println(df.format(sharpeDistribution[i]));
		}
	}


	public double getKPI(int kpiChoice) {	
		return kpi.getKPI(kpiChoice);
	}



	
	
	
}
