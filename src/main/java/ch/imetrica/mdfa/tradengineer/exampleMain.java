package ch.imetrica.mdfa.tradengineer;

import com.espertech.esper.client.*;
import java.util.Random;
import java.util.Date;

public class exampleMain {

    public static class Tick {
        String symbol;
        Double price;
        Date timeStamp;

        public Tick(String s, double p, long t) {
            symbol = s;
            price = p;
            timeStamp = new Date(t);
        }
        public double getPrice() {return price;}
        public String getSymbol() {return symbol;}
        public Date getTimeStamp() {return timeStamp;}

        @Override
        public String toString() {
            return "Price: " + price.toString() + " time: " + timeStamp.toString();
        }
    }

    private static Random generator = new Random();

    public static void GenerateRandomTick(EPRuntime cepRT) {

        double price = (double) generator.nextInt(10);
        long timeStamp = System.currentTimeMillis();
        String symbol = "AAPL";
        Tick tick = new Tick(symbol, price, timeStamp);
        System.out.println("Sending tick:" + tick);
        cepRT.sendEvent(tick);

    }

    public static class CEPListener implements UpdateListener {

        public void update(EventBean[] newData, EventBean[] oldData) {
            System.out.println("Event received: " + newData[0].getUnderlying());
            System.out.println(newData.length + " " + oldData.length);
        }
    }

    public static void main(String[] args) {

    //The Configuration is meant only as an initialization-time object.
        Configuration cepConfig = new Configuration();
        cepConfig.addEventType("StockTick", Tick.class.getName());
        EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig);
        EPRuntime cepRT = cep.getEPRuntime();

        EPAdministrator cepAdm = cep.getEPAdministrator();
        EPStatement cepStatement = cepAdm.createEPL("select * from " +
                "StockTick(symbol='AAPL').win:length(2) " +
                "having avg(price) > 6.0");

        cepStatement.addListener(new CEPListener());

       // We generate a few ticks...
        for (int i = 0; i < 50; i++) {
            GenerateRandomTick(cepRT);
        }
    }
}
