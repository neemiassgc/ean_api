package com.api.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.stream.Collectors;

final class ProductExternalServiceTestTools {

    static <T> String getGenericHtmlContent(final T t) {
        final InputStreamReader isr = new InputStreamReader(
            Objects.requireNonNull(t.getClass().getClassLoader().getResourceAsStream("html_for_test.html"))
        );

        try (BufferedReader br = new BufferedReader(isr)) {
            return br.lines().collect(Collectors.joining("\n"));
        }
        catch (IOException ignored) {
            return "";
        }
    }
}
