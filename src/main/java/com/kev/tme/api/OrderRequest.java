package com.kev.tme.api;

import com.kev.tme.model.OrderSide;
import com.kev.tme.model.OrderType;
import com.kev.tme.model.TimeInForce;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Incoming order payload.
 *
 * <p>Price is expressed in integer ticks (paise/cents) as {@code priceTicks} so
 * no floating point enters the engine. The field is nullable so that "price
 * absent" is distinguishable from "price zero" for market-order validation.</p>
 */
public class OrderRequest {

    @NotNull(message = "orderId is required")
    public Long orderId;

    @NotNull(message = "side is required (BUY or SELL)")
    public OrderSide side;

    @NotNull(message = "type is required (LIMIT or MARKET)")
    public OrderType type;

    public Long priceTicks;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be greater than 0")
    public Long quantity;

    /** Optional; defaults to GTC when absent. */
    public TimeInForce tif;

    @AssertTrue(message = "priceTicks must be > 0 for LIMIT orders and absent/zero for MARKET orders")
    public boolean isPriceConsistentWithType() {
        if (type == OrderType.LIMIT) {
            return priceTicks != null && priceTicks > 0;
        }
        if (type == OrderType.MARKET) {
            return priceTicks == null || priceTicks == 0;
        }
        // type null is reported by its own @NotNull; don't double-report here.
        return true;
    }

    public TimeInForce effectiveTif() {
        return tif == null ? TimeInForce.GTC : tif;
    }

    public long effectivePriceTicks() {
        return priceTicks == null ? 0L : priceTicks;
    }
}
