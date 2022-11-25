package com.api.controller;

import com.api.entity.Product;
import com.api.projection.SimpleProductWithStatus;
import com.api.service.interfaces.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import static com.api.controller.ProductControllerTestHelper.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(value = {ProductController.class, GlobalErrorHandlingController.class})
class ProductControllerTest {

    @MockBean
    private ProductService productService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(
            new ProductController(productService),
            new GlobalErrorHandlingController()
        )
        .alwaysDo(print()).build();
        ProductControllerTestHelper.mockMvc = mockMvc;
    }

    @Nested
    class GetAllTest {

        @Test
        @DisplayName("GET /api/products -> 200 OK")
        void should_return_all_products__OK() throws Exception {
            given(productService.findAll(ArgumentMatchers.any(Sort.class))).willReturn(PRODUCTS_SAMPLE);

            makeRequest()
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[*].barcode", contains("7891000055120", "7896336010058", "7896656800018", "78982797922990")))
                .andExpect(jsonPath("[*].links[0].rel", everyItem(equalTo("prices"))))
                .andExpect(
                    jsonPath(
                        "$[*].links[0].href",
                        contains(concatWithUrl("http://localhost/api/prices?barcode=", "7891000055120", "7896336010058", "7896656800018", "78982797922990"))
                    )
                )
                .andExpect(jsonPath("[*].links[1].rel", everyItem(equalTo("self"))))
                .andExpect(
                    jsonPath(
                        "$[*].links[1].href",
                        contains(concatWithUrl("http://localhost/api/products/", "7891000055120", "7896336010058", "7896656800018", "78982797922990"))
                    )
                );

            verify(productService, times(1)).findAll((ArgumentMatchers.any(Sort.class)));
        }

        @Test
        @DisplayName("GET /api/products -> 200 OK")
        void should_return_a_empty_json__OK() throws Exception {
            given(productService.findAll(ArgumentMatchers.any(Sort.class))).willReturn(Collections.emptyList());

            makeRequest()
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

            verify(productService, times(1)).findAll(ArgumentMatchers.any(Sort.class));
        }
    }

    @Nested
    class GetAllPagedTest {

        @Test
        @DisplayName("GET /api/products?pag=0-1 -> 200 OK")
        void should_return_the_fist_page_with_one_product__OK() throws Exception  {
            final Pageable firstPageOrderedByDescriptionAsc = PageRequest.of(0, 1, Sort.by("description").ascending());
            final List<Product> firstProduct = PRODUCTS_SAMPLE.subList(0, 1);

            given(productService.findAll(eq(firstPageOrderedByDescriptionAsc)))
                .willReturn(new PageImpl<>(firstProduct, firstPageOrderedByDescriptionAsc, 3));

            makeRequestWithPage("0-1")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].description").value("ACHOC PO NESCAU 800G"))
                .andExpect(jsonPath("$.content[0].sequenceCode").value(29250))
                .andExpect(jsonPath("$.content[0].barcode").value("7891000055120"))
                .andExpect(jsonPath("$.content[0].links[0].rel").value("prices"))
                .andExpect(jsonPath("$.content[0].links[1].rel").value("self"))
                .andExpect(jsonPath("$.content[0].links[0].href").value("http://localhost/api/prices?barcode=7891000055120"))
                .andExpect(jsonPath("$.content[0].links[1].href").value("http://localhost/api/products/7891000055120"))
                .andExpect(jsonPath("$.currentCountOfItems").value(1))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.totalOfPages").value(3))
                .andExpect(jsonPath("$.totalOfItems").value(3))
                .andExpect(jsonPath("$.links[0].rel").value("next page"))
                .andExpect(jsonPath("$.links[0].href").value("http://localhost/api/products?pag=1-1"));

            verify(productService, times(1)).findAll(eq(firstPageOrderedByDescriptionAsc));
        }

        @Test
        @DisplayName("GET /api/products?pag=1-1 -> 200 OK")
        void should_return_the_middle_page_with_one_product__OK() throws Exception  {
            final Pageable secondPageOrderedByDescriptionAsc = PageRequest.of(1, 1, Sort.by("description").ascending());
            final List<Product> secondProduct = PRODUCTS_SAMPLE.subList(1, 2);

            given(productService.findAll(eq(secondPageOrderedByDescriptionAsc)))
                .willReturn(new PageImpl<>(secondProduct, secondPageOrderedByDescriptionAsc, 3));

            makeRequestWithPage("1-1")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].description").value("AMENDOIM SALG CROKISSIMO 400G PIMENTA"))
                .andExpect(jsonPath("$.content[0].sequenceCode").value(120983))
                .andExpect(jsonPath("$.content[0].barcode").value("7896336010058"))
                .andExpect(jsonPath("$.content[0].links[0].rel").value("prices"))
                .andExpect(jsonPath("$.content[0].links[1].rel").value("self"))
                .andExpect(jsonPath("$.content[0].links[0].href").value("http://localhost/api/prices?barcode=7896336010058"))
                .andExpect(jsonPath("$.content[0].links[1].href").value("http://localhost/api/products/7896336010058"))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalOfPages").value(3))
                .andExpect(jsonPath("$.currentCountOfItems").value(1))
                .andExpect(jsonPath("$.totalOfItems").value(3))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.links[0].rel").value("next page"))
                .andExpect(jsonPath("$.links[0].href").value("http://localhost/api/products?pag=2-1"));

            verify(productService, times(1)).findAll(eq(secondPageOrderedByDescriptionAsc));
        }

        @Test
        @DisplayName("GET /api/products?pag=2-1 -> 200 OK")
        void should_return_the_last_page_with_one_product__OK() throws Exception  {
            final Pageable thirdPageOrderedByDescriptionAsc = PageRequest.of(2, 1, Sort.by("description").ascending());
            final List<Product> thirdProduct = PRODUCTS_SAMPLE.subList(2, 3);

            given(productService.findAll(eq(thirdPageOrderedByDescriptionAsc)))
                .willReturn(new PageImpl<>(thirdProduct, thirdPageOrderedByDescriptionAsc, 3));

            makeRequestWithPage("2-1")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].description").value("CAFE UTAM 500G"))
                .andExpect(jsonPath("$.content[0].sequenceCode").value(2909))
                .andExpect(jsonPath("$.content[0].barcode").value("7896656800018"))
                .andExpect(jsonPath("$.content[0].links[0].rel").value("prices"))
                .andExpect(jsonPath("$.content[0].links[1].rel").value("self"))
                .andExpect(jsonPath("$.content[0].links[0].href").value("http://localhost/api/prices?barcode=7896656800018"))
                .andExpect(jsonPath("$.content[0].links[1].href").value("http://localhost/api/products/7896656800018"))
                .andExpect(jsonPath("$.currentCountOfItems").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.totalOfPages").value(3))
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.totalOfItems").value(3))
                .andExpect(jsonPath("$.links").isArray())
                .andExpect(jsonPath("$.links").isEmpty());

            verify(productService, times(1)).findAll(eq(thirdPageOrderedByDescriptionAsc));
        }

        @Test
        @DisplayName("GET /api/products?pag=3-1 -> 200 OK")
        void when_pag_is_over_the_limits_then_should_return_an_empty_array__OK() throws Exception  {
            final Pageable fourthPageProductOrderedByDescriptionAsc = PageRequest.of(3, 1, Sort.by("description").ascending());

            given(productService.findAll(eq(fourthPageProductOrderedByDescriptionAsc)))
                .willReturn(new PageImpl<>(Collections.emptyList(), fourthPageProductOrderedByDescriptionAsc, 0));

            makeRequestWithPage("3-1")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

            verify(productService, times(1)).findAll(eq(fourthPageProductOrderedByDescriptionAsc));
            verify(productService, only()).findAll(eq(fourthPageProductOrderedByDescriptionAsc));
        }
    }

    @Nested
    class GetByBarcodeTest {

        @Test
        @DisplayName("GET /api/products/7891000051230 -> 404 - NOT FOUND")
        void when_the_product_is_not_found_then_should_return_a_message_error__NOT_FOUND() throws Exception  {
            final String targetBarcode = "7891000051230";

            given(productService.getByBarcodeAndSaveIfNecessary(eq(targetBarcode)))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

            makeRequestByBarcode(targetBarcode)
                .andExpect(status().isNotFound())
                .andExpect(header().exists("Content-Type"))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Product not found"));

            verify(productService, times(1)).getByBarcodeAndSaveIfNecessary(eq(targetBarcode));
            verify(productService, only()).getByBarcodeAndSaveIfNecessary(eq(targetBarcode));
        }

        @Test
        @DisplayName("GET /api/products/7891000055120 -> 200 - OK")
        void when_the_product_is_found_should_return_a_product__OK() throws Exception  {
            final String targetBarcode = "7891000055120";
            final SimpleProductWithStatus simpleProductWithStatus =
                PRODUCTS_SAMPLE.get(0).toSimpleProductWithStatus(HttpStatus.OK);

            given(productService.getByBarcodeAndSaveIfNecessary(eq(targetBarcode)))
                .willReturn(simpleProductWithStatus);

            makeRequestByBarcode(targetBarcode)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.description").value("ACHOC PO NESCAU 800G"))
                .andExpect(jsonPath("$.sequenceCode").value(29250))
                .andExpect(jsonPath("$.barcode").value("7891000055120"))
                .andExpect(jsonPath("$.links[0].rel").value("prices"))
                .andExpect(jsonPath("$.links[1].rel").value("self"))
                .andExpect(jsonPath("$.links[0].href").value("http://localhost/api/prices?barcode=7891000055120"))
                .andExpect(jsonPath("$.links[1].href").value("http://localhost/api/products/7891000055120"));

            verify(productService, times(1)).getByBarcodeAndSaveIfNecessary(eq(targetBarcode));
        }
    }

    @Nested
    class GetAllPagedContainingDescriptionTest {

        @Test
        @DisplayName("GET /api/products?pag=1-3&contains=500g -> 200 - OK")
        void should_return_the_second_page_with_three_products_that_contains_500g__OK() throws Exception {
            final Pageable fistPageOrderedByDescriptionAsc = PageRequest.of(1, 1, Sort.by("description").ascending());
            final String contains = "500g";

            given(productService.findAllByDescriptionIgnoreCaseContaining(eq(contains), eq(fistPageOrderedByDescriptionAsc)))
                .willReturn(new PageImpl<>(PRODUCTS_SAMPLE.subList(2, 3), fistPageOrderedByDescriptionAsc, 0));

            makeRequestWithPageAndContains("1-1", contains)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].description").value("CAFE UTAM 500G"))
                .andExpect(jsonPath("$.content[0].sequenceCode").value(2909))
                .andExpect(jsonPath("$.content[0].barcode").value("7896656800018"))
                .andExpect(jsonPath("$.content[0].links[0].rel").value("prices"))
                .andExpect(jsonPath("$.content[0].links[1].rel").value("self"))
                .andExpect(jsonPath("$.content[0].links[0].href").value("http://localhost/api/prices?barcode=7896656800018"))
                .andExpect(jsonPath("$.content[0].links[1].href").value("http://localhost/api/products/7896656800018"))
                .andExpect(jsonPath("$.currentCountOfItems").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.totalOfPages").value(2))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalOfItems").value(2))
                .andExpect(jsonPath("$.links").isEmpty());

            verify(productService, times(1)).findAllByDescriptionIgnoreCaseContaining(eq(contains), eq(fistPageOrderedByDescriptionAsc));
            verify(productService, only()).findAllByDescriptionIgnoreCaseContaining(eq(contains), eq(fistPageOrderedByDescriptionAsc));
        }

        @Test
        @DisplayName("GET /api/products?pag-1-1&contains= -> 200 OK")
        void when_contains_is_empty_then_should_return_an_empty_json__OK() throws Exception {
            final Pageable firstPageOrderedByDescriptionAsc = PageRequest.of(1, 1, Sort.by("description").ascending());
            final String contains = "";

            given(productService.findAllByDescriptionIgnoreCaseContaining(eq(contains), eq(firstPageOrderedByDescriptionAsc)))
                .willReturn(new PageImpl<>(Collections.emptyList(), firstPageOrderedByDescriptionAsc, 0));

            makeRequestWithPageAndContains("1-1", contains)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

            verify(productService, times(1)).findAllByDescriptionIgnoreCaseContaining(eq(contains), eq(firstPageOrderedByDescriptionAsc));
            verify(productService, only()).findAllByDescriptionIgnoreCaseContaining(eq(contains), eq(firstPageOrderedByDescriptionAsc));
        }
    }
}