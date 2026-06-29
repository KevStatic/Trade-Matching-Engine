package com.kev.tme.api;

import com.kev.tme.engine.MatchingEngine;
import com.kev.tme.engine.OrderResult;
import com.kev.tme.model.Order;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final MatchingEngine engine;

    public OrderController(MatchingEngine engine) {
        this.engine = engine;
    }

    @PostMapping
    public ResponseEntity<OrderResult> placeOrder(@Valid @RequestBody OrderRequest request) {
        Order order = new Order(
                request.orderId,
                request.side,
                request.type,
                request.effectivePriceTicks(),
                request.quantity,
                request.effectiveTif()
        );
        return ResponseEntity.ok(engine.submitOrder(order));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable long orderId) {
        boolean cancelled = engine.cancelOrder(orderId);
        if (!cancelled) {
            return ResponseEntity.status(404)
                    .body(Map.of("orderId", orderId, "status", "NOT_FOUND"));
        }
        return ResponseEntity.ok(Map.of("orderId", orderId, "status", "CANCELLED"));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResult> modifyOrder(
            @PathVariable long orderId,
            @Valid @RequestBody OrderRequest request
    ) {
        OrderResult result = engine.modifyOrder(
                orderId,
                request.effectivePriceTicks(),
                request.quantity
        );
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
