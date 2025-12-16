package com.kev.tme.api;

import com.kev.tme.engine.MatchingEngine;
import com.kev.tme.model.Trade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/trades")
public class TradeController {

    private final MatchingEngine engine;

    public TradeController(MatchingEngine engine){
        this.engine = engine;
    }

    @GetMapping
    public List<Trade> getTrades(){
        return engine.getTradeHistory();
    }

}
