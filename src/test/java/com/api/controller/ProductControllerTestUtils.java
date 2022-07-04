package com.api.controller;

import static com.api.projection.Projection.*;

import com.api.projection.Projection;
import com.api.projection.ProjectionFactory;
import org.assertj.core.util.Arrays;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.AbstractList;
import java.util.List;

class ProductControllerTestUtils {

    @SuppressWarnings("unchecked")
    static <P extends ProductBase> P getNewProduct(final String... values) {
        if (Arrays.isNullOrEmpty(values)) {
            return (P) ProjectionFactory.productWithLatestPriceBuilder()
                .barcode("7891962057620")
                .description("PAO 400G")
                .sequenceCode(134262)
                .latestPrice(new PriceWithInstant(new BigDecimal("2.6"), Instant.now().minus(1, ChronoUnit.DAYS)))
                .build();
        }

        return (P) ProjectionFactory.productWithManyPricesBuilder()
            .barcode("7891962057620")
            .description("PAO 400G")
            .sequenceCode(134262)
            .prices(
                new AbstractList<>() {
                    @Override
                    public PriceWithInstant get(int index) {
                        return new PriceWithInstant(new BigDecimal(values[index]), Instant.now().minus(index + 1, ChronoUnit.DAYS));
                    }

                    @Override
                    public int size() {
                        return values.length;
                    }
                }
            )
            .build();
    }

    static List<ProductWithManyPrices> getProductList() {
        return List.of(
            getNewProduct("5.45", "3.67", "8.5"),
            getNewProduct("5.45", "3.67", "8.5"),
            getNewProduct("5.45", "3.67", "8.5")
        );
    }

    static Paged<List<ProductWithManyPrices>> getPagedProductList() {
        Page<?> page = new PageImpl<>(List.of(1, 2, 3), PageRequest.of(0, 3), 3);
        return ProjectionFactory.paged(page, getProductList());
    }
}
