package com.xyz.ean.controller;

import com.xyz.ean.dto.ProductResponseDTO;
import com.xyz.ean.service.DomainMapper;
import com.xyz.ean.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @MockBean
    private ProductService productServiceMock;

    @MockBean
    private DomainMapper domainMapperMock;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = standaloneSetup(new ProductController(this.productServiceMock, this.domainMapperMock)).build();
    }

    private static ProductResponseDTO getProductResponseDTO() {
        final ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setDescription("default description");
        productResponseDTO.setPrices(List.of(new ProductResponseDTO.PriceInstant(Instant.now(), 4.55)));
        productResponseDTO.setEanCode("1234567890123");
        productResponseDTO.setSequenceCode(12345);
        return productResponseDTO;
    }

    @Test
    void whenPOSTAnExistentEanCodeThenResponseOK() throws Exception {
        given(this.productServiceMock.saveByEanCode(anyString())).willReturn(null);
        given(this.domainMapperMock.mapToDto(isNull())).willReturn(getProductResponseDTO());

        mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .content("{\"eanCode\":\"1234567890123\"}")
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.description").value("default description"))
        .andExpect(jsonPath("$.prices").isArray())
        .andExpect(jsonPath("$.prices").isNotEmpty())
        .andExpect(jsonPath("$.prices[0].price").value(4.55))
        .andExpect(jsonPath("$.eanCode").value("1234567890123"))
        .andExpect(jsonPath("$.sequenceCode").value(12345));

        verify(this.productServiceMock, times(1)).saveByEanCode(anyString());
        verify(this.domainMapperMock, times(1)).mapToDto(isNull());
    }

    @Test
    void whenPOSTANonExistentEanCodeThenResponseNotFound() throws Exception {
        given(this.productServiceMock.saveByEanCode(anyString())).willReturn(null);
        given(this.domainMapperMock.mapToDto(isNull())).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        final MvcResult mvcResult = mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content("{\"eanCode\":\"1234567890124\"}")
        )
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.reasons").isArray())
        .andExpect(jsonPath("$.reasons", hasSize(1)))
        .andExpect(jsonPath("$.reasons[0]").value("Product not found"))
        .andExpect(jsonPath("$.status").value("NOT_FOUND"))
        .andReturn();

        assertThat(mvcResult.getResolvedException()).isInstanceOf(ResponseStatusException.class);
        assertThat((ResponseStatusException)mvcResult.getResolvedException()).extracting("reason").isEqualTo("Product not found");
        assertThat((ResponseStatusException)mvcResult.getResolvedException()).extracting("status").isEqualTo(HttpStatus.NOT_FOUND);


        verify(this.productServiceMock, times(1)).saveByEanCode(anyString());
        verify(this.domainMapperMock, times(1)).mapToDto(isNull());
    }
}