package com.api.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static com.api.projection.Projection.ProductWithLatestPrice;
import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductExternalServiceIT {

    @Autowired
    private ProductExternalService productExternalServiceUnderTest;

    @Test
    void should_return_a_product() {
        final String existingBarcode = "7896336014230";
        
        final Optional<ProductWithLatestPrice> optionalProduct =
            productExternalServiceUnderTest.fetchByBarcode(existingBarcode)
                .map(p -> (ProductWithLatestPrice) p);

        assertThat(optionalProduct).isNotNull();
        assertThat(optionalProduct.isPresent()).isTrue();
        assertThat(optionalProduct.get()).extracting("description").isEqualTo("PASTA AMENDOIM FIRST");
        assertThat(optionalProduct.get()).extracting("barcode").isEqualTo(existingBarcode);
        assertThat(optionalProduct.get()).extracting("sequenceCode").isEqualTo(137638);
    }
}