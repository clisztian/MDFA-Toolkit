package ch.imetrica.mdfa.tradengineer.eventbeans;

public class PriceLimit {
    String userId;
    String symbol;
    double limitPct;

    public PriceLimit(String userId, String stockSymbol, double limitPct) {
        this.userId = userId;
        this.symbol = stockSymbol;
        this.limitPct = limitPct;
    }

    public String getUserId() {
        return userId;
    }

    public String getStockSymbol() {
        return symbol;
    }

    public double getLimitPct() {
        return limitPct;
    }

    public String toString() {
        return "userId=" + userId +
                "  stockSymbol=" + symbol +
                "  limitPct=" + limitPct;
    }
}