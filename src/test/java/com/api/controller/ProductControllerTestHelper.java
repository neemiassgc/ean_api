package com.api.controller;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

final class ProductControllerTestHelper {

    static MockMvc mockMvc;

    private ProductControllerTestHelper() {}

    private static MockHttpServletRequestBuilder setupRequestHeaders(final MockHttpServletRequestBuilder mockHttpServletRequestBuilder) {
        return mockHttpServletRequestBuilder
            .accept(MediaType.ALL_VALUE)
            .characterEncoding(StandardCharsets.UTF_8);
    }

    static ResultActions makeRequestByBarcode(final String barcode) throws Exception {
        return mockMvc.perform(setupRequestHeaders(get("/api/products/"+barcode)));
    }

    static ResultActions makeRequestByBarcodeWithPage(final String barcode, final String page) throws Exception {
        return mockMvc.perform(setupRequestHeaders(get("/api/products/"+barcode+"?page="+page)));
    }

    static ResultActions makeRequestWithPage(final String page) throws Exception {
        return mockMvc.perform(setupRequestHeaders(get("/api/products?pag="+page)));
    }

    static ResultActions makeRequest() throws Exception {
        return mockMvc.perform(setupRequestHeaders(get("/api/products")));
    }

    static String[] concatWithUrl(final String url, final String... values) {
        return Stream.of(values)
            .map(value -> url+value)
            .toArray(String[]::new);
    }
}
