package com.kev.tme.engine;

import com.kev.tme.model.Order;
import com.kev.tme.model.OrderSide;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class OrderBook {

    // BUY: highest price first, then earlier time
    private final PriorityQueue<Order> buyOrders;

    // SELL: lowest price first, then earlier time
    private final PriorityQueue<Order> sellOrders;

    private final Map<Long, Order> orderMap = new HashMap<>();

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

        orderMap.put(order.getOrderId(), order);

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

    public boolean cancelOrder(long orderId){
        Order order = orderMap.remove(orderId);
        if (order == null) return false;

        if (order.getSide() == OrderSide.BUY){
            return buyOrders.remove(order);
        }
        else{
            return sellOrders.remove(order);
        }
    }

    public boolean modifyOrder(long orderId, double newPrice, long newQuantity){
        Order order = orderMap.get(orderId);
        if (order == null) return false;

        cancelOrder(orderId);

        Order modified = new Order(
                orderId,
                order.getSide(),
                order.getType(),
                newPrice,
                newQuantity
        );

        addOrder(modified);
        return true;
    }

}
