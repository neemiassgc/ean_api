package com.api.controller;

import com.api.entity.Product;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

final class ProductControllerTestHelper {

    static MockMvc mockMvc;

    private static final String URL = "/api/products/";

    private ProductControllerTestHelper() {}

    private static MockHttpServletRequestBuilder setupRequestHeaders(final MockHttpServletRequestBuilder mockHttpServletRequestBuilder) {
        return mockHttpServletRequestBuilder
            .accept(MediaType.ALL_VALUE)
            .characterEncoding(StandardCharsets.UTF_8);
    }

    static ResultActions makeRequestByBarcode(final String barcode) throws Exception {
        return mockMvc.perform(setupRequestHeaders(get(URL+barcode)));
    }

    static ResultActions makeRequestWithPage(final String page) throws Exception {
        return mockMvc.perform(setupRequestHeaders(get(URL+"?pag="+page)));
    }

    static ResultActions makeRequest() throws Exception {
        return mockMvc.perform(setupRequestHeaders(get(URL)));
    }

    static String[] concatWithUrl(final String url, final String... values) {
        return Stream.of(values)
            .map(value -> url+value)
            .toArray(String[]::new);
    }

    static ResultActions makeRequestWithPageAndContains(final String page, final String contains) throws Exception {
        return mockMvc.perform(setupRequestHeaders(get(String.format("%s?pag=%s&contains=%s", URL, page, contains))));
    }

    static final List<Product> PRODUCTS_SAMPLE = List.of(
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
            .build(),
        Product.builder()
            .description("BALA GELATINA FINI 500G BURGUER")
            .barcode("78982797922990")
            .sequenceCode(93556)
            .build()
    );
}
