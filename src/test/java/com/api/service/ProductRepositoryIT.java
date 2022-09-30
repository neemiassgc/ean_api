package com.api.service;

import com.api.entity.Product;
import com.api.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
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

    @Test
    void should_return_all_product_with_their_last_price() {
        final List<Product> productList = productRepository.findAllWithLastPrice();
        final List<BigDecimal> pricesToCheckInOrder = List.of(
            new BigDecimal("12.70"),
            new BigDecimal("5.65"),
            new BigDecimal("4.06"),
            new BigDecimal("18.00"),
            new BigDecimal("5.29"),
            new BigDecimal("3.75"),
            new BigDecimal("9.74"),
            new BigDecimal("11.30"),
            new BigDecimal("6.49"),
            new BigDecimal("3.50"),
            new BigDecimal("8.49")
        );

        final Iterator<BigDecimal> bigDecimalIterator = pricesToCheckInOrder.iterator();

        assertThat(productList).hasSize(11);

        for (final Product productUnderTest : productList) {
            assertThat(productUnderTest.getPrices()).hasSize(1);
            assertThat(productUnderTest.getPrices().get(0).getValue()).isEqualTo(bigDecimalIterator.next());
        }
    }
}
