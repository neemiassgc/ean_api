package com.api.service;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.projection.SimpleProductWithStatus;
import com.api.repository.ProductRepository;
import com.api.service.interfaces.ProductExternalService;
import com.api.service.interfaces.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.*;

public class ProductServiceTest {

    private ProductService productServiceUnderTest;
    private ProductRepository productRepositoryMock;
    private ProductExternalService productExternalServiceMock;

    @BeforeEach
    void setup() {
        productExternalServiceMock = mock(ProductExternalService.class);
        productRepositoryMock = mock(ProductRepository.class);
        productServiceUnderTest = new ProductServiceImpl(productRepositoryMock, productExternalServiceMock);
    }

    @Nested
    class GetByBarcodeAndSaveIfNecessaryTest {

        private static final String BARCODE = "7891000055120";

        @Test
        @DisplayName("Should throw NullPointerException")
        void if_barcode_is_null_then_should_throw_an_exception() {
            final Throwable actualThrowable =
                catchThrowable(() -> productServiceUnderTest.getByBarcodeAndSaveIfNecessary(null));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(NullPointerException.class);

            verifyNoInteractions(productRepositoryMock);
        }

        @Test
        @DisplayName("Should return a product from db")
        void when_a_product_exist_in_db_then_should_return_it() {
            final Product expectedProduct = Resources.PRODUCT_LIST.get(0);
            given(productRepositoryMock.findByBarcode(eq(BARCODE)))
                .willReturn(Optional.of(expectedProduct));

            final SimpleProductWithStatus actualSimpleProductWithStatus =
                productServiceUnderTest.getByBarcodeAndSaveIfNecessary(BARCODE);

            checkProductWithHttpStatus(actualSimpleProductWithStatus, HttpStatus.OK);

            verify(productRepositoryMock, times(1)).findByBarcode(eq(BARCODE));
            verify(productRepositoryMock, only()).findByBarcode(eq(BARCODE));
        }

        @Test
        @DisplayName("Should return a product from an external api")
        void when_a_product_does_not_exist_in_db_then_should_return_from_an_external_api() {
            final Product expectedProduct = Resources.PRODUCT_LIST.get(0);
            given(productRepositoryMock.findByBarcode(eq(BARCODE)))
                .willReturn(Optional.empty());
            given(productExternalServiceMock.fetchByBarcode(eq(BARCODE)))
                .willReturn(Optional.of(expectedProduct));
            given(productRepositoryMock.save(eq(expectedProduct)))
                .willAnswer(answer -> answer.getArgument(0, Product.class));

            final SimpleProductWithStatus actualSimpleProductWithStatus =
                productServiceUnderTest.getByBarcodeAndSaveIfNecessary(BARCODE);

            checkProductWithHttpStatus(actualSimpleProductWithStatus, HttpStatus.CREATED);

            verify(productRepositoryMock, times(1)).findByBarcode(eq(BARCODE));
            verify(productExternalServiceMock, times(1)).fetchByBarcode(eq(BARCODE));
            verify(productRepositoryMock, times(1)).save(eq(expectedProduct));
        }

        @Test
        @DisplayName("Should throw an ResponseStatusException | NOT FOUND")
        void when_a_product_is_not_found_then_should_throw_an_exception() {
            final String nonExistentBarcodeAnywhere = "7891000055345";
            given(productRepositoryMock.findByBarcode(eq(nonExistentBarcodeAnywhere)))
                .willReturn(Optional.empty());
            given(productExternalServiceMock.fetchByBarcode(eq(nonExistentBarcodeAnywhere)))
                .willReturn(Optional.empty());

            final Throwable actualThrowable =
                catchThrowable(() -> productServiceUnderTest.getByBarcodeAndSaveIfNecessary(nonExistentBarcodeAnywhere));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(ResponseStatusException.class);
            assertThat((ResponseStatusException) actualThrowable).satisfies(exception -> {
                assertThat(exception.getReason()).isEqualTo("Product not found");
                assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            });

            verify(productRepositoryMock, times(1)).findByBarcode(eq(nonExistentBarcodeAnywhere));
            verify(productExternalServiceMock, times(1)).fetchByBarcode(eq(nonExistentBarcodeAnywhere));
            verifyNoMoreInteractions(productRepositoryMock, productExternalServiceMock);
        }

        private void checkProductWithHttpStatus(final SimpleProductWithStatus simpleProductWithStatus, final HttpStatus httpStatus) {
            assertThat(simpleProductWithStatus).isNotNull();
            assertThat(simpleProductWithStatus.getHttpStatus()).isEqualTo(httpStatus);
            assertThat(simpleProductWithStatus.getSimpleProduct()).satisfies(simpleProduct -> {
                assertThat(simpleProduct.getDescription()).isEqualTo("ACHOC PO NESCAU 800G");
                assertThat(simpleProduct.getBarcode()).isEqualTo("7891000055120");
                assertThat(simpleProduct.getSequenceCode()).isEqualTo(29250);
            });
        }
    }

    @Test
    @DisplayName("Should return all products available")
    void should_return_all_products_available() {
        given(productRepositoryMock.findAllWithLastPrice())
            .willReturn(Resources.PRODUCT_LIST);

        final List<Product> actualProducts = productServiceUnderTest.findAllWithLatestPrice();

        assertThat(actualProducts).hasSize(3);
        assertThat(actualProducts).extracting(Product::getPrices).hasSize(3);

        verify(productRepositoryMock, times(1)).findAllWithLastPrice();
        verify(productRepositoryMock, only()).findAllWithLastPrice();
    }

    @Nested
    class FindAllTest {

        private final List<Product> ORDERED_LIST = Resources.PRODUCT_LIST
            .stream()
            .sorted(Comparator.comparing(Product::getSequenceCode))
            .collect(Collectors.toList());

        private final Sort ORDER_BY_SEQUENCE_CODE = Sort.by("sequenceCode").ascending();

        @Test
        @DisplayName("Should return all products ordered by its sequence code")
        void should_return_all_products_ordered_by_its_sequence_code() {
            given(productRepositoryMock.findAll(eq(ORDER_BY_SEQUENCE_CODE)))
                .willReturn(ORDERED_LIST);

            final List<Product> actualList = productServiceUnderTest.findAll(ORDER_BY_SEQUENCE_CODE);

            assertThat(actualList).hasSize(3);
            checkSortingWithSequenceCode(actualList, 29250, 93556, 120983);

            verify(productRepositoryMock, times(1)).findAll(eq(ORDER_BY_SEQUENCE_CODE));
            verify(productRepositoryMock, only()).findAll(eq(ORDER_BY_SEQUENCE_CODE));
        }

        @Test
        @DisplayName("Should return a page with the first two products")
        void should_return_a_page_with_the_first_two_products_ordered_by_its_sequence_code() {
            final Pageable pageWithTheFirstTwoProducts = PageRequest.of(0, 2).withSort(ORDER_BY_SEQUENCE_CODE);
            final List<Product> twoProducts = ORDERED_LIST.subList(0, 2);
            given(productRepositoryMock.findAll(eq(pageWithTheFirstTwoProducts)))
                .willReturn(new PageImpl<>(twoProducts));

            final Page<Product> actualPage = productServiceUnderTest.findAll(pageWithTheFirstTwoProducts);

            assertThat(actualPage).isNotNull();
            assertThat(actualPage.getTotalPages()).isOne();
            assertThat(actualPage.getTotalElements()).isEqualTo(2);
            assertThat(actualPage.getContent()).hasSize(2);
            checkSortingWithSequenceCode(actualPage.getContent(), 29250, 93556);

            verify(productRepositoryMock, times(1)).findAll(eq(pageWithTheFirstTwoProducts));
            verify(productRepositoryMock, only()).findAll(eq(pageWithTheFirstTwoProducts));
        }

        private void checkSortingWithSequenceCode(final List<Product> actualProducts, final Integer... sequenceCodes) {
            assertThat(actualProducts).extracting(Product::getSequenceCode)
                .containsExactly(sequenceCodes);
        }
    }

    @Nested
    class FindAllByUsernameIgnoreCaseContainingTest {

        @Test
        @DisplayName("Should return a page with two products like username")
        void should_return_a_page_with_two_products_like_username() {
            final String expressionToLookFor = "achoc";
            final Sort orderBySequenceCodeDesc = Sort.by("sequenceCode").descending();
            final Pageable theFirstPageWithThreeProductsOrLess = PageRequest.of(0, 3, orderBySequenceCodeDesc);
            given(productRepositoryMock.findAllByDescriptionIgnoreCaseContaining(eq(expressionToLookFor), eq(theFirstPageWithThreeProductsOrLess)))
                .willReturn(new PageImpl<>(Resources.PRODUCT_LIST.subList(0, 1)));

            final Page<Product> actualPage =
                productServiceUnderTest.findAllByUsernameIgnoreCaseContaining(expressionToLookFor, theFirstPageWithThreeProductsOrLess);

            assertThat(actualPage.getContent()).hasSize(1);
            assertThat(actualPage.getContent()).flatExtracting(Product::getPrices).hasSize(3);
            assertThat(actualPage).extracting(Product::getSequenceCode).containsExactly(29250);

            verify(productRepositoryMock, times(1))
                .findAllByDescriptionIgnoreCaseContaining(eq(expressionToLookFor), eq(theFirstPageWithThreeProductsOrLess));
            verify(productRepositoryMock, only())
                .findAllByDescriptionIgnoreCaseContaining(eq(expressionToLookFor), eq(theFirstPageWithThreeProductsOrLess));
        }
    }

    private static final class Resources {

        private static final List<Product> PRODUCT_LIST = List.of(
            Product.builder()
                .description("ACHOC PO NESCAU 800G")
                .barcode("7891000055120")
                .sequenceCode(29250)
                .build(),
            Product.builder()
                .description("AMENDOIM SALG CROKISSIMO 400G PIMENTA")
                .barcode("7896336010058")
                .sequenceCode(120983)
                .build(),
            Product.builder()
                .description("BALA GELATINA FINI 500G BURGUER")
                .barcode("7896336010058")
                .sequenceCode(93556)
                .build()
        );

        static {
            PRODUCT_LIST.get(0)
                .addPrice(new Price(new BigDecimal("16.98")))
                .addPrice(new Price(new BigDecimal("11.54")))
                .addPrice(new Price(new BigDecimal("9")));

            PRODUCT_LIST.get(1)
                .addPrice(new Price(new BigDecimal("5.2")))
                .addPrice(new Price(new BigDecimal("1.39")))
                .addPrice(new Price(new BigDecimal("8.75")));

            PRODUCT_LIST.get(2)
                .addPrice(new Price(new BigDecimal("6.11")))
                .addPrice(new Price(new BigDecimal("2.49")))
                .addPrice(new Price(new BigDecimal("6.96")));
        }
    }
}
