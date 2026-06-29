package com.kev.tme.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end HTTP tests over the real Spring context. Each method gets a fresh
 * context (hence a fresh engine/book) so cumulative endpoints like /orderbook
 * and /trades are deterministic.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderApiTest {

    @Autowired
    private MockMvc mockMvc;

    private void postOrder(String json) throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void placeMatchAndObserveBookAndTrades() throws Exception {
        // Resting sell: 100.50 (10050 ticks) x 10.
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":1,"side":"SELL","type":"LIMIT","priceTicks":10050,"quantity":10}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.filledQuantity").value(0))
                .andExpect(jsonPath("$.remainingQuantity").value(10));

        // Incoming buy crosses for 4 -> fully filled, leaving 6 resting on the ask.
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":2,"side":"BUY","type":"LIMIT","priceTicks":10050,"quantity":4}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FILLED"))
                .andExpect(jsonPath("$.filledQuantity").value(4))
                .andExpect(jsonPath("$.remainingQuantity").value(0))
                .andExpect(jsonPath("$.trades.length()").value(1))
                .andExpect(jsonPath("$.trades[0].buyOrderId").value(2))
                .andExpect(jsonPath("$.trades[0].sellOrderId").value(1))
                .andExpect(jsonPath("$.trades[0].priceTicks").value(10050))
                .andExpect(jsonPath("$.trades[0].quantity").value(4));

        // /orderbook: one ask level (6 left), no bids.
        mockMvc.perform(get("/orderbook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bids.length()").value(0))
                .andExpect(jsonPath("$.asks.length()").value(1))
                .andExpect(jsonPath("$.asks[0].priceTicks").value(10050))
                .andExpect(jsonPath("$.asks[0].quantity").value(6));

        // /trades: the single executed trade.
        mockMvc.perform(get("/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].priceTicks").value(10050))
                .andExpect(jsonPath("$[0].quantity").value(4));
    }

    @Test
    void marketOrderWithNoLiquidityIsRejected() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":1,"side":"BUY","type":"MARKET","quantity":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.filledQuantity").value(0))
                .andExpect(jsonPath("$.trades.length()").value(0));
    }

    @Test
    void limitOrderWithoutPriceIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":1,"side":"BUY","type":"LIMIT","quantity":5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.messages").isArray());
    }

    @Test
    void marketOrderWithPriceIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":1,"side":"BUY","type":"MARKET","priceTicks":10050,"quantity":5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void nonPositiveQuantityIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":1,"side":"BUY","type":"LIMIT","priceTicks":10050,"quantity":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
