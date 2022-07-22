package com.api.service;

import static com.api.projection.Projection.*;
import static org.assertj.core.api.Assertions.*;

import com.api.repository.PriceRepository;
import com.api.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@Transactional
public class PersistenceServiceIT {

    @Autowired
    private PersistenceService persistenceServiceUnderTest;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PriceRepository priceRepository;

    private final String GLOBAL_BARCODE = "7897534852624";

    @Nested
    class FindProductByBarcodeTest {

        @Test
        @DisplayName("When limit is zero then should return all products from db")
        void should_return_all_products_from_db() {
            final int limit = 0;

            final ProductWithManyPrices actualProduct = persistenceServiceUnderTest.findProductByBarcode(GLOBAL_BARCODE, limit);

            assertThat(actualProduct).isNotNull();
            assertThat(actualProduct).extracting(ProductBase::getDescription).isEqualTo("ALCOOL HIG AZULIM 50");
            assertThat(actualProduct).extracting(ProductBase::getBarcode).isEqualTo("7897534852624");
            assertThat(actualProduct).extracting(ProductBase::getSequenceCode).isEqualTo(137513);
            assertThat(actualProduct.getPrices()).isNotNull();
            assertThat(actualProduct.getPrices()).hasSize(4);
            assertThat(actualProduct.getPrices()).extracting(PriceWithInstant::getValue)
                .containsExactly(
                    new BigDecimal("5.65"), new BigDecimal("9.90"), new BigDecimal("10.75"), new BigDecimal("7.50")
                );
        }

        @Test
        @DisplayName("Should return a few products from drop with a given limit")
        void should_return_a_few_products_from_db() {
            final int limit = 2;

            final ProductWithManyPrices actualProduct = persistenceServiceUnderTest.findProductByBarcode(GLOBAL_BARCODE, limit);

            assertThat(actualProduct).isNotNull();
            assertThat(actualProduct).extracting(ProductBase::getDescription).isEqualTo("ALCOOL HIG AZULIM 50");
            assertThat(actualProduct).extracting(ProductBase::getBarcode).isEqualTo("7897534852624");
            assertThat(actualProduct).extracting(ProductBase::getSequenceCode).isEqualTo(137513);
            assertThat(actualProduct.getPrices()).isNotNull();
            assertThat(actualProduct.getPrices()).hasSize(2);
            assertThat(actualProduct.getPrices()).extracting(PriceWithInstant::getValue)
                .containsExactly(new BigDecimal("5.65"), new BigDecimal("9.90"));

        }

        @Test
        @DisplayName("Should fetch from the external service and save it in the db")
        void should_return_a_product_from_the_external_service() {
            final String barcode = "7891095005178";

            final ProductWithLatestPrice actualProduct = persistenceServiceUnderTest.findProductByBarcode(barcode, 0);
            final long actualProductsCount = productRepository.count();
            final long actualPricesCount = priceRepository.count();

            assertThat(actualProduct).isNotNull();
            assertThat(actualProduct).extracting(ProductBase::getDescription).isEqualTo("AMEND YOKI");
            assertThat(actualProduct).extracting(ProductBase::getBarcode).isEqualTo("7891095005178");
            assertThat(actualProduct).extracting(ProductBase::getSequenceCode).isEqualTo(8769);
            assertThat(actualProduct.getLatestPrice()).isNotNull();
            assertThat(actualProductsCount).isEqualTo(12);
            assertThat(actualPricesCount).isEqualTo(67);
        }

        @Test
        @DisplayName("When no products can be found then should throw an exception")
        void when_no_products_can_be_found_should_throw_an_exception() {
            final String nonExistentBarcode = "134810923434";

            final Throwable actualThrowable = catchThrowable(() -> {
                persistenceServiceUnderTest.findProductByBarcode(nonExistentBarcode, 0);
            });

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOfSatisfying(ResponseStatusException.class, exception -> {
                assertThat(exception.getReason()).isEqualTo("Product not found");
                assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            });
        }
    }

    @DisplayName("Testing findAllProducts method")
    @Test
    void should_return_all_the_products() {
        final List<ProductWithManyPrices> actualProducts = persistenceServiceUnderTest.findAllProducts();

        assertThat(actualProducts).hasSize(11);
        assertThat(actualProducts).flatExtracting(ProductWithManyPrices::getPrices).hasSize(66);

        // verify order of the products
        assertThat(actualProducts).extracting(ProductWithManyPrices::getBarcode)
            .containsExactly(
                "7891000055120", "7897534852624", "7896336010058", "7898279792299", "7896045104482",
                "7891962047560", "7896656800018", "7896004004501", "7891098010575", "7896036093085", "7891962057620"
            );

        // verify order of the prices
        assertThat(actualProducts.get(0).getPrices().stream().mapToDouble(it -> it.getValue().doubleValue()))
            .containsExactly(12.7, 19.0, 16.5, 6.61, 16.8, 9.85, 10.6, 16.1, 12.6, 19.1);

    }

    @Test
    @DisplayName("Should return the first page of three pages - findAllPagedProducts")
    void should_return_the_first_page_of_paged_products() {
        final Paged<List<ProductWithManyPrices>> firstPage = persistenceServiceUnderTest.findAllPagedProducts(PageRequest.ofSize(4));

        assertThat(firstPage).isNotNull();
        assertThat(firstPage.getContent()).hasSize(4);
        assertThat(firstPage.getCurrentPage()).isEqualTo(0);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
        assertThat(firstPage.getNumberOfItems()).isEqualTo(4);
        assertThat(firstPage.getHasNext()).isTrue();

        // testing the order of the products
        assertThat(firstPage.getContent()).extracting("barcode")
            .containsExactly("7891000055120", "7897534852624", "7896336010058", "7898279792299");

        // testing the order of the prices
        assertThat(firstPage.getContent().get(0).getPrices().stream().mapToDouble(it -> it.getValue().doubleValue()))
            .containsExactly(12.7, 19.0, 16.5, 6.61, 16.8, 9.85, 10.6, 16.1, 12.6, 19.1);
    }

    @Test
    @DisplayName("Should return the second page of three pages - findAllPagedProducts")
    void should_return_the_second_page_of_paged_products() {
        final Paged<List<ProductWithManyPrices>> secondPage = persistenceServiceUnderTest.findAllPagedProducts(PageRequest.of(1, 4));

        assertThat(secondPage).isNotNull();
        assertThat(secondPage.getContent()).hasSize(4);
        assertThat(secondPage.getCurrentPage()).isEqualTo(1);
        assertThat(secondPage.getTotalPages()).isEqualTo(3);
        assertThat(secondPage.getNumberOfItems()).isEqualTo(4);
        assertThat(secondPage.getHasNext()).isTrue();

        // Testing the order of the products
        assertThat(secondPage.getContent()).extracting("barcode")
            .containsExactly("7896045104482", "7891962047560", "7896656800018", "7896004004501");

        // testing the order of the prices
        assertThat(secondPage.getContent().get(0).getPrices().stream().mapToDouble(it -> it.getValue().doubleValue()))
            .containsExactly(5.29, 8.48, 7.91, 16.4, 8.58, 6.17, 5.85);

    }

    @Test
    @DisplayName("Should return the last page of three pages - findAllPagedProducts")
    void should_return_the_third_page_of_paged_products() {
        final Paged<List<ProductWithManyPrices>> thirdPage = persistenceServiceUnderTest.findAllPagedProducts(PageRequest.of(2, 4));

        assertThat(thirdPage).isNotNull();
        assertThat(thirdPage.getContent()).hasSize(3);
        assertThat(thirdPage.getCurrentPage()).isEqualTo(2);
        assertThat(thirdPage.getTotalPages()).isEqualTo(3);
        assertThat(thirdPage.getNumberOfItems()).isEqualTo(3);
        assertThat(thirdPage.getHasNext()).isFalse();

        // Testing the order of the products
        assertThat(thirdPage.getContent()).extracting("barcode")
            .containsExactly("7891098010575", "7896036093085", "7891962057620");

        // testing the order of the prices
        assertThat(thirdPage.getContent().get(0).getPrices().stream().mapToDouble(it -> it.getValue().doubleValue()))
            .containsExactly(6.49, 1.49);
    }

    @Test
    @DisplayName("Testing findAllProductsWithLatestPrice method")
    void should_return_all_products_with_latest_price() {
        final List<ProductWithLatestPrice> products = persistenceServiceUnderTest.findAllProductsWithLatestPrice();

        assertThat(products).isNotNull();
        assertThat(products).hasSize(11);

        // Verifying order of the products
        assertThat(products).extracting("barcode")
            .containsExactly(
                "7891000055120", "7897534852624", "7896336010058", "7898279792299",
                "7896045104482", "7891962047560", "7896656800018", "7896004004501",
                "7891098010575", "7896036093085", "7891962057620"
            );
    }
}
