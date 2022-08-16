package com.api.controller;

import com.api.entity.Product;
import com.api.projection.Projection;
import com.api.service.DomainMapper;
import com.api.service.interfaces.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(value = {ProductController.class, GlobalErrorHandlingController.class})
class ProductControllerTest {

    @MockBean
    private ProductService productService;

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
        this.mockMvc = standaloneSetup(
            new ProductController(productService, domainMapper),
            new GlobalErrorHandlingController()
        ).alwaysDo(print()).build();
    }

    @Test
    @DisplayName("GET /api/products -> 200 OK")
    void when_GET_getAll_should_return_all_products_with_200() throws Exception {
        final String urlBaseToPrices = "http://localhost/api/prices?barcode=";
        final String urlBaseToSelf = "http://localhost/api/products/";

        given(productService.findAll(ArgumentMatchers.any(Sort.class))).willReturn(Resources.products);
        given(domainMapper.mapToSimpleProductList(eq(Resources.products))).willReturn(Resources.simpleProducts);

        mockMvc.perform(get("/api/products")
            .characterEncoding("UTF-8")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[*].barcode", contains("7891000055120", "7896336010058", "7896656800018")))
        .andExpect(jsonPath("$[*].links[0].rel", everyItem(equalTo("prices"))))
        .andExpect(
            jsonPath(
                "$[*].links[0].href",
                contains(urlBaseToPrices+"7891000055120", urlBaseToPrices+"7896336010058", urlBaseToPrices+"7896656800018")
            )
        )
        .andExpect(jsonPath("$[*].links[1].rel", everyItem(equalTo("self"))))
        .andExpect(
            jsonPath(
                "$[*].links[1].href",
                contains(urlBaseToSelf+"7891000055120", urlBaseToSelf+"7896336010058", urlBaseToSelf+"7896656800018")
            )
        );

        verify(productService, times(1)).findAll((ArgumentMatchers.any(Sort.class)));
        verify(domainMapper, times(1)).mapToSimpleProductList(eq(Resources.products));
    }

    @Test
    @DisplayName("GET /api/products -> 200 OK")
    void when_GET_getAll_should_response_a_empty_json_with_200() throws Exception  {
        given(productService.findAll(ArgumentMatchers.any(Sort.class))).willReturn(Collections.emptyList());
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

        verify(productService, times(1)).findAll(ArgumentMatchers.any(Sort.class));
        verify(domainMapper, times(1)).mapToSimpleProductList(eq(Collections.emptyList()));
    }

    @Test
    @DisplayName("GET /api/products?pag=0-1 -> 200 OK")
    void when_GET_getAll_should_response_the_fist_page_of_products_with_200() throws Exception  {
        final Pageable pageableInUse = PageRequest.of(0, 1, Sort.by("description").ascending());
        final List<Product> subList = Resources.products.subList(0, 1);

        given(productService.findAll(eq(pageableInUse))).willReturn(new PageImpl<>(subList, pageableInUse, 3));
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
        .andExpect(jsonPath("$.content[0].links[1].rel").value("self"))
        .andExpect(jsonPath("$.content[0].links[1].href").value("http://localhost/api/products/7891000055120"))
        .andExpect(jsonPath("$.numberOfItems").value(1))
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.links[0].rel").value("next page"))
        .andExpect(jsonPath("$.links[0].href").value("http://localhost/api/products?pag=1-1"));

        verify(productService, times(1)).findAll(eq(pageableInUse));
        verify(domainMapper, times(1)).mapToSimpleProductList(eq(subList));
    }

    @Test
    @DisplayName("GET /api/products?pag=1-1 -> 200 OK")
    void when_GET_getAll_should_response_the_middle_page_of_products_with_200() throws Exception  {
        final Pageable pageableInUse = PageRequest.of(1, 1, Sort.by("description").ascending());
        final List<Product> subList = Resources.products.subList(1, 2);

        given(productService.findAll(eq(pageableInUse))).willReturn(new PageImpl<>(subList, pageableInUse, 3));
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
        .andExpect(jsonPath("$.content[0].links[1].rel").value("self"))
        .andExpect(jsonPath("$.content[0].links[1].href").value("http://localhost/api/products/7896336010058"))
        .andExpect(jsonPath("$.numberOfItems").value(1))
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.links[0].rel").value("next page"))
        .andExpect(jsonPath("$.links[0].href").value("http://localhost/api/products?pag=2-1"));

        verify(productService, times(1)).findAll(eq(pageableInUse));
        verify(domainMapper, times(1)).mapToSimpleProductList(eq(subList));
    }

    @Test
    @DisplayName("GET /api/products?pag=2-1 -> 200 OK")
    void when_GET_getAll_should_response_the_last_page_of_products_with_200() throws Exception  {
        final Pageable pageableInUse = PageRequest.of(2, 1, Sort.by("description").ascending());
        final List<Product> subList = Resources.products.subList(2, 3);

        given(productService.findAll(eq(pageableInUse))).willReturn(new PageImpl<>(subList, pageableInUse, 3));
        given(domainMapper.mapToSimpleProductList(eq(subList))).willReturn(Resources.simpleProducts.subList(2, 3));

        mockMvc.perform(get("/api/products?pag=2-1")
            .characterEncoding("UTF-8")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].description").value("CAFE UTAM 500G"))
        .andExpect(jsonPath("$.content[0].sequenceCode").value(2909))
        .andExpect(jsonPath("$.content[0].barcode").value("7896656800018"))
        .andExpect(jsonPath("$.content[0].links[0].rel").value("prices"))
        .andExpect(jsonPath("$.content[0].links[0].href").value("http://localhost/api/prices?barcode=7896656800018"))
        .andExpect(jsonPath("$.content[0].links[1].rel").value("self"))
        .andExpect(jsonPath("$.content[0].links[1].href").value("http://localhost/api/products/7896656800018"))
        .andExpect(jsonPath("$.numberOfItems").value(1))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.links").isArray())
        .andExpect(jsonPath("$.links").isEmpty());

        verify(productService, times(1)).findAll(eq(pageableInUse));
        verify(domainMapper, times(1)).mapToSimpleProductList(eq(subList));
    }

    @Test
    @DisplayName("GET /api/products?pag=3-1 -> 200 OK")
    void when_GET_getAll_should_not_response_any_products_with_200() throws Exception  {
        final Pageable pageableInUse = PageRequest.of(3, 1, Sort.by("description").ascending());

        given(productService.findAll(eq(pageableInUse)))
            .willReturn(new PageImpl<>(Collections.emptyList(), pageableInUse, 0));

        mockMvc.perform(get("/api/products?pag=3-1")
            .characterEncoding("UTF-8")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());

        verify(productService, times(1)).findAll(eq(pageableInUse));
        verify(productService, only()).findAll(eq(pageableInUse));
    }

    @Test
    @DisplayName("GET /api/products/7891000051230 -> 404 - NOT FOUND")
    void when_GET_getByBarcode_should_return_a_message_error_with_404() throws Exception  {
        final String barcode = "7891000051230";

        given(productService.getByBarcode(eq(barcode)))
            .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        mockMvc.perform(get("/api/products/"+barcode)
            .characterEncoding("UTF-8")
            .accept(MediaType.ALL)
        )
        .andExpect(status().isNotFound())
        .andExpect(header().exists("Content-Type"))
        .andExpect(content().contentType(MediaType.TEXT_PLAIN))
        .andExpect(content().string("Product not found"));

        verify(productService, times(1)).getByBarcode(eq(barcode));
        verify(productService, only()).getByBarcode(eq(barcode));
    }

    @Test
    @DisplayName("GET /api/products/7891000055120 -> 200 - OK")
    void when_GET_getByBarcode_should_return_a_product_with_200() throws Exception  {
        final String barcode = "7891000055120";

        given(productService.getByBarcode(eq(barcode))).willReturn(Resources.products.get(0));
        given(domainMapper.mapToSimpleProduct(Resources.products.get(0)))
            .willReturn(Resources.simpleProducts.get(0));

        mockMvc.perform(get("/api/products/"+barcode)
            .characterEncoding("UTF-8")
            .accept(MediaType.ALL)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.description").value("ACHOC PO NESCAU 800G"))
        .andExpect(jsonPath("$.sequenceCode").value(29250))
        .andExpect(jsonPath("$.barcode").value("7891000055120"))
        .andExpect(jsonPath("$.links[0].rel").value("prices"))
        .andExpect(jsonPath("$.links[0].href").value("http://localhost/api/prices?barcode=7891000055120"))
        .andExpect(jsonPath("$.links[1].rel").value("self"))
        .andExpect(jsonPath("$.links[1].href").value("http://localhost/api/products/7891000055120"));

        verify(productService, times(1)).getByBarcode(eq(barcode));
        verify(domainMapper, times(1)).mapToSimpleProduct(Resources.products.get(0));
    }
}