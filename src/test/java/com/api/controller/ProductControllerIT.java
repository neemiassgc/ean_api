package com.api.controller;

import com.api.repository.PriceRepository;
import com.api.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.hamcrest.MockitoHamcrest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

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
}
