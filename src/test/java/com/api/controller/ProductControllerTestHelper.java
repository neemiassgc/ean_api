package com.api.controller;

import com.api.Resources;
import com.api.component.Constants;
import com.api.component.DomainUtils;
import com.api.entity.Product;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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

    static ResultActions makeRequestWithPageAndStartsWith(final String page, final String startsWith) throws Exception {
        return mockMvc.perform(setupRequestHeaders(get(String.format("%s?pag=%s&starts-with=%s", URL, page, startsWith))));
    }

    static ResultActions makeRequestWithPageAndEndsWith(final String page, final String endsWith) throws Exception {
        return mockMvc.perform(setupRequestHeaders(get(String.format("%s?pag=%s&ends-with=%s", URL, page, endsWith))));
    }

    private static List<Product> filterByDescription(final Predicate<String> predicate) {
        return Resources.PRODUCTS_SAMPLE
            .stream()
            .filter(product -> predicate.test(product.getDescription().toLowerCase()))
            .collect(Collectors.toList());
    }

    static List<Product> filterByContaining(final String keyword) {
        return filterByDescription(description -> description.contains(keyword));
    }

    static List<Product> filterByStartingWith(final String keyword) {
        return filterByDescription(description -> description.startsWith(keyword));
    }

    static List<Product> filterByEndingWith(final String keyword) {
        return filterByDescription(description -> description.endsWith(keyword));
    }

    static Sort getDefaultSorting() {
        return Sort.by("description").ascending();
    }

    static Pageable createPageable(final String pageExpression, final Sort sort) {
        return DomainUtils.parsePage(pageExpression, sort);
    }

    static Page<Product> createPage(final Pageable pageable) {
        return createPage(pageable, Resources.PRODUCTS_SAMPLE);
    }

    private static List<Product> selectProducts(final Pageable pageable, final List<Product> content) {
        final int startIndex = pageable.getPageNumber() * pageable.getPageSize();
        final int endIndex = startIndex + pageable.getPageSize();
        return content.subList(startIndex, Math.min(endIndex, content.size()));
    }

    static Page<Product> createPage(final Pageable pageable, final List<Product> products) {
        final int totalItems = products.size();
        final List<Product> content = selectProducts(pageable, products);
        return new PageImpl<>(content, pageable, totalItems);
    }

    static Page<Product> emptyPage() {
        return new PageImpl<>(Collections.emptyList(), PageRequest.ofSize(5), 0);
    }

    static class ContentTester {

        private ContentTester() {}

        private String nextPageExpression;
        private String[] expectedBarcodeSet;

        static ContentTester builder() {
            return new ContentTester();
        }

        ContentTester withNextPage(final String nextPageExpression) {
            this.nextPageExpression = nextPageExpression;
            return this;
        }

        ContentTester withExpectedBarcodeSet(final String... expectedBarcodeSet) {
            this.expectedBarcodeSet = expectedBarcodeSet;
            return this;
        }

        ResultMatcher[] test() {
            final List<ResultMatcher> resultMatcherList = new ArrayList<>(7);
            resultMatcherList.add(jsonPath("$.content[*].barcode", contains(expectedBarcodeSet)));
            resultMatcherList.add(jsonPath("$.content[*].links[0].rel", everyItem(equalTo("prices"))));
            resultMatcherList.add(jsonPath("$.content[*].links[0].href", contains(concatWithUrl(Constants.PRICES_URL, expectedBarcodeSet))));
            resultMatcherList.add(jsonPath("$.content[*].links[1].rel", everyItem(equalTo("self"))));
            resultMatcherList.add(jsonPath("$.content[*].links[1].href", contains(concatWithUrl(Constants.PRODUCTS_URL+"/", expectedBarcodeSet))));

            if (Objects.nonNull(nextPageExpression)) {
                resultMatcherList.add(jsonPath("$.links[0].rel").value("Next page"));
                resultMatcherList.add(jsonPath("$.links[0].href").value(Constants.PRODUCTS_URL + "?pag=" + nextPageExpression));
            }
            else resultMatcherList.add(jsonPath("$.links").isEmpty());

            return resultMatcherList.toArray(ResultMatcher[]::new);
        }
    }
}
