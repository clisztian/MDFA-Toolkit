package ch.imetrica.mdfa.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import ch.imetrica.mdfa.series.TimeSeries;
import ch.imetrica.mdfa.series.TimeSeriesEntry;

public class TradeReport {

	HashMap<String, ArrayList<Trade>> anyTrades;
	
	
	public TradeReport() {
		anyTrades = new HashMap<String, ArrayList<Trade>>();
	}
	
	public void add(String timestamp, Trade newTrade) {
		
		if(!anyTrades.containsKey(timestamp)) {
			anyTrades.put(timestamp, (new ArrayList<Trade>()));
			anyTrades.get(timestamp).add(newTrade);
		}
		else {
			anyTrades.get(timestamp).add(newTrade);
		}	
	}
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		Iterator<Entry<String, ArrayList<Trade>>> it = anyTrades.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        
	        ArrayList<Trade> trades = (ArrayList<Trade>) pair.getValue();
	        
	        sb.append(pair.getKey() + ": ");
	        
	        for(Trade trade : trades) {
	        	sb.append(trades.toString() + " ");
	        }
	        sb.append("\n");
	        it.remove(); // avoids a ConcurrentModificationException
	    }
		
		
		return null;
		
	}
	
}
