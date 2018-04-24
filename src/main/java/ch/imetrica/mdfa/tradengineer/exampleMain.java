package ch.imetrica.mdfa.tradengineer;

import com.espertech.esper.client.*;

import ch.imetrica.mdfa.tradengineer.eventbeans.PriceLimit;
import ch.imetrica.mdfa.tradengineer.eventbeans.PriceTick;

import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class exampleMain {



	static anyConfiguration myConfig = new anyConfiguration("PriceTick", PriceTick.class.getName());

	static int clockCount = 0;
	static double priceStart = 1.20;
	static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	static DateTime dt = formatter.parseDateTime("2018-04-19 10:00:00");
	static private EPStatement newLimitListener = null;
	static private EPStatement newTimeListener = null;
	static private EPStatement newIntialPriceListener = null;
    private static Random generator = new Random();
    public static PriceLimit myLimit;
    
    public static void GenerateRandomTick(EPRuntime cepRT) {

        double bid = priceStart + (1.0 - 2.0*generator.nextDouble())*.0005;
        double ask = bid + generator.nextDouble()*.0001;
        
        String symbol = "EURCHF";
        PriceTick tick = new PriceTick(symbol, dt, bid, ask);
        		
        System.out.println("Sending tick:" + tick);
        cepRT.sendEvent(tick);
        
        dt = dt.plusMinutes(1);
    }

    public static class TimeListener implements UpdateListener {

        public void update(EventBean[] newData, EventBean[] oldData) {
        	   
            PriceTick newtick = (PriceTick) newData[0].get("tick");
            System.out.println("Compute Signal: " + newtick.getTimestamp().toString());
        }
    }

    public static class LimitListener implements UpdateListener {

        public void update(EventBean[] newData, EventBean[] oldData) {
        	
        	PriceTick newtick = (PriceTick) newData[0].get("tick");
            System.out.println("Achieved Limit " + newtick.getTimestamp().toString() 
		              + " bid = " + newtick.getBid());
            
            newLimitListener = null;
       
        }
    }

	
    
    public static class InitialPriceListener implements UpdateListener {

        public void update(EventBean[] newData, EventBean[] oldData) {
        	
        	PriceTick newtick = (PriceTick) newData[0].get("tick");
            System.out.println("My Initial Price set at " + newtick.getTimestamp().toString() 
		              + " bid = " + newtick.getBid());
                  
            myLimit = new PriceLimit(newtick.toString(), newtick.getSymbol(), 0.01);
       
            if (newIntialPriceListener != null) 
            	newIntialPriceListener.removeAllListeners();
        
            
            double limitPct = myLimit.getLimitPct();
            double upperLimit = newtick.getAsk() * (1.0 + (limitPct / 100.0));
            
            System.out.println("Waiting for price at " + upperLimit);
            
            String expressionText = "every tick=PriceTick" +
                    "(symbol='" + myLimit.getSymbol() + "', bid > " + upperLimit + ")";
            
            newLimitListener = myConfig.getCEP().getEPAdministrator().createPattern(expressionText);
            newLimitListener.addListener(new LimitListener());
        }
    }
    
    public static void main(String[] args) {

        
    	
        EPRuntime cepRT = myConfig.getCEP().getEPRuntime();
        double lowerLimit = 1.20;

        String initialPriceexpressionText = "tick=PriceTick" +
                "(ask < " + lowerLimit + ")";
        
        String TimeExpressionText = "every tick=PriceTick" +
                "(symbol='EURCHF', longtime%300 = 0" + ")";
        
        newIntialPriceListener = myConfig.getCEP().getEPAdministrator().createPattern(initialPriceexpressionText);
        newTimeListener = myConfig.getCEP().getEPAdministrator().createPattern(TimeExpressionText);
        
        newIntialPriceListener.addListener(new InitialPriceListener());
        newTimeListener.addListener(new TimeListener());
        
        
        
        for (int i = 0; i < 50; i++) {
            GenerateRandomTick(cepRT);
        }
    }
    
    public static class anyConfiguration {
    	
        Configuration cepConfig = new Configuration();
        EPServiceProvider cep;
        
        anyConfiguration(String name, String className) {
        	
        	cepConfig.addEventType(name, className);
            cep = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig);
        }
        
        final EPServiceProvider getCEP() {
        	return cep;
        }
        
    }
    
}
