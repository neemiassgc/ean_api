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

import static org.hamcrest.Matchers.*;
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

    @Test
    @DisplayName("GET /api/prices?barcode=7897534852624 - 200 OK")
    void should_return_all_prices_for_a_barcode() throws Exception {
        final String barcode = "7897534852624";

        mockMvc.perform(get("/api/prices").param("barcode", barcode)
            .characterEncoding(StandardCharsets.UTF_8)
            .accept(MediaType.ALL)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(4)))
        .andExpect(jsonPath("$[*].value", contains(5.65, 9.90, 10.75, 7.50)));
    }

    @Test
    @DisplayName("GET /api/prices?barcode=78975348526 - 400 BAD REQUEST")
    void should_return_constraint_violations_for_barcode_field() throws Exception {
        final String problematicBarcode = "78975348526";

        mockMvc.perform(get("/api/prices").param("barcode", problematicBarcode)
            .characterEncoding(StandardCharsets.UTF_8)
            .accept(MediaType.ALL)
        )
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.violations", hasSize(2)))
        .andExpect(jsonPath("$.violations[*].field", everyItem(is(problematicBarcode))))
        .andExpect(jsonPath(
            "$.violations[*].violationMessage",
            containsInAnyOrder("barcode must contain only numbers", "barcode must has 13 characters")
        ));
    }

    @Test
    @DisplayName("GET /api/prices?barcode=7897534852624?limit=3 - 200 OK")
    void should_return_only_three_prices_for_a_barcode() throws Exception {
        final String barcode = "7897534852624";

        mockMvc.perform(get("/api/prices")
            .param("barcode", barcode)
            .param("limit", "3")
            .characterEncoding(StandardCharsets.UTF_8)
            .accept(MediaType.ALL)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[*].value", contains(5.65, 9.90, 10.75)));
    }
}
