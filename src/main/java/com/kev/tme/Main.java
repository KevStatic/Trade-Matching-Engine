package com.kev.tme;

import com.kev.tme.engine.MatchingEngine;
import com.kev.tme.model.Order;
import com.kev.tme.model.OrderSide;
import com.kev.tme.model.OrderType;

public class Main {
    public static void main(String[] args){
        System.out.println("Trade Matching Engine Started!");

        MatchingEngine engine = new MatchingEngine();

        engine.submitOrder(new Order(1, OrderSide.BUY, OrderType.LIMIT, 100.50, 10));
        engine.submitOrder(new Order(2, OrderSide.SELL, OrderType.LIMIT, 99.75, 5));
        engine.submitOrder(new Order(3, OrderSide.SELL, OrderType.LIMIT, 100.50, 5));


    }
}
