package com.kev.tme;

import com.kev.tme.engine.MatchingEngine;
import com.kev.tme.model.Order;
import com.kev.tme.model.OrderSide;
import com.kev.tme.model.OrderType;

public class Main {
    public static void main(String[] args){
        System.out.println("Trade Matching Engine Started!");

        MatchingEngine engine = new MatchingEngine();

        // 1) Basic LIMIT Order Matching
        engine.submitOrder(new Order(1, OrderSide.BUY,  OrderType.LIMIT, 100.50, 10));
        engine.submitOrder(new Order(2, OrderSide.SELL, OrderType.LIMIT, 99.75, 5));
        engine.submitOrder(new Order(3, OrderSide.SELL, OrderType.LIMIT, 100.50, 5));

        // 2) MARKET Order vs LIMIT Orders
//        engine.submitOrder(new Order(10, OrderSide.SELL, OrderType.LIMIT, 101.00, 5));
//        engine.submitOrder(new Order(11, OrderSide.SELL, OrderType.LIMIT, 102.00, 5));
//        engine.submitOrder(new Order(12, OrderSide.BUY, OrderType.MARKET, 0, 7));

        // 3) LIMIT Order That Does NOT Match
//        engine.submitOrder(new Order(20, OrderSide.BUY,  OrderType.LIMIT, 95.00, 5));
//        engine.submitOrder(new Order(21, OrderSide.SELL, OrderType.LIMIT, 105.00, 5));

        // 4) Order Cancellation
//        engine.submitOrder(new Order(30, OrderSide.BUY, OrderType.LIMIT, 100.00, 5));
//        engine.cancelOrder(30);
//        engine.submitOrder(new Order(31, OrderSide.SELL, OrderType.LIMIT, 99.00, 5));

        // 5) Order Modification
//        engine.submitOrder(new Order(40, OrderSide.BUY, OrderType.LIMIT, 98.00, 5));
//        engine.modifyOrder(40, 101.00, 5);
//        engine.submitOrder(new Order(41, OrderSide.SELL, OrderType.LIMIT, 100.00, 5));

        // 6) Trade History verification
        System.out.println("Total Trades: " + engine.getTradeHistory().size());


    }
}
