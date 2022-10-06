package com.api.service;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.projection.SimpleProductWithStatus;
import com.api.repository.PriceRepository;
import com.api.repository.ProductRepository;
import com.api.service.interfaces.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest
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

        @Test
        @DisplayName("Should return a product from an external api and persist it")
        @Transactional
        void when_a_product_does_not_exist_in_db_then_should_return_from_an_external_api_and_persist_it() {
            final String targetBarcode = "7894321724027";
            final SimpleProductWithStatus actualSimpleProductWithStatus =
                productServiceUnderTest.getByBarcodeAndSaveIfNecessary(targetBarcode);

            assertThat(actualSimpleProductWithStatus).isNotNull();
            assertThat(actualSimpleProductWithStatus.getHttpStatus()).isEqualTo(HttpStatus.CREATED);
            assertThat(actualSimpleProductWithStatus.getSimpleProduct()).satisfies(simpleProduct -> {
                assertThat(simpleProduct.getDescription()).isEqualTo("ACHOC LQ TODDY");
                assertThat(simpleProduct.getBarcode()).isEqualTo("7894321724027");
                assertThat(simpleProduct.getSequenceCode()).isEqualTo(5418);
            });
            assertThat(productRepository.count()).isEqualTo(12);
            assertThat(priceRepository.count()).isEqualTo(67);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException NOT FOUND")
        void when_a_product_does_not_exist_anywhere_then_should_throw_an_exception() {
            final String nonExistentBarcode = "7894321724039";

            final Throwable actualThrowable =
                catchThrowable(() -> productServiceUnderTest.getByBarcodeAndSaveIfNecessary(nonExistentBarcode));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(ResponseStatusException.class);
            assertThat((ResponseStatusException) actualThrowable).satisfies(exception -> {
                assertThat(exception.getReason()).isEqualTo("Product not found");
                assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            });
        }
    }

    @Test
    @DisplayName("Should save a new product")
    @Transactional
    @Commit
    void should_save_a_new_product() {
        final Product newProduct = Product.builder()
            .description("Green Powder")
            .barcode("8473019283745")
            .sequenceCode(8374)
            .build()
            .addPrice(new Price(new BigDecimal("9.9")));

        productServiceUnderTest.save(newProduct);

        assertThat(productRepository.count()).isEqualTo(12);
        assertThat(priceRepository.count()).isEqualTo(67);
    }

    @Nested
    class FindAllTest {

        @Test
        @DisplayName("Should return all products with its latest price")
        void should_return_all_products_with_its_latest_price() {
            final List<Product> actualProductList =
                productServiceUnderTest.findAllWithLatestPrice();

            assertThat(actualProductList).hasSize(11);
            assertThat(actualProductList).extracting(Product::getPrices)
                .allMatch(prices -> prices.size() == 1);
        }

        @Test
        @DisplayName("Should return all products ordered by sequence code desc")
        @Transactional
        void should_return_all_products_ordered_by_sequence_code_desc() {
            final Sort orderBySequenceCodeDesc = Sort.by("sequenceCode").descending();
            final List<Product> actualProducts =
                productServiceUnderTest.findAll(orderBySequenceCodeDesc);

            assertThat(actualProducts).hasSize(11);
            assertThat(actualProducts).flatExtracting(Product::getPrices).hasSize(66);
            assertThat(actualProducts)
                .extracting(Product::getSequenceCode)
                .containsExactly(142862, 137513, 134262, 120983, 113249, 105711, 93556, 29250, 9785, 2909, 1184);
        }
    }
}