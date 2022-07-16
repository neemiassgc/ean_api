package com.api.service;

import static com.api.projection.Projection.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
public class PersistenceServiceIT {

    @Autowired
    private PersistenceService persistenceServiceUnderTest;

    @Nested
    class FindProductByBarcodeTest {

        @Test
        @DisplayName("When limit is zero then should return all products from db")
        void should_return_all_products_from_db() {
            final int limit = 0;
            final String barcode = "7897534852624";

            final ProductWithManyPrices actualProduct = persistenceServiceUnderTest.findProductByBarcode(barcode, limit);

            assertThat(actualProduct).isNotNull();
            assertThat(actualProduct).extracting(ProductBase::getDescription).isEqualTo("ALCOOL HIG AZULIM 50");
            assertThat(actualProduct).extracting(ProductBase::getBarcode).isEqualTo("7897534852624");
            assertThat(actualProduct).extracting(ProductBase::getSequenceCode).isEqualTo(137513);
            assertThat(actualProduct.getPrices()).isNotNull();
            assertThat(actualProduct.getPrices()).hasSize(4);
            assertThat(actualProduct.getPrices()).extracting(PriceWithInstant::getValue)
                .containsExactly(
                    new BigDecimal("5.65"), new BigDecimal("9.90"), new BigDecimal("10.75"), new BigDecimal("7.50")
                );
        }
    }
}
