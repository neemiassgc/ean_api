package com.xyz.ean.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void given_an_existing_bar_code_should_create_a_product_with_200_create() throws Exception {
        final String existentBarCodeJson = "{\"eanCode\":\"7897534852624\"}";

        mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(existentBarCodeJson)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.description").value("ALCOOL HIG AZULIM 50"))
        .andExpect(jsonPath("$.priceInstants").isArray())
        .andExpect(jsonPath("$.priceInstants", hasSize(4)))
        .andExpect(jsonPath("$.priceInstants[*].price", contains(5.65, 9.9, 10.75, 7.5)))
        .andExpect(jsonPath("$.eanCode").value("7897534852624"))
        .andExpect(jsonPath("$.sequenceCode").value(137513));
    }
}
