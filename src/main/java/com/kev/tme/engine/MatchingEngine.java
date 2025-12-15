package com.kev.tme.engine;

import com.kev.tme.model.Order;
import com.kev.tme.model.Trade;

import java.util.List;

public class MatchingEngine {

    private final OrderBook orderBook;

    private final TradeStore tradeStore;

    public MatchingEngine(){
        this.orderBook = new OrderBook();
        this.tradeStore = new TradeStore();
    }

    public synchronized void submitOrder(Order order) {
        orderBook.addOrder(order);
        matchOrders();
    }

    private synchronized void matchOrders(){

        while(!orderBook.getBuyOrders().isEmpty() && !orderBook.getSellOrders().isEmpty()){

            Order buy = orderBook.getBuyOrders().peek();
            Order sell = orderBook.getSellOrders().peek();

            boolean priceMatch = buy.isMarketOrder() || sell.isMarketOrder() || buy.getPrice() >= sell.getPrice();

            if (!priceMatch) {
                break;
            }

            long tradedQty = Math.min(
                    buy.getQuantity(),
                    sell.getQuantity()
            );

            double tradePrice = sell.isMarketOrder()
                    ? buy.getPrice()
                    : sell.getPrice();

            Trade trade = new Trade(
                    buy.getOrderId(),
                    sell.getOrderId(),
                    tradePrice,
                    tradedQty
            );

            tradeStore.recordTrade(trade);
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

    public synchronized boolean cancelOrder(long orderId){
        return orderBook.cancelOrder(orderId);
    }

    public synchronized boolean modifyOrder(long orderId, double newPrice, long newQuantity){
        return orderBook.modifyOrder(orderId, newPrice, newQuantity);
    }

    public List<Trade> getTradeHistory() {
        return tradeStore.getAllTrades();
    }

}
