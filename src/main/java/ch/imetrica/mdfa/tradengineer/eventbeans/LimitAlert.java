package ch.imetrica.mdfa.tradengineer.eventbeans;

public class LimitAlert {
    private PriceTick tick;
    private PriceLimit limit;
    double initialPrice;

    public LimitAlert(PriceTick tick, PriceLimit limit, double initialPrice) {
        this.tick = tick;
        this.limit = limit;
        this.initialPrice = initialPrice;
    }

    public PriceTick getTick() {
        return tick;
    }

    public PriceLimit getPriceLimit() {
        return limit;
    }

    public double getInitialPrice() {
        return initialPrice;
    }

    public String toString() {
        return tick.toString() +
                "  " + limit.toString() +
                "  initialPrice=" + initialPrice;
    }

}