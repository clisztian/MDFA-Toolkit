package ch.imetrica.mdfa.tradengineer;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

public class AnyConfiguration {
	
    Configuration cepConfig = new Configuration();
    EPServiceProvider cep;
    
    public AnyConfiguration(String name, String className) {  	
    	cepConfig.addEventType(name, className);      
    }
    
    public void addEventType(String name, String className) {
    	cepConfig.addEventType(name, className);  	
    }
    
    public void initialize() {
    	
    	cep = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig);
    	cep.initialize();
    }
    
    public final EPServiceProvider getCEP() {
    	return cep;
    }
    
}
