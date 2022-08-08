package com.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // disable in-memory database
public class PriceControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/prices/5ad12c1a-2103-407c-adcc-832e3f99fb5b - 200 OK")
    void should_return_a_price_by_its_id_with_200() throws Exception {
        mockMvc.perform(get("/api/prices/5ad12c1a-2103-407c-adcc-832e3f99fb5b")
            .characterEncoding(StandardCharsets.UTF_8)
            .accept(MediaType.ALL)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.value").value(18.40))
        .andExpect(jsonPath("$.instant").value("2021-09-09T16:50:10.554Z"));
    }

    @Test
    @DisplayName("GET /api/prices/5ad12c1a-2103-407c-adcc-832e3f99fa9a - 404 NOT FOUND")
    void when_a_price_does_not_exist_then_should_return_404_not_found() throws Exception {
        mockMvc.perform(get("/api/prices/5ad12c1a-2103-407c-adcc-832e3f99fa9a")
            .characterEncoding(StandardCharsets.UTF_8)
            .accept(MediaType.ALL)
        )
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.TEXT_PLAIN))
        .andExpect(content().string("Price not found"));
    }
}
