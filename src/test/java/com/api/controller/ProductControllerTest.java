package com.api.controller;

import com.api.component.Constants;
import com.api.projection.SimpleProductWithStatus;
import com.api.service.interfaces.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

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

    @BeforeEach
    void setUp() {
        ProductControllerTestHelper.mockMvc = standaloneSetup(
            new ProductController(productService),
            new GlobalErrorHandlingController()
        )
        .alwaysDo(print()).build();
    }

    @Nested
    class GetAllTest {

        @Test
        @DisplayName("GET /api/products -> 200 OK")
        void should_return_all_products__OK() throws Exception {
            given(productService.findAll(ArgumentMatchers.any(Sort.class))).willReturn(PRODUCTS_SAMPLE);

            final String[] barcodesForTest = {
                "7891000055120", "7896336010058", "78982797922990",
                "7896003737257", "7896071024709", "7896085087028",
                "7891962037219", "7896656800018", "7891000000427",
                "7891150080850", "7896102513714", "7896292340503",
                "7896036090619", "7898930672441", "7891172422379",
                "7891991002646", "7896110195162", "7896048285539"
            };
            makeRequest()
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(18)))
                .andExpect(jsonPath("$[*].barcode", contains(barcodesForTest)))
                .andExpect(jsonPath("[*].links[0].rel", everyItem(equalTo("prices"))))
                .andExpect(
                    jsonPath(
                        "$[*].links[0].href",
                        contains(concatWithUrl(Constants.PRICES_URL, barcodesForTest))
                    )
                )
                .andExpect(jsonPath("[*].links[1].rel", everyItem(equalTo("self"))))
                .andExpect(
                    jsonPath(
                        "$[*].links[1].href",
                        contains(concatWithUrl(Constants.PRODUCTS_URL+"/", barcodesForTest))
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
        @DisplayName("GET /api/products?pag=0-5 -> 200 OK")
        void should_return_the_fist_page_with_five_products__OK() throws Exception  {
            final Sort orderByDescriptionAsc = getDefaultSorting();
            final Pageable firstPageWithFiveProducts = createPageable("0-5", orderByDescriptionAsc);

            given(productService.findAll(eq(firstPageWithFiveProducts)))
                .willReturn(createPage(firstPageWithFiveProducts));

            final String[] expectedBarcodeList = {
                "7891000055120", "7896336010058",
                "78982797922990", "7896003737257",
                "7896071024709"
            };
            makeRequestWithPage("0-5")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[*].barcode", contains(expectedBarcodeList)))
                .andExpect(jsonPath("$.content[*].links[0].rel", everyItem(equalTo("prices"))))
                .andExpect(jsonPath("$.content[*].links[1].rel", everyItem(equalTo("self"))))
                .andExpect(jsonPath(
                    "$.content[*].links[0].href",
                    contains(concatWithUrl(Constants.PRICES_URL, expectedBarcodeList))
                ))
                .andExpect(jsonPath(
                    "$.content[*].links[1].href",
                    contains(concatWithUrl(Constants.PRODUCTS_URL+"/", expectedBarcodeList))
                ))
                .andExpect(jsonPath("$.currentCountOfItems").value(5))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.totalOfPages").value(4))
                .andExpect(jsonPath("$.totalOfItems").value(18))
                .andExpect(jsonPath("$.links[0].rel").value("Next page"))
                .andExpect(jsonPath("$.links[0].href").value(Constants.PRODUCTS_URL+"?pag=1-5"));

            verify(productService, times(1)).findAll(eq(firstPageWithFiveProducts));
        }

        @Test
        @DisplayName("GET /api/products?pag=1-5 -> 200 OK")
        void should_return_the_second_page_with_five_products__OK() throws Exception  {
            final Sort orderByDescriptionAsc = getDefaultSorting();
            final Pageable secondPageWithFiveProducts = createPageable("1-5", orderByDescriptionAsc);

            given(productService.findAll(eq(secondPageWithFiveProducts)))
                .willReturn(createPage(secondPageWithFiveProducts));

            final String[] expectedBarcodeList = {
                "7896085087028", "7891962037219",
                "7896656800018", "7891000000427",
                "7891150080850"
            };

            makeRequestWithPage("1-5")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[*].barcode", contains(expectedBarcodeList)))
                .andExpect(jsonPath("$.content[*].links[0].rel", everyItem(equalTo("prices"))))
                .andExpect(jsonPath("$.content[*].links[1].rel", everyItem(equalTo("self"))))
                .andExpect(jsonPath(
                        "$.content[*].links[0].href",
                        contains(concatWithUrl(Constants.PRICES_URL, expectedBarcodeList))
                ))
                .andExpect(jsonPath(
                        "$.content[*].links[1].href",
                        contains(concatWithUrl(Constants.PRODUCTS_URL+"/", expectedBarcodeList))
                ))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalOfPages").value(4))
                .andExpect(jsonPath("$.currentCountOfItems").value(5))
                .andExpect(jsonPath("$.totalOfItems").value(18))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.links[0].rel").value("Next page"))
                .andExpect(jsonPath("$.links[0].href").value(Constants.PRODUCTS_URL+"?pag=2-5"));

            verify(productService, times(1)).findAll(eq(secondPageWithFiveProducts));
        }

        @Test
        @DisplayName("GET /api/products?pag=2-5 -> 200 OK")
        void should_return_the_third_page_with_five_products__OK() throws Exception  {
            final Sort orderByDescriptionAsc = getDefaultSorting();
            final Pageable thirdPageWithFiveProducts = createPageable("2-5", orderByDescriptionAsc);

            given(productService.findAll(eq(thirdPageWithFiveProducts)))
                .willReturn(createPage(thirdPageWithFiveProducts));

            final String[] expectedBarcodeList = {
                "7896102513714", "7896292340503",
                "7896036090619", "7898930672441",
                "7891172422379"
            };

            makeRequestWithPage("2-5")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[*].barcode", contains(expectedBarcodeList)))
                .andExpect(jsonPath("$.content[*].links[0].rel", everyItem(equalTo("prices"))))
                .andExpect(jsonPath("$.content[*].links[1].rel", everyItem(equalTo("self"))))
                .andExpect(jsonPath(
                    "$.content[*].links[0].href",
                    contains(concatWithUrl(Constants.PRICES_URL, expectedBarcodeList))
                ))
                .andExpect(jsonPath(
                    "$.content[*].links[1].href",
                    contains(concatWithUrl(Constants.PRODUCTS_URL+"/", expectedBarcodeList))
                ))
                .andExpect(jsonPath("$.currentCountOfItems").value(5))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.totalOfPages").value(4))
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.totalOfItems").value(18))
                .andExpect(jsonPath("$.links[0].rel").value("Next page"))
                .andExpect(jsonPath("$.links[0].href").value(Constants.PRODUCTS_URL+"?pag=3-5"));

            verify(productService, times(1)).findAll(eq(thirdPageWithFiveProducts));
        }

        @Test
        @DisplayName("GET /api/products?pag=3-5 -> 200 OK")
        void should_return_the_fourth_page_with_three_products() throws Exception {
            final Sort orderByDescriptionAsc = getDefaultSorting();
            final Pageable fourthPageWithThreeProducts = createPageable("3-5", orderByDescriptionAsc);

            given(productService.findAll(eq(fourthPageWithThreeProducts)))
                .willReturn(createPage(fourthPageWithThreeProducts));

            final String[] expectedBarcodeList = { "7891991002646", "7896110195162", "7896048285539" };

            makeRequestWithPage("3-5")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[*].barcode", contains(expectedBarcodeList)))
                .andExpect(jsonPath("$.content[*].links[0].rel", everyItem(equalTo("prices"))))
                .andExpect(jsonPath("$.content[*].links[1].rel", everyItem(equalTo("self"))))
                .andExpect(jsonPath(
                        "$.content[*].links[0].href",
                        contains(concatWithUrl(Constants.PRICES_URL, expectedBarcodeList))
                ))
                .andExpect(jsonPath(
                        "$.content[*].links[1].href",
                        contains(concatWithUrl(Constants.PRODUCTS_URL+"/", expectedBarcodeList))
                ))
                .andExpect(jsonPath("$.currentCountOfItems").value(3))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.totalOfPages").value(4))
                .andExpect(jsonPath("$.currentPage").value(3))
                .andExpect(jsonPath("$.totalOfItems").value(18))
                .andExpect(jsonPath("$.links").isArray())
                .andExpect(jsonPath("$.links").isEmpty());

            verify(productService, times(1)).findAll(eq(fourthPageWithThreeProducts));
        }

        @Test
        @DisplayName("GET /api/products?pag=4-5 -> 200 OK")
        void when_pag_is_over_the_limits_then_should_return_an_empty_array__OK() throws Exception  {
            final Sort orderByDescriptionAsc = getDefaultSorting();
            final Pageable fourthPageProductWithFiveProducts = createPageable("3-5", orderByDescriptionAsc);

            given(productService.findAll(eq(fourthPageProductWithFiveProducts)))
                .willReturn(emptyPage());

            makeRequestWithPage("3-5")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

            verify(productService, times(1)).findAll(eq(fourthPageProductWithFiveProducts));
            verify(productService, only()).findAll(eq(fourthPageProductWithFiveProducts));
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
                .andExpect(jsonPath("$.links[1].href").value(Constants.PRODUCTS_URL+"/7891000055120"));

            verify(productService, times(1)).getByBarcodeAndSaveIfNecessary(eq(targetBarcode));
        }
    }

    @Nested
    class GetAllPagedContainingDescriptionTest {

        @Test
        @DisplayName("GET /api/products?pag=0-2&contains=500g -> 200 - OK")
        void should_return_the_first_page_with_two_products_that_contain_500g__OK() throws Exception {
            final Sort orderByDescriptionAsc = getDefaultSorting();
            final Pageable firstPageWithTwoProducts = createPageable("0-2", orderByDescriptionAsc);
            final String contains = "500g";

            given(productService.findAllByDescriptionIgnoreCaseContaining(eq(contains), eq(firstPageWithTwoProducts)))
                .willReturn(createPage(firstPageWithTwoProducts, filterByContaining(contains)));

            makeRequestWithPageAndContains("0-2", contains)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[*].description", contains("BALA GELATINA FINI 500G BURGUER", "CAFE UTAM 500G")))
                .andExpect(jsonPath("$.content[*].barcode", contains("78982797922990", "7896656800018")))
                .andExpect(jsonPath("$.content[*].links[0].rel", everyItem(equalTo("prices"))))
                .andExpect(jsonPath("$.content[*].links[1].rel", everyItem(equalTo("self"))))
                .andExpect(jsonPath(
                    "$.content[*].links[0].href",
                    contains(concatWithUrl(Constants.PRICES_URL, "78982797922990", "7896656800018"))
                ))
                .andExpect(jsonPath(
                    "$.content[*].links[1].href",
                    contains(concatWithUrl(Constants.PRODUCTS_URL+"/", "78982797922990", "7896656800018"))
                ))
                .andExpect(jsonPath("$.currentCountOfItems").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.totalOfPages").value(2))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalOfItems").value(3))
                .andExpect(jsonPath("$.links[0].rel").value("Next page"))
                .andExpect(jsonPath("$.links[0].href").value(Constants.PRODUCTS_URL+"?pag=1-2&contains=500g"));

            verify(productService, times(1)).findAllByDescriptionIgnoreCaseContaining(eq(contains), eq(firstPageWithTwoProducts));
            verify(productService, only()).findAllByDescriptionIgnoreCaseContaining(eq(contains), eq(firstPageWithTwoProducts));
        }

        @Test
        @DisplayName("GET /api/products?pag-1-1&contains= -> 200 OK")
        void when_contains_is_empty_then_should_return_an_empty_json__OK() throws Exception {
            final Sort orderByDescriptionAsc = getDefaultSorting();
            final Pageable secondPageWithOneProduct = createPageable("1-1", orderByDescriptionAsc);
            final String contains = "";

            given(productService.findAllByDescriptionIgnoreCaseContaining(eq(contains), eq(secondPageWithOneProduct)))
                .willReturn(emptyPage());

            makeRequestWithPageAndContains("1-1", contains)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

            verify(productService, times(1)).findAllByDescriptionIgnoreCaseContaining(eq(contains), eq(secondPageWithOneProduct));
            verify(productService, only()).findAllByDescriptionIgnoreCaseContaining(eq(contains), eq(secondPageWithOneProduct));
        }

        @Test
        @DisplayName("GET /api/products?pag-1-1&contains=800g -> 200 OK")
        void when_contains_does_not_match_anything_then_should_return_an_empty_json__OK() throws Exception {
            final Sort orderByDescriptionAsc = getDefaultSorting();
            final Pageable secondPageWithOneProduct = createPageable("1-1", orderByDescriptionAsc);
            final String contains = "800g";

            given(productService.findAllByDescriptionIgnoreCaseContaining(eq(contains), eq(secondPageWithOneProduct)))
                .willReturn(emptyPage());

            makeRequestWithPageAndContains("1-1", contains)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

            verify(productService, times(1)).findAllByDescriptionIgnoreCaseContaining(eq(contains), eq(secondPageWithOneProduct));
            verify(productService, only()).findAllByDescriptionIgnoreCaseContaining(eq(contains), eq(secondPageWithOneProduct));
        }
    }

    @Nested
    class GetAllPagedStartingWithDescriptionTest {

        @Test
        @DisplayName("GET /api/products?pag=0-2&starts-with=bisc -> 200")
        void should_return_the_first_page_with_two_products_that_start_with_bisc__OK() throws Exception {
            final Sort orderByDescriptionAsc = getDefaultSorting();
            final Pageable firstPageWithTwoProducts = createPageable("0-2", orderByDescriptionAsc);
            final String startsWith = "bisc";

            given(productService.findAllByDescriptionIgnoreCaseStartingWith(eq(startsWith), eq(firstPageWithTwoProducts)))
                .willReturn(createPage(firstPageWithTwoProducts, filterByStartingWith(startsWith)));

            makeRequestWithPageAndStartsWith("0-2", startsWith)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(
                    "$.content[*].description",
                    contains("BISC ROSQ MARILAN 350G INT", "BISC WAFER TODDY 132G CHOC")
                ))
                .andExpect(jsonPath("$.content[*].barcode", contains("7896003737257", "7896071024709")))
                .andExpect(jsonPath("$.content[*].links[0].rel", everyItem(equalTo("prices"))))
                .andExpect(jsonPath("$.content[*].links[1].rel", everyItem(equalTo("self"))))
                .andExpect(jsonPath(
                    "$.content[*].links[0].href",
                    contains(concatWithUrl(Constants.PRICES_URL, "7896003737257", "7896071024709"))
                ))
                .andExpect(jsonPath(
                    "$.content[*].links[1].href",
                    contains(concatWithUrl(Constants.PRODUCTS_URL+"/", "7896003737257", "7896071024709"))
                ))
                .andExpect(jsonPath("$.currentCountOfItems").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.totalOfPages").value(2))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalOfItems").value(3))
                .andExpect(jsonPath("$.links[0].rel").value("Next page"))
                .andExpect(jsonPath("$.links[0].href").value(Constants.PRODUCTS_URL+"?pag=1-2&starts-with=bisc"));


            verify(productService, times(1)).findAllByDescriptionIgnoreCaseStartingWith(eq(startsWith), eq(firstPageWithTwoProducts));
            verify(productService, only()).findAllByDescriptionIgnoreCaseStartingWith(eq(startsWith), eq(firstPageWithTwoProducts));
        }

        @Test
        @DisplayName("GET /api/products?pag=0-5&starts-with= -> 200")
        void when_startsWith_is_empty_then_should_return_an_empty_json__200() throws Exception {
            final Sort orderByDescriptionAsc = getDefaultSorting();
            final Pageable firstPageWithFiveProducts = createPageable("0-5", orderByDescriptionAsc);
            final String startsWith = "";

            given(productService.findAllByDescriptionIgnoreCaseContaining(eq(startsWith), eq(firstPageWithFiveProducts)))
                .willReturn(emptyPage());

            makeRequestWithPageAndContains("0-5", startsWith)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

            verify(productService, times(1)).findAllByDescriptionIgnoreCaseContaining(eq(startsWith), eq(firstPageWithFiveProducts));
            verify(productService, only()).findAllByDescriptionIgnoreCaseContaining(eq(startsWith), eq(firstPageWithFiveProducts));
        }

        @Test
        @DisplayName("GET /api/products?pag=0-5&startsWith=cheese")
        void when_startsWith_does_not_match_anything_then_should_return_an_empty_json__200() throws Exception {
            final Sort orderByDescriptionAsc = getDefaultSorting();
            final Pageable firstPageWithFiveProducts = createPageable("0-5", orderByDescriptionAsc);
            final String startsWith = "cheese";

            given(productService.findAllByDescriptionIgnoreCaseContaining(eq(startsWith), eq(firstPageWithFiveProducts)))
                .willReturn(emptyPage());

            makeRequestWithPageAndContains("0-5", startsWith)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

            verify(productService, times(1)).findAllByDescriptionIgnoreCaseContaining(eq(startsWith), eq(firstPageWithFiveProducts));
            verify(productService, only()).findAllByDescriptionIgnoreCaseContaining(eq(startsWith), eq(firstPageWithFiveProducts));
        }
    }
}