package com.api.repository;

import com.api.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional(readOnly = true)
public class ProductRepositoryIT {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void should_return_a_product_by_barcode() {
        final String barcode = "7896336010058";

        final Optional<Product> optionalProduct =
            productRepository.findByBarcode(barcode);

        assertThat(optionalProduct).isPresent();
        assertThat(optionalProduct.get()).satisfies(productUnderTest -> {
            assertThat(productUnderTest.getDescription()).isEqualTo("AMENDOIM SALG CROKISSIMO 400G PIMENTA");
            assertThat(productUnderTest.getBarcode()).isEqualTo(barcode);
            assertThat(productUnderTest.getSequenceCode()).isEqualTo(120983);
        });
    }
}
