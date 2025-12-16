package com.kev.tme.api;

import com.kev.tme.model.OrderSide;
import com.kev.tme.model.OrderType;

public class OrderRequest {

    public long orderId;
    public OrderSide side;
    public OrderType type;
    public double price;
    public long quantity;

}
