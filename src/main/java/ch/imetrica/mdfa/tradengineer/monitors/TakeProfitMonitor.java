package ch.imetrica.mdfa.tradengineer.monitors;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import ch.imetrica.mdfa.tradengineer.eventbeans.PriceLimit;
import ch.imetrica.mdfa.tradengineer.eventbeans.PriceTick;

public class TakeProfitMonitor {

	private final EPServiceProvider epService;
	
	private PriceLimit limit = null;

    private EPStatement newLimitListener = null;
    private EPStatement initialPriceListener = null;
    private EPStatement lowPriceListener = null;
    private EPStatement highPriceListener = null;
    
    
    public TakeProfitMonitor(EPServiceProvider epService, PriceLimit limit) {
       
    	this.epService = epService;
        this.limit = limit;
       
        String expressionText = "every pricelimit=PriceLimit" +
                "(userId='" + limit.getUserId() + "'," +
                "symbol='" + limit.getSymbol() + "')";
        newLimitListener = epService.getEPAdministrator().createPattern(expressionText);

        newLimitListener.addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
               
            	System.out.println(".update Received an override limit, stopping listeners");               
                die();
            }
        });

        expressionText = "tick=PriceTick(stockSymbol='" + limit.getSymbol() + "')";
        initialPriceListener = epService.getEPAdministrator().createPattern(expressionText);

        initialPriceListener.addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                PriceTick tick = (PriceTick) newEvents[0].get("tick");
                PriceLimit limit = TakeProfitMonitor.this.limit;

                initialPriceListener = null;

                double limitPct = limit.getLimitPct();
                double upperLimit = tick.getBid() * (1.0 + (limitPct / 100.0));
                double lowerLimit = tick.getAsk() * (1.0 - (limitPct / 100.0));

                System.out.println(".update Received initial tick, stock=" + tick.getSymbol() +
                            "  price=" + tick.getBid() +
                            "  limit.limitPct=" + limitPct +
                            "  lowerLimit=" + lowerLimit +
                            "  upperLimit=" + upperLimit);
                

                TakeProfitListener listener = new TakeProfitListener(TakeProfitMonitor.this.epService, limit, tick);

                String expressionText = "every tick=PriceTick" +
                        "(stockSymbol='" + limit.getSymbol() + "', ask < " + lowerLimit + ")";
                lowPriceListener = TakeProfitMonitor.this.epService.getEPAdministrator().createPattern(expressionText);
                lowPriceListener.addListener(listener);

                expressionText = "every tick=PriceTick" +
                        "(stockSymbol='" + limit.getSymbol() + "', bid > " + upperLimit + ")";
                highPriceListener = TakeProfitMonitor.this.epService.getEPAdministrator().createPattern(expressionText);
                highPriceListener.addListener(listener);
            }
        });
    }

    private void die() {
        if (newLimitListener != null) newLimitListener.removeAllListeners();
        if (initialPriceListener != null) initialPriceListener.removeAllListeners();
        if (lowPriceListener != null) lowPriceListener.removeAllListeners();
        if (highPriceListener != null) highPriceListener.removeAllListeners();
    }
    
}
