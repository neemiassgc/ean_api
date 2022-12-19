package com.api.service;

import com.api.Resources;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
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

        private final String BARCODE = "7891000055120";
        private final Product EXPECTED_PRODUCT = Resources.PRODUCTS_SAMPLE.get(0);

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
            given(productRepositoryMock.findByBarcode(eq(BARCODE))).willReturn(Optional.of(EXPECTED_PRODUCT));

            final SimpleProductWithStatus actualSimpleProductWithStatus =
                productServiceUnderTest.getByBarcodeAndSaveIfNecessary(BARCODE);

            checkProductWithHttpStatus(actualSimpleProductWithStatus, HttpStatus.OK);

            verify(productRepositoryMock, times(1)).findByBarcode(eq(BARCODE));
            verify(productRepositoryMock, only()).findByBarcode(eq(BARCODE));
        }

        @Test
        @DisplayName("Should return a product from an external api")
        void when_a_product_does_not_exist_in_db_then_should_return_from_an_external_api() {
            given(productRepositoryMock.findByBarcode(eq(BARCODE))).willReturn(Optional.empty());
            given(productExternalServiceMock.fetchByBarcode(eq(BARCODE))).willReturn(Optional.of(EXPECTED_PRODUCT));
            given(productRepositoryMock.save(eq(EXPECTED_PRODUCT)))
                .willAnswer(answer -> answer.getArgument(0, Product.class));

            final SimpleProductWithStatus actualSimpleProductWithStatus =
                productServiceUnderTest.getByBarcodeAndSaveIfNecessary(BARCODE);

            checkProductWithHttpStatus(actualSimpleProductWithStatus, HttpStatus.CREATED);

            verify(productRepositoryMock, times(1)).findByBarcode(eq(BARCODE));
            verify(productExternalServiceMock, times(1)).fetchByBarcode(eq(BARCODE));
            verify(productRepositoryMock, times(1)).save(eq(EXPECTED_PRODUCT));
        }

        @Test
        @DisplayName("Should throw an ResponseStatusException | NOT FOUND")
        void when_a_product_is_not_found_then_should_throw_an_exception() {
            final String nonExistentBarcode = "7891000055345";
            given(productRepositoryMock.findByBarcode(eq(nonExistentBarcode))).willReturn(Optional.empty());
            given(productExternalServiceMock.fetchByBarcode(eq(nonExistentBarcode))).willReturn(Optional.empty());

            final Throwable actualThrowable =
                catchThrowable(() -> productServiceUnderTest.getByBarcodeAndSaveIfNecessary(nonExistentBarcode));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(ResponseStatusException.class);
            assertThat((ResponseStatusException) actualThrowable).satisfies(exception -> {
                assertThat(exception.getReason()).isEqualTo("Product not found");
                assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            });

            verify(productRepositoryMock, times(1)).findByBarcode(eq(nonExistentBarcode));
            verify(productExternalServiceMock, times(1)).fetchByBarcode(eq(nonExistentBarcode));
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
        given(productRepositoryMock.findAllWithLastPrice()).willReturn(Resources.PRODUCTS_SAMPLE);

        final List<Product> actualProducts = productServiceUnderTest.findAllWithLatestPrice();

        assertThat(actualProducts).hasSize(18);
        assertThat(actualProducts).flatExtracting(Product::getPrices).hasSize(54);

        verify(productRepositoryMock, times(1)).findAllWithLastPrice();
        verify(productRepositoryMock, only()).findAllWithLastPrice();
    }

    @Nested
    class FindAllTest {

        private final List<Product> ORDERED_LIST = Resources.PRODUCTS_SAMPLE
            .stream()
            .sorted(Comparator.comparing(Product::getDescription))
            .collect(Collectors.toList());

        private final Sort ORDER_BY_SEQUENCE_CODE = Sort.by("sequenceCode").ascending();

        @Test
        @DisplayName("Should return all products ordered by its description")
        void should_return_all_products_ordered_by_its_description() {
            given(productRepositoryMock.findAll(eq(ORDER_BY_SEQUENCE_CODE))).willReturn(ORDERED_LIST);

            final List<Product> actualList = productServiceUnderTest.findAll(ORDER_BY_SEQUENCE_CODE);
            final Integer[] expectedSequenceCodes = new Integer[] {
                29250, 120983, 93556, 127635,
                122504, 144038, 98894, 2909,
                892, 141947, 87689, 134049,
                5648, 30881, 25336, 6367,
                128177, 125017
            };

            assertThat(actualList).hasSize(18);
            checkSortingWithSequenceCode(actualList, expectedSequenceCodes);

            verify(productRepositoryMock, times(1)).findAll(eq(ORDER_BY_SEQUENCE_CODE));
            verify(productRepositoryMock, only()).findAll(eq(ORDER_BY_SEQUENCE_CODE));
        }

        @Test
        @DisplayName("Should return a page with the first two products")
        void should_return_a_page_with_the_first_two_products_ordered_by_its_sequence_code() {
            final Pageable pageWithTwoProducts = PageRequest.of(0, 2).withSort(ORDER_BY_SEQUENCE_CODE);
            final List<Product> theFistTwoProducts = ORDERED_LIST.subList(0, 2);
            given(productRepositoryMock.findAll(eq(pageWithTwoProducts)))
                .willReturn(Utility.createPage(theFistTwoProducts));

            final Page<Product> actualPage = productServiceUnderTest.findAll(pageWithTwoProducts);

            assertThat(actualPage).isNotNull();
            assertThat(actualPage.getTotalPages()).isOne();
            assertThat(actualPage.getTotalElements()).isEqualTo(2);
            assertThat(actualPage.getContent()).hasSize(2);
            checkSortingWithSequenceCode(actualPage.getContent(), 29250, 120983);

            verify(productRepositoryMock, times(1)).findAll(eq(pageWithTwoProducts));
            verify(productRepositoryMock, only()).findAll(eq(pageWithTwoProducts));
        }

        private void checkSortingWithSequenceCode(final List<Product> actualProducts, final Integer... sequenceCodes) {
            assertThat(actualProducts).extracting(Product::getSequenceCode)
                .containsExactly(sequenceCodes);
        }
    }

    @Nested
    class FindAllByUsernameIgnoreCaseContainingTest {

        @Test
        @DisplayName("Should return a page with three products that contain 500g")
        void should_return_a_page_with_three_products_that_contain_500g() {
            final String expressionToLookFor = "500g";
            final Sort orderBySequenceCodeDesc = Sort.by("sequenceCode").descending();
            final Pageable theFirstPageWithThreeProducts = PageRequest.of(0, 3, orderBySequenceCodeDesc);
            given(productRepositoryMock.findAllByDescriptionIgnoreCaseContaining(eq(expressionToLookFor), eq(theFirstPageWithThreeProducts)))
                .willReturn(Utility.createPage(Utility.getAllContaining(expressionToLookFor)));

            final Page<Product> actualPage =
                productServiceUnderTest.findAllByDescriptionIgnoreCaseContaining(expressionToLookFor, theFirstPageWithThreeProducts);

            assertThat(actualPage.getContent()).hasSize(3);
            assertThat(actualPage.getContent()).flatExtracting(Product::getPrices).hasSize(9);
            assertThat(actualPage).extracting(Product::getSequenceCode).containsExactly(93556, 2909, 128177);

            verify(productRepositoryMock, times(1))
                .findAllByDescriptionIgnoreCaseContaining(eq(expressionToLookFor), eq(theFirstPageWithThreeProducts));
            verify(productRepositoryMock, only())
                .findAllByDescriptionIgnoreCaseContaining(eq(expressionToLookFor), eq(theFirstPageWithThreeProducts));
        }

        @Test
        @DisplayName("When expression is empty then should return an empty list ")
        void when_expression_is_empty_then_should_not_return_any_products() {
            final String emptyExpression = "";
            final Sort orderBySequenceCodeDesc = Sort.by("sequenceCode").descending();
            final Pageable theFirstPageWithThreeProducts = PageRequest.of(0, 3, orderBySequenceCodeDesc);

            final Page<Product> actualPage =
                productServiceUnderTest.findAllByDescriptionIgnoreCaseContaining(emptyExpression, theFirstPageWithThreeProducts);

            assertThat(actualPage.getContent()).isEmpty();

            verifyNoInteractions(productRepositoryMock);
        }

        @Test
        @DisplayName("When contains doesn't match anything then should return an empty list")
        void when_contains_does_not_match_anything_then_should_return_an_empty_list() {
            final String expressionToLookFor = "fruit";
            final Sort orderBySequenceCodeDesc = Sort.by("sequenceCode").descending();
            final Pageable theFirstPageWithThreeProducts = PageRequest.of(0, 3, orderBySequenceCodeDesc);
            given(productRepositoryMock.findAllByDescriptionIgnoreCaseContaining(eq(expressionToLookFor), eq(theFirstPageWithThreeProducts)))
                .willReturn(Utility.createPage(Collections.emptyList()));

            final Page<Product> actualPage =
                productServiceUnderTest.findAllByDescriptionIgnoreCaseContaining(expressionToLookFor, theFirstPageWithThreeProducts);

            assertThat(actualPage.getContent()).hasSize(0);
            assertThat(actualPage.getContent()).isEmpty();

            verify(productRepositoryMock, times(1))
                .findAllByDescriptionIgnoreCaseContaining(eq(expressionToLookFor), eq(theFirstPageWithThreeProducts));
            verify(productRepositoryMock, only())
                .findAllByDescriptionIgnoreCaseContaining(eq(expressionToLookFor), eq(theFirstPageWithThreeProducts));
        }
    }

    @Nested
    class FindAllByDescriptionIgnoreCaseStartingWithTest {

        @Test
        @DisplayName("Should return a page with three products that start with bisc")
        void should_return_a_page_with_three_products_that_start_with_bisc() {
            final Sort orderByDescriptionAsc = Sort.by("description").ascending();
            final Pageable firstPageWithTwoProducts = PageRequest.of(0, 2, orderByDescriptionAsc);
            final String startsWith = "bisc";
            given(productRepositoryMock
                .findAllByDescriptionIgnoreCaseStartingWith(eq(startsWith), eq(firstPageWithTwoProducts)))
                .willReturn(Utility.createPage(Utility.getAllStartingWith(startsWith)));

            final Page<Product> actualPage = productServiceUnderTest
                .findAllByDescriptionIgnoreCaseStartingWith(startsWith, firstPageWithTwoProducts);

            assertThat(actualPage).isNotNull();
            assertThat(actualPage.getTotalPages()).isOne();
            assertThat(actualPage.getTotalElements()).isEqualTo(3);
            assertThat(actualPage.getContent()).hasSize(3);
            assertThat(actualPage.getContent())
                .extracting(Product::getSequenceCode)
                .containsExactly(127635, 122504, 144038);
            assertThat(actualPage.getContent())
                .flatExtracting(Product::getPrices).hasSize(9);

            verify(productRepositoryMock, times(1))
                .findAllByDescriptionIgnoreCaseStartingWith(eq(startsWith), eq(firstPageWithTwoProducts));
            verify(productRepositoryMock, only())
                .findAllByDescriptionIgnoreCaseStartingWith(eq(startsWith), eq(firstPageWithTwoProducts));
        }

        @Test
        @DisplayName("Should return an empty page when startsWith does not match anything")
        void should_return_an_empty_page_when_startsWith_does_not_match_anything() {
            final Sort orderByDescriptionAsc = Sort.by("description").ascending();
            final Pageable firstPageWithTwoProducts = PageRequest.of(0, 2, orderByDescriptionAsc);
            final String startsWith = "pao";

            given(productRepositoryMock
                .findAllByDescriptionIgnoreCaseStartingWith(eq(startsWith), eq(firstPageWithTwoProducts)))
                .willReturn(Utility.createPage(Collections.emptyList()));

            final Page<Product> actualPage = productServiceUnderTest
                .findAllByDescriptionIgnoreCaseStartingWith(startsWith, firstPageWithTwoProducts);

            assertThat(actualPage).isNotNull();
            assertThat(actualPage.getContent()).isEmpty();

            verify(productRepositoryMock, times(1))
                .findAllByDescriptionIgnoreCaseStartingWith(eq(startsWith), eq(firstPageWithTwoProducts));
            verify(productRepositoryMock, only())
                .findAllByDescriptionIgnoreCaseStartingWith(eq(startsWith), eq(firstPageWithTwoProducts));
        }

        @Test
        @DisplayName("Should return an empty page when startsWith is an empty value")
        void should_return_an_empty_page_when_startsWith_is_an_empty_value() {
            final Sort orderByDescriptionAsc = Sort.by("description").ascending();
            final Pageable firstPageWithTwoProducts = PageRequest.of(0, 2, orderByDescriptionAsc);
            final String startsWith = "";
            given(productRepositoryMock
                .findAllByDescriptionIgnoreCaseStartingWith(eq(startsWith), eq(firstPageWithTwoProducts)))
                .willReturn(Utility.createPage(Collections.emptyList()));

            final Page<Product> actualPage = productServiceUnderTest
                .findAllByDescriptionIgnoreCaseStartingWith(startsWith, firstPageWithTwoProducts);

            assertThat(actualPage).isNotNull();
            assertThat(actualPage.getContent()).isEmpty();

            verifyNoInteractions(productRepositoryMock);
        }
    }

    @Nested
    class FindAllByDescriptionIgnoreCaseEndingWithTest {

        @Test
        @DisplayName("Should return a page with two products that end with choc")
        void should_return_a_page_with_two_products_that_end_with_choc() {
            final Sort orderByDescriptionAsc = Sort.by("description").ascending();
            final Pageable firstPageWithTwoProducts = PageRequest.of(0, 2).withSort(orderByDescriptionAsc);
            final String endsWith = "choc";
            given(productRepositoryMock.findAllByDescriptionIgnoreCaseEndingWith(eq(endsWith), eq(firstPageWithTwoProducts)))
                .willReturn(Utility.createPage(Utility.getAllEndingWith(endsWith)));

            final Page<Product> actualPage =
                productServiceUnderTest.findAllByDescriptionIgnoreCaseEndingWith(endsWith, firstPageWithTwoProducts);

            assertThat(actualPage).isNotNull();
            assertThat(actualPage.getTotalPages()).isOne();
            assertThat(actualPage.getTotalElements()).isEqualTo(2);
            assertThat(actualPage.getContent()).hasSize(2);
            assertThat(actualPage.getContent()).flatExtracting(Product::getPrices).hasSize(6);
            assertThat(actualPage.getContent())
                .extracting(Product::getSequenceCode).containsExactly(122504, 98894);

            verify(productRepositoryMock, times(1))
                .findAllByDescriptionIgnoreCaseEndingWith(eq(endsWith), eq(firstPageWithTwoProducts));
            verify(productRepositoryMock, only())
                .findAllByDescriptionIgnoreCaseEndingWith(eq(endsWith), eq(firstPageWithTwoProducts));

        }

        @Test
        @DisplayName("When ends with is empty then should return an empty page")
        void when_ends_with_is_empty_then_should_return_an_empty_page() {
            final Sort orderByDescriptionAsc = Sort.by("description").ascending();
            final Pageable firstPageWithTwoProducts = PageRequest.of(0, 2).withSort(orderByDescriptionAsc);
            final String endsWith = "";

            final Page<Product> actualPage =
                productServiceUnderTest.findAllByDescriptionIgnoreCaseEndingWith(endsWith, firstPageWithTwoProducts);

            assertThat(actualPage).isNotNull();
            assertThat(actualPage).isEmpty();
            assertThat(actualPage.getTotalElements()).isZero();

            verifyNoInteractions(productRepositoryMock);
        }
    }

    private static final class Utility {

        private static Page<Product> createPage(final List<Product> content) {
            return new PageImpl<>(content);
        }

        private static List<Product> getUntil(final int index) {
            return Resources.PRODUCTS_SAMPLE.subList(0, index);
        }

        private static List<Product> getAllByFiltering(final Predicate<String> predicate) {
            return Resources
                .PRODUCTS_SAMPLE
                .stream()
                .filter(prod -> predicate.test(prod.getDescription().toLowerCase()))
                .collect(Collectors.toList());
        }

        private static List<Product> getAllContaining(final String expression) {
            return getAllByFiltering(description -> description.contains(expression));
        }

        private static List<Product> getAllStartingWith(final String expression) {
            return getAllByFiltering(description -> description.startsWith(expression));
        }

        private static List<Product> getAllEndingWith(final String expression) {
            return getAllByFiltering(description -> description.endsWith(expression));
        }
    }
}
