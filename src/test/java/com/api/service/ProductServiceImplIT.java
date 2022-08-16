package com.api.service;

import com.api.entity.Product;
import com.api.repository.PriceRepository;
import com.api.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest
@Transactional(readOnly = true)
public class ProductServiceImplIT {

    @Autowired
    private ProductServiceImpl productServiceImplUnderTest;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PriceRepository priceRepository;

    @Test
    public void should_return_a_product_from_db() {
        final String targetBarcode = "7897534852624";
        final Product product =
            productServiceImplUnderTest.getByBarcode(targetBarcode);

        assertThat(product).isNotNull();
        assertThat(product.getPrices()).hasSize(4);
        assertThat(product.getDescription()).isEqualTo("ALCOOL HIG AZULIM 50");
        assertThat(product.getBarcode()).isEqualTo("7897534852624");
        assertThat(product.getId()).isEqualTo(UUID.fromString("7e49cbbf-0d4b-4a67-b108-346bef1c961f"));
    }

    @Test
    @Transactional
    public void should_return_a_product_from_the_external_service() {
        final String targetBarcode = "7892840819507";
        final Product product =
            productServiceImplUnderTest.getByBarcode(targetBarcode);

        assertThat(product).isNotNull();
        assertThat(product.getDescription()).isEqualTo("ACHOC PO TODDY 370G");
        assertThat(product.getBarcode()).isEqualTo("7892840819507");
        assertThat(product.getId()).isNotNull();
        assertThat(productRepository.count()).isEqualTo(12);
        assertThat(priceRepository.count()).isEqualTo(67);
    }

    @Test
    @DisplayName("When a product does not exist then throws an exception")
    public void should_return_throw_an_exception() {
        final String targetBarcode = "1391840815511";
        final Runnable exceptional = () ->
            productServiceImplUnderTest.getByBarcode(targetBarcode);

        final Throwable actualThrowable = catchThrowable(exceptional::run);

        assertThat(actualThrowable).isNotNull();
        assertThat(actualThrowable).isInstanceOf(ResponseStatusException.class);
        assertThat((ResponseStatusException)actualThrowable).satisfies(exception -> {
            assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(exception.getReason()).isEqualTo("Product not found");
        });
    }
}