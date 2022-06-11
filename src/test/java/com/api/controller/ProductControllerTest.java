package com.api.controller;

import com.api.service.DomainMapper;
import com.api.dto.ProductResponseDTO;
import com.api.service.ProductService;
import org.assertj.core.util.Objects;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @MockBean
    private ProductService productServiceMock;

    @SuppressWarnings("unused")
    @MockBean
    private DomainMapper domainMapperMock;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = standaloneSetup(new ProductController(this.productServiceMock, this.domainMapperMock))
            .alwaysDo(print()).build();
    }
    
    private ProductResponseDTO getANewInstanceOfResponseDTO() {
        return ProductResponseDTO.builder()
            .description("default description")
            .priceInstants(List.of(new ProductResponseDTO.PriceInstant(Instant.now(), 4.55)))
            .barcode("1234567890123")
            .sequenceCode(12345).build();
    }

    @Test
    void when_POST_an_existent_bar_code_then_response_200_create() throws Exception {
        given(this.productServiceMock.saveByBarcode(anyString())).willReturn(null);
        given(this.domainMapperMock.mapToDto(isNull())).willReturn(this.getANewInstanceOfResponseDTO());
        final String existentBarCodeJson = "{\"eanCode\":\"1234567890123\"}";

        mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .content(existentBarCodeJson)
            .characterEncoding("UTF-8")
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.description").value("default description"))
        .andExpect(jsonPath("$.priceInstants").isArray())
        .andExpect(jsonPath("$.priceInstants").isNotEmpty())
        .andExpect(jsonPath("$.priceInstants[0].priceValue").value(4.55))
        .andExpect(jsonPath("$.barcode").value("1234567890123"))
        .andExpect(jsonPath("$.sequenceCode").value(12345));

        verify(this.productServiceMock, times(1)).saveByBarcode(anyString());
        verify(this.domainMapperMock, times(1)).mapToDto(isNull());
    }

    @Test
    void when_POST_a_non_existent_bar_code_then_response_404_create() throws Exception {
        given(this.productServiceMock.saveByBarcode(anyString())).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        given(this.domainMapperMock.mapToDto(isNull())).willReturn(null);
        final String nonExistentBarCodeJson = "{\"eanCode\":\"1234567890123\"}";

        final MvcResult mvcResult = mockMvc.perform(post("/api/products")
            .characterEncoding("UTF-8")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(nonExistentBarCodeJson)
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

        verify(this.productServiceMock, times(1)).saveByBarcode(anyString());
        verify(this.domainMapperMock, never()).mapToDto(isNull());
    }

    @Test
    void if_there_are_products_available_then_response_them_with_200_getAll() throws Exception {
        final List<ProductResponseDTO> productResponseDTOList = List.of(
            this.getANewInstanceOfResponseDTO(),
            this.getANewInstanceOfResponseDTO(),
            this.getANewInstanceOfResponseDTO()
        );
        given(this.productServiceMock.findAllByOrderByDescriptionAsc()).willReturn(null);
        given(this.domainMapperMock.mapToDtoList(isNull())).willReturn(productResponseDTOList);

        mockMvc.perform(get("/api/products").accept(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].priceInstants").isArray())
            .andExpect(jsonPath("$[0].priceInstants", hasSize(1)))
            .andExpect(jsonPath("$[0].priceInstants[0].instant").exists())
            .andExpect(jsonPath("$[0].priceInstants[0].priceValue").value(4.55));

        verify(this.productServiceMock, times(1)).findAllByOrderByDescriptionAsc();
        verify(this.domainMapperMock, times(1)).mapToDtoList(isNull());
    }

    @Test
    void if_there_are_no_products_available_then_response_an_empty_list_with_200_getAll() throws Exception {
        given(this.productServiceMock.findAllByOrderByDescriptionAsc()).willReturn(null);
        given(this.domainMapperMock.mapToDtoList(isNull())).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/products").accept(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(0)));

        verify(this.productServiceMock, times(1)).findAllByOrderByDescriptionAsc();
        verify(this.domainMapperMock, times(1)).mapToDtoList(isNull());
    }

    @Test
    void given_an_existent_bar_code_then_response_a_product_with_200_getByEanCode() throws Exception {
        final String existentBarCode = "1234567890123";

        given(this.productServiceMock.findByBarcode(eq(existentBarCode))).willReturn(null);
        given(this.domainMapperMock.mapToDto(isNull())).willReturn(this.getANewInstanceOfResponseDTO());

        mockMvc.perform(get("/api/products/"+existentBarCode).accept(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.description").value("default description"))
            .andExpect(jsonPath("$.priceInstants").isArray())
            .andExpect(jsonPath("$.priceInstants").isNotEmpty())
            .andExpect(jsonPath("$.priceInstants[0].priceValue").value(4.55))
            .andExpect(jsonPath("$.barcode").value(existentBarCode))
            .andExpect(jsonPath("$.sequenceCode").value(12345));

        verify(this.productServiceMock, times(1)).findByBarcode(eq(existentBarCode));
        verify(this.domainMapperMock, times(1)).mapToDto(isNull());
    }

    @Test
    void given_a_non_existent_bar_code_then_response_400_getByEanCode() throws Exception {
        final String nonExistentBarCode = "1234567890123";

        given(this.productServiceMock.findByBarcode(eq(nonExistentBarCode))).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        given(this.domainMapperMock.mapToDto(isNull())).willReturn(this.getANewInstanceOfResponseDTO());

        final MvcResult mvcResult = mockMvc.perform(
            get("/api/products/"+nonExistentBarCode)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
        )
        .andExpect(jsonPath("$.reasons").isArray())
        .andExpect(jsonPath("$.reasons", hasSize(1)))
        .andExpect(jsonPath("$.reasons[0]").value("Product not found"))
        .andExpect(jsonPath("$.status").value("NOT_FOUND"))
        .andReturn();

        assertThat(mvcResult.getResolvedException())
            .isNotNull()
            .isInstanceOf(ResponseStatusException.class);

        assertThat(Objects.castIfBelongsToType(mvcResult.getResolvedException(), ResponseStatusException.class))
            .satisfies(exception -> {
                assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                assertThat(exception.getReason()).isEqualTo("Product not found");
            });

        verify(this.productServiceMock, times(1)).findByBarcode(eq(nonExistentBarCode));
        verify(this.domainMapperMock, never()).mapToDto(isNull());
        verify(this.productServiceMock, only()).findByBarcode(eq(nonExistentBarCode));

    }
}