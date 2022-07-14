package com.api.repository;

import com.api.entity.Price;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PriceRepositoryIT {

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void should_return_all_latest_prices_with_their_products_findAllLatestPrice() {
        final List<Price> latestPrices = priceRepository.findAllLatestPrice();

        assertThat(latestPrices).hasSize(11);
        assertThat(latestPrices).allSatisfy(price -> assertThat(price.getProduct()).isNotNull());
        assertThat(latestPrices).extracting("value", BigDecimal.class)
            .containsExactly(
                new BigDecimal("12.70"),
                new BigDecimal("4.06"),
                new BigDecimal("18.00"),
                new BigDecimal("5.29"),
                new BigDecimal("3.75"),
                new BigDecimal("9.74"),
                new BigDecimal("11.30"),
                new BigDecimal("5.65"),
                new BigDecimal("3.50"),
                new BigDecimal("6.49"),
                new BigDecimal("8.49")
            );

    }

    @Test
    void should_return_all_prices_with_their_products_findAll() {
        final List<Price> prices = priceRepository.findAll();

        assertThat(prices).isNotNull();
        assertThat(prices).hasSize(66);
        assertThat(prices).allSatisfy(price -> assertThat(price.getProduct()).isNotNull());
    }

    @Test
    void should_return_all_prices_for_a_given_product_barcode_findAllByProductBarcode() {
        final List<Price> prices = priceRepository.findAllByProductBarcode("7891000055120", PageRequest.ofSize(10));

        assertThat(prices).isNotNull();
        assertThat(prices).hasSize(10);
        assertThat(prices).allSatisfy(price -> assertThat(price).isNotNull());
    }

    @Test
    void should_return_all_prices_by_a_product_id_list_findAllByProductId() {
        final List<UUID> UUIDList = List.of(
            UUID.fromString("3bf5543e-923d-46ff-ae36-127575fd30f8"),
            UUID.fromString("f3a9f940-9c07-4986-a655-8b91119dae8a"),
            UUID.fromString("04f3dfdd-c811-4cc7-8e82-62d8406ad32c"),
            UUID.fromString("f346cc85-bb05-4a3a-baf7-28d13d07f381")
        );

        final List<Price> fetchedPrices = priceRepository.findAllByProductId(UUIDList);

        assertThat(fetchedPrices).hasSize(29);
    }
}
