package com.api.service;

import com.api.Resources;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.*;

public class ProductServiceTest {

    private ProductService productServiceUnderTest;
    private ProductRepository productRepositoryMock;
    private ProductExternalService productExternalServiceMock;
    private CacheManager<Product, UUID> productCacheManager;

    @BeforeEach
    void setup() {
        productExternalServiceMock = mock(ProductExternalService.class);
        productRepositoryMock = mock(ProductRepository.class);
        productCacheManager = mock(CacheManager.class);
        productServiceUnderTest = new ProductServiceImpl(productRepositoryMock, productExternalServiceMock, productCacheManager);
    }

    @Nested
    class GetByBarcodeAndSaveIfNecessaryTest {

        private final String BARCODE = "7891000055120";
        private final Product EXPECTED_PRODUCT = Resources.PRODUCTS_SAMPLE.get(0);

        @BeforeEach
        void mock_to_clean_cache() {
            willDoNothing().given(productCacheManager).evictAll();
        }

        @Test
        @DisplayName("Should throw NullPointerException")
        void if_barcode_is_null_then_should_throw_an_exception() {
            final Throwable actualThrowable =
                catchThrowable(() -> productServiceUnderTest.getByBarcodeAndSaveIfNecessary(null));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(NullPointerException.class);

            verifyNoInteractions(productRepositoryMock);
            verifyNoInteractions(productCacheManager);
            verifyNoInteractions(productExternalServiceMock);
        }

        @Test
        @DisplayName("Should return a product from db")
        void when_a_product_exist_in_db_then_should_return_it() {
            given(productRepositoryMock.findByBarcode(eq(BARCODE))).willReturn(Optional.of(EXPECTED_PRODUCT));
            given(productCacheManager.containsKey(eq(BARCODE))).willReturn(true);
            given(productCacheManager.sync(eq(BARCODE), any(Supplier.class)))
                .willAnswer(invocation -> Optional.of(invocation.getArgument(1, Supplier.class).get()));

            final SimpleProductWithStatus actualSimpleProductWithStatus =
                productServiceUnderTest.getByBarcodeAndSaveIfNecessary(BARCODE);
            final boolean isCached = productCacheManager.containsKey(BARCODE);

            checkProductWithHttpStatus(actualSimpleProductWithStatus, HttpStatus.OK);
            assertThat(isCached).isTrue();

            verify(productRepositoryMock, times(1)).findByBarcode(eq(BARCODE));
            verify(productCacheManager, times(1)).sync(eq(BARCODE), any(Supplier.class));
            verify(productCacheManager, times(1)).containsKey(eq(BARCODE));
            verifyNoMoreInteractions(productRepositoryMock, productCacheManager);
            verifyNoInteractions(productExternalServiceMock);
        }

        @Test
        @DisplayName("Should return a product from an external api")
        void when_a_product_does_not_exist_in_db_then_should_return_from_an_external_api() {
            given(productRepositoryMock.findByBarcode(eq(BARCODE))).willReturn(Optional.empty());
            given(productExternalServiceMock.fetchByBarcode(eq(BARCODE))).willReturn(Optional.of(EXPECTED_PRODUCT));
            given(productCacheManager.containsKey(eq(BARCODE))).willReturn(false);
            given(productRepositoryMock.save(eq(EXPECTED_PRODUCT)))
                .willAnswer(answer -> answer.getArgument(0, Product.class));
            given(productCacheManager.sync(eq(BARCODE), any(Supplier.class)))
                .willAnswer(invocation ->
                    Optional.of(invocation.getArgument(1, Supplier.class).get()).map(v -> null)
                );

            final SimpleProductWithStatus actualSimpleProductWithStatus =
                productServiceUnderTest.getByBarcodeAndSaveIfNecessary(BARCODE);
            final boolean isCached = productCacheManager.containsKey(BARCODE);

            checkProductWithHttpStatus(actualSimpleProductWithStatus, HttpStatus.CREATED);
            assertThat(isCached).isFalse();

            verify(productRepositoryMock, times(1)).findByBarcode(eq(BARCODE));
            verify(productExternalServiceMock, times(1)).fetchByBarcode(eq(BARCODE));
            verify(productRepositoryMock, times(1)).save(eq(EXPECTED_PRODUCT));
            verify(productCacheManager, times(1)).sync(eq(BARCODE), any(Supplier.class));
            verify(productCacheManager, times(1)).evictAll();
            verify(productCacheManager, times(1)).containsKey(eq(BARCODE));
            verifyNoMoreInteractions(productRepositoryMock, productExternalServiceMock, productCacheManager);
        }

        @Test
        @DisplayName("Should throw an ResponseStatusException | NOT FOUND")
        void when_a_product_is_not_found_then_should_throw_an_exception() {
            final String nonExistentBarcode = "7891000055345";
            given(productRepositoryMock.findByBarcode(eq(nonExistentBarcode))).willReturn(Optional.empty());
            given(productExternalServiceMock.fetchByBarcode(eq(nonExistentBarcode))).willReturn(Optional.empty());
            given(productCacheManager.sync(eq(nonExistentBarcode), any(Supplier.class)))
                .willAnswer(invocation ->
                    Optional.of(invocation.getArgument(1, Supplier.class).get()).map(v -> null)
                );
            given(productCacheManager.containsKey(eq(nonExistentBarcode))).willReturn(false);

            final Throwable actualThrowable =
                catchThrowable(() -> productServiceUnderTest.getByBarcodeAndSaveIfNecessary(nonExistentBarcode));
            final boolean isCached = productCacheManager.containsKey(nonExistentBarcode);

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(ResponseStatusException.class);
            assertThat((ResponseStatusException) actualThrowable).satisfies(exception -> {
                assertThat(exception.getReason()).isEqualTo("Product not found");
                assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            });
            assertThat(isCached).isFalse();

            verify(productRepositoryMock, times(1)).findByBarcode(eq(nonExistentBarcode));
            verify(productExternalServiceMock, times(1)).fetchByBarcode(eq(nonExistentBarcode));
            verify(productCacheManager, times(1)).sync(eq(nonExistentBarcode), any(Supplier.class));
            verify(productCacheManager, times(1)).containsKey(eq(nonExistentBarcode));
            verifyNoMoreInteractions(productRepositoryMock, productExternalServiceMock, productCacheManager);
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
        final String key = "withLatestPrice";
        given(productRepositoryMock.findAllWithLastPrice()).willReturn(Resources.PRODUCTS_SAMPLE);
        given(productCacheManager.sync(eq(key), any(Supplier.class)))
            .willAnswer(invocation -> Optional.of(invocation.getArgument(1, Supplier.class).get()));
        given(productCacheManager.containsKey(eq(key))).willReturn(true);

        final List<Product> actualProducts = productServiceUnderTest.findAllWithLatestPrice();
        final boolean isCached = productCacheManager.containsKey(key);

        assertThat(actualProducts).hasSize(18);
        assertThat(actualProducts).flatExtracting(Product::getPrices).hasSize(54);
        assertThat(isCached).isTrue();

        verify(productRepositoryMock, times(1)).findAllWithLastPrice();
        verify(productCacheManager, times(1)).sync(eq(key), any(Supplier.class));
        verify(productCacheManager, times(1)).containsKey(eq(key));
        verifyNoMoreInteractions(productRepositoryMock, productCacheManager);
        verifyNoInteractions(productExternalServiceMock);
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
            given(productCacheManager.sync(eq(ORDER_BY_SEQUENCE_CODE.toString()), any(Supplier.class)))
                .willAnswer(invocation -> Optional.of(invocation.getArgument(1, Supplier.class).get()));
            given(productCacheManager.containsKey(eq(ORDER_BY_SEQUENCE_CODE.toString()))).willReturn(true);

            List<Product> actualList = productServiceUnderTest.findAll(ORDER_BY_SEQUENCE_CODE);
            final Integer[] expectedSequenceCodes = new Integer[] {
                29250, 120983, 93556, 127635,
                122504, 144038, 98894, 2909,
                892, 141947, 87689, 134049,
                5648, 30881, 25336, 6367,
                128177, 125017
            };
            final boolean isCached = productCacheManager.containsKey(ORDER_BY_SEQUENCE_CODE.toString());

            assertThat(actualList).hasSize(18);
            assertThat(isCached).isTrue();
            checkSortingWithSequenceCode(actualList, expectedSequenceCodes);

            verify(productRepositoryMock, times(1)).findAll(eq(ORDER_BY_SEQUENCE_CODE));
            verify(productCacheManager, times(1)).sync(eq(ORDER_BY_SEQUENCE_CODE.toString()), any(Supplier.class));
            verify(productCacheManager, times(1)).containsKey(eq(ORDER_BY_SEQUENCE_CODE.toString()));
            verifyNoMoreInteractions(productRepositoryMock, productCacheManager);
            verifyNoInteractions(productExternalServiceMock);
        }

        @Test
        @DisplayName("Should return a page with the first two products")
        void should_return_a_page_with_the_first_two_products_ordered_by_its_sequence_code() {
            final Pageable pageWithTwoProducts = PageRequest.of(0, 2).withSort(ORDER_BY_SEQUENCE_CODE);
            final String key = "pag=0-2";
            final List<Product> theFistTwoProducts = ORDERED_LIST.subList(0, 2);
            given(productRepositoryMock.findAll(eq(pageWithTwoProducts)))
                .willReturn(Utility.createPage(theFistTwoProducts));
            given(productCacheManager.sync(eq(key), any(Supplier.class)))
                .willAnswer(invocation -> Optional.of(invocation.getArgument(1, Supplier.class).get()));
            given(productCacheManager.containsKey(eq(key))).willReturn(true);


            final Page<Product> actualPage = productServiceUnderTest.findAll(pageWithTwoProducts);
            final boolean isCached = productCacheManager.containsKey(key);

            assertThat(actualPage).isNotNull();
            assertThat(actualPage.getTotalPages()).isOne();
            assertThat(actualPage.getTotalElements()).isEqualTo(2);
            assertThat(actualPage.getContent()).hasSize(2);
            assertThat(isCached).isTrue();
            checkSortingWithSequenceCode(actualPage.getContent(), 29250, 120983);

            verify(productRepositoryMock, times(1)).findAll(eq(pageWithTwoProducts));
            verify(productCacheManager, times(1)).sync(eq(key), any(Supplier.class));
            verify(productCacheManager, times(1)).containsKey(eq(key));
            verifyNoMoreInteractions(productRepositoryMock, productCacheManager);
            verifyNoInteractions(productExternalServiceMock);
        }

        private void checkSortingWithSequenceCode(final List<Product> actualProducts, final Integer... sequenceCodes) {
            assertThat(actualProducts).extracting(Product::getSequenceCode)
                .containsExactly(sequenceCodes);
        }
    }

    @Nested
    class FindAllByDescriptionIgnoreCaseContainingTest {

        private final Sort ORDER_BY_SEQUENCE_CODE_DESC = Sort.by("sequenceCode").descending();

        @Test
        @DisplayName("Should return a page with three products that contain 500g")
        void should_return_a_page_with_three_products_that_contain_500g() {
            final String expressionToLookFor = "500g";
            final String key = expressionToLookFor+"-pag=0-3";
            final Pageable theFirstPageWithThreeProducts = PageRequest.of(0, 3, ORDER_BY_SEQUENCE_CODE_DESC);
            given(productCacheManager.sync(eq(key), any(Supplier.class)))
                .willAnswer(invocation -> Optional.of(invocation.getArgument(1, Supplier.class).get()));
            given(productCacheManager.containsKey(eq(key))).willReturn(true);
            given(productRepositoryMock.findAllByDescriptionIgnoreCaseContaining(eq(expressionToLookFor), eq(theFirstPageWithThreeProducts)))
                .willReturn(Utility.createPage(Utility.getAllContaining()));

            final Page<Product> actualPage =
                productServiceUnderTest.findAllByDescriptionIgnoreCaseContaining(expressionToLookFor, theFirstPageWithThreeProducts);
            final boolean isCached = productCacheManager.containsKey(key);

            assertThat(actualPage.getContent()).hasSize(3);
            assertThat(actualPage.getContent()).flatExtracting(Product::getPrices).hasSize(9);
            assertThat(actualPage).extracting(Product::getSequenceCode).containsExactly(93556, 2909, 128177);
            assertThat(isCached).isTrue();

            verify(productRepositoryMock, times(1))
                .findAllByDescriptionIgnoreCaseContaining(eq(expressionToLookFor), eq(theFirstPageWithThreeProducts));
            verify(productCacheManager, times(1)).sync(eq(key), any(Supplier.class));
            verify(productCacheManager, times(1)).containsKey(eq(key));
            verifyNoMoreInteractions(productRepositoryMock, productCacheManager);
            verifyNoInteractions(productExternalServiceMock);
        }

        @Test
        @DisplayName("When expression is empty then should return an empty list ")
        void when_expression_is_empty_then_should_not_return_any_products() {
            final String emptyExpression = "";
            final Pageable theFirstPageWithThreeProducts = PageRequest.of(0, 3, ORDER_BY_SEQUENCE_CODE_DESC);
            given(productCacheManager.containsKey(anyString())).willReturn(false);

            final Page<Product> actualPage =
                productServiceUnderTest.findAllByDescriptionIgnoreCaseContaining(emptyExpression, theFirstPageWithThreeProducts);
            final boolean isCached = productCacheManager.containsKey("-pag=0-3");

            assertThat(actualPage.getContent()).isEmpty();
            assertThat(isCached).isFalse();

            verify(productCacheManager, times(1)).containsKey(anyString());
            verifyNoMoreInteractions(productCacheManager);
            verifyNoInteractions(productExternalServiceMock);
            verifyNoInteractions(productRepositoryMock);
        }

        @Test
        @DisplayName("When contains doesn't match anything then should return an empty list")
        void when_contains_does_not_match_anything_then_should_return_an_empty_list() {
            final String expressionToLookFor = "fruit";
            final String key = expressionToLookFor+"-pag=0-3";
            final Pageable theFirstPageWithThreeProducts = PageRequest.of(0, 3, ORDER_BY_SEQUENCE_CODE_DESC);
            given(productCacheManager.sync(eq(key), any(Supplier.class)))
                .willAnswer(invocation -> Optional.of(invocation.getArgument(1, Supplier.class).get()));
            given(productRepositoryMock.findAllByDescriptionIgnoreCaseContaining(eq(expressionToLookFor), eq(theFirstPageWithThreeProducts)))
                .willReturn(Utility.createPage(Collections.emptyList()));
            given(productCacheManager.containsKey(eq(key))).willReturn(false);

            final Page<Product> actualPage =
                productServiceUnderTest.findAllByDescriptionIgnoreCaseContaining(expressionToLookFor, theFirstPageWithThreeProducts);
            final boolean isCached = productCacheManager.containsKey(key);

            assertThat(actualPage.getContent()).hasSize(0);
            assertThat(actualPage.getContent()).isEmpty();
            assertThat(isCached).isFalse();

            verify(productRepositoryMock, times(1))
                .findAllByDescriptionIgnoreCaseContaining(eq(expressionToLookFor), eq(theFirstPageWithThreeProducts));
            verify(productCacheManager, times(1)).containsKey(eq(key));
            verify(productCacheManager, times(1)).sync(eq(key), any(Supplier.class));
            verifyNoMoreInteractions(productRepositoryMock, productCacheManager);
            verifyNoInteractions(productExternalServiceMock);
        }
    }

    @Nested
    class FindAllByDescriptionIgnoreCaseStartingWithTest {

        private final Sort ORDER_BY_DESCRIPTION_ASC = Sort.by("description").descending();

        @Test
        @DisplayName("Should return a page with three products that start with bisc")
        void should_return_a_page_with_three_products_that_start_with_bisc() {
            final Pageable firstPageWithThreeProducts = PageRequest.of(0, 3, ORDER_BY_DESCRIPTION_ASC);
            final String startsWith = "bisc";
            final String key = startsWith+"-pag=0-3";
            given(productRepositoryMock
                .findAllByDescriptionIgnoreCaseStartingWith(eq(startsWith), eq(firstPageWithThreeProducts)))
                .willReturn(Utility.createPage(Utility.getAllStartingWith()));
            given(productCacheManager.sync(eq(key), any(Supplier.class)))
                .willAnswer(invocation -> Optional.of(invocation.getArgument(1, Supplier.class).get()));
            given(productCacheManager.containsKey(eq(key))).willReturn(true);

            final Page<Product> actualPage = productServiceUnderTest
                .findAllByDescriptionIgnoreCaseStartingWith(startsWith, firstPageWithThreeProducts);
            final boolean isCached = productCacheManager.containsKey(key);

            assertThat(actualPage).isNotNull();
            assertThat(actualPage.getTotalPages()).isOne();
            assertThat(actualPage.getTotalElements()).isEqualTo(3);
            assertThat(actualPage.getContent()).hasSize(3);
            assertThat(actualPage.getContent())
                .extracting(Product::getSequenceCode)
                .containsExactly(127635, 122504, 144038);
            assertThat(actualPage.getContent())
                .flatExtracting(Product::getPrices).hasSize(9);
            assertThat(isCached).isTrue();

            verify(productRepositoryMock, times(1))
                .findAllByDescriptionIgnoreCaseStartingWith(eq(startsWith), eq(firstPageWithThreeProducts));
            verify(productCacheManager, times(1)).sync(eq(key), any(Supplier.class));
            verify(productCacheManager, times(1)).containsKey(eq(key));
            verifyNoMoreInteractions(productRepositoryMock, productCacheManager);
            verifyNoInteractions(productExternalServiceMock);
        }

        @Test
        @DisplayName("Should return an empty page when startsWith does not match anything")
        void should_return_an_empty_page_when_startsWith_does_not_match_anything() {
            final Pageable firstPageWithTwoProducts = PageRequest.of(0, 2, ORDER_BY_DESCRIPTION_ASC);
            final String startsWith = "pao";
            final String key = startsWith+"-pag=0-2";
            given(productRepositoryMock
                .findAllByDescriptionIgnoreCaseStartingWith(eq(startsWith), eq(firstPageWithTwoProducts)))
                .willReturn(Utility.createPage(Collections.emptyList()));
            given(productCacheManager.sync(eq(key), any(Supplier.class)))
                .willAnswer(invocation -> Optional.of(invocation.getArgument(1, Supplier.class).get()));
            given(productCacheManager.containsKey(eq(key))).willReturn(false);

            final Page<Product> actualPage = productServiceUnderTest
                .findAllByDescriptionIgnoreCaseStartingWith(startsWith, firstPageWithTwoProducts);
            final boolean isCached = productCacheManager.containsKey(key);

            assertThat(actualPage).isNotNull();
            assertThat(actualPage.getContent()).isEmpty();
            assertThat(isCached).isFalse();

            verify(productRepositoryMock, times(1))
                .findAllByDescriptionIgnoreCaseStartingWith(eq(startsWith), eq(firstPageWithTwoProducts));
            verify(productCacheManager, times(1)).sync(eq(key), any(Supplier.class));
            verify(productCacheManager, times(1)).containsKey(eq(key));
            verifyNoMoreInteractions(productRepositoryMock, productCacheManager);
            verifyNoInteractions(productExternalServiceMock);
        }

        @Test
        @DisplayName("Should return an empty page when startsWith is an empty value")
        void should_return_an_empty_page_when_startsWith_is_an_empty_value() {
            final Pageable firstPageWithTwoProducts = PageRequest.of(0, 2, ORDER_BY_DESCRIPTION_ASC);
            final String startsWith = "";
            final String key = startsWith+"-pag=0-2";
            given(productCacheManager.containsKey(eq(key))).willReturn(false);;

            final Page<Product> actualPage = productServiceUnderTest
                .findAllByDescriptionIgnoreCaseStartingWith(startsWith, firstPageWithTwoProducts);
            final boolean isCached = productCacheManager.containsKey(key);

            assertThat(actualPage).isNotNull();
            assertThat(actualPage.getContent()).isEmpty();
            assertThat(isCached).isFalse();

            verify(productCacheManager, times(1)).containsKey(eq(key));
            verifyNoInteractions(productRepositoryMock);
            verifyNoInteractions(productExternalServiceMock);

        }
    }

    @Nested
    class FindAllByDescriptionIgnoreCaseEndingWithTest {

        private final Pageable FIRST_PAGE_WITH_TWO_PRODUCTS =
            PageRequest.of(0, 2).withSort(Sort.by("description").ascending());

        @Test
        @DisplayName("Should return two pages with two products that end with choc")
        void should_return_two_pages_with_two_products_that_end_with_choc() {
            final String endsWith = "choc";
            final String key = endsWith+"-pag=0-2";
            given(productCacheManager.sync(eq(key), any(Supplier.class)))
                .willAnswer(invocation -> Optional.of(invocation.getArgument(1, Supplier.class).get()));
            given(productCacheManager.containsKey(eq(key))).willReturn(true);
            given(productRepositoryMock.findAllByDescriptionIgnoreCaseEndingWith(eq(endsWith), eq(FIRST_PAGE_WITH_TWO_PRODUCTS)))
                .willReturn(Utility.createPage(Utility.getAllEndingWith()));

            final Page<Product> actualPage =
                productServiceUnderTest.findAllByDescriptionIgnoreCaseEndingWith(endsWith, FIRST_PAGE_WITH_TWO_PRODUCTS);
            final boolean isCached = productCacheManager.containsKey(key);

            assertThat(actualPage).isNotNull();
            assertThat(actualPage.getTotalPages()).isOne();
            assertThat(actualPage.getTotalElements()).isEqualTo(2);
            assertThat(actualPage.getContent()).hasSize(2);
            assertThat(actualPage.getContent()).flatExtracting(Product::getPrices).hasSize(6);
            assertThat(actualPage.getContent())
                .extracting(Product::getSequenceCode).containsExactly(122504, 98894);
            assertThat(isCached).isTrue();

            verify(productRepositoryMock, times(1))
                .findAllByDescriptionIgnoreCaseEndingWith(eq(endsWith), eq(FIRST_PAGE_WITH_TWO_PRODUCTS));
            verify(productCacheManager, times(1)).containsKey(eq(key));
            verify(productCacheManager, times(1)).sync(eq(key), any(Supplier.class));
            verifyNoMoreInteractions(productRepositoryMock, productCacheManager);
            verifyNoInteractions(productExternalServiceMock);

        }

        @Test
        @DisplayName("When endsWith is empty then should return an empty page")
        void when_endsWith_is_empty_then_should_return_an_empty_page() {
            final String endsWith = "";
            final String key = "-pag=0-2";
            given(productCacheManager.containsKey(eq(key))).willReturn(false);

            final Page<Product> actualPage =
                productServiceUnderTest.findAllByDescriptionIgnoreCaseEndingWith(endsWith, FIRST_PAGE_WITH_TWO_PRODUCTS);
            final boolean isCached = productCacheManager.containsKey(key);

            assertThat(actualPage).isNotNull();
            assertThat(actualPage).isEmpty();
            assertThat(actualPage.getTotalElements()).isZero();
            assertThat(isCached).isFalse();

            verify(productCacheManager, times(1)).containsKey(eq(key));
            verifyNoMoreInteractions(productCacheManager);
            verifyNoInteractions(productRepositoryMock, productExternalServiceMock);
        }

        @Test
        @DisplayName("When endsWith does not match anything then should return an empty page")
        void when_endsWith_does_not_match_anything_then_should_return_an_empty_page() {
            final String endsWith = "carrot";
            final String key = endsWith+"-pag=0-2";
            given(productCacheManager.sync(eq(key), any(Supplier.class)))
                .willAnswer(invocation -> Optional.of(invocation.getArgument(1, Supplier.class).get()));
            given(productCacheManager.containsKey(eq(key))).willReturn(false);
            given(productRepositoryMock
                .findAllByDescriptionIgnoreCaseEndingWith(eq(endsWith), eq(FIRST_PAGE_WITH_TWO_PRODUCTS)))
                .willReturn(Utility.createPage(Collections.emptyList()));

            final Page<Product> actualPage = productServiceUnderTest
                .findAllByDescriptionIgnoreCaseEndingWith(endsWith, FIRST_PAGE_WITH_TWO_PRODUCTS);
            final boolean isCached = productCacheManager.containsKey(key);

            assertThat(actualPage).isNotNull();
            assertThat(actualPage.getContent()).isEmpty();
            assertThat(isCached).isFalse();

            verify(productCacheManager, times(1)).sync(eq(key), any(Supplier.class));
            verify(productCacheManager, times(1)).containsKey(eq(key));
            verify(productRepositoryMock, times(1))
                .findAllByDescriptionIgnoreCaseEndingWith(eq(endsWith), eq(FIRST_PAGE_WITH_TWO_PRODUCTS));
            verifyNoMoreInteractions(productRepositoryMock, productCacheManager);
            verifyNoInteractions(productExternalServiceMock);
        }
    }

    private static final class Utility {

        private static Page<Product> createPage(final List<Product> content) {
            return new PageImpl<>(content);
        }

        private static List<Product> getAllByFiltering(final Predicate<String> predicate) {
            return Resources
                .PRODUCTS_SAMPLE
                .stream()
                .filter(prod -> predicate.test(prod.getDescription().toLowerCase()))
                .collect(Collectors.toList());
        }

        private static List<Product> getAllContaining() {
            return getAllByFiltering(description -> description.contains("500g"));
        }

        private static List<Product> getAllStartingWith() {
            return getAllByFiltering(description -> description.startsWith("bisc"));
        }

        private static List<Product> getAllEndingWith() {
            return getAllByFiltering(description -> description.endsWith("choc"));
        }
    }
}
