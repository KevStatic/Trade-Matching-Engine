package com.kev.tme.engine;

import com.kev.tme.model.Order;
import com.kev.tme.model.OrderSide;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

/**
 * The resting limit-order book: two price-time-ordered priority queues plus an
 * id index for O(1) lookup.
 *
 * <p>Ordering uses {@code long} price ticks (exact, no floating point):</p>
 * <ul>
 *     <li>Bids: highest price first, then earliest timestamp.</li>
 *     <li>Asks: lowest price first, then earliest timestamp.</li>
 * </ul>
 *
 * <p><b>Cancel/modify cost.</b> {@link PriorityQueue#remove(Object)} is O(n):
 * it scans the heap linearly using {@link Order#equals(Object)} (keyed by
 * {@code orderId}) rather than reference identity. For this engine's scope that
 * tradeoff is acceptable; a production book would index heap positions (e.g. an
 * intrusive node per order) to make cancel O(log n).</p>
 *
 * <p>This class is not thread-safe on its own; it is only ever touched from the
 * {@link MatchingEngine}'s {@code synchronized} methods.</p>
 */
public class OrderBook {

    // BUY: highest price first, then earlier time.
    private final PriorityQueue<Order> buyOrders;

    // SELL: lowest price first, then earlier time.
    private final PriorityQueue<Order> sellOrders;

    private final Map<Long, Order> orderMap = new HashMap<>();

    public OrderBook() {
        buyOrders = new PriorityQueue<>(
                Comparator.comparingLong(Order::getPriceTicks)
                        .reversed()
                        .thenComparing(Order::getTimestamp)
        );

        sellOrders = new PriorityQueue<>(
                Comparator.comparingLong(Order::getPriceTicks)
                        .thenComparing(Order::getTimestamp)
        );
    }

    private PriorityQueue<Order> queueFor(OrderSide side) {
        return side == OrderSide.BUY ? buyOrders : sellOrders;
    }

    /** Rest an order in the book (caller guarantees it is a non-market remainder). */
    public void addOrder(Order order) {
        orderMap.put(order.getOrderId(), order);
        queueFor(order.getSide()).offer(order);
    }

    /** Best order resting on the given side, or {@code null} if that side is empty. */
    public Order peekBest(OrderSide side) {
        return queueFor(side).peek();
    }

    /** Remove and return the best order on the given side, keeping the id index in sync. */
    public Order pollBest(OrderSide side) {
        Order order = queueFor(side).poll();
        if (order != null) {
            orderMap.remove(order.getOrderId());
        }
        return order;
    }

    public boolean contains(long orderId) {
        return orderMap.containsKey(orderId);
    }

    public Order get(long orderId) {
        return orderMap.get(orderId);
    }

    public boolean cancelOrder(long orderId) {
        Order order = orderMap.remove(orderId);
        if (order == null) {
            return false;
        }
        // O(n) heap scan, located by Order.equals (orderId). See class javadoc.
        return queueFor(order.getSide()).remove(order);
    }

    /**
     * Aggregated depth snapshot: bids high-to-low, asks low-to-high, with
     * quantities summed per price level.
     */
    public OrderBookView snapshot() {
        return new OrderBookView(
                aggregate(buyOrders, true),
                aggregate(sellOrders, false)
        );
    }

    private List<PriceLevel> aggregate(PriorityQueue<Order> queue, boolean descending) {
        // Reverse natural order => descending keys (bids high-to-low).
        TreeMap<Long, Long> byPrice = descending
                ? new TreeMap<>(Comparator.<Long>reverseOrder())
                : new TreeMap<>();

        for (Order order : queue) {
            byPrice.merge(order.getPriceTicks(), order.getQuantity(), Long::sum);
        }

        List<PriceLevel> levels = new ArrayList<>(byPrice.size());
        byPrice.forEach((price, qty) -> levels.add(new PriceLevel(price, qty)));
        return levels;
    }
}
