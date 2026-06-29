package com.kev.tme.model;

import java.time.Instant;
import java.util.Objects;

/**
 * A single order in the book.
 *
 * <p>Price is held as {@code long} integer ticks (e.g. paise/cents) so that all
 * price arithmetic is exact and free of floating-point drift. Market orders do
 * not have a meaningful price; their {@code priceTicks} is ignored by the
 * matching logic and a market order never rests in the book.</p>
 *
 * <p>Equality is keyed solely on {@code orderId}. Order ids are unique, so two
 * {@code Order} instances refer to the same logical order iff their ids match.
 * This makes {@link java.util.PriorityQueue#remove(Object)} able to locate an
 * order by id even after a modify replaces the instance.</p>
 */
public class Order {

    private final long orderId;
    private final OrderSide side;
    private final OrderType type;
    private final long priceTicks;
    private final TimeInForce timeInForce;
    private long quantity;
    private final Instant timestamp;

    public Order(
            long orderId,
            OrderSide side,
            OrderType type,
            long priceTicks,
            long quantity,
            TimeInForce timeInForce
    ) {
        this.orderId = orderId;
        this.side = side;
        this.type = type;
        this.priceTicks = priceTicks;
        this.quantity = quantity;
        this.timeInForce = timeInForce == null ? TimeInForce.GTC : timeInForce;
        this.timestamp = Instant.now();
    }

    public long getOrderId() {
        return orderId;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public long getPriceTicks() {
        return priceTicks;
    }

    public TimeInForce getTimeInForce() {
        return timeInForce;
    }

    public long getQuantity() {
        return quantity;
    }

    public void reduceQuantity(long qty) {
        this.quantity -= qty;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean isMarketOrder() {
        return type == OrderType.MARKET;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order other)) return false;
        return orderId == other.orderId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(orderId);
    }

    @Override
    public String toString() {
        return "Order{id=" + orderId +
                ", side=" + side +
                ", type=" + type +
                ", priceTicks=" + priceTicks +
                ", qty=" + quantity +
                ", tif=" + timeInForce + '}';
    }
}
