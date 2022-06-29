package com.api.controller;

import com.api.projection.Projection;
import com.api.projection.ProjectionFactory;
import org.assertj.core.util.Arrays;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.AbstractList;
import java.util.List;

class ProductControllerTestUtils {

    @SuppressWarnings("unchecked")
    static <P extends Projection.ProductBase> P getNewProduct(final String... values) {
        if (Arrays.isNullOrEmpty(values)) {
            return (P) ProjectionFactory.productWithLatestPriceBuilder()
                .barcode("7891962057620")
                .description("PAO 400G")
                .sequenceCode(134262)
                .latestPrice(new Projection.PriceWithInstant(new BigDecimal("2.6"), Instant.now().minus(1, ChronoUnit.DAYS)))
                .build();
        }

        return (P) ProjectionFactory.productWithManyPricesBuilder()
            .barcode("7891962057620")
            .description("PAO 400G")
            .sequenceCode(134262)
            .prices(
                new AbstractList<>() {
                    @Override
                    public Projection.PriceWithInstant get(int index) {
                        return new Projection.PriceWithInstant(new BigDecimal(values[index]), Instant.now().minus(index + 1, ChronoUnit.DAYS));
                    }

                    @Override
                    public int size() {
                        return values.length;
                    }
                }
            )
            .build();
    }

    static List<Projection.ProductWithManyPrices> getProductList() {
        return List.of(
            getNewProduct("5.45", "3.67", "8.5"),
            getNewProduct("5.45", "3.67", "8.5"),
            getNewProduct("5.45", "3.67", "8.5")
        );
    }
}
