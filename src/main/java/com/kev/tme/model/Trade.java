package com.kev.tme.model;

import java.time.Instant;

/**
 * An executed trade between a resting order and an incoming order.
 *
 * <p>Price is held as {@code long} integer ticks and is always taken from the
 * resting limit order on the opposite side, so a trade can never execute at a
 * fabricated price (e.g. a market order's price of zero).</p>
 */
public class Trade {

    private final long buyOrderId;
    private final long sellOrderId;
    private final long priceTicks;
    private final long quantity;
    private final Instant timestamp;

    public Trade(
            long buyOrderId,
            long sellOrderId,
            long priceTicks,
            long quantity
    ) {
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.priceTicks = priceTicks;
        this.quantity = quantity;
        this.timestamp = Instant.now();
    }

    public long getBuyOrderId() {
        return buyOrderId;
    }

    public long getSellOrderId() {
        return sellOrderId;
    }

    public long getPriceTicks() {
        return priceTicks;
    }

    public long getQuantity() {
        return quantity;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return " TRADE | BUY = " + buyOrderId +
                " SELL = " + sellOrderId +
                " PRICE = " + priceTicks +
                " QUANTITY = " + quantity;
    }
}
