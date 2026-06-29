package com.kev.tme.engine;

import java.util.List;

/**
 * Aggregated snapshot of the resting book: bids high-to-low, asks low-to-high.
 */
public class OrderBookView {

    private final List<PriceLevel> bids;
    private final List<PriceLevel> asks;

    public OrderBookView(List<PriceLevel> bids, List<PriceLevel> asks) {
        this.bids = List.copyOf(bids);
        this.asks = List.copyOf(asks);
    }

    public List<PriceLevel> getBids() {
        return bids;
    }

    public List<PriceLevel> getAsks() {
        return asks;
    }
}
