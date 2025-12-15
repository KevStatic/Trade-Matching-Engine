package com.kev.tme.engine;

import com.kev.tme.model.Order;
import com.kev.tme.model.OrderSide;

import java.util.Comparator;
import java.util.PriorityQueue;

public class OrderBook {

    // BUY: highest price first, then earlier time
    private final PriorityQueue<Order> buyOrders;

    // SELL: lowest price first, then earlier time
    private final PriorityQueue<Order> sellOrders;

    public OrderBook(){

        buyOrders = new PriorityQueue<>(
                Comparator
                        .comparingDouble(Order::getPrice)
                        .reversed()
                        .thenComparing(Order::getTimestamp)
        );

        sellOrders = new PriorityQueue<>(
                Comparator
                        .comparingDouble(Order::getPrice)
                        .thenComparing(Order::getTimestamp)
        );

    }

    public void addOrder(Order order){
        if (order.getSide() == OrderSide.BUY){
            buyOrders.offer(order);
        }
        else{
            sellOrders.offer(order);
        }
    }

    public PriorityQueue<Order> getBuyOrders(){
        return buyOrders;
    }

    public PriorityQueue<Order> getSellOrders(){
        return sellOrders;
    }

}
