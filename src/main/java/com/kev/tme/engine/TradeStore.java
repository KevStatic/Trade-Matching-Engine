package com.kev.tme.engine;

import com.kev.tme.model.Trade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TradeStore {

    private final List<Trade> trades =
            Collections.synchronizedList(new ArrayList<>());

    public void recordTrade(Trade trade) {
        trades.add(trade);
    }

    public List<Trade> getAllTrades() {
        return new ArrayList<>(trades);
    }

    public int tradeCount() {
        return trades.size();
    }
}
