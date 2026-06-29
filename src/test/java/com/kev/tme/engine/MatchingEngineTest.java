package com.kev.tme.engine;

import com.kev.tme.model.Order;
import com.kev.tme.model.OrderSide;
import com.kev.tme.model.OrderStatus;
import com.kev.tme.model.OrderType;
import com.kev.tme.model.TimeInForce;
import com.kev.tme.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Engine-level tests (no HTTP). Each test gets a fresh engine, so the book and
 * trade history start empty.
 */
class MatchingEngineTest {

    private MatchingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new MatchingEngine();
    }

    // ---- helpers -----------------------------------------------------------

    private static Order limit(long id, OrderSide side, long priceTicks, long qty) {
        return new Order(id, side, OrderType.LIMIT, priceTicks, qty, TimeInForce.GTC);
    }

    private static Order ioc(long id, OrderSide side, long priceTicks, long qty) {
        return new Order(id, side, OrderType.LIMIT, priceTicks, qty, TimeInForce.IOC);
    }

    private static Order market(long id, OrderSide side, long qty) {
        return new Order(id, side, OrderType.MARKET, 0, qty, TimeInForce.GTC);
    }

    /** Ensure the next order constructed has a strictly later timestamp. */
    private static void tick() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ---- basic limit matching ---------------------------------------------

    @Test
    void limitOrderFullFill() {
        OrderResult rested = engine.submitOrder(limit(1, OrderSide.SELL, 10000, 10));
        assertEquals(OrderStatus.ACCEPTED, rested.getStatus());
        assertEquals(0, rested.getFilledQuantity());
        assertEquals(10, rested.getRemainingQuantity());

        OrderResult taker = engine.submitOrder(limit(2, OrderSide.BUY, 10000, 10));
        assertEquals(OrderStatus.FILLED, taker.getStatus());
        assertEquals(10, taker.getFilledQuantity());
        assertEquals(0, taker.getRemainingQuantity());
        assertEquals(1, taker.getTrades().size());

        Trade trade = taker.getTrades().get(0);
        assertEquals(2, trade.getBuyOrderId());
        assertEquals(1, trade.getSellOrderId());
        assertEquals(10000, trade.getPriceTicks());
        assertEquals(10, trade.getQuantity());

        // Book fully drained.
        assertTrue(engine.getOrderBookView().getBids().isEmpty());
        assertTrue(engine.getOrderBookView().getAsks().isEmpty());
    }

    @Test
    void limitOrderPartialFillRestsRemainder() {
        engine.submitOrder(limit(1, OrderSide.SELL, 10000, 5));

        OrderResult taker = engine.submitOrder(limit(2, OrderSide.BUY, 10000, 8));
        assertEquals(OrderStatus.PARTIALLY_FILLED, taker.getStatus());
        assertEquals(5, taker.getFilledQuantity());
        assertEquals(3, taker.getRemainingQuantity());

        // The 3 unfilled units rest as a bid.
        List<PriceLevel> bids = engine.getOrderBookView().getBids();
        assertEquals(1, bids.size());
        assertEquals(10000, bids.get(0).getPriceTicks());
        assertEquals(3, bids.get(0).getQuantity());
        assertTrue(engine.getOrderBookView().getAsks().isEmpty());
    }

    // ---- price-time priority ----------------------------------------------

    @Test
    void earlierOrderAtSamePriceFillsFirst() {
        engine.submitOrder(limit(1, OrderSide.BUY, 10000, 5)); // earlier
        tick();
        engine.submitOrder(limit(2, OrderSide.BUY, 10000, 5)); // later, same price

        OrderResult sell = engine.submitOrder(limit(3, OrderSide.SELL, 10000, 5));
        assertEquals(OrderStatus.FILLED, sell.getStatus());
        assertEquals(1, sell.getTrades().size());
        // Earlier resting buy (id 1) must trade first.
        assertEquals(1, sell.getTrades().get(0).getBuyOrderId());

        // Order 2 still rests.
        List<PriceLevel> bids = engine.getOrderBookView().getBids();
        assertEquals(1, bids.size());
        assertEquals(5, bids.get(0).getQuantity());
    }

    // ---- market orders walking multiple levels ----------------------------

    @Test
    void marketBuyWalksMultipleAskLevels() {
        engine.submitOrder(limit(1, OrderSide.SELL, 10000, 5));
        engine.submitOrder(limit(2, OrderSide.SELL, 10100, 5));

        OrderResult mkt = engine.submitOrder(market(3, OrderSide.BUY, 7));
        assertEquals(OrderStatus.FILLED, mkt.getStatus());
        assertEquals(7, mkt.getFilledQuantity());
        assertEquals(0, mkt.getRemainingQuantity());
        assertEquals(2, mkt.getTrades().size());

        // Cheapest level first, at the resting limit prices (never price 0).
        assertEquals(10000, mkt.getTrades().get(0).getPriceTicks());
        assertEquals(5, mkt.getTrades().get(0).getQuantity());
        assertEquals(10100, mkt.getTrades().get(1).getPriceTicks());
        assertEquals(2, mkt.getTrades().get(1).getQuantity());

        // 3 units left resting at the higher ask level.
        List<PriceLevel> asks = engine.getOrderBookView().getAsks();
        assertEquals(1, asks.size());
        assertEquals(10100, asks.get(0).getPriceTicks());
        assertEquals(3, asks.get(0).getQuantity());
    }

    @Test
    void marketSellWalksMultipleBidLevels() {
        engine.submitOrder(limit(1, OrderSide.BUY, 10000, 5)); // best bid
        engine.submitOrder(limit(2, OrderSide.BUY, 9900, 5));

        OrderResult mkt = engine.submitOrder(market(3, OrderSide.SELL, 7));
        assertEquals(OrderStatus.FILLED, mkt.getStatus());
        assertEquals(7, mkt.getFilledQuantity());
        assertEquals(2, mkt.getTrades().size());

        // Highest bid consumed first.
        assertEquals(10000, mkt.getTrades().get(0).getPriceTicks());
        assertEquals(5, mkt.getTrades().get(0).getQuantity());
        assertEquals(9900, mkt.getTrades().get(1).getPriceTicks());
        assertEquals(2, mkt.getTrades().get(1).getQuantity());
    }

    @Test
    void marketOrderWithPartialLiquidityFillsWhatItCanThenDiscards() {
        engine.submitOrder(limit(1, OrderSide.SELL, 10000, 3));

        OrderResult mkt = engine.submitOrder(market(2, OrderSide.BUY, 10));
        assertEquals(OrderStatus.PARTIALLY_FILLED, mkt.getStatus());
        assertEquals(3, mkt.getFilledQuantity());
        assertEquals(7, mkt.getRemainingQuantity()); // remainder discarded, not rested

        // Nothing rests on either side.
        assertTrue(engine.getOrderBookView().getBids().isEmpty());
        assertTrue(engine.getOrderBookView().getAsks().isEmpty());
    }

    // ---- market order with no liquidity -> REJECTED -----------------------

    @Test
    void marketOrderWithNoLiquidityIsRejected() {
        OrderResult mkt = engine.submitOrder(market(1, OrderSide.BUY, 5));
        assertEquals(OrderStatus.REJECTED, mkt.getStatus());
        assertEquals(0, mkt.getFilledQuantity());
        assertEquals(5, mkt.getRemainingQuantity());
        assertTrue(mkt.getTrades().isEmpty());
    }

    @Test
    void marketOrderDoesNotMatchSameSideLiquidity() {
        engine.submitOrder(limit(1, OrderSide.BUY, 10000, 5)); // a bid

        // A market BUY needs asks; there are none -> rejected (no price-0 trade).
        OrderResult mkt = engine.submitOrder(market(2, OrderSide.BUY, 5));
        assertEquals(OrderStatus.REJECTED, mkt.getStatus());
        assertTrue(mkt.getTrades().isEmpty());
    }

    // ---- cancel / modify ---------------------------------------------------

    @Test
    void cancelRemovesRestingOrder() {
        engine.submitOrder(limit(1, OrderSide.BUY, 10000, 5));
        assertTrue(engine.cancelOrder(1));
        assertTrue(engine.getOrderBookView().getBids().isEmpty());

        // Cancelling again (or an unknown id) is a no-op.
        assertFalse(engine.cancelOrder(1));
        assertFalse(engine.cancelOrder(999));

        // A new sell finds no bid to cross and simply rests.
        OrderResult sell = engine.submitOrder(limit(2, OrderSide.SELL, 10000, 5));
        assertEquals(OrderStatus.ACCEPTED, sell.getStatus());
        assertTrue(sell.getTrades().isEmpty());
    }

    @Test
    void modifyReprioritizesOrder() {
        engine.submitOrder(limit(1, OrderSide.BUY, 10000, 5)); // earlier, lower
        tick();
        engine.submitOrder(limit(2, OrderSide.BUY, 10000, 5)); // later, same price

        // Raise order 2 above order 1 -> it should now have priority.
        OrderResult modified = engine.modifyOrder(2, 10100, 5);
        assertEquals(OrderStatus.ACCEPTED, modified.getStatus());

        OrderResult sell = engine.submitOrder(limit(3, OrderSide.SELL, 10000, 5));
        assertEquals(1, sell.getTrades().size());
        Trade trade = sell.getTrades().get(0);
        assertEquals(2, trade.getBuyOrderId());   // re-prioritised order trades first
        assertEquals(10100, trade.getPriceTicks()); // at its (resting) price
    }

    @Test
    void modifyThatNowCrossesMatchesImmediately() {
        engine.submitOrder(limit(1, OrderSide.SELL, 10100, 5)); // resting ask
        engine.submitOrder(limit(2, OrderSide.BUY, 10000, 5));  // resting bid, no cross

        OrderResult modified = engine.modifyOrder(2, 10100, 5); // now crosses the ask
        assertEquals(OrderStatus.FILLED, modified.getStatus());
        assertEquals(5, modified.getFilledQuantity());
        assertEquals(1, modified.getTrades().size());
        assertEquals(10100, modified.getTrades().get(0).getPriceTicks());

        assertTrue(engine.getOrderBookView().getBids().isEmpty());
        assertTrue(engine.getOrderBookView().getAsks().isEmpty());
    }

    @Test
    void modifyUnknownOrderReturnsNull() {
        assertNull(engine.modifyOrder(42, 10000, 5));
    }

    // ---- IOC ---------------------------------------------------------------

    @Test
    void iocPartialFillThenCancelsRemainder() {
        engine.submitOrder(limit(1, OrderSide.SELL, 10000, 5));

        OrderResult iocBuy = engine.submitOrder(ioc(2, OrderSide.BUY, 10000, 12));
        assertEquals(OrderStatus.PARTIALLY_FILLED, iocBuy.getStatus());
        assertEquals(5, iocBuy.getFilledQuantity());
        assertEquals(7, iocBuy.getRemainingQuantity()); // cancelled, not rested

        // IOC remainder did not rest as a bid.
        assertTrue(engine.getOrderBookView().getBids().isEmpty());
        assertTrue(engine.getOrderBookView().getAsks().isEmpty());
    }

    @Test
    void iocWithNoCrossIsRejected() {
        engine.submitOrder(limit(1, OrderSide.SELL, 10000, 5)); // best ask 10000

        OrderResult iocBuy = engine.submitOrder(ioc(2, OrderSide.BUY, 9900, 5)); // below ask
        assertEquals(OrderStatus.REJECTED, iocBuy.getStatus());
        assertTrue(iocBuy.getTrades().isEmpty());

        // The resting ask is untouched; IOC order left nothing behind.
        assertEquals(1, engine.getOrderBookView().getAsks().size());
        assertTrue(engine.getOrderBookView().getBids().isEmpty());
    }

    // ---- integer-tick exactness -------------------------------------------

    @Test
    void integerTickPricingIsExact() {
        // 0.10 per unit, expressed as 10 ticks. With double, 0.1+0.1+0.1 drifts.
        engine.submitOrder(limit(1, OrderSide.SELL, 10, 1));
        engine.submitOrder(limit(2, OrderSide.SELL, 10, 1));
        engine.submitOrder(limit(3, OrderSide.SELL, 10, 1));
        engine.submitOrder(limit(4, OrderSide.BUY, 10, 3));

        List<Trade> trades = engine.getTradeHistory();
        assertEquals(3, trades.size());

        long totalTicks = 0;
        for (Trade t : trades) {
            assertEquals(10, t.getPriceTicks());
            totalTicks += t.getPriceTicks() * t.getQuantity();
        }

        // Exact in long arithmetic...
        assertEquals(30L, totalTicks);
        // ...whereas the equivalent double computation would NOT be exact.
        assertTrue(0.10 + 0.10 + 0.10 != 0.30,
                "sanity check: double arithmetic drifts where long ticks do not");
    }
}
