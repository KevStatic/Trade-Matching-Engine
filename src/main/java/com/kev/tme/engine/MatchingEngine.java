package com.kev.tme.engine;

import com.kev.tme.model.Order;
import com.kev.tme.model.Trade;

public class MatchingEngine {

    private final OrderBook orderBook;

    public MatchingEngine(){
        this.orderBook = new OrderBook();
    }

    public void submitOrder(Order order){
        orderBook.addOrder(order);
        matchOrders();
    }

    private void matchOrders(){

        while(!orderBook.getBuyOrders().isEmpty() && !orderBook.getSellOrders().isEmpty()){

            Order buy = orderBook.getBuyOrders().peek();
            Order sell = orderBook.getSellOrders().peek();

            // Price check
            if (buy.getPrice() < sell.getPrice()){
                break; // Not possible
            }

            long tradedQty = Math.min(
                    buy.getQuantity(),
                    sell.getQuantity()
            );

            double tradePrice = sell.getPrice();

            Trade trade = new Trade(
                    buy.getOrderId(),
                    sell.getOrderId(),
                    tradePrice,
                    tradedQty
            );

            System.out.println(trade);

            buy.reduceQuantity(tradedQty);
            sell.reduceQuantity(tradedQty);

            if (buy.getQuantity() == 0){
                orderBook.getBuyOrders().poll();
            }

            if (sell.getQuantity() == 0){
                orderBook.getSellOrders().poll();
            }

        }

    }

}
