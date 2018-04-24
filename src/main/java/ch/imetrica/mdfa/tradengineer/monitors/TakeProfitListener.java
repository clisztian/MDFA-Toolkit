package ch.imetrica.mdfa.tradengineer.monitors;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import ch.imetrica.mdfa.tradengineer.eventbeans.LimitAlert;
import ch.imetrica.mdfa.tradengineer.eventbeans.PriceLimit;
import ch.imetrica.mdfa.tradengineer.eventbeans.PriceTick;

public class TakeProfitListener implements UpdateListener {

	    private final EPServiceProvider epService;
	    private final PriceLimit limit;
	    private final PriceTick initialPriceTick;


	    public TakeProfitListener(EPServiceProvider epService, 
	    						  PriceLimit limit, 
	    						  PriceTick initialPriceTick) {
	        this.epService = epService;
	        this.limit = limit;
	        this.initialPriceTick = initialPriceTick;
	    }

	    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
	        Object theEvent = newEvents[0].get("tick");
	        PriceTick tick = (PriceTick) theEvent;

	        System.out.println(".update Alert for stock=" + tick.getSymbol() +
	                "  bid=" + tick.getBid() +
	                "  initialPriceTick=" + initialPriceTick.getBid() +
	                "  limt=" + limit.getLimitPct());

	        LimitAlert alert = new LimitAlert(tick, limit, initialPriceTick.getBid());
	        
	    } 	
}
