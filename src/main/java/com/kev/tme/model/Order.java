package com.kev.tme.model;

import java.time.Instant;

public class Order {

    private final long orderId;
    private final OrderSide side;
    private final OrderType type;
    private final double price;
    private long quantity;
    private final Instant timestamp;

    public Order(
            long orderId,
            OrderSide side,
            OrderType type,
            double price,
            long quantity
    ){
        this.orderId = orderId;
        this.side = side;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = Instant.now();
    }

    public long getOrderId(){
        return orderId;
    }

    public OrderSide getSide(){
        return side;
    }

    public OrderType getType(){
        return type;
    }

    public double getPrice(){
        return price;
    }

    public long getQuantity(){
        return quantity;
    }

    public void reduceQuantity(long qty){
        this.quantity -= qty;
    }

    public Instant getTimestamp(){
        return timestamp;
    }

}
