package com.kev.tme.api;

import com.kev.tme.engine.MatchingEngine;
import com.kev.tme.engine.OrderBookView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the resting book so it is observable: aggregated bids (high-to-low)
 * and asks (low-to-high) by price level.
 */
@RestController
@RequestMapping("/orderbook")
public class OrderBookController {

    private final MatchingEngine engine;

    public OrderBookController(MatchingEngine engine) {
        this.engine = engine;
    }

    @GetMapping
    public OrderBookView getOrderBook() {
        return engine.getOrderBookView();
    }
}
