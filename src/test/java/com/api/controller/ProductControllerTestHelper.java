package com.api.controller;

import com.api.entity.Product;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
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
        final String[] valuesToReturn = new String[values.length];

        for (int i = 0; i < values.length; i++)
            valuesToReturn[i] = url+values[i];

        return valuesToReturn;
    }

    static ResultActions makeRequestWithPageAndContains(final String page, final String contains) throws Exception {
        return mockMvc.perform(setupRequestHeaders(get(String.format("%s?pag=%s&contains=%s", URL, page, contains))));
    }

    static List<Product> filterByContaining(final List<Product> productsToFilter, final String keyword) {
        return productsToFilter
            .stream()
            .filter(product -> product.getDescription().contains(keyword))
            .collect(Collectors.toList());
    }

    static List<Product> filterByStartingWith(final List<Product> productsToFilter, final String keyword) {
        return productsToFilter
            .stream()
            .filter(product -> product.getDescription().startsWith(keyword))
            .collect(Collectors.toList());
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
            .build(),
        Product.builder()
            .description("BISC ROSQ MARILAN 350G INT")
            .barcode("7896003737257")
            .sequenceCode(127635)
            .build(),
        Product.builder()
            .description("LEITE PO NINHO 400G INTEG")
            .barcode("7891000000427")
            .sequenceCode(892)
            .build(),
        Product.builder()
            .description("LIMP M.USO OMO 500ML DESINF HERBAL")
            .barcode("7891150080850")
            .sequenceCode(141947)
            .build(),
        Product.builder()
            .description("MAIONESE QUERO 210G TP")
            .barcode("7896102513714")
            .sequenceCode(87689)
            .build(),
        Product.builder()
            .description("MILHO VDE PREDILECTA 170G LT")
            .barcode("7896292340503")
            .sequenceCode(134049)
            .build(),
        Product.builder()
            .description("OLEO MILHO LIZA 900ML")
            .barcode("7896036090619")
            .sequenceCode(5648)
            .build(),
        Product.builder()
            .description("PAP ALUMINIO WYDA 30X7.5")
            .barcode("7898930672441")
            .sequenceCode(30881)
            .build(),
        Product.builder()
            .description("PAP HIG F.D NEVE C8 COMPACTO NEUT")
            .barcode("7891172422379")
            .sequenceCode(25336)
            .build(),
        Product.builder()
            .description("REFRIG ANTARCT 600ML PET GUARANA")
            .barcode("7891991002646")
            .sequenceCode(6367)
            .build(),
        Product.builder()
            .description("SAL MARINHO LEBRE 500G GOURMET")
            .barcode("7896110195162")
            .sequenceCode(128177)
            .build(),
        Product.builder()
            .description("VINAGRE CASTELO 500ML VD FRUTA MACA")
            .barcode("7896048285539")
            .sequenceCode(125017)
            .build()
    );
}
