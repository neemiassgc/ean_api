package com.api.controller;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

final class PriceControllerTestHelper {

    static MockMvc mockMvc;

    private PriceControllerTestHelper() {}

    static ResultActions makeRequestWithBarcode(final String barcode) throws Exception {
        return makeRequestWithBarcodeAndLimit(barcode, 0);
    }

    static ResultActions makeRequestWithBarcodeAndLimit(final String barcode, final int limit) throws Exception {
        return mockMvc.perform(
            get("/api/prices").param("barcode", barcode).param("limit", limit+"")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.ALL)
        );
    }

    static ResultActions makeRequestByUuid(final String uuid) throws Exception {
        return mockMvc.perform(
            get("/api/prices/"+uuid)
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.ALL)
        );
    }
}