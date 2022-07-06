package com.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // disable in-memory database
public class ProductControllerIT {

    @Autowired private MockMvc mockMvc;
    
    private static final String DEFAULT_URL = "/api/products";

    @Test
    void when_GET_getAll_should_response_all_products_with_200() throws Exception {
        mockMvc.perform(get(DEFAULT_URL)
            .characterEncoding(StandardCharsets.UTF_8)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$", hasSize(11)))
        .andExpect(jsonPath("$[0].description").value("ALCOOL HIG AZULIM 50"))
        .andExpect(jsonPath("$[0].sequenceCode").value(is(137513)))
        .andExpect(jsonPath("$[0].barcode").value("7897534852624"))
        .andExpect(jsonPath("$[*].prices[*].value", hasSize(66)))
        .andExpect(jsonPath("$[0].prices[*].value", contains(5.65, 9.9, 10.75, 7.5)))
        .andExpect(jsonPath("$[1].prices[*].value", contains(11.30, 6.57, 4.31, 7.04, 14.3, 13.40, 17.40, 12.00, 3.26, 17.7, 11.50)));
    }

    @Test
    void when_GET_getAll_should_response_the_first_page_of_paged_products_with_200() throws Exception {
        final String firstPage = "0-5";

        mockMvc.perform(get(DEFAULT_URL).queryParam("pag", firstPage)
            .characterEncoding(StandardCharsets.UTF_8)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(5)))
        .andExpect(jsonPath("$.currentPage").value(0))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.numberOfItems").value(5))
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(
            jsonPath("$.content[*].barcode",
            contains("7896336010058", "7891000055120", "7898279792299", "7891962047560", "7896045104482"))
        )
        .andExpect(jsonPath("$.content[*].prices[*]", hasSize(40)));
    }

    @Test
    void when_GET_getAll_should_response_the_middle_page_of_paged_products_with_200() throws Exception {
        final String middlePag = "1-5";

        mockMvc.perform(get(DEFAULT_URL).queryParam("pag", middlePag)
            .characterEncoding(StandardCharsets.UTF_8)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(5)))
        .andExpect(jsonPath("$.currentPage").value(1))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.numberOfItems").value(5))
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(
            jsonPath("$.content[*].barcode",
            contains("7897534852624", "7896004004501", "7891098010575", "7896036093085", "7896656800018"))
        )
        .andExpect(jsonPath("$.content[*].prices[*]", hasSize(25)));
    }

    @Test
    void when_GET_getAll_should_response_the_last_page_of_paged_products_with_200() throws Exception {
        final String lastPage = "2-5";

        mockMvc.perform(get(DEFAULT_URL).queryParam("pag", lastPage)
            .characterEncoding(StandardCharsets.UTF_8)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.currentPage").value(2))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.numberOfItems").value(1))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.content[*].barcode", contains("7891962057620")))
        .andExpect(jsonPath("$.content[*].prices[*]", hasSize(1)));
    }

    @Test
    void when_GET_getAll_violating_parameter_pag_should_response_400() throws Exception {
        final String violatedPag = "0-)";

        mockMvc.perform(get(DEFAULT_URL).queryParam("pag", violatedPag)
            .characterEncoding(StandardCharsets.UTF_8)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.violations").isArray())
        .andExpect(jsonPath("$.violations", hasSize(1)))
        .andExpect(jsonPath("$.violations[0].field").value(violatedPag))
        .andExpect(jsonPath("$.violations[0].violationMessage").value("pag must match digit-digit"));
    }

    @Test
    void when_GET_getByBarcode_should_response_a_product_with_200() throws Exception {
        final String barcode = "7891000055120";

        mockMvc.perform(get(DEFAULT_URL+"/"+barcode)
            .characterEncoding(StandardCharsets.UTF_8)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.description").value("ACHOC PO NESCAU 800G"))
        .andExpect(jsonPath("$.sequenceCode").value(29250))
        .andExpect(jsonPath("$.barcode").value(barcode))
        .andExpect(jsonPath("$.prices").isArray())
        .andExpect(jsonPath("$.prices", hasSize(10)))
        .andExpect(jsonPath("$.prices[*].value", contains(12.70, 19.00, 16.50, 6.61, 16.80, 9.85, 10.60, 16.10, 12.60, 19.10)));
    }
}
