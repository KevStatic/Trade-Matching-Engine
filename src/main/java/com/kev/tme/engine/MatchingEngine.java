package com.kev.tme.engine;

import com.kev.tme.model.Order;
import com.kev.tme.model.OrderSide;
import com.kev.tme.model.OrderStatus;
import com.kev.tme.model.TimeInForce;
import com.kev.tme.model.Trade;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Price-time-priority matching engine.
 *
 * <p>An incoming order is matched against the <em>opposite</em> side of the book
 * on submission, walking it level by level:</p>
 * <ul>
 *     <li><b>Market orders</b> never rest. They walk the opposite book until
 *     filled or the opposite side is exhausted; any remainder is discarded. The
 *     trade price is always the resting limit order's price, so a market order
 *     can never trade at a fabricated price. A market order with no opposite
 *     liquidity is {@link OrderStatus#REJECTED}.</li>
 *     <li><b>Limit orders</b> match while they cross the opposite best. A GTC
 *     remainder rests; an IOC remainder is cancelled (discarded).</li>
 * </ul>
 *
 * <p>All mutating operations are {@code synchronized}, making the engine safe
 * for concurrent callers (e.g. multiple HTTP request threads).</p>
 */
@Service
public class MatchingEngine {

    private final OrderBook orderBook;
    private final TradeStore tradeStore;

    public MatchingEngine() {
        this.orderBook = new OrderBook();
        this.tradeStore = new TradeStore();
    }

    public synchronized OrderResult submitOrder(Order order) {
        long originalQty = order.getQuantity();
        List<Trade> trades = new ArrayList<>();

        matchAgainstBook(order, trades);

        boolean rested = false;
        if (!order.isMarketOrder()
                && order.getQuantity() > 0
                && order.getTimeInForce() == TimeInForce.GTC) {
            orderBook.addOrder(order);
            rested = true;
        }

        long filled = originalQty - order.getQuantity();
        OrderStatus status = statusOf(filled, order.getQuantity(), rested);
        return new OrderResult(order.getOrderId(), status, filled, order.getQuantity(), trades);
    }

    /**
     * Walk the opposite side of the book, generating trades until the incoming
     * order is filled, the opposite side is exhausted, or (for limit orders) the
     * prices no longer cross.
     */
    private void matchAgainstBook(Order incoming, List<Trade> trades) {
        OrderSide oppositeSide = incoming.getSide() == OrderSide.BUY
                ? OrderSide.SELL
                : OrderSide.BUY;

        while (incoming.getQuantity() > 0) {
            Order resting = orderBook.peekBest(oppositeSide);
            if (resting == null) {
                break;
            }

            if (!incoming.isMarketOrder() && !crosses(incoming, resting)) {
                break;
            }

            long tradedQty = Math.min(incoming.getQuantity(), resting.getQuantity());

            // Price is always the resting limit order's price (resting orders are
            // never market orders), so market orders trade at real book prices.
            long tradePrice = resting.getPriceTicks();

            long buyOrderId = incoming.getSide() == OrderSide.BUY
                    ? incoming.getOrderId()
                    : resting.getOrderId();
            long sellOrderId = incoming.getSide() == OrderSide.BUY
                    ? resting.getOrderId()
                    : incoming.getOrderId();

            Trade trade = new Trade(buyOrderId, sellOrderId, tradePrice, tradedQty);
            tradeStore.recordTrade(trade);
            trades.add(trade);

            incoming.reduceQuantity(tradedQty);
            resting.reduceQuantity(tradedQty);

            if (resting.getQuantity() == 0) {
                orderBook.pollBest(oppositeSide);
            }
        }
    }

    /** Does the incoming limit order's price cross the resting opposite order? */
    private boolean crosses(Order incoming, Order resting) {
        return incoming.getSide() == OrderSide.BUY
                ? incoming.getPriceTicks() >= resting.getPriceTicks()
                : incoming.getPriceTicks() <= resting.getPriceTicks();
    }

    private OrderStatus statusOf(long filled, long remaining, boolean rested) {
        if (filled == 0) {
            return rested ? OrderStatus.ACCEPTED : OrderStatus.REJECTED;
        }
        return remaining == 0 ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
    }

    public synchronized boolean cancelOrder(long orderId) {
        return orderBook.cancelOrder(orderId);
    }

    /**
     * Modify is implemented as cancel + re-submit, so the replacement is
     * re-prioritised under price-time rules (it receives a fresh timestamp) and
     * matches immediately if its new price now crosses the book.
     *
     * @return the result of re-submitting, or {@code null} if no such order rests.
     */
    public synchronized OrderResult modifyOrder(long orderId, long newPriceTicks, long newQuantity) {
        Order existing = orderBook.get(orderId);
        if (existing == null) {
            return null;
        }
        orderBook.cancelOrder(orderId);
        Order replacement = new Order(
                orderId,
                existing.getSide(),
                existing.getType(),
                newPriceTicks,
                newQuantity,
                existing.getTimeInForce()
        );
        return submitOrder(replacement);
    }

    public synchronized OrderBookView getOrderBookView() {
        return orderBook.snapshot();
    }

    public List<Trade> getTradeHistory() {
        return tradeStore.getAllTrades();
    }
}
