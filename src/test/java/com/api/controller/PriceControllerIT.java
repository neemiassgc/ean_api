package com.api.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static com.api.controller.PriceControllerTestHelper.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // disable in-memory database
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PriceControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    void init() {
        PriceControllerTestHelper.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("GET /api/prices/5ad12c1a-2103-407c-adcc-832e3f99fb5b - 200 OK")
    void should_return_a_price_by_its_id_with_200() throws Exception {
        makeRequestByUuid("5ad12c1a-2103-407c-adcc-832e3f99fb5b")
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.value").value(18.40))
            .andExpect(jsonPath("$.instant").value("2021-09-09T16:50:10.554Z"));
    }

    @Test
    @DisplayName("GET /api/prices/5ad12c1a-2103-407c-adcc-832e3f99fa9a - 404 NOT FOUND")
    void when_a_price_does_not_exist_then_should_return_404_not_found() throws Exception {
        makeRequestByUuid("5ad12c1a-2103-407c-adcc-832e3f99fa9a")
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andExpect(content().string("Price not found"));
    }

    @Test
    @DisplayName("GET /api/prices?barcode=7897534852624 - 200 OK")
    void should_return_all_prices_for_a_barcode() throws Exception {
        makeRequestWithBarcode("7897534852624")
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(4)))
            .andExpect(jsonPath("$[*].value", contains(5.65, 9.90, 10.75, 7.50)));
    }

    @Test
    @DisplayName("GET /api/prices?barcode=78975348526 - 400 BAD REQUEST")
    void should_return_constraint_violations_for_barcode_field() throws Exception {
        final String problematicBarcode = "7897534852ab";

        makeRequestWithBarcode(problematicBarcode)
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.violations", hasSize(2)))
            .andExpect(jsonPath("$.violations[*].field", everyItem(is(problematicBarcode))))
            .andExpect(jsonPath(
                "$.violations[*].violationMessage",
                containsInAnyOrder("barcode must contain only numbers", "barcode must have 13 characters")
            ));
    }

    @Test
    @DisplayName("GET /api/prices?barcode=7897534852624?limit=3 - 200 OK")
    void should_return_only_three_prices_for_a_barcode() throws Exception {
        makeRequestWithBarcodeAndLimit("7897534852624", 3)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[*].value", contains(5.65, 9.90, 10.75)));
    }

    @Test
    @DisplayName("GET /api/prices?barcode=7897534852836 - 404 NOT FOUND")
    void if_there_are_no_prices_for_a_barcode_then_return_not_found() throws Exception {
        makeRequestWithBarcodeAndLimit("7897534852836", 3)
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andExpect(content().string("Product not found"));
    }

    @Test
    @DisplayName("GET /api/prices?barcode=7897534852624?limit=-3 - 400 BAD REQUEST")
    void should_return_constraint_violations_for_limit_param() throws Exception {
        final int negativeLimit = -3;

        makeRequestWithBarcodeAndLimit("7897534852624", -3)
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.violations", hasSize(1)))
            .andExpect(jsonPath("$.violations[0].field").value(negativeLimit))
            .andExpect(jsonPath("$.violations[0].violationMessage").value("must be greater than or equal to 0"));
    }
}
