package com.api.service;

import com.api.projection.SimpleProductWithStatus;
import com.api.repository.PriceRepository;
import com.api.repository.ProductRepository;
import com.api.service.interfaces.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional(readOnly = true)
public class ProductServiceIT {

    @Autowired
    private ProductService productServiceUnderTest;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PriceRepository priceRepository;

    @Nested
    class GetByBarcodeAndSaveIfNecessaryTest {

        @Test
        @DisplayName("Should return a product from db")
        void when_a_product_exist_in_db_then_should_return_it() {
            final String targetBarcode = "7896004004501";
            final SimpleProductWithStatus actualSimpleProductWithStatus =
                productServiceUnderTest.getByBarcodeAndSaveIfNecessary(targetBarcode);

            assertThat(actualSimpleProductWithStatus).isNotNull();
            assertThat(actualSimpleProductWithStatus.getHttpStatus()).isEqualTo(HttpStatus.OK);
            assertThat(actualSimpleProductWithStatus.getSimpleProduct()).satisfies(simpleProduct -> {
                assertThat(simpleProduct.getDescription()).isEqualTo("CEREAL BARRA KELLOGGS 60G SUCRILHOS CHOC");
                assertThat(simpleProduct.getBarcode()).isEqualTo("7896004004501");
                assertThat(simpleProduct.getSequenceCode()).isEqualTo(105711);
            });
        }
    }
}