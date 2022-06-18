package com.api.pojo;

import static com.api.projection.Projection.*;

import lombok.Builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public final class DomainUtils {

    private DomainUtils() {}

    public static BigDecimal parsePrice(final String priceInput) {
        return new BigDecimal((priceInput.replace(",", ".")));
    }

    @Builder(builderMethodName = "productWithManyPricesBuilder")
    public static ProductBase productWithManyPrices(
        final String description,
        final String barcode,
        final Integer sequenceCode,
        final List<PriceWithInstant> prices
    ) {
        return new ProductWithManyPrices() {
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
            public List<PriceWithInstant> getPrices() {
                return prices;
            }
        };
    }

    @Builder(builderMethodName = "productWithLatestPriceBuilder")
    public static ProductBase productWithLatestPrice(
        final String description,
        final String barcode,
        final Integer sequenceCode,
        final PriceWithInstant latestPrice
    ) {
        return new ProductWithLatestPrice() {
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
            public PriceWithInstant getLatestPrice() {
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
