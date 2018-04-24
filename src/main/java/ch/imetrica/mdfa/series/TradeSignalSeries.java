package ch.imetrica.mdfa.series;

import java.io.IOException;

import org.joda.time.DateTime;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import ch.imetrica.mdfa.datafeeds.CsvFeed;
import ch.imetrica.mdfa.mdfa.MDFABase;
import ch.imetrica.mdfa.tradengineer.AnyConfiguration;
import ch.imetrica.mdfa.tradengineer.eventbeans.MultivarPriceTick;
import ch.imetrica.mdfa.tradengineer.eventbeans.PriceTick;

public class TradeSignalSeries {

	
	public enum SideType { 
		BUY,
		SELL;
	}
	
	
	private MultivariateFXSeries tradeSignal;

    SideType side;	
	
	private AnyConfiguration cep;
	private EPRuntime cepRT;
	private EPStatement takeProfitStatement = null;
	private EPStatement stopLossStatement = null;
	private EPStatement initialPriceStatement = null;
	private EPStatement entryRuleStatement = null;
	private EPStatement signalChangeStatement = null;

	private EPStatement updateCoefficientStatement = null;
	private EPStatement updateSignalStatement = null;
	private EPStatement addValueStatement = null;
	
	private int updateObservations;

	
	
	
	public TradeSignalSeries(MultivariateFXSeries signal) {
		
		this.tradeSignal = signal;
		
		cep = new AnyConfiguration("MultivarPriceTick", MultivarPriceTick.class.getName());
		cep.addEventType("FXSeries", MultivariateFXSeries.class.getName());
		cep.initialize();
		
		cepRT = cep.getCEP().getEPRuntime();
	}
	

	
	public double getLatest() {
		return this.tradeSignal.getLatestSignalEntry().getValue()[0];
	}
	
	public double getPrevious() {
		return this.tradeSignal.getSignal(this.tradeSignal.size()-2).getValue()[0];
	}
	
	public MultivariateFXSeries getTradeSignal() {
		return this.tradeSignal;
	}
	
	public void addValue(String date, double[] ask, double[] bid) {
		
		MultivarPriceTick tick = new MultivarPriceTick(tradeSignal.getTargetName(), 
				tradeSignal.getFormatter().parseDateTime(date), bid, ask);
        cepRT.sendEvent(tick);
	}
	
	
	/**
	 * Sets the rule to update the MDFA Coefficients automatically
	 * after a certain number of signal observations. 
	 * @param nObservations
	 * @return this TradeSignalSeries
	 */
	public TradeSignalSeries addUpdateCoefficientRule(int nObservations) {
		
		this.updateObservations = nObservations;
		String updateCoefficientExpression = "every tradeSignal=FXSeries(" + 
		    "size = " + nObservations + ")"; 
		
		updateCoefficientStatement = cep.getCEP().getEPAdministrator()
				                    	.createPattern(updateCoefficientExpression);
		
		updateCoefficientStatement.addListener(new ComputeCoefficientListener());
		
		return this;
	}
	
	/**
	 * Sets the rule for updating the signal. 
	 * The signal update is made every time%seconds = 0
	 * 
	 * @param nseconds
	 * @return 
	 */
	public TradeSignalSeries addUpdateSignalRule(int seconds) {
		
		 String TimeExpressionText = "every tick=MultivarPriceTick(" +
	                "longtime%" + seconds + " = 0" + ")";
		 
		 updateSignalStatement = cep.getCEP().getEPAdministrator().createPattern(TimeExpressionText);
		 updateSignalStatement.addListener(new UpdateSignalListener());
		 
		return this;
	}
	
	/**
	 * 
	 * 
	 * 
	 * @return this TradeSignalSeries
	 */
	public TradeSignalSeries addSignalChangeRule() {
		
		String changeExpressionText = "every tradeSignal=FXSeries(latest*previous < 0)";
		
		signalChangeStatement = cep.getCEP().getEPAdministrator().createPattern(changeExpressionText);
		signalChangeStatement.addListener(new SignalChangeListener());
		
		return this;
	}
	

	public TradeSignalSeries addEnterMarketRule(double percent) {
		
		
		
		return this;
	}
	
	
	/**
	 * Sets the rule for adding a non-signal value. These values 
	 * do not influence the signal but rather the trading rules
	 * governed by the market entry/exit rules at time%seconds = 0
	 * 
	 * @param seconds
	 * @return this TradeSignalSeries
	 */
	public TradeSignalSeries addNonsignalValueRule(int seconds) {
		
		
		return this;
	}
	
	
	public class SignalChangeListener implements UpdateListener {
		
		public void update(EventBean[] newData, EventBean[] oldData) {
			
			double latest = getLatest();
			System.out.println("Latest = " + latest);
			if(latest > 0) {
			
				
			   System.out.println("Signal went positive");				
			}
			else {
			   System.out.println("Signal went negative");		
			}
		}		
	}
	
    public class ComputeCoefficientListener implements UpdateListener {

        public void update(EventBean[] newData, EventBean[] oldData) {
        	   
        	try {
        		tradeSignal.computeAllFilterCoefficients();
				tradeSignal.chopFirstObservations(updateObservations);
			} catch (Exception e) {
				e.printStackTrace();
			}            
        }
    }
    
    public class UpdateSignalListener implements UpdateListener {

        public void update(EventBean[] newData, EventBean[] oldData) {
        	
        	MultivarPriceTick newtick = (MultivarPriceTick) newData[0].get("tick");
        	
        	double[] value = newtick.getMid();
        	String date = newtick.getTimestamp().toString(tradeSignal.getFormatter());

        	try {       		
        		
        		tradeSignal.addValue(date, value);
        		
        		System.out.println(date + ", " + tradeSignal.getLatestSignalEntry().getValue()[0]);
        		
        		cepRT.sendEvent(tradeSignal);
        		
			} catch (Exception e) {
				e.printStackTrace();
			}            
        }
    }
	
    
    public static void main(String[] args) throws Exception {
    	
    	String[] dataFiles = new String[3];
		dataFiles[0] = "/home/lisztian/mdfaData/AAPL.daily.csv";
		dataFiles[1] = "/home/lisztian/mdfaData/QQQ.daily.csv";
		dataFiles[2] = "/home/lisztian/mdfaData/SPY.daily.csv";
		
		CsvFeed marketFeed = new CsvFeed(dataFiles, "Index", "Open");
				
		/* Create some MDFA sigEx processes */
		MDFABase[] anyMDFAs = new MDFABase[1];
		
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
    	
		MultivariateFXSeries fxSeries = new MultivariateFXSeries(anyMDFAs, "yyyy-MM-dd");	
		fxSeries.addSeries(new TargetSeries(1.0, true, "AAPL"));
		fxSeries.addSeries(new TargetSeries(1.0, true, "QQQ"));
		fxSeries.addSeries(new TargetSeries(1.0, true, "SPY"));
		
		
        for(int i = 0; i < 500; i++) {
			
			TimeSeriesEntry<double[]> observation = marketFeed.getNextMultivariateObservation();
			fxSeries.addValue(observation.getDateTime(), observation.getValue());
		}
        
        fxSeries.computeAllFilterCoefficients();	
        fxSeries.chopFirstObservations(70);	
	       
        
        TradeSignalSeries tradeSignal = new TradeSignalSeries(fxSeries)
        								.addUpdateSignalRule(300)
        								.addSignalChangeRule();


               
        for(int i = 0; i < 100; i++) {
			
			TimeSeriesEntry<double[]> observation = marketFeed.getNextMultivariateObservation();
		
			tradeSignal.addValue(observation.getDateTime(), 
					 			 observation.getValue(), 
					 			 observation.getValue()); 
             
			
        }
        
     
  
    	
    }



	private String getName() {
		return tradeSignal.getTargetName();
	}
}



