package com.api.service;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.repository.PriceRepository;
import org.apache.tomcat.util.digester.ObjectCreateRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Supplier;

import static java.time.Month.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.*;

final class PriceServiceImplTest {

    private PriceRepository priceRepositoryMock;
    private PriceServiceImpl priceServiceUnderTest;
    private CacheManager<Price, UUID> priceCacheManager;

    @BeforeEach
    void setup() {
        priceRepositoryMock = mock(PriceRepository.class);
        priceCacheManager = mock(CacheManager.class);
        priceServiceUnderTest = new PriceServiceImpl(priceRepositoryMock, priceCacheManager);
    }

    @Nested
    class FindByIdTest {

        @Test
        @DisplayName("Should throw NullPointerException")
        void when_id_is_null_then_throw_an_exception() {
            final Throwable actualThrowable = catchThrowable(() -> priceServiceUnderTest.findById(null));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(NullPointerException.class);

            verifyNoInteractions(priceRepositoryMock, priceCacheManager);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException with NOT FOUND EXCEPTION")
        void when_id_does_not_exist_then_should_throw_an_exception() {
            final UUID nonExistentId = UUID.fromString("4843ca41-2532-4247-bae4-16e61b8108cc");
            given(priceRepositoryMock.findById(eq(nonExistentId))).willReturn(Optional.empty());
            given(priceCacheManager.sync(eq(nonExistentId.toString()), any(Supplier.class)))
                .willAnswer(invocation -> {
                    invocation.getArgument(1, Supplier.class).get();
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Price not found");
                });
            given(priceCacheManager.containsKey(eq(nonExistentId.toString()))).willReturn(false);

            final Throwable actualThrowable = catchThrowable(() -> priceServiceUnderTest.findById(nonExistentId));
            final boolean isCached = priceCacheManager.containsKey(nonExistentId.toString());

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(ResponseStatusException.class);
            assertThat((ResponseStatusException) actualThrowable).satisfies(exception -> {
                assertThat(exception.getReason()).isEqualTo("Price not found");
                assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            });
            assertThat(isCached).isFalse();

            verify(priceRepositoryMock, times(1)).findById(eq(nonExistentId));
            verify(priceCacheManager, times(1)).sync(eq(nonExistentId.toString()), any(Supplier.class));
            verify(priceCacheManager, times(1)).containsKey(eq(nonExistentId.toString()));
            verifyNoMoreInteractions(priceRepositoryMock, priceCacheManager);
        }

        @Test
        @DisplayName("Should return a price successfully")
        void when_id_exists_then_should_return_a_price_successfully() {
            final UUID existingId = UUID.fromString("5b17f3d7-5fd7-4564-a994-23613d993a57");
            final Price expectedPrice = Resources.LIST_OF_PRICES.get(0);
            given(priceRepositoryMock.findById(eq(existingId))).willReturn(Optional.of(expectedPrice));
            given(priceCacheManager.sync(eq(existingId.toString()), any(Supplier.class)))
                .willAnswer(invocation -> Optional.of(invocation.getArgument(1, Supplier.class).get()));
            given(priceCacheManager.containsKey(eq(existingId.toString()))).willReturn(true);

            final Price actualPrice = priceServiceUnderTest.findById(existingId);
            final boolean isCached = priceCacheManager.containsKey(existingId.toString());

            assertThat(actualPrice).isNotNull();
            assertThat(actualPrice).isEqualTo(expectedPrice);
            assertThat(isCached).isTrue();

            verify(priceRepositoryMock, times(1)).findById(eq(existingId));
            verify(priceCacheManager, times(1)).containsKey(eq(existingId.toString()));
            verify(priceCacheManager, times(1)).sync(eq(existingId.toString()), any(Supplier.class));
            verifyNoMoreInteractions(priceRepositoryMock, priceCacheManager);
        }
    }

    @Nested
    class FindByProductBarcodeTest {

        private static final String BARCODE = "7891000055120";

        @Test
        @DisplayName("Should throw NullPointerException")
        void when_barcode_is_null_then_throw_an_exception() {
            final Sort orderByCreationDateDesc = Sort.by("creationDate").descending();
            final Throwable actualThrowable =
                catchThrowable(() -> priceServiceUnderTest.findByProductBarcode(null, orderByCreationDateDesc));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(NullPointerException.class);

            verifyNoInteractions(priceRepositoryMock, priceCacheManager);
        }

        @Test
        @DisplayName("Should throw NullPointerException")
        void when_sort_is_null_then_throw_an_exception() {
            final Throwable actualThrowable =
                catchThrowable(() -> priceServiceUnderTest.findByProductBarcode(BARCODE, (Sort) null));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(NullPointerException.class);

            verifyNoInteractions(priceRepositoryMock, priceCacheManager);
        }

        @Test
        @DisplayName("Should return prices ordered by instant desc")
        void if_the_product_barcode_exist_then_should_return_its_price_ordered_by_instant_desc() {
            final Sort orderByInstantDesc = Sort.by("instant").descending();
            final List<Price> orderedPrices = new ArrayList<>(Resources.LIST_OF_PRICES);
            orderedPrices.sort(Resources.ORDER_BY_INSTANT_DESC);
            given(priceRepositoryMock.findByProductBarcode(eq(BARCODE), eq(orderByInstantDesc)))
                .willReturn(orderedPrices);
            given(priceCacheManager.sync(eq(BARCODE+orderByInstantDesc), any(Supplier.class)))
                .willAnswer(invocation -> Optional.of(invocation.getArgument(1, Supplier.class).get()));
            given(priceCacheManager.containsKey(eq(BARCODE+orderByInstantDesc))).willReturn(true);

            final List<Price> actualPrices = priceServiceUnderTest.findByProductBarcode(BARCODE, orderByInstantDesc);
            final boolean cached = priceCacheManager.containsKey(BARCODE+orderByInstantDesc);

            assertThat(actualPrices).isNotNull();
            assertThat(actualPrices).hasSize(5);
            // Checking ordering
            assertThat(actualPrices)
                .extracting(Price::getInstant)
                .map(Resources::extractMonthFromInstant)
                .containsExactly(MAY, APRIL, MARCH, FEBRUARY, JANUARY);
            assertThat(cached).isTrue();

            verify(priceRepositoryMock, times(1)).findByProductBarcode(eq(BARCODE), eq(orderByInstantDesc));
            verify(priceCacheManager, times(1)).sync(eq(BARCODE+orderByInstantDesc), any(Supplier.class));
            verify(priceCacheManager, times(1)).containsKey(eq(BARCODE+orderByInstantDesc));
            verifyNoMoreInteractions(priceRepositoryMock, priceCacheManager);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException")
        void when_product_barcode_does_not_exist_then_should_throw_an_exception() {
            final String nonExistentBarcode = "3817304916283";
            final Sort orderByInstantDesc = Sort.by("instant").descending();
            final String key = nonExistentBarcode+orderByInstantDesc;
            given(priceRepositoryMock.findByProductBarcode(eq(nonExistentBarcode), eq(orderByInstantDesc)))
                .willReturn(Collections.emptyList());
            given(priceCacheManager.sync(eq(key), any(Supplier.class)))
                .willAnswer(invocation -> {
                    invocation.getArgument(1, Supplier.class).get();
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Price not found");
                });
            given(priceCacheManager.containsKey(eq(key))).willReturn(false);

            final Throwable actualThrowable = catchThrowable(() ->
                    priceServiceUnderTest.findByProductBarcode(nonExistentBarcode, orderByInstantDesc));
            final boolean cached = priceCacheManager.containsKey(key);

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(ResponseStatusException.class);
            assertThat((ResponseStatusException) actualThrowable).satisfies(exception -> {
                assertThat(exception.getReason()).isEqualTo("Price not found");
                assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            });
            assertThat(cached).isFalse();

            verify(priceRepositoryMock, times(1)).findByProductBarcode(eq(nonExistentBarcode), eq(orderByInstantDesc));
            verify(priceCacheManager, times(1)).sync(eq(key), any(Supplier.class));
            verify(priceCacheManager, times(1)).containsKey(eq(key));
            verifyNoMoreInteractions(priceRepositoryMock, priceCacheManager);
        }

        @Test
        @DisplayName("Should return only the first three prices")
        void given_a_pageable_then_should_return_only_the_first_three_prices() {
            final Sort orderByInstantDesc = Sort.by("instant").descending();
            final Pageable theFirstThreePrices = PageRequest.of(0, 3).withSort(orderByInstantDesc);
            final String key = BARCODE+"-pag=0-3";
            final List<Price> expectedPrices = new ArrayList<>(Resources.LIST_OF_PRICES.subList(0, 3));
            expectedPrices.sort(Resources.ORDER_BY_INSTANT_DESC);
            given(priceRepositoryMock.findByProductBarcode(eq(BARCODE), eq(theFirstThreePrices)))
                .willReturn(expectedPrices);
            given(priceCacheManager.sync(eq(key), any(Supplier.class)))
                .willAnswer(invocation -> Optional.of(invocation.getArgument(1, Supplier.class).get()));
            given(priceCacheManager.containsKey(eq(key))).willReturn(true);

            final List<Price> actualPrices = priceServiceUnderTest.findByProductBarcode(BARCODE, theFirstThreePrices);
            final boolean cached = priceCacheManager.containsKey(key);

            assertThat(actualPrices).hasSize(3);
            // Checking ordering
            assertThat(actualPrices)
                .extracting(Price::getInstant)
                .map(Resources::extractMonthFromInstant)
                .containsExactly(APRIL, FEBRUARY, JANUARY);

            verify(priceRepositoryMock, times(1)).findByProductBarcode(eq(BARCODE), eq(theFirstThreePrices));
            verify(priceCacheManager, times(1)).sync(eq(key), any(Supplier.class));
            verify(priceCacheManager, times(1)).containsKey(eq(key));
            verifyNoMoreInteractions(priceRepositoryMock, priceCacheManager);
        }
    }

    private static class Resources {

        private final static List<Price> LIST_OF_PRICES = List.of(
            new Price(UUID.fromString("5b17f3d7-5fd7-4564-a994-23613d993a57"), new BigDecimal("13.35"), null, null),
            new Price(UUID.fromString("4d2bd6d8-7cf0-4e4e-b439-4582e63f4526"), new BigDecimal("5.5"), null, null),
            new Price(UUID.fromString("8fc79bdd-f587-4894-9596-7c9298c4d7df"), new BigDecimal("10.09"), null, null),
            new Price(UUID.fromString("4b8bce95-aa20-4917-bfda-2f7ad89b7a91"), new BigDecimal("7.5"), null, null),
            new Price(UUID.fromString("9229c9be-8af1-4e82-a0db-5e7f16388171"), new BigDecimal("2.47"), null, null)
        );

        private static Month extractMonthFromInstant(final Instant instant) {
            return instant.atOffset(ZoneOffset.UTC).getMonth();
        }

        private final static Comparator<Price> ORDER_BY_INSTANT_DESC =
            Comparator.comparing(Price::getInstant).reversed();

        private final static Product PRODUCT = Product.builder()
            .description("ACHOC PO NESCAU 800G")
            .barcode("7891000055120")
            .sequenceCode(29250)
            .build();

        static {
            LIST_OF_PRICES.forEach(PRODUCT::addPrice);

            final Instant january = LocalDateTime.of(2022, JANUARY, 1, 12, 0).toInstant(ZoneOffset.UTC);
            final Instant february = LocalDateTime.of(2022, FEBRUARY, 1, 12, 0).toInstant(ZoneOffset.UTC);
            final Instant march = LocalDateTime.of(2022, MARCH, 1, 12, 0).toInstant(ZoneOffset.UTC);
            final Instant april = LocalDateTime.of(2022, APRIL, 1, 12, 0).toInstant(ZoneOffset.UTC);
            final Instant may = LocalDateTime.of(2022, MAY, 1, 12, 0).toInstant(ZoneOffset.UTC);

            LIST_OF_PRICES.get(1).setInstant(january);
            LIST_OF_PRICES.get(0).setInstant(february);
            LIST_OF_PRICES.get(4).setInstant(march);
            LIST_OF_PRICES.get(2).setInstant(april);
            LIST_OF_PRICES.get(3).setInstant(may);
        }
    }
}
