package com.kev.tme.engine;

/**
 * Aggregated resting quantity at a single price level.
 */
public class PriceLevel {

    private final long priceTicks;
    private final long quantity;

    public PriceLevel(long priceTicks, long quantity) {
        this.priceTicks = priceTicks;
        this.quantity = quantity;
    }

    public long getPriceTicks() {
        return priceTicks;
    }

    public long getQuantity() {
        return quantity;
    }
}
