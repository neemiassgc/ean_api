package com.api.controller;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.service.CacheManager;
import com.api.service.interfaces.PriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.api.controller.PriceControllerTestHelper.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {PriceController.class, GlobalErrorHandlingController.class})
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class PriceControllerTest {

    @MockBean
    private PriceService priceService;

    @Autowired
    private MockMvc mockMvc;

    private final List<Price> usefulPrices = List.of(
        new Price(
            UUID.fromString("5b3e4ff1-99de-4d82-927e-3b26d868925c"),
            new BigDecimal("34.5"), Instant.now(), null
        ),
        new Price(
            UUID.fromString("e1960bf6-381b-458f-bb1f-2a41236337cd"),
            new BigDecimal("4.52"), Instant.now(), null
        ),
        new Price(
            UUID.fromString("b92b558b-b851-46cc-a2a3-b566e7e44966"),
            new BigDecimal("16.75"), Instant.now(), null
        ),
        new Price(
            UUID.fromString("6bba668b-eea7-46c7-99de-d80c965c847b"),
            new BigDecimal("12.12"), Instant.now(), null
        ),
        new Price(
            UUID.fromString("45578afd-75ad-4ba0-9aa6-f0391bca9d2a"),
            new BigDecimal("6.39"), Instant.now(), null
        )
    );

    @BeforeEach
    void setUp() {
        PriceControllerTestHelper.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("GET /api/prices/b92b558b-b851-46cc-a2a3-b566e7e44966 - 200 OK")
    void should_return_one_only_price_by_id() throws Exception {
        final UUID uuid = UUID.fromString("b92b558b-b851-46cc-a2a3-b566e7e44966");

        given(priceService.findById(eq(uuid))).willReturn(usefulPrices.get(2));

        makeRequestByUuid(uuid+"")
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", matchesRegex("^max-age=\\d{2,}$")))
            .andExpect(header().doesNotExist("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.value").value("16.75"));

        verify(priceService, times(1)).findById(eq(uuid));
        verify(priceService, only()).findById(eq(uuid));
    }

    @Test
    @DisplayName("GET /api/prices/b92b558b-b851-46cc-a2a3-b566e7e37d34 - 404 NOT FOUND")
    void when_a_price_is_not_found_then_should_return_a_error_message() throws Exception {
        final UUID uuid = UUID.fromString("b92b558b-b851-46cc-a2a3-b566e7e37d34");

        given(priceService.findById(eq(uuid)))
            .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Price not found"));

        makeRequestByUuid(uuid+"")
            .andExpect(status().isNotFound())
            .andExpect(header().doesNotExist("ETag"))
            .andExpect(header().doesNotExist("Cache-Control"))
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andExpect(content().string("Price not found"));

        verify(priceService, times(1)).findById(eq(uuid));
        verify(priceService, only()).findById(eq(uuid));
    }

    @Test
    @DisplayName("GET /api/prices?barcode=7896656800018 - 200 OK")
    void should_return_all_prices_for_a_barcode() throws Exception {
        final String targetBarcode = "7896656800018";
        final Sort orderByInstantDesc = Sort.by("instant").descending();

        given(priceService.findByProductBarcode(eq(targetBarcode), eq(orderByInstantDesc)))
            .willReturn(usefulPrices);

        makeRequestWithBarcode(targetBarcode)
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", matchesRegex("^max-age=\\d{2,}$")))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(5)))
            .andExpect(jsonPath("$[*].value", contains(34.5, 4.52, 16.75, 12.12, 6.39)));

        verify(priceService, times(1)).findByProductBarcode(eq(targetBarcode), eq(orderByInstantDesc));
        verify(priceService, only()).findByProductBarcode(eq(targetBarcode), eq(orderByInstantDesc));
    }

    @Test
    @DisplayName("GET /api/prices?barcode=7896656800018&limit=3 - 200 OK")
    void should_return_three_prices_for_a_barcode() throws Exception {
        final String targetBarcode = "7896656800018";
        final Pageable pageWith3ItemsOrderedByInstantDesc = PageRequest.ofSize(3).withSort(Sort.by("instant").descending());
        final List<Price> pricesToBeVerified = usefulPrices.subList(0, 3);
        
        given(priceService.findByProductBarcode(eq(targetBarcode), eq(pageWith3ItemsOrderedByInstantDesc)))
            .willReturn(pricesToBeVerified);

        makeRequestWithBarcodeAndLimit(targetBarcode, 3)
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", matchesRegex("^max-age=\\d{2,}$")))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[*].value", contains(34.5, 4.52, 16.75)));

        verify(priceService, times(1)).findByProductBarcode(eq(targetBarcode), eq(pageWith3ItemsOrderedByInstantDesc));
        verify(priceService, only()).findByProductBarcode(eq(targetBarcode), eq(pageWith3ItemsOrderedByInstantDesc));
    }

    @Test
    @DisplayName("GET /api/prices?barcode=7896656811118 - 404 NOT FOUND")
    void when_there_are_no_prices_for_a_barcode_then_should_return_a_error_massage() throws Exception {
        final String targetBarcode = "7896656811118";
        final Sort orderByInstantDesc = Sort.by("instant").descending();

        given(priceService.findByProductBarcode(eq(targetBarcode), eq(orderByInstantDesc)))
            .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        makeRequestWithBarcode(targetBarcode)
            .andExpect(status().isNotFound())
            .andExpect(header().doesNotExist("ETag"))
            .andExpect(header().doesNotExist("Cache-Control"))
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andExpect(content().string("Product not found"));

        verify(priceService, times(1)).findByProductBarcode(eq(targetBarcode), eq(orderByInstantDesc));
        verify(priceService, only()).findByProductBarcode(eq(targetBarcode), eq(orderByInstantDesc));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public CacheManager<Product, UUID> productCacheManager() {
            return new CacheManager<>(Product::getId);
        }
    }
}