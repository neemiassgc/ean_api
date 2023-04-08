package com.api.controller;

import com.api.component.Constants;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static com.api.controller.ProductControllerTestHelper.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // disable in-memory database
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductControllerIT {

    @Autowired private MockMvc mockMvc;

    @BeforeAll
    void setup() {
        ProductControllerTestHelper.mockMvc = mockMvc;
    }

    @Nested
    class GetAllTest {

        @Test
        @DisplayName("GET /api/products -> 200 OK")
        void when_getAll_should_return_all_products__OK() throws Exception {
            final String[] expectedBarcodeList = {
                "7891000055120", "7897534852624", "7896336010058",
                "7898279792299", "7896045104482", "7891962047560",
                "7896656800018", "7896004004501", "7891098010575",
                "7896036093085", "7891962057620"
            };

            makeRequest()
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(11)))
                .andExpect(jsonPath("$[*].barcode", contains(expectedBarcodeList)))
                .andExpect(jsonPath("$[*].links[0].rel", everyItem(equalTo("prices"))))
                .andExpect(jsonPath("$[*].links[0].href", contains(concatWithUrl(Constants.PRICES_URL, expectedBarcodeList))))
                .andExpect(jsonPath("$[*].links[1].rel", everyItem(equalTo("self"))))
                .andExpect(jsonPath("$[*].links[1].href", contains(concatWithUrl(Constants.PRODUCTS_URL+"/", expectedBarcodeList))));
        }

        @Test
        @DisplayName("GET /api/products?pag=0-5 -> 200 OK")
        void should_return_the_first_page_with_five_products__OK() throws Exception {
            final String firstPageWithFiveProducts = "0-5";
            final String[] expectedBarcodeList = {
                "7891000055120", "7897534852624", "7896336010058",
                "7898279792299", "7896045104482"
            };

            makeRequestWithPage(firstPageWithFiveProducts)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalOfPages").value(3))
                .andExpect(jsonPath("$.currentCountOfItems").value(5))
                .andExpect(jsonPath("$.totalOfItems").value(11))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpectAll(ContentTester.builder()
                    .withNextPage("1-5")
                    .withExpectedBarcodeSet(expectedBarcodeList).test()
                );
        }

        @Test
        @DisplayName("GET /api/products?pag=1-5 -> 200 OK")
        void should_return_the_second_page_with_five_products__OK() throws Exception {
            final String secondPageWithFiveProducts = "1-5";
            final String[] expectedBarcodeList = {
                "7891962047560", "7896656800018", "7896004004501",
                "7891098010575", "7896036093085"
            };

            makeRequestWithPage(secondPageWithFiveProducts)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalOfPages").value(3))
                .andExpect(jsonPath("$.currentCountOfItems").value(5))
                .andExpect(jsonPath("$.totalOfItems").value(11))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpectAll(ContentTester.builder()
                    .withExpectedBarcodeSet(expectedBarcodeList)
                    .withNextPage("2-5").test()
                );
        }

        @Test
        @DisplayName("GET /api/products?pag=2-5 -> 200 OK")
        void should_return_the_third_page_with_one_product__OK() throws Exception {
            final String thirdPageWithOneProduct = "2-5";

            makeRequestWithPage(thirdPageWithOneProduct)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.totalOfPages").value(3))
                .andExpect(jsonPath("$.totalOfItems").value(11))
                .andExpect(jsonPath("$.currentCountOfItems").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpectAll(ContentTester.builder()
                    .withExpectedBarcodeSet("7891962057620")
                    .test()
                );
        }

        @Test
        @DisplayName("GET /api/products?pag=3-5 -> 200 OK")
        void when_the_page_is_too_big_should_not_return_anything__OK() throws Exception {
            final String fourthPage = "3-5";

            makeRequestWithPage(fourthPage)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("GET /api/products?pag=0- -> 400 BAD REQUEST")
        void should_return_violations_of_the_parameter_pag__BAD_REQUEST() throws Exception {
            final String firstPage = "0-";

            makeRequestWithPage(firstPage)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].violationMessage").value("must match digit-digit"));
        }
    }

    @Nested
    class GetByBarcodeTest {

        @Test
        @DisplayName("GET /api/products/7891000055120 -> 200 OK")
        void should_return_a_product__OK() throws Exception {
            final String targetBarcode = "7891000055120";

            makeRequestByBarcode(targetBarcode)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.description").value("ACHOC PO NESCAU 800G"))
                .andExpect(jsonPath("$.sequenceCode").value(29250))
                .andExpect(jsonPath("$.barcode").value(targetBarcode))
                .andExpect(jsonPath("$.links[0].rel").value("prices"))
                .andExpect(jsonPath("$.links[0].href").value(Constants.PRICES_URL+"7891000055120"))
                .andExpect(jsonPath("$.links[1].rel").value("self"))
                .andExpect(jsonPath("$.links[1].href").value(Constants.PRODUCTS_URL+"/7891000055120"));
        }

        @Test
        @DisplayName("GET /api/products/7891000055129 -> 404 NOT FOUND")
        void when_a_product_is_not_found_then_should_return_an_error_message__NOT_FOUND() throws Exception {
            final String nonExistingBarcode = "7891000055129";

            makeRequestByBarcode(nonExistingBarcode)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Product not found"));
        }

        @Test
        @DisplayName("GET /api/products/7890foobar -> 400 BAD REQUEST")
        void when_barcode_is_not_valid_then_should_return_violations__BAD_REQUEST() throws Exception {
            final String violatedBarcode = "7890foobar";

            makeRequestByBarcode(violatedBarcode)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[*].field", everyItem(is(violatedBarcode))))
                .andExpect(jsonPath(
                    "$.violations[*].violationMessage",
                    containsInAnyOrder("barcode must has 13 characters", "barcode must contain only numbers")
                ));
        }
    }

    @Nested
    class GetAllContainingDescriptionTest {

        @Test
        @DisplayName("GET /api/products?pag=0-2&contains=500g -> 200 OK")
        void should_response_a_page_with_two_products__OK() throws Exception {
            final String contains = "500g";
            final String firstPageWithTwoProducts = "0-2";

            makeRequestWithPageAndContains(firstPageWithTwoProducts, contains)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalOfPages").value(1))
                .andExpect(jsonPath("$.currentCountOfItems").value(2))
                .andExpect(jsonPath("$.totalOfItems").value(2))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpectAll(ContentTester.builder().withExpectedBarcodeSet("7898279792299", "7896656800018").test());
        }

        @Test
        @DisplayName("GET /api/products?pag=1-1&contains=400g -> 200 OK")
        void should_response_a_page_with_one_product__OK() throws Exception {
            final String contains = "400g";
            final String secondPageWithOneProduct = "1-1";

            makeRequestWithPageAndContains(secondPageWithOneProduct, contains)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalOfPages").value(2))
                .andExpect(jsonPath("$.currentCountOfItems").value(1))
                .andExpect(jsonPath("$.totalOfItems").value(2))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpectAll(ContentTester.builder().withExpectedBarcodeSet("7891962057620").test());
        }

        @Test
        @DisplayName("GET /api/products?pag=1-1&contains= -> 200 OK")
        void should_response_an_empty_page__OK() throws Exception {
            final String contains = "";
            final String secondPageWithOneProduct = "1-1";

            makeRequestWithPageAndContains(secondPageWithOneProduct, contains)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("GET /api/products?pag=0-1&contains=400g -> 200 OK")
        void should_return_a_page_with_one_product__OK() throws Exception {
            final String contains = "400g";
            final String secondPageWithOneProduct = "0-1";

            makeRequestWithPageAndContains(secondPageWithOneProduct, contains)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalOfPages").value(2))
                .andExpect(jsonPath("$.currentCountOfItems").value(1))
                .andExpect(jsonPath("$.totalOfItems").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpectAll(ContentTester.builder()
                    .withExpectedBarcodeSet("7896336010058")
                    .withNextPage("1-1&contains=400g").test()
                );
        }
    }

    @Nested
    class GetAllStartingWithDescriptionTest {

        @Test
        @DisplayName("GET /api/products?pag=0-2&starts-with=b -> 200 OK")
        void should_return_the_first_page_with_two_products__OK() throws Exception {
            final String firstPageWithTwoProducts = "0-2";

            makeRequestWithPageAndStartsWith(firstPageWithTwoProducts, "b")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalOfPages").value(2))
                .andExpect(jsonPath("$.currentCountOfItems").value(2))
                .andExpect(jsonPath("$.totalOfItems").value(3))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpectAll(ContentTester.builder()
                    .withExpectedBarcodeSet("7898279792299", "7896045104482")
                    .withNextPage("1-2&starts-with=b").test()
                );
        }

        @Test
        @DisplayName("GET /api/products?pag=1-2&starts-with=b -> 200 OK")
        void should_return_the_second_page_with_one_product__OK() throws Exception {
            final String secondPageWithOneProduct = "1-2";

            makeRequestWithPageAndStartsWith(secondPageWithOneProduct, "b")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalOfPages").value(2))
                .andExpect(jsonPath("$.currentCountOfItems").value(1))
                .andExpect(jsonPath("$.totalOfItems").value(3))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.content[0].description").value("BISC CEREALE BAUDUC 170G CACAU E CAST"))
                .andExpectAll(ContentTester.builder().withExpectedBarcodeSet("7891962047560").test());
        }

        @Test
        @DisplayName("GET /api/products?pag=2-5&starts-with=torr")
        void when_startsWith_does_not_match_anything_then_should_return_nothing() throws Exception {
            final String thirdPageWithFiveProducts = "2-5";
            final String startsWith = "torr";

            makeRequestWithPageAndStartsWith(thirdPageWithFiveProducts, startsWith)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    class GetAllEndingWithDescriptionTest {

        @Test
        @DisplayName("GET /api/products?pag=0-2&ends-with=a -> 200 OK")
        void should_return_the_first_page_with_two_products__OK() throws Exception {
            final String firstPageWithTwoProducts = "0-2";
            final String endsWith = "a";

            makeRequestWithPageAndEndsWith(firstPageWithTwoProducts, endsWith)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalOfPages").value(2))
                .andExpect(jsonPath("$.currentCountOfItems").value(2))
                .andExpect(jsonPath("$.totalOfItems").value(3))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpectAll(ContentTester.builder()
                    .withExpectedBarcodeSet("7896336010058", "7891098010575")
                    .withNextPage("1-2&ends-with=a").test()
                );
        }

        @Test
        @DisplayName("GET /api/products?pag=1-2&ends-with=a -> 200 OK")
        void should_return_the_last_page_with_one_product__OK() throws Exception {
            final String lastPageWithOneProduct = "1-2";
            final String endsWith = "a";

            makeRequestWithPageAndEndsWith(lastPageWithOneProduct, endsWith)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalOfPages").value(2))
                .andExpect(jsonPath("$.currentCountOfItems").value(1))
                .andExpect(jsonPath("$.totalOfItems").value(3))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpectAll(ContentTester.builder().withExpectedBarcodeSet("7896036093085").test());
        }

        @Test
        @DisplayName("GET /api/products?pag=1-3&ends-with=mango -> 200 OK")
        void when_ends_with_is_does_not_match_anything_then_should_return_an_empty_json__OK() throws Exception {
            final String secondPageWithThreeProducts = "1-2";
            final String endsWith = "mango";

            makeRequestWithPageAndEndsWith(secondPageWithThreeProducts, endsWith)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("GET /api/products?pag=1-3&ends-with= -> 200 OK")
        void when_ends_with_is_empty_then_should_return_an_empty_json__OK() throws Exception {
            final String secondPageWithThreeProducts = "1-2";
            final String endsWith = "";

            makeRequestWithPageAndEndsWith(secondPageWithThreeProducts, endsWith)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    class FieldValidatorsTest {

        @Nested
        class GetByBarcodeTest {

            @Test
            @DisplayName("GET /api/products/18908 -> BAD_REQUEST 400")
            void when_barcode_is_less_than_13_then_should_return_a_violation() throws Exception {
                final String invalidBarcode = "18908";
                mockMvc.perform(get("/api/products/"+invalidBarcode))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violations[0].field").value(invalidBarcode))
                    .andExpect(jsonPath("$.violations[0].violationMessage").value("barcode must has 13 characters"));
            }

            @Test
            @DisplayName("GET /api/products/1927384019283145 -> BAD_REQUEST 400")
            void when_barcode_is_greater_than_13_then_should_return_a_violation() throws Exception {
                final String invalidBarcode = "1927384019283145";
                mockMvc.perform(get("/api/products/"+invalidBarcode))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violations[0].field").value(invalidBarcode))
                    .andExpect(jsonPath("$.violations[0].violationMessage").value("barcode must has 13 characters"));
            }

            @Test
            @DisplayName("GET /api/products/7alfpm439ayra -> BAD_REQUEST 400")
            void when_barcode_contains_letters_then_should_return_a_violation() throws Exception {
                final String invalidBarcode = "7alfpm439ayra";
                mockMvc.perform(get("/api/products/" + invalidBarcode))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violations[0].field").value(invalidBarcode))
                    .andExpect(jsonPath("$.violations[0].violationMessage").value("barcode must contain only numbers"));
            }
        }
    }
}
