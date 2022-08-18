package com.api.service;

import com.api.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductExternalServiceImplIT {

    @Autowired
    private ProductExternalServiceImpl productExternalServiceImplUnderTest;

    @Test
    void should_return_a_product() {
        final String existingBarcode = "7896336014230";

        final Optional<Product> optionalProduct =
            productExternalServiceImplUnderTest.fetchByBarcode(existingBarcode);

        assertThat(optionalProduct).isNotNull();
        assertThat(optionalProduct.isPresent()).isTrue();
        assertThat(optionalProduct.get().getDescription()).isEqualTo("PASTA AMENDOIM FIRST");
        assertThat(optionalProduct.get().getBarcode()).isEqualTo(existingBarcode);
        assertThat(optionalProduct.get().getSequenceCode()).isEqualTo(137638);
        assertThat(optionalProduct.get().getPrices()).hasSize(1);
    }

    @Test
    void should_return_an_empty_optional() {
        final String nonExistingBarcode = "7896336014765";

        final Optional<Product> optionalProduct =
            productExternalServiceImplUnderTest.fetchByBarcode(nonExistingBarcode);

        assertThat(optionalProduct).isNotNull();
        assertThat(optionalProduct.isPresent()).isFalse();
    }
}