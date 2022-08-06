package com.api.controller;

import com.api.entity.Product;
import com.api.projection.Projection;
import com.api.projection.ProjectionFactory;
import com.api.repository.ProductRepository;
import com.api.service.DomainMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private DomainMapper domainMapper;

    private MockMvc mockMvc;

    private static class Resources {

        private static List<Product> products;
        private static List<Projection.SimpleProduct> simpleProducts;

        static {
            products = List.of(
                Product.builder()
                    .description("ACHOC PO NESCAU 800G")
                    .sequenceCode(29250)
                    .barcode("7891000055120")
                    .build(),
                Product.builder()
                    .description("AMENDOIM SALG CROKISSIMO 400G PIMENTA")
                    .sequenceCode(120983)
                    .barcode("7896336010058")
                    .build(),
                Product.builder()
                    .description("CAFE UTAM 500G")
                    .sequenceCode(2909)
                    .barcode("7896656800018")
                    .build()
            );

            simpleProducts = products
                .stream()
                .map(product -> new Projection.SimpleProduct() {
                    @Override
                    public String getDescription() {
                        return product.getDescription();
                    }

                    @Override
                    public String getBarcode() {
                        return product.getBarcode();
                    }

                    @Override
                    public Integer getSequenceCode() {
                        return product.getSequenceCode();
                    }
                }).collect(Collectors.toList());
        }
    }

    @BeforeEach
    void setUp() {
        this.mockMvc = standaloneSetup(new ProductController(productRepository, domainMapper)).alwaysDo(print()).build();
    }

    @Test
    void when_GET_getAll_should_response_all_products_with_200() throws Exception {
        final String urlBase = "http://localhost/api/prices?barcode=";
        given(productRepository.findAll()).willReturn(Resources.products);
        given(domainMapper.mapToSimpleProductList(eq(Resources.products))).willReturn(Resources.simpleProducts);

        mockMvc.perform(get("/api/products")
            .characterEncoding("UTF-8")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(
            jsonPath("$[*].barcode", contains("7891000055120", "7896336010058", "7896656800018"))
        )
        .andExpect(
            jsonPath(
                "$[*].links[0].href",
                contains(urlBase+"7891000055120", urlBase+"7896336010058", urlBase+"7896656800018")
            )
        );

        verify(productRepository, times(1)).findAll();
        verify(domainMapper, times(1)).mapToSimpleProductList(eq(Resources.products));
    }

    @Test
    void when_GET_getAll_should_response_a_empty_json_with_200() throws Exception  {
        given(productRepository.findAll()).willReturn(Collections.emptyList());
        given(domainMapper.mapToSimpleProductList(eq(Collections.emptyList())))
            .willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/products")
            .characterEncoding("UTF-8")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());

        verify(productRepository, times(1)).findAll();
        verify(domainMapper, times(1)).mapToSimpleProductList(eq(Collections.emptyList()));
    }

    @Test
    void when_GET_getAll_should_response_the_fist_page_of_products_with_200() throws Exception  {
        final Pageable pageableInUse = PageRequest.of(0, 1);
        final List<Product> subList = Resources.products.subList(0, 1);

        given(productRepository.findAll(eq(pageableInUse))).willReturn(new PageImpl<>(subList, pageableInUse, 3));
        given(domainMapper.mapToSimpleProductList(eq(subList))).willReturn(Resources.simpleProducts.subList(0, 1));

        mockMvc.perform(get("/api/products?pag=0-1")
            .characterEncoding("UTF-8")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].description").value("ACHOC PO NESCAU 800G"))
        .andExpect(jsonPath("$.content[0].sequenceCode").value(29250))
        .andExpect(jsonPath("$.content[0].barcode").value("7891000055120"))
        .andExpect(jsonPath("$.content[0].links[0].rel").value("prices"))
        .andExpect(jsonPath("$.content[0].links[0].href").value("http://localhost/api/prices?barcode=7891000055120"))
        .andExpect(jsonPath("$.numberOfItems").value(1))
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.links[0].rel").value("next page"))
        .andExpect(jsonPath("$.links[0].href").value("http://localhost/api/products?pag=1-1"));

        verify(productRepository, times(1)).findAll(eq(pageableInUse));
        verify(domainMapper, times(1)).mapToSimpleProductList(eq(subList));
    }

    @Test
    void when_GET_getAll_should_response_the_middle_page_of_products_with_200() throws Exception  {
        final Pageable pageableInUse = PageRequest.of(1, 1);
        final List<Product> subList = Resources.products.subList(1, 2);

        given(productRepository.findAll(eq(pageableInUse))).willReturn(new PageImpl<>(subList, pageableInUse, 3));
        given(domainMapper.mapToSimpleProductList(eq(subList))).willReturn(Resources.simpleProducts.subList(1, 2));

        mockMvc.perform(get("/api/products?pag=1-1")
            .characterEncoding("UTF-8")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].description").value("AMENDOIM SALG CROKISSIMO 400G PIMENTA"))
        .andExpect(jsonPath("$.content[0].sequenceCode").value(120983))
        .andExpect(jsonPath("$.content[0].barcode").value("7896336010058"))
        .andExpect(jsonPath("$.content[0].links[0].rel").value("prices"))
        .andExpect(jsonPath("$.content[0].links[0].href").value("http://localhost/api/prices?barcode=7896336010058"))
        .andExpect(jsonPath("$.numberOfItems").value(1))
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.links[0].rel").value("next page"))
        .andExpect(jsonPath("$.links[0].href").value("http://localhost/api/products?pag=2-1"));

        verify(productRepository, times(1)).findAll(eq(pageableInUse));
        verify(domainMapper, times(1)).mapToSimpleProductList(eq(subList));
    }
}