package com.kev.tme.model;

import java.time.Instant;

public class Trade {

    private final long buyOrderId;
    private final long sellOrderId;
    private final double price;
    private final long quantity;
    private final Instant timestamp;

    public Trade(
            long buyOrderId,
            long sellOrderId,
            double price,
            long quantity
    ){
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = Instant.now();
    }

    @Override
    public String toString(){
        return " TRADE | BUY =" + buyOrderId +
                " SELL =" + sellOrderId +
                " PRICE =" + price +
                " QUANTITY =" + quantity;
    }

}
