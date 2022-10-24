package com.api.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static com.api.controller.ProductControllerTestHelper.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // disable in-memory database
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductControllerIT {

    @Autowired private MockMvc mockMvc;
    
    private final String BASE_ENDPOINT = "/api/products";
    private final String PRICES_URL = "http://localhost/api/prices?barcode=";
    private final String SELF_URL = "http://localhost"+BASE_ENDPOINT;

    @BeforeAll
    void setup() {
        ProductControllerTestHelper.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("GET "+BASE_ENDPOINT+" - 200 OK")
    void when_getAll_should_return_all_products_with_200() throws Exception {
        makeRequest()
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(11)))
            // Verifying the first product
            .andExpect(jsonPath("$[0].description").value("ACHOC PO NESCAU 800G"))
            .andExpect(jsonPath("$[0].sequenceCode").value(is(29250)))
            .andExpect(jsonPath("$[0].barcode").value("7891000055120"))
            .andExpect(jsonPath("$[0].links[0].rel").value("prices"))
            .andExpect(jsonPath("$[0].links[1].rel").value("self"))
            .andExpect(jsonPath("$[0].links[0].href").value(PRICES_URL+"7891000055120"))
            .andExpect(jsonPath("$[0].links[1].href").value(SELF_URL+"/7891000055120"))
            // Verifying the last product
            .andExpect(jsonPath("$[10].description").value("PAO BAUDUC 400G INTE"))
            .andExpect(jsonPath("$[10].sequenceCode").value(is(134262)))
            .andExpect(jsonPath("$[10].barcode").value("7891962057620"))
            .andExpect(jsonPath("$[10].links[0].rel").value("prices"))
            .andExpect(jsonPath("$[10].links[1].rel").value("self"))
            .andExpect(jsonPath("$[10].links[0].href").value(PRICES_URL+"7891962057620"))
            .andExpect(jsonPath("$[10].links[1].href").value(SELF_URL+"/7891962057620"))
            // Verifying the order
            .andExpect(jsonPath(
                "$[*].sequenceCode",
                contains(29250, 137513, 120983, 93556, 142862, 113249, 2909, 105711, 9785, 1184, 134262)
            ));
    }

    @Nested
    class GetAllPagedTest {

        @Test
        @DisplayName("GET "+BASE_ENDPOINT+"?pag=0-5 - 200 OK")
        void should_return_the_first_page_of_paged_products_with_200() throws Exception {
            final String firstPage = "0-5";

            makeRequestWithPage(firstPage)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalOfPages").value(3))
                .andExpect(jsonPath("$.currentCountOfItems").value(5))
                .andExpect(jsonPath("$.totalOfItems").value(11))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.content[*].barcode",
                    contains(
                        "7891000055120",
                        "7897534852624",
                        "7896336010058",
                        "7898279792299",
                        "7896045104482")
                    )
                )
                .andExpect(jsonPath("$.content[*].links[0].rel", everyItem(equalTo("prices"))))
                .andExpect(jsonPath("$.content[*].links[0].href",
                    contains(concatWithUrl(PRICES_URL,
                        "7891000055120",
                        "7897534852624",
                        "7896336010058",
                        "7898279792299",
                        "7896045104482"
                    ))
                ))
                .andExpect(jsonPath("$.content[*].links[1].rel", everyItem(equalTo("self"))))
                .andExpect(jsonPath("$.content[*].links[1].href",
                    contains(concatWithUrl(SELF_URL+"/",
                        "7891000055120",
                        "7897534852624",
                        "7896336010058",
                        "7898279792299",
                        "7896045104482"
                    ))
                ))
                .andExpect(jsonPath("$.links[0].rel").value("next page"))
                .andExpect(jsonPath("$.links[0].href").value(SELF_URL+"?pag=1-5"));
        }

        @Test
        @DisplayName("GET "+BASE_ENDPOINT+"?pag=1-5 - 200 OK")
        void should_return_the_middle_page_of_paged_products_with_200() throws Exception {
            final String secondPage = "1-5";

            makeRequestWithPage(secondPage)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalOfPages").value(3))
                .andExpect(jsonPath("$.currentCountOfItems").value(5))
                .andExpect(jsonPath("$.totalOfItems").value(11))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.content[*].barcode",
                    contains("7891962047560", "7896656800018", "7896004004501", "7891098010575", "7896036093085"))
                )
                .andExpect(jsonPath("$.content[*].links[0].rel", everyItem(equalTo("prices"))))
                .andExpect(jsonPath("$.content[*].links[0].href",
                    contains(concatWithUrl(PRICES_URL,
                        "7891962047560",
                        "7896656800018",
                        "7896004004501",
                        "7891098010575",
                        "7896036093085"
                    ))
                ))
                .andExpect(jsonPath("$.content[*].links[1].href",
                    contains(concatWithUrl(SELF_URL+"/",
                        "7891962047560",
                        "7896656800018",
                        "7896004004501",
                        "7891098010575",
                        "7896036093085"
                    ))
                ))
                .andExpect(jsonPath("$.links[0].rel").value("next page"))
                .andExpect(jsonPath("$.links[0].href").value(SELF_URL+"?pag=2-5"));
        }

        @Test
        @DisplayName("GET "+BASE_ENDPOINT+"?pag=2-5 - 200 OK")
        void should_return_the_last_page_of_paged_products_with_200() throws Exception {
            final String thirdPage = "2-5";

            makeRequestWithPage(thirdPage)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.totalOfPages").value(3))
                .andExpect(jsonPath("$.totalOfItems").value(11))
                .andExpect(jsonPath("$.currentCountOfItems").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.content[*].barcode", contains("7891962057620")))
                .andExpect(jsonPath("$.content[*].links[0].rel", everyItem(equalTo("prices"))))
                .andExpect(jsonPath("$.content[*].links[0].href",
                    contains(PRICES_URL+"7891962057620")
                ))
                .andExpect(jsonPath("$.content[*].links[1].rel", everyItem(equalTo("self"))))
                .andExpect(jsonPath("$.content[*].links[1].href",
                    contains(SELF_URL+"/7891962057620")
                ))
                .andExpect(jsonPath("$.links").isEmpty());
        }

        @Test
        @DisplayName("GET "+BASE_ENDPOINT+"?pag=3-5 - 200 OK")
        void should_not_return_anything_with_200() throws Exception {
            final String fourthPage = "3-5";

            makeRequestWithPage(fourthPage)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("GET "+BASE_ENDPOINT+"?pag=0-&contains=500g -> 400 BAD REQUEST")
        void should_response_violations_of_the_parameter_pag_with_400() throws Exception {
            final String contains = "500g";
            final String firstPageWithTwoProducts = "0-";

            makeRequestWithPageAndContains(firstPageWithTwoProducts, contains)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].violationMessage").value("must match digit-digit"));
        }
    }

    @Nested
    class GetByBarcodeTest {

        @Test
        @DisplayName("GET "+BASE_ENDPOINT+"/7891000055120 - 200 OK")
        void should_return_a_product_with_200() throws Exception {
            final String targetBarcode = "7891000055120";

            makeRequestByBarcode(targetBarcode)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.description").value("ACHOC PO NESCAU 800G"))
                .andExpect(jsonPath("$.sequenceCode").value(29250))
                .andExpect(jsonPath("$.barcode").value(targetBarcode))
                .andExpect(jsonPath("$.links[0].rel").value("prices"))
                .andExpect(jsonPath("$.links[0].href").value(PRICES_URL+"7891000055120"))
                .andExpect(jsonPath("$.links[1].rel").value("self"))
                .andExpect(jsonPath("$.links[1].href").value(SELF_URL+"/7891000055120"));
        }

        @Test
        @DisplayName("GET "+BASE_ENDPOINT+"/7891000055129 - 404 NOT FOUND")
        void should_return_an_error_message_with_404() throws Exception {
            final String nonExistingBarcode = "7891000055129";

            makeRequestByBarcode(nonExistingBarcode)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Product not found"));
        }

        @Test
        @DisplayName("GET "+BASE_ENDPOINT+"/7890foobar - 400 BAD REQUEST")
        void should_return_violations_with_400() throws Exception {
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
    class GetAllPagedContainingDescriptionTest {

        @Test
        @DisplayName("GET "+BASE_ENDPOINT+"?pag=0-2&contains=500g -> 200 OK")
        void should_response_a_page_with_two_products_filtered_by_description() throws Exception {
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
                .andExpect(jsonPath("$.content[*].barcode", contains("7898279792299", "7896656800018")))
                .andExpect(jsonPath("$.content[*].links[0].rel", everyItem(equalTo("prices"))))
                .andExpect(jsonPath("$.content[*].links[0].href", contains(concatWithUrl(PRICES_URL, "7898279792299", "7896656800018"))))
                .andExpect(jsonPath("$.content[*].links[1].rel", everyItem(equalTo("self"))))
                .andExpect(jsonPath("$.content[*].links[1].href", contains(concatWithUrl(SELF_URL+"/", "7898279792299", "7896656800018"))))
                .andExpect(jsonPath("$.links").isEmpty());
        }

        @Test
        @DisplayName("GET "+BASE_ENDPOINT+"?pag=1-1&contains=400g -> 200 OK")
        void should_response_a_page_with_one_product_filtered_by_description() throws Exception {
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
                .andExpect(jsonPath("$.content[*].barcode").value("7891962057620"))
                .andExpect(jsonPath("$.content[*].links[0].rel", everyItem(equalTo("prices"))))
                .andExpect(jsonPath("$.content[*].links[0].href", contains(concatWithUrl(PRICES_URL, "7891962057620"))))
                .andExpect(jsonPath("$.content[*].links[1].rel", everyItem(equalTo("self"))))
                .andExpect(jsonPath("$.content[*].links[1].href", contains(concatWithUrl(SELF_URL+"/", "7891962057620"))))
                .andExpect(jsonPath("$.links").isEmpty());
        }
    }
}
