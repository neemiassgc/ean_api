package com.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // disable in-memory database
public class ProductControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    
    private static final String DEFAULT_URL = "/api/products";

    @Test
    void when_POST_existing_bar_code_should_create_a_product_with_200_create() throws Exception {
        final String existentBarCodeJson = "{\"eanCode\":\"7897534852624\"}";

        mockMvc.perform(post(DEFAULT_URL)
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
        .andExpect(jsonPath("$.barcode").value("7897534852624"))
        .andExpect(jsonPath("$.sequenceCode").value(137513));
    }

    @Test
    void when_POST_a_non_existing_bar_code_should_response_404_create() throws Exception {
        final String nonExistentBarCodeJson = "{\"eanCode\":\"5897534852624\"}";

        mockMvc.perform(post(DEFAULT_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(nonExistentBarCodeJson)
        )
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.reasons").isArray())
        .andExpect(jsonPath("$.reasons", hasSize(1)))
        .andExpect(jsonPath("$.reasons[0]").value("Product not found"))
        .andExpect(jsonPath("$.status").value("NOT_FOUND"));
    }

    @Test
    void when_GET_should_response_all_products_with_200_getAll() throws Exception {
        mockMvc.perform(get(DEFAULT_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(4)))
            .andExpect(jsonPath("$[*].priceInstants[0]", hasSize(0)))
            .andExpect(jsonPath("$[*].priceInstants[1]", hasSize(3)))
            .andExpect(jsonPath("$[*].priceInstants[2]", hasSize(2)))
            .andExpect(jsonPath("$[*].priceInstants[3]", hasSize(1)))
            .andExpect(jsonPath("$[*].description", contains("ALCOOL HIG AZULIM 50", "OLEO MARIA", "CHA CAMOMILA", "PAO BAUDUC 400G INTE")));
    }

    @Test
    void when_GET_an_existent_barcode_should_response_a_product_with_200_getByBarcode() throws Exception {
        final String existentBarcode = "7891098010575";

        mockMvc.perform(get(DEFAULT_URL+"/"+existentBarcode).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.description").value("CHA CAMOMILA"))
            .andExpect(jsonPath("$.priceInstants").isArray())
            .andExpect(jsonPath("$.priceInstants", hasSize(2)))
            .andExpect(jsonPath("$.priceInstants[*].price", contains(6.49, 1.49)))
            .andExpect(jsonPath("$.barcode").value(existentBarcode))
            .andExpect(jsonPath("$.sequenceCode").value(9785));
    }

    @Test
    void when_GET_a_non_existent_barcode_should_response_404_getByBarcode() throws Exception {
        final String nonExistentBarcode = "7891098010576";

        mockMvc.perform(get(DEFAULT_URL+"/"+nonExistentBarcode).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.reasons").isArray())
            .andExpect(jsonPath("$.reasons", hasSize(1)))
            .andExpect(jsonPath("$.reasons[0]").value("Product not found"))
            .andExpect(jsonPath("$.status").value("NOT_FOUND"));
    }
}
