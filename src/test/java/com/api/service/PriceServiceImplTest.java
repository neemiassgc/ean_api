package com.api.service;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.repository.PriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.*;

final class PriceServiceImplTest {

    private PriceRepository priceRepositoryMock;
    private PriceServiceImpl priceServiceUnderTest;

    @BeforeEach
    void setup() {
        priceServiceUnderTest = new PriceServiceImpl(priceRepositoryMock = mock(PriceRepository.class));
    }

    @Nested
    class FindByIdTest {

        @Test
        @DisplayName("Should throw NullPointerException")
        void when_id_is_null_then_throw_an_exception() {
            final Throwable actualThrowable = catchThrowable(() -> priceServiceUnderTest.findById(null));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException with NOT FOUND EXCEPTION")
        void when_id_does_not_exist_then_should_throw_an_exception() {
            final UUID nonExistentId = UUID.fromString("4843ca41-2532-4247-bae4-16e61b8108cc");
            given(priceRepositoryMock.findById(eq(nonExistentId))).willReturn(Optional.empty());

            final Throwable actualThrowable = catchThrowable(() -> priceServiceUnderTest.findById(nonExistentId));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(ResponseStatusException.class);
            assertThat((ResponseStatusException) actualThrowable).satisfies(exception -> {
                assertThat(exception.getReason()).isEqualTo("Price not found");
                assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            });

            verify(priceRepositoryMock, times(1)).findById(eq(nonExistentId));
            verify(priceRepositoryMock, only()).findById(eq(nonExistentId));
        }

        @Test
        @DisplayName("Should return a price successfully")
        void when_id_exists_then_should_return_a_price_successfully() {
            final UUID existingId = UUID.fromString("5b17f3d7-5fd7-4564-a994-23613d993a57");
            final Price expectedPrice = Resources.listOfPrices.get(0);
            given(priceRepositoryMock.findById(eq(existingId))).willReturn(Optional.of(expectedPrice));

            final Price actualPrice = priceServiceUnderTest.findById(existingId);

            assertThat(actualPrice).isNotNull();
            assertThat(actualPrice).isEqualTo(expectedPrice);

            verify(priceRepositoryMock, times(1)).findById(eq(existingId));
            verify(priceRepositoryMock, only()).findById(eq(existingId));
        }
    }

    @Nested
    class FindByProductBarcodeTest {

        @Test
        @DisplayName("Should throw NullPointerException")
        void when_barcode_is_null_then_throw_an_exception() {
            final Sort orderByCreationDateDesc = Sort.by("creationDate").descending();
            final Throwable actualThrowable =
                catchThrowable(() -> priceServiceUnderTest.findByProductBarcode(null, orderByCreationDateDesc));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException")
        void when_sort_is_null_then_throw_an_exception() {
            final String targetProductBarcode = "7891000055120";
            final Throwable actualThrowable =
                catchThrowable(() -> priceServiceUnderTest.findByProductBarcode(targetProductBarcode, (Sort) null));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(NullPointerException.class);
        }
    }

    private static class Resources {

        private final static List<Price> listOfPrices = List.of(
            new Price(UUID.fromString("5b17f3d7-5fd7-4564-a994-23613d993a57"), new BigDecimal("13.35"), null, null),
            new Price(UUID.fromString("4d2bd6d8-7cf0-4e4e-b439-4582e63f4526"), new BigDecimal("5.5"), null, null),
            new Price(UUID.fromString("8fc79bdd-f587-4894-9596-7c9298c4d7df"), new BigDecimal("10.09"), null, null),
            new Price(UUID.fromString("4b8bce95-aa20-4917-bfda-2f7ad89b7a91"), new BigDecimal("7.5"), null, null),
            new Price(UUID.fromString("9229c9be-8af1-4e82-a0db-5e7f16388171"), new BigDecimal("2.47"), null, null)
        );

        private final static Comparator<Price> orderByInstantDesc =
            Comparator.comparing(Price::getInstant).reversed();

        private final static Product product = Product.builder()
            .description("ACHOC PO NESCAU 800G")
            .barcode("7891000055120")
            .sequenceCode(29250)
            .build();

        static {
            listOfPrices.forEach(product::addPrice);

            final Instant january = LocalDateTime.of(2022, Month.JANUARY, 1, 12, 0).toInstant(ZoneOffset.UTC);
            final Instant february = LocalDateTime.of(2022, Month.FEBRUARY, 1, 12, 0).toInstant(ZoneOffset.UTC);
            final Instant march = LocalDateTime.of(2022, Month.MARCH, 1, 12, 0).toInstant(ZoneOffset.UTC);
            final Instant april = LocalDateTime.of(2022, Month.APRIL, 1, 12, 0).toInstant(ZoneOffset.UTC);
            final Instant may = LocalDateTime.of(2022, Month.MAY, 1, 12, 0).toInstant(ZoneOffset.UTC);

            listOfPrices.get(0).setInstant(january);
            listOfPrices.get(1).setInstant(february);
            listOfPrices.get(2).setInstant(march);
            listOfPrices.get(3).setInstant(april);
            listOfPrices.get(4).setInstant(may);
        }
    }
}
