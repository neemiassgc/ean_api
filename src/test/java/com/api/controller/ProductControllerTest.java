package com.api.controller;

import com.api.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static com.api.controller.ProductControllerTestUtils.*;
import static com.api.projection.Projection.ProductWithManyPrices;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(ProductController.class)
@Import(ValidationAutoConfiguration.class)
class ProductControllerTest {

    @MockBean
    private ProductRepository productRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = standaloneSetup(new ProductController(this.productRepository)).alwaysDo(print()).build();
    }

    @Test
    void when_GET_getAll_should_response_all_products_with_200() throws Exception {
        given(productRepository.<ProductWithManyPrices>findAllProducts()).willReturn(getProductList());

        mockMvc.perform(get("/api/products")
            .characterEncoding("UTF-8")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[*].prices.*").isArray())
        .andExpect(jsonPath("$[*].prices.*", hasSize(9)))
        .andExpect(jsonPath("$[0].prices[0].value").value(5.45));

        verify(productRepository, times(1)).findAllProducts();
        verify(productRepository, only()).findAllProducts();
    }

    @Test
    void when_GET_getAll_should_response_a_empty_list_with_200() throws Exception {
        given(productRepository.findAllProducts()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/products")
            .characterEncoding("UTF-8")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isEmpty());

        verify(productRepository, times(1)).findAllProducts();
        verify(productRepository, only()).findAllProducts();
    }

    @Test
    void when_GET_getAll_should_response_a_paged_product_list_with_200() throws Exception {
        given(productRepository.findAllPagedProducts(Mockito.any(Pageable.class))).willReturn(getPagedProductList());

        mockMvc.perform(get("/api/products?pag=0-3")
            .characterEncoding("UTF-8")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(3)))
        .andExpect(jsonPath("$.content[0].prices").isArray())
        .andExpect(jsonPath("$.content[*].prices[*]", hasSize(9)))
        .andExpect(jsonPath("$.content[0].prices[*].value", contains(5.45, 3.67, 8.5)))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.numberOfItems").value(3))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.currentPage").value(0));

        verify(productRepository, never()).findAllProducts();
    }

    @Test
    void when_GET_getByBarcode_should_response_a_product_with_all_prices_with_200() throws Exception {
        final String validBarcode = "7891962057620";

        given(productRepository.findProductByBarcode(eq(validBarcode), eq(0))).willReturn(getNewProduct("5.45", "8.56", "6.3"));

        mockMvc.perform(get("/api/products/"+validBarcode)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").exists())
        .andExpect(jsonPath("$.description", is("PAO 400G")))
        .andExpect(jsonPath("$.sequenceCode", is(134262)))
        .andExpect(jsonPath("$.barcode", is(validBarcode)))
        .andExpect(jsonPath("$.prices", hasSize(3)))
        .andExpect(jsonPath("$.prices[*].value", contains(5.45, 8.56, 6.3)));

        verify(productRepository, times(1)).findProductByBarcode(eq(validBarcode), eq(0));
        verify(productRepository, only()).findProductByBarcode(eq(validBarcode), eq(0));
    }

    @Test
    void when_GET_getByBarcode_should_response_a_product_with_latest_price() throws Exception {
        final String validBarcode = "7891962057620";
        final int limitOfPrices = 1;

        given(productRepository.findProductByBarcode(eq(validBarcode), eq(limitOfPrices)))
            .willReturn(getNewProduct());

        mockMvc.perform(get("/api/products/"+validBarcode+"?lop="+limitOfPrices)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").exists())
        .andExpect(jsonPath("$.description", is("PAO 400G")))
        .andExpect(jsonPath("$.sequenceCode", is(134262)))
        .andExpect(jsonPath("$.barcode", is(validBarcode)))
        .andExpect(jsonPath("$.prices").doesNotExist())
        .andExpect(jsonPath("$.latestPrice.value", is(2.6)));

        verify(productRepository, times(1)).findProductByBarcode(eq(validBarcode), eq(limitOfPrices));
        verify(productRepository, only()).findProductByBarcode(eq(validBarcode), eq(limitOfPrices));
    }

    @Test
    void when_GET_getByBarcode_should_response_a_product_with_two_latest_prices() throws Exception {
        final String validBarcode = "7891962057620";
        final int limitOfPrices = 2;

        given(productRepository.findProductByBarcode(eq(validBarcode), eq(limitOfPrices))).willReturn(getNewProduct("2.6", "3.4"));

        mockMvc.perform(get("/api/products/"+validBarcode+"?lop="+limitOfPrices)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").exists())
        .andExpect(jsonPath("$.description", is("PAO 400G")))
        .andExpect(jsonPath("$.sequenceCode", is(134262)))
        .andExpect(jsonPath("$.barcode", is(validBarcode)))
        .andExpect(jsonPath("$.prices", hasSize(2)))
        .andExpect(jsonPath("$.prices[*].value", contains(2.6, 3.4)));

        verify(productRepository, times(1)).findProductByBarcode(eq(validBarcode), eq(limitOfPrices));
        verify(productRepository, only()).findProductByBarcode(eq(validBarcode), eq(limitOfPrices));
    }
}