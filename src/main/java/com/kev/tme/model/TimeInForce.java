package com.kev.tme.model;

/**
 * Time-in-force policy for an order.
 *
 * <ul>
 *     <li>{@link #GTC} - Good-Til-Cancelled: any unfilled remainder of a limit
 *     order rests in the book until matched or cancelled.</li>
 *     <li>{@link #IOC} - Immediate-Or-Cancel: match whatever can be filled
 *     immediately, then cancel (discard) any remainder. Never rests.</li>
 * </ul>
 *
 * Time-in-force only affects limit orders. Market orders never rest regardless
 * of this value.
 */
public enum TimeInForce {
    GTC,
    IOC
}
