package com.api.controller;

import com.api.component.Constants;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
                "7891000055120", "7897534852624", "7896336010058", "7898279792299",
                "7896000594198", "7892840812737", "7896045104482", "7891962047560",
                "7896656800018", "7896004004501", "7891098010575", "7894000840079",
                "7896214532108", "7898080641618", "7896036093085", "7891962057620"
            };

            makeRequest()
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(16)))
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
                "7891000055120", "7897534852624",
                "7896336010058", "7898279792299", "7896000594198"
            };

            makeRequestWithPage(firstPageWithFiveProducts)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalOfPages").value(4))
                .andExpect(jsonPath("$.currentCountOfItems").value(5))
                .andExpect(jsonPath("$.totalOfItems").value(16))
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
                "7892840812737", "7896045104482", "7891962047560",
                "7896656800018", "7896004004501"
            };

            makeRequestWithPage(secondPageWithFiveProducts)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalOfPages").value(4))
                .andExpect(jsonPath("$.currentCountOfItems").value(5))
                .andExpect(jsonPath("$.totalOfItems").value(16))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpectAll(ContentTester.builder()
                    .withExpectedBarcodeSet(expectedBarcodeList)
                    .withNextPage("2-5").test()
                );
        }

        @Test
        @DisplayName("GET /api/products?pag=2-5 -> 200 OK")
        void should_return_the_third_page_with_five_products__OK() throws Exception {
            final String thirdPageWithFiveProducts = "2-5";

            makeRequestWithPage(thirdPageWithFiveProducts)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.totalOfPages").value(4))
                .andExpect(jsonPath("$.totalOfItems").value(16))
                .andExpect(jsonPath("$.currentCountOfItems").value(5))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpectAll(
                    ContentTester.builder().withExpectedBarcodeSet(
                        "7891098010575", "7894000840079",
                        "7896214532108", "7898080641618", "7896036093085"
                    ).withNextPage("3-5").test()
                );
        }

        @Test
        @DisplayName("GET /api/products?pag=3-5 -> 200 OK")
        void should_return_the_last_page_with_one_product__OK() throws Exception {
            final String lastPageWithOneProduct = "3-5";

            makeRequestWithPage(lastPageWithOneProduct)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.currentPage").value(3))
                .andExpect(jsonPath("$.totalOfPages").value(4))
                .andExpect(jsonPath("$.totalOfItems").value(16))
                .andExpect(jsonPath("$.currentCountOfItems").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpectAll(
                    ContentTester
                        .builder()
                        .withExpectedBarcodeSet("7891962057620").test()
                );
        }

        @Test
        @DisplayName("GET /api/products?pag=4-5 -> 200 OK")
        void when_the_page_is_too_big_should_not_return_anything__OK() throws Exception {
            final String fourthPage = "4-5";

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
    }

    @Nested
    class GetAllContainingDescriptionTest {

        @Test
        @DisplayName("GET /api/products?pag=0-1&contains=400g -> 200 OK")
        void should_respond_with_the_fist_page_with_one_product__OK() throws Exception {
            final String contains = "400g";
            final String firstPageWithOneProduct = "0-1";

            makeRequestWithPageAndContains(firstPageWithOneProduct, contains)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalOfPages").value(3))
                .andExpect(jsonPath("$.currentCountOfItems").value(1))
                .andExpect(jsonPath("$.totalOfItems").value(3))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpectAll(
                    ContentTester.builder()
                        .withExpectedBarcodeSet("7896336010058")
                        .withNextPage("1-1&contains=400g").test()
                );
        }

        @Test
        @DisplayName("GET /api/products?pag=1-2&contains=400g -> 200 OK")
        void should_respond_with_the_second_page_with_one_product__OK() throws Exception {
            final String contains = "400g";
            final String secondPageWithTwoProducts = "1-2";

            makeRequestWithPageAndContains(secondPageWithTwoProducts, contains)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalOfPages").value(2))
                .andExpect(jsonPath("$.currentCountOfItems").value(1))
                .andExpect(jsonPath("$.totalOfItems").value(3))
                .andExpect(jsonPath("$.hasNext").value(false));
        }

        @Test
        @DisplayName("GET /api/products?pag=4-2&contains=beb -> 200 OK")
        void when_the_pagination_is_out_of_bounds_then_should_return_nothing_as_an_empty_array__OK() throws Exception {
            final String fourthPageWithTwoProducts = "4-2";

            makeRequestWithPageAndStartsWith(fourthPageWithTwoProducts, "beb")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("GET /api/products?pag=3-2&contains=400g -> 200 OK")
        void when_there_are_no_products_to_return_then_should_respond_with_nothing__OK() throws Exception {
            final String contains = "400g";
            final String thirdPageWithTwoProducts = "3-2";

            makeRequestWithPageAndContains(thirdPageWithTwoProducts, contains)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", empty()));
        }
    }

    @Nested
    class GetAllStartingWithDescriptionTest {

        @Test
        @DisplayName("GET /api/products?pag=0-2&starts-with=beb -> 200 OK")
        void should_return_the_first_page_with_two_products__OK() throws Exception {
            final String firstPageWithTwoProducts = "0-2";

            makeRequestWithPageAndStartsWith(firstPageWithTwoProducts, "beb")
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
                    .withExpectedBarcodeSet("7896000594198", "7892840812737")
                    .withNextPage("1-2&starts-with=beb").test()
                );
        }

        @Test
        @DisplayName("GET /api/products?pag=1-2&starts-with=beb -> 200 OK")
        void should_return_the_second_page_with_one_product__OK() throws Exception {
            final String secondPageWithOneProduct = "1-2";

            makeRequestWithPageAndStartsWith(secondPageWithOneProduct, "beb")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalOfPages").value(2))
                .andExpect(jsonPath("$.currentCountOfItems").value(1))
                .andExpect(jsonPath("$.totalOfItems").value(3))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.content[0].description").value("BEB LACT 3 CORACOES 260ML PINGADO"))
                .andExpectAll(ContentTester.builder().withExpectedBarcodeSet("7896045104482").test());
        }

        @Test
        @DisplayName("GET /api/products?pag=4-2&starts-with=beb -> 200 OK")
        void when_the_pagination_is_out_of_bounds_then_should_return_nothing_as_an_empty_array__OK() throws Exception {
            final String fourthPageWithTwoProducts = "4-2";

            makeRequestWithPageAndStartsWith(fourthPageWithTwoProducts, "beb")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
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
        @DisplayName("GET /api/products?pag=0-2&ends-with=laranja -> 200 OK")
        void should_return_the_first_page_with_two_products__OK() throws Exception {
            final String firstPageWithTwoProducts = "0-2";
            final String endsWith = "laranja";

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
                    .withExpectedBarcodeSet("7894000840079", "7896214532108")
                    .withNextPage("1-2&ends-with=laranja").test()
                );
        }

        @Test
        @DisplayName("GET /api/products?pag=1-2&ends-with=laranja -> 200 OK")
        void should_return_the_seconds_page_with_one_product__OK() throws Exception {
            final String secondPageWithOneProduct = "1-2";
            final String endsWith = "laranja";

            makeRequestWithPageAndEndsWith(secondPageWithOneProduct, endsWith)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalOfPages").value(2))
                .andExpect(jsonPath("$.currentCountOfItems").value(1))
                .andExpect(jsonPath("$.totalOfItems").value(3))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpectAll(ContentTester.builder().withExpectedBarcodeSet("7898080641618").test());
        }

        @Test
        @DisplayName("GET /api/products?pag=5-2&ends-with=laranja")
        void when_pagination_is_out_of_bounds_then_should_respond_with_nothing_as_an_empty_array() throws Exception {
            final String fifthPageWithTwoProducts = "5-2";
            final String endsWith = "laranja";

            makeRequestWithPageAndEndsWith(fifthPageWithTwoProducts, endsWith)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
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
    }

    @Nested
    class FieldValidatorsTest {

        @Nested
        class GetByBarcodeTest {

            @Test
            @DisplayName("GET /api/products/18908 -> BAD_REQUEST 400")
            void when_barcode_is_less_than_13_then_should_return_a_violation() throws Exception {
                final String invalidBarcode = "18908";
                testWithBarcodeTemplate(invalidBarcode)
                    .andExpect(jsonPath("$.violations[0].violationMessage").value("barcode must have 13 characters"));
            }

            @Test
            @DisplayName("GET /api/products/1927384019283145 -> BAD_REQUEST 400")
            void when_barcode_is_greater_than_13_then_should_return_a_violation() throws Exception {
                final String invalidBarcode = "1927384019283145";
                testWithBarcodeTemplate(invalidBarcode)
                    .andExpect(jsonPath("$.violations[0].violationMessage").value("barcode must have 13 characters"));
            }

            @Test
            @DisplayName("GET /api/products/7alfpm439ayra -> BAD_REQUEST 400")
            void when_barcode_contains_letters_then_should_return_a_violation() throws Exception {
                final String invalidBarcode = "7alfpm439ayra";
                testWithBarcodeTemplate(invalidBarcode)
                    .andExpect(jsonPath("$.violations[0].violationMessage").value("barcode must contain only numbers"));
            }

            @Test
            @DisplayName("GET /api/products/17802a")
            void should_return_bad_request_when_barcode_contains_any_violations() throws Exception {
                final String invalidBarcode = "17802a";
                testWithBarcodeTemplate(invalidBarcode)
                    .andExpect(jsonPath("$.violations[*].violationMessage",
                        containsInAnyOrder(
                            equalTo("barcode must have 13 characters"),
                            equalTo("barcode must contain only numbers")
                        )
                    ));
            }

            private ResultActions testWithBarcodeTemplate(final String barcode) throws Exception {
                return mockMvc.perform(get("/api/products/"+barcode))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violations[*].field", everyItem(equalTo(barcode))));
            }
        }

        @Test
        @DisplayName("GET /api/products?pag=0--2")
        void when_pag_is_not_in_a_valid_format_then_should_return_a_violation() throws Exception {
            mockMvc.perform(get("/api/products")
                .queryParam("pag", "0--2")
                .accept(MediaType.ALL)
            )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.violations[*]").value(hasSize(1)))
            .andExpect(jsonPath("$.violations[0].field").value("0--2"))
            .andExpect(jsonPath("$.violations[0].violationMessage").value("must match digit-digit"));
        }

        @Nested
        class GetAllContainingDescriptionTest {

            @Test
            @DisplayName("GET /api/products?pag=0-6&contains=all -> 400 BAD_REQUEST")
            void when_contains_param_is_all_then_should_return_a_violation() throws Exception {
                testParamViolation("contains", "all", "Expression cannot contain 'all'");
            }

            @Test
            @DisplayName("GET /api/produdcts?pag=0-6&contains=bi -> 400 BAD_REQUEST")
            void when_contains_param_is_less_than_3_then_should_return_a_violation() throws Exception {
                testParamViolation("contains", "bi", "Expression length must be between 3 and 16");
            }

            @Test
            @DisplayName("GET /api/products?pag=0-6&contains=black and white triangle -> 400 BAD_REQUEST")
            void when_contains_param_is_greater_than_16_then_should_return_a_violation() throws Exception {
                testParamViolation("contains", "black and white triangle", "Expression length must be between 3 and 16");
            }

            @Test
            @DisplayName("GET /api/products?pag=0-6&contains=qi -> 400 BAD_REQUEST")
            void when_the_inputs_are_invalid_then_should_return_violations() throws Exception {
                testWithTwoParamViolations("contains");
            }
        }

        @Nested
        class GetAllStartingWithDescriptionTest {

            @Test
            @DisplayName("GET /api/products?pag=0-6&starts-with=po -> 400 BAD_REQUEST")
            void when_startsWith_param_is_less_than_3_then_should_return_a_violation() throws Exception {
                testParamViolation("starts-with", "po", "Expression length must be between 3 and 16");
            }

            @Test
            @DisplayName("GET /api/products?pag=0-6&starts-with=some words for some tests -> 400 BAD_REQUEST")
            void when_startsWith_param_is_greater_than_16_then_should_return_a_violation() throws Exception {
                testParamViolation("starts-with", "some words for some tests", "Expression length must be between 3 and 16");
            }

            @Test
            @DisplayName("GET /api/products?pag=0-6&starts-with=all 400 -> BAD_REQUEST")
            void should_return_a_violation_if_startsWith_param_is_all_() throws Exception {
                testParamViolation("starts-with", "all", "Expression cannot contain 'all'");
            }

            @Test
            @DisplayName("GET /api/products?pag=0--6&starts_with=qi -> 400 BAD_REQUEST")
            void when_there_are_any_violations_then_should_respond_with_bad_request() throws Exception {
               testWithTwoParamViolations("starts-with");
            }
        }

        @Nested
        class GetAllEndingWithDescriptionTest {

            @Test
            @DisplayName("GET /api/products?pag=0-6&ends-with=all -> 400 BAD_REQUEST")
            void should_return_a_violation_when_endsWith_param_is_all() throws Exception {
                testParamViolation("ends-with", "all", "Expression cannot contain 'all'");
            }

            @Test
            @DisplayName("GET /api/products?pag=0-6&ends-with=ti -> 400 BAD_REQUEST")
            void when_endsWith_param_is_less_than_3_then_should_respond_with_a_violation() throws Exception {
                testParamViolation("ends-with", "ti", "Expression length must be between 3 and 16");
            }

            @Test
            @DisplayName("GET /api/products?pag=0-6&ends-with=this must be over 16 -> 400 BAD_REQUEST")
            void when_endsWith_param_is_greater_than_16_then_should_respond_with_a_violation() throws Exception {
                testParamViolation("ends-with", "this must be over 16", "Expression length must be between 3 and 16");
            }

            @Test
            @DisplayName("GET /api/products?pag=0--6&ends-with=qi")
            void when_there_are_any_violations_with_endsWith_param_then_should_respond_with_bad_request() throws Exception {
                testWithTwoParamViolations("ends-with");
            }
        }

        private void testParamViolation(final String paramName, final String paramValue, final String expectedViolation) throws Exception {
            mockMvc.perform(get(String.format("/api/products?pag=0-6&%s=%s", paramName, paramValue))
                .accept(MediaType.ALL)
            )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.violations[*]").value(hasSize(1)))
            .andExpect(jsonPath("$.violations[0].field").value(paramValue))
            .andExpect(jsonPath("$.violations[0].violationMessage").value(expectedViolation));
        }

        private void testWithTwoParamViolations(final String paramName) throws Exception {
            mockMvc.perform(get("/api/products?pag=0--6&"+paramName+"=qi")
                .accept(MediaType.ALL)
            )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.violations[*]").value(hasSize(2)))
            .andExpect(jsonPath("$.violations[*].field").value(containsInAnyOrder("0--6", "qi")))
            .andExpect(jsonPath("$.violations[*].violationMessage").value(containsInAnyOrder(
                "must match digit-digit",
                "Expression length must be between 3 and 16"
            )));
        }
    }
}
