package ch.imetrica.mdfa.tradengineer.eventbeans;

public class PriceTick {
    private String symbol;
    private double bid;
    private double ask;

    public PriceTick(String stockSymbol, double bid, double ask) {
        this.symbol = stockSymbol;
        this.bid = bid;
        this.ask = ask;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getBid() {
        return bid;
    }
    
    public double getAsk() {
        return ask;
    }

    public String toString() {
        return "stockSymbol=" + symbol +
                "  bid=" + bid + " ask=" + ask;
    }
}