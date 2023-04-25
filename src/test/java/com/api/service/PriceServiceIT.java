package com.api.service;

import com.api.entity.Price;
import com.api.service.interfaces.PriceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest
@Transactional(readOnly = true)
public class PriceServiceIT {

    @Autowired
    private PriceService priceService;

    @Autowired
    private CacheManager<Price, UUID> priceCacheManager;

    @Nested
    final class FindByIdTest {

        @Test
        @DisplayName("Should return a price")
        void when_id_exist_then_should_return_a_price() {
            final UUID existentUuid = UUID.fromString("9423f8be-2a4f-4baa-b457-6a904bf633f0");

            final Price actualPrice = priceService.findById(existentUuid);
            final boolean cached = priceCacheManager.containsKey(existentUuid.toString());

            assertThat(actualPrice).isNotNull();
            assertThat(actualPrice.getValue()).isEqualTo(new BigDecimal("12.70"));
            assertThat(cached).isTrue();
        }

        @Test
        @DisplayName("Should throw ResponseStatusException NOT FOUND")
        void when_id_does_not_exist_then_should_throw_an_exception() {
            final UUID nonExisting = UUID.fromString("e236e904-49f0-41b0-b3aa-c9f582f38fc1");

            final Throwable actualThrowable = catchThrowable(() -> priceService.findById(nonExisting));
            final boolean cached = priceCacheManager.containsKey(nonExisting.toString());

            checkResponseStatusExceptionWithMessage(actualThrowable, "Price not found");
            assertThat(cached).isFalse();
        }
    }

    @Nested
    final class FindByProductBarcodeTest {

        private final String BARCODE = "7891000055120";
        private final Sort ORDER_BY_INSTANT_DESC = Sort.by("instant").descending();

        @Test
        @DisplayName("Should throw ResponseStatusException NOT FOUND")
        void when_product_does_not_exist_then_should_throw_an_exception() {
            final String nonExistingBarcode = "7891000055121";

            final Throwable actualThrowable =
                catchThrowable(() -> priceService.findByProductBarcode(nonExistingBarcode, ORDER_BY_INSTANT_DESC));
            final boolean cached = priceCacheManager.containsKey(nonExistingBarcode);


            checkResponseStatusExceptionWithMessage(actualThrowable, "Product not found");
            assertThat(cached).isFalse();
        }

        @Test
        void should_return_prices_ordered_by_its_instant_desc() {
            final List<Price> actualPrices = priceService.findByProductBarcode(BARCODE, ORDER_BY_INSTANT_DESC);
            final boolean cached = priceCacheManager.containsKey(BARCODE+ ORDER_BY_INSTANT_DESC);

            assertThat(actualPrices).hasSize(10);
            checkOrderingWithAllPrices(actualPrices);
            assertThat(cached).isTrue();
        }

        @Test
        void should_return_only_the_first_three_prices() {
            final Pageable theFirstThreePrices = PageRequest.of(0, 3, ORDER_BY_INSTANT_DESC);

            final List<Price> actualPrices = priceService.findByProductBarcode(BARCODE, theFirstThreePrices);
            final boolean cached = priceCacheManager.containsKey(BARCODE+"-pag=0-3");

            assertThat(actualPrices).hasSize(3);
            checkOrderingWithPrices(actualPrices, "12.70", "19.00", "16.50");
            assertThat(cached).isTrue();
        }

        @Test
        @DisplayName("Should return all prices available")
        void when_page_size_is_over_max_page_size_then_should_return_all_prices() {
            final Pageable overMaxPageSize = PageRequest.of(0, 12, ORDER_BY_INSTANT_DESC);

            final List<Price> actualPrices = priceService.findByProductBarcode(BARCODE, overMaxPageSize);
            final boolean cached = priceCacheManager.containsKey(BARCODE+"-pag=0-12");


            assertThat(actualPrices).hasSize(10);
            checkOrderingWithAllPrices(actualPrices);
            assertThat(cached).isTrue();
        }

        @Test
        @DisplayName("Should throw ResponseStatusException")
        void when_page_is_over_max_page_then_should_throw_an_exception() {
            final Pageable overMaxPageSize = PageRequest.of(2, 5, ORDER_BY_INSTANT_DESC);

            final Throwable actualThrowable =
                catchThrowable(() -> priceService.findByProductBarcode(BARCODE, overMaxPageSize));
            final boolean cached = priceCacheManager.containsKey(BARCODE+"-pag=2-5");

            checkResponseStatusExceptionWithMessage(actualThrowable, "Product not found");
            assertThat(cached).isFalse();
        }
    }

    private void checkResponseStatusExceptionWithMessage(final Throwable actualThrowable, final String message) {
        assertThat(actualThrowable).isNotNull();
        assertThat(actualThrowable).isInstanceOf(ResponseStatusException.class);
        assertThat((ResponseStatusException) actualThrowable).satisfies(exception -> {
            assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(exception.getReason()).isEqualTo(message);
        });
    }

    private void checkOrderingWithPrices(final List<Price> actualPrices, final String... expectedPricesInString) {
        assertThat(actualPrices)
            .extracting(Price::getValue)
            .map(BigDecimal::toPlainString)
            .containsExactly(expectedPricesInString);
    }

    private void checkOrderingWithAllPrices(final List<Price> actualPrices) {
        checkOrderingWithPrices(actualPrices, "12.70", "19.00", "16.50", "6.61", "16.80", "9.85", "10.60", "16.10", "12.60", "19.10");
    }
}