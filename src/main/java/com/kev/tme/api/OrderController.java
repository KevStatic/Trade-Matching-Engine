package com.kev.tme.api;

import com.kev.tme.engine.MatchingEngine;
import com.kev.tme.model.Order;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final MatchingEngine engine;

    public OrderController(MatchingEngine engine){
        this.engine = engine;
    }

    @PostMapping
    public String placeOrder(@RequestBody OrderRequest request){
        Order order = new Order(
                request.orderId,
                request.side,
                request.type,
                request.price,
                request.quantity
        );
        engine.submitOrder(order);
        return "Order Accepted!";
    }

    @DeleteMapping("/{orderId}")
    public String cancelOrder(@PathVariable long orderId){
        boolean result = engine.cancelOrder(orderId);
        return result ? "Order cancelled" : "Order not found";
    }

    @PutMapping("/{orderId}")
    public String modifyOrder(
            @PathVariable long orderId,
            @RequestBody OrderRequest request
    ){
        boolean result = engine.modifyOrder(
                orderId,
                request.price,
                request.quantity
        );

        return result ? "Order Modified" : "Order Not found";
    }

}
