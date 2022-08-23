package com.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // disable in-memory database
public class ProductControllerIT {

    @Autowired private MockMvc mockMvc;
    
    private static final String DEFAULT_URL = "/api/products";


    @Nested
    class GetAllTest {

        @Test
        @DisplayName("GET "+DEFAULT_URL+" - 200 OK")
        void should_return_all_products_with_200() throws Exception {
            mockMvc.perform(get(DEFAULT_URL)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$._embedded.List").isArray())
            .andExpect(jsonPath("$._embedded.List", hasSize(11)))
            // Verifying the first product
            .andExpect(jsonPath("$._embedded.List[0].description").value("ACHOC PO NESCAU 800G"))
            .andExpect(jsonPath("$._embedded.List[0].sequenceCode").value(is(29250)))
            .andExpect(jsonPath("$._embedded.List[0].barcode").value("7891000055120"))
            .andExpect(jsonPath("$._embedded.List[0]._links.prices.href").value("http://localhost/api/prices?barcode=7891000055120"))
            .andExpect(jsonPath("$._embedded.List[0]._links.self.href").value("http://localhost/api/products/7891000055120"))
            // Verifying the last product
            .andExpect(jsonPath("$._embedded.List[10].description").value("PAO BAUDUC 400G INTE"))
            .andExpect(jsonPath("$._embedded.List[10].sequenceCode").value(is(134262)))
            .andExpect(jsonPath("$._embedded.List[10].barcode").value("7891962057620"))
            .andExpect(jsonPath("$._embedded.List[10]._links.prices.href").value("http://localhost/api/prices?barcode=7891962057620"))
            .andExpect(jsonPath("$._embedded.List[10]._links.self.href").value("http://localhost/api/products/7891962057620"))
            // Verifying the order
            .andExpect(jsonPath(
                "$._embedded.List[*].sequenceCode",
                contains(29250, 137513, 120983, 93556, 142862, 113249, 2909, 105711, 9785, 1184, 134262)
            ));
        }

        @Test
        @DisplayName("GET "+DEFAULT_URL+"?pag=0-5 - 200 OK")
        void should_return_the_first_page_of_paged_products_with_200() throws Exception {
            final String firstPage = "0-5";
            final String urlBasePrices = "http://localhost/api/prices?barcode=";
            final String urlBaseSelf = "http://localhost/api/products/";

            mockMvc.perform(get(DEFAULT_URL).queryParam("pag", firstPage)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$._embedded.List").isArray())
            .andExpect(jsonPath("$._embedded.List", hasSize(5)))
            .andExpect(jsonPath("$.currentPage").value(0))
            .andExpect(jsonPath("$.totalOfPages").value(3))
            .andExpect(jsonPath("$.numberOfItems").value(5))
            .andExpect(jsonPath("$.hasNext").value(true))
            .andExpect(
                jsonPath("$._embedded.List[*].barcode",
                contains("7891000055120", "7897534852624", "7896336010058", "7898279792299", "7896045104482"))
            )
            .andExpect(jsonPath(
                "$._embedded.List[*]._links.prices.href",
                contains(
                    urlBasePrices+"7891000055120", urlBasePrices+"7897534852624",
                    urlBasePrices+"7896336010058", urlBasePrices+"7898279792299", urlBasePrices+"7896045104482"
                )
            ))
            .andExpect(jsonPath(
                "$._embedded.List[*]._links.self.href",
                contains(
                    urlBaseSelf+"7891000055120", urlBaseSelf+"7897534852624",
                    urlBaseSelf+"7896336010058", urlBaseSelf+"7898279792299", urlBaseSelf+"7896045104482"
                )
            ))
            .andExpect(jsonPath("$._links.['next page'].href").value("http://localhost/api/products?pag=1-5"));
        }

        @Test
        @DisplayName("GET "+DEFAULT_URL+"?pag=1-5 - 200 OK")
        void should_return_the_middle_page_of_paged_products_with_200() throws Exception {
            final String secondPage = "1-5";
            final String urlBasePrices = "http://localhost/api/prices?barcode=";
            final String urlBaseSelf = "http://localhost/api/products/";

            mockMvc.perform(get(DEFAULT_URL).queryParam("pag", secondPage)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$._embedded.List").isArray())
            .andExpect(jsonPath("$._embedded.List", hasSize(5)))
            .andExpect(jsonPath("$.currentPage").value(1))
            .andExpect(jsonPath("$.totalOfPages").value(3))
            .andExpect(jsonPath("$.numberOfItems").value(5))
            .andExpect(jsonPath("$.hasNext").value(true))
            .andExpect(
                jsonPath("$._embedded.List[*].barcode",
                contains("7891962047560", "7896656800018", "7896004004501", "7891098010575", "7896036093085"))
            )
            .andExpect(jsonPath(
                "$._embedded.List[*]._links.prices.href",
                contains(
                    urlBasePrices+"7891962047560", urlBasePrices+"7896656800018",
                    urlBasePrices+"7896004004501", urlBasePrices+"7891098010575", urlBasePrices+"7896036093085"
                )
            ))
            .andExpect(jsonPath(
                "$._embedded.List[*]._links.self.href",
                contains(
                    urlBaseSelf+"7891962047560", urlBaseSelf+"7896656800018",
                    urlBaseSelf+"7896004004501", urlBaseSelf+"7891098010575", urlBaseSelf+"7896036093085"
                )
            ))
            .andExpect(jsonPath("$._links['next page'].href").value("http://localhost/api/products?pag=2-5"));
        }

        @Test
        @DisplayName("GET "+DEFAULT_URL+"?pag=2-5 - 200 OK")
        void should_return_the_last_page_of_paged_products_with_200() throws Exception {
            final String thirdPage = "2-5";

            mockMvc.perform(get(DEFAULT_URL).queryParam("pag", thirdPage)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$._embedded.List").isArray())
            .andExpect(jsonPath("$._embedded.List", hasSize(1)))
            .andExpect(jsonPath("$.currentPage").value(2))
            .andExpect(jsonPath("$.totalOfPages").value(3))
            .andExpect(jsonPath("$.numberOfItems").value(1))
            .andExpect(jsonPath("$.hasNext").value(false))
            .andExpect(jsonPath("$._embedded.List[*].barcode", contains("7891962057620")))
            .andExpect(jsonPath(
                "$._embedded.List[*]._links.prices.href",
                contains("http://localhost/api/prices?barcode=7891962057620")
            ))
            .andExpect(jsonPath(
                "$._embedded.List[*]._links.self.href",
                contains("http://localhost/api/products/7891962057620")
            ))
            .andExpect(jsonPath("$._links").doesNotExist());
        }

        @Test
        @DisplayName("GET "+DEFAULT_URL+"?pag=3-5 - 200 OK")
        void should_not_return_anything_with_200() throws Exception {
            final String thirdPage = "3-5";

            mockMvc.perform(get(DEFAULT_URL).queryParam("pag", thirdPage)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    class GetByBarcodeTest {

        @Test
        @DisplayName("GET "+DEFAULT_URL+"/7891000055120 - 200 OK")
        void should_return_a_product_with_200() throws Exception {
            final String barcode = "7891000055120";

            mockMvc.perform(get(DEFAULT_URL+"/"+barcode)
                .characterEncoding(StandardCharsets.UTF_8)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.description").value("ACHOC PO NESCAU 800G"))
            .andExpect(jsonPath("$.sequenceCode").value(29250))
            .andExpect(jsonPath("$.barcode").value(barcode))
            .andExpect(jsonPath("$._links.prices.href").value("http://localhost/api/prices?barcode=7891000055120"))
            .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/products/7891000055120"));
        }

        @Test
        @DisplayName("GET "+DEFAULT_URL+"/7891000055129 - 404 NOT FOUND")
        void should_return_an_error_message_with_404() throws Exception {
            final String nonExistingBarcode = "7891000055129";

            mockMvc.perform(get(DEFAULT_URL+"/"+nonExistingBarcode)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.ALL)
            )
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andExpect(content().string("Product not found"));
        }

        @Test
        @DisplayName("GET "+DEFAULT_URL+"/7890foobar - 400 BAD REQUEST")
        void should_return_violations_with_400() throws Exception {
            final String violatedBarcode = "7890foobar";

            mockMvc.perform(get(DEFAULT_URL+"/"+violatedBarcode)
                .accept(MediaType.ALL)
            )
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

}
