package com.api.pojo;

import lombok.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.stream.Collectors;

public final class DomainUtils {

    private DomainUtils() {}

    public static Pageable parsePage(@NonNull final String value, @NonNull Sort sort) {
        final String[] split = value.split("-");
        return PageRequest.of(
            Integer.parseInt(split[0]),
            Integer.parseInt(split[1]),
            sort
        );
    }

    public static BigDecimal parsePrice(final String priceInput) {
        return new BigDecimal((priceInput.replace(",", ".")));
    }

    public static String readFromInputStream(final InputStream inputStream) throws IOException {
        try (final InputStreamReader isr = new InputStreamReader(inputStream)) {
            try (final BufferedReader br = new BufferedReader(isr)) {
                return br.lines().collect(Collectors.joining(""));
            }
        }
    }

    public static <O> O requireNonNull(final O object, final RuntimeException runtimeException) {
        if (object == null) {
            throw runtimeException;
        }
        return object;
    }
}
