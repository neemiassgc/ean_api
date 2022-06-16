package com.api.pojo;

import com.api.projection.Projection;
import lombok.Builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.stream.Collectors;

public final class DomainUtils {

    private DomainUtils() {}

    public static BigDecimal parsePrice(final String priceInput) {
        return new BigDecimal((priceInput.replace(",", ".")));
    }

    @Builder(builderMethodName = "productWithLatestPriceBuilder")
    public static Projection.ProductWithLatestPrice productWithLatestPrice(
        final String description,
        final String barcode,
        final Integer sequenceCode,
        final BigDecimal latestPrice
    ) {
        return new Projection.ProductWithLatestPrice() {
            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public String getBarcode() {
                return barcode;
            }

            @Override
            public Integer getSequenceCode() {
                return sequenceCode;
            }

            @Override
            public BigDecimal getLatestPrice() {
                return latestPrice;
            }
        };
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
