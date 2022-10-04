package com.api.service;

import com.api.entity.Price;
import com.api.service.interfaces.PriceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest
@Transactional(readOnly = true)
public class PriceServiceIT {

    @Autowired
    private PriceService priceService;

    @Nested
    final class FindByIdTest {

        @Test
        @DisplayName("Should return a price")
        void when_id_exist_the_should_return_a_price() {
            final UUID existentUuid = UUID.fromString("9423f8be-2a4f-4baa-b457-6a904bf633f0");

            final Price actualPrice = priceService.findById(existentUuid);

            assertThat(actualPrice).isNotNull();
            assertThat(actualPrice.getValue()).isEqualTo(new BigDecimal("12.70"));
        }

        @Test
        @DisplayName("Should throw ResponseStatusException NOT FOUND")
        void when_id_does_not_exist_then_should_throw_an_exception() {
            final UUID nonExisting = UUID.fromString("e236e904-49f0-41b0-b3aa-c9f582f38fc1");

            final Throwable actualThrowable = catchThrowable(() -> priceService.findById(nonExisting));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(ResponseStatusException.class);
            assertThat((ResponseStatusException) actualThrowable).satisfies(exception -> {
                assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                assertThat(exception.getReason()).isEqualTo("Price not found");
            });
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

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(ResponseStatusException.class);
            assertThat((ResponseStatusException) actualThrowable).satisfies(exception -> {
                assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                assertThat(exception.getReason()).isEqualTo("Product not found");
            });
        }
    }
}