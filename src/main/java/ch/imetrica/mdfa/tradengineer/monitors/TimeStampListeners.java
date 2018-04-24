package ch.imetrica.mdfa.tradengineer.monitors;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import ch.imetrica.mdfa.series.MultivariateFXSeries;
import ch.imetrica.mdfa.tradengineer.eventbeans.PriceTick;

public class TimeStampListeners {

	
    public class ComputeCoefficientListener implements UpdateListener {

        public void update(EventBean[] newData, EventBean[] oldData) {
        	   
        	try {
				((MultivariateFXSeries) newData[0].get("fxSignal")).computeAllFilterCoefficients();
			} catch (Exception e) {
				e.printStackTrace();
			}            
        }
    }
	
    public class UpdateSignalListener implements UpdateListener {

        public void update(EventBean[] newData, EventBean[] oldData) {
        	   
            PriceTick newtick = (PriceTick) newData[0].get("tick");
            System.out.println("Compute Signal: " + newtick.getTimestamp().toString());
            
        }
    }
	
    
    
}
