package ch.imetrica.mdfa.market;

import java.text.DecimalFormat;
import java.util.ArrayList;

import ch.imetrica.mdfa.series.TimeSeries;


/**
 * A collection of KeyPeformanceIndicators that are computed from a give TradingPerformance.
 * The KPIs that computed so far are 
 * 
 *  -Max Drawdown
 *  -Sharpe Ratio
 *  -Win percentage
 *  -Max Drawdown
 *  -Peak Performance
 *  -Max Loss
 *  -Peak Gain
 *  -Avg Gain
 *  -Avg Loss
 *  -ROI
 *  
 * @author lisztian
 *
 */
public class KeyPerformanceIndicator {

	private DecimalFormat df;
	private TradingPerformance performance;
	private double[] drawdown;
	
	private int ntrades;
	private double sharpeRatio;
	private double winPercentage;
	private double maxDrawdown;
	private double peakPerformance;
	private double maxLoss;
	private double peakGain;
	private double avgGain;
	private double avgLoss;
	private double roi;
	private double meanReturn;
	
	
	public KeyPerformanceIndicator(TradingPerformance performance) {		
		this.performance = performance;
		df = new DecimalFormat("#.##");
	}
	
	/**
	 * Computes all the KPIs given the historical trading performance.
	 * All KPIs based on realized returns
	 */
	public void computeKPIs() {
		
		TimeSeries<Double> realized = performance.getRealized();		
		
		roi = realized.last().getValue();
		
		ArrayList<Double> avgGains = new ArrayList<Double>();
		ArrayList<Double> avgLosses = new ArrayList<Double>();
		
		ntrades = 0;
		int count_win = 0;
		meanReturn = 0;
		double sd = 0;
		maxLoss = 0;
		peakGain = 0;
		avgGain = 0;
		avgLoss = 0;
		
		for(int t = 1; t < realized.size(); t++) {			
			
			double myReturn = realized.get(t).getValue() - realized.get(t-1).getValue(); 	
			meanReturn += myReturn;	
			
			if(myReturn > 0) {
				ntrades++;
				count_win++;
				avgGains.add(myReturn);
				if(myReturn > peakGain) {
					peakGain = myReturn;
				}
			}
			else if(myReturn < 0) {
				ntrades++;
				avgLosses.add(myReturn);
				if(myReturn < maxLoss) {
					maxLoss = myReturn;
				}
			}
		}
		winPercentage = (double)count_win/(double)ntrades;
		
		for(int i = 0; i < avgGains.size(); i++) {
			avgGain += avgGains.get(i);
		}
		for(int i = 0; i < avgLosses.size(); i++) {
			avgLoss += avgLosses.get(i);
		}
		avgGain = avgGain/avgGains.size();
		avgLoss = avgLoss/avgLosses.size();
		
		
		
		meanReturn = meanReturn/realized.size();
		for(int t = 1; t < realized.size(); t++) {
			
			double myReturn = realized.get(t).getValue() - realized.get(t-1).getValue();
			sd += (myReturn - meanReturn)*(myReturn - meanReturn);
		}
		sd = sd/realized.size();
		sd = Math.sqrt(sd);
		sharpeRatio = Math.sqrt(255)*meanReturn/sd;
		
		double peak = Double.MIN_VALUE;
		drawdown = new double[realized.size()];
		maxDrawdown = Double.MIN_VALUE;
		peakPerformance = Double.MIN_VALUE;
		
		for (int t = 0; t < realized.size(); t++) {
		      
			double value = realized.get(t).getValue();
		    if (value > peak) {
		        
		    	peak = value;
		        drawdown[t] = 0;
		        
		        if(peak > peakPerformance) {
		        	peakPerformance = peak;
		        }
		    } 
		    else {
		        drawdown[t] = (peak - value);
		    }
		    if(drawdown[t] > maxDrawdown) {
				maxDrawdown = drawdown[t];
			}
		}		
	}
	
	
	public int getNtrades() {
		return ntrades;
	}
	public void setNtrades(int ntrades) {
		this.ntrades = ntrades;
	}
	public double getSharpeRatio() {
		return sharpeRatio;
	}
	public void setSharpeRatio(double sharpeRatio) {
		this.sharpeRatio = sharpeRatio;
	}
	public double getWinPercentage() {
		return winPercentage;
	}
	public void setWinPercentage(double winPercentage) {
		this.winPercentage = winPercentage;
	}
	public double getMaxDrawdown() {
		return maxDrawdown;
	}
	public void setMaxDrawdown(double maxDrawdown) {
		this.maxDrawdown = maxDrawdown;
	}
	public double getRoi() {
		return roi;
	}
	public void setRoi(double roi) {
		this.roi = roi;
	}

	public double getAvgLoss() {
		return avgLoss;
	}
	public void setAvgLoss(double avgLoss) {
		this.avgLoss = avgLoss;
	}
	public double getPeakPerformance() {
		return peakPerformance;
	}
	public void setPeakPerformance(double peakPerformance) {
		this.peakPerformance = peakPerformance;
	}
	public double getPeakGain() {
		return peakGain;
	}
	public void setPeakGain(double peakGain) {
		this.peakGain = peakGain;
	}
	public double getAvgGain() {
		return avgGain;
	}
	public void setAvgGain(double avgGain) {
		this.avgGain = avgGain;
	}

	public double getMaxLoss() {
		return maxLoss;
	}

	public void setMaxLoss(double maxLoss) {
		this.maxLoss = maxLoss;
	}

	@Override
	public String toString() {
		
		DecimalFormat meanFormat = new DecimalFormat("#.####");
		StringBuilder sb = new StringBuilder();
		sb.append("KPI Results: \n");
		
		sb.append("nTrades: " + ntrades + "\n");
		sb.append("ROI: " + df.format(roi) + "\n");
		sb.append("High: " + df.format(peakPerformance) + "\n");
		sb.append("Mean Return: " + meanFormat.format(meanReturn) + "\n");
		sb.append("Max Loss: " + df.format(maxLoss) + "\n");
		sb.append("Max Gain: " + df.format(peakGain) + "\n");
		sb.append("Avg Gain: " + df.format(avgGain) + "\n");
		sb.append("Avg Loss: " + df.format(avgLoss) + "\n");
		sb.append("Max Drawdown: " + df.format(maxDrawdown) + "\n");
		sb.append("Sharpe Ratio: " + df.format(sharpeRatio) + "\n");
		sb.append("Positive Return Ratio: " + df.format(winPercentage) + "\n");
		
		return sb.toString();
	}
	
	/**
	 * Returns the KPIs in vector form for external use 
	 * (eg Machine learning methods)
	 * @return
	 */
	public double[] getVectorKPIs() {		
		return new double[] {ntrades, roi, peakPerformance, meanReturn, maxLoss, peakGain, avgGain, avgLoss, maxDrawdown, sharpeRatio, winPercentage};		
	}
	
	/**
	 * Returns the headers of the KPIs
	 * @return
	 */
	public static String[] getHeaders() {
		return new String[] {"nTrades", "ROI", "High", "Mean Return", "MaxLoss", "MaxGain", "Avg Gain", "Avg Loss", "Max Drawdown", "SharpeRatio", "Win Percentage"};	
	}

	public double getKPI(int kpiChoice) {	
		return getVectorKPIs()[kpiChoice];
	}
	
	
	
	
}
