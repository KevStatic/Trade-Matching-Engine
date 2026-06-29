package com.kev.tme.model;

/**
 * Outcome of submitting an order to the matching engine.
 *
 * <ul>
 *     <li>{@link #ACCEPTED} - nothing filled, the order rests in the book
 *     (GTC limit order that did not cross).</li>
 *     <li>{@link #PARTIALLY_FILLED} - some quantity filled, some remains
 *     (either resting for GTC, or discarded for market/IOC).</li>
 *     <li>{@link #FILLED} - fully filled, nothing remains.</li>
 *     <li>{@link #REJECTED} - nothing filled and nothing rests (market order
 *     with no opposite liquidity, or IOC limit order that could not cross).</li>
 * </ul>
 */
public enum OrderStatus {
    ACCEPTED,
    PARTIALLY_FILLED,
    FILLED,
    REJECTED
}
