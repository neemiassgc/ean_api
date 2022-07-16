package com.api.service;

import com.api.entity.Price;
import com.api.projection.ProjectionFactory;
import com.api.repository.PriceRepository;
import com.api.repository.ProductRepository;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.junit.jupiter.api.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.api.projection.Projection.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PersistenceServiceTest {

    private PersistenceService persistenceServiceUnderTest;
    private PriceRepository priceRepository;
    private ProductRepository productRepository;
    private ProductExternalService productExternalService;
    private DomainMapper domainMapper;

    // resources for testing
    private final String DEFAULT_BARCODE = "7891000055120";
    private List<Price> pricesForTesting = null;

    @BeforeAll
    void init() {
        this.pricesForTesting = getSomePrices();
    }

    @BeforeEach
    void preset() {
        this.priceRepository = mock(PriceRepository.class);
        this.productRepository = mock(ProductRepository.class);
        this.productExternalService = mock(ProductExternalService.class);
        this.domainMapper = mock(DomainMapper.class);
        this.persistenceServiceUnderTest =  new PersistenceService(productRepository, priceRepository, productExternalService, domainMapper);
    }

    private ProductWithManyPrices getDefaultProductWithManyPrices(final List<Price> priceList) {
        return ProjectionFactory.productWithManyPricesBuilder()
            .description("A product")
            .barcode(DEFAULT_BARCODE)
            .sequenceCode(12345)
            .prices(priceList
                .stream()
                .map(it -> new PriceWithInstant(it.getValue(), it.getInstant()))
                .collect(Collectors.toList())
            )
            .build();
    }

    private List<ProductWithLatestPrice> getProductListWithLatestPrice(final int quantity) {
        final List<ProductWithLatestPrice> listToReturn = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            listToReturn.add(
                ProjectionFactory.productWithLatestPriceBuilder()
                    .description("CAFE UTAM 500G")
                    .barcode(DEFAULT_BARCODE)
                    .sequenceCode(2909)
                    .latestPrice(new PriceWithInstant(new BigDecimal("16.90"), Instant.now()))
                    .build()
            );
        }
        
        return listToReturn;
    }

    private List<Price> getSomePrices() {
        final List<Price> pricesToReturn = new ArrayList<>(15);

        pricesToReturn.add(new Price(new BigDecimal("59.90"), null));
        pricesToReturn.add(new Price(new BigDecimal("18.37"), null));
        pricesToReturn.add(new Price(new BigDecimal("24.90"), null));
        pricesToReturn.add(new Price(new BigDecimal("21.62"), null));
        pricesToReturn.add(new Price(new BigDecimal("21.16"), null));
        pricesToReturn.add(new Price(new BigDecimal("7.46"), null));
        pricesToReturn.add(new Price(new BigDecimal("54.21"), null));
        pricesToReturn.add(new Price(new BigDecimal("41.80"), null));
        pricesToReturn.add(new Price(new BigDecimal("6.29"), null));
        pricesToReturn.add(new Price(new BigDecimal("9.50"), null));
        pricesToReturn.add(new Price(new BigDecimal("45.87"), null));
        pricesToReturn.add(new Price(new BigDecimal("26.30"), null));
        pricesToReturn.add(new Price(new BigDecimal("20.38"), null));
        pricesToReturn.add(new Price(new BigDecimal("19.18"), null));
        pricesToReturn.add(new Price(new BigDecimal("33.40"), null));
        pricesToReturn.add(new Price(new BigDecimal("15.66"), null));

        return pricesToReturn;
    }

    @Nested
    class FindProductByBarcodeTest {

        @Test
        @DisplayName("Given a negative limit then should throw an exception")
        void when_limit_is_negative_should_throw_an_exception() {
            // given
            final int negativeLimit = -4;

            // when
            final Throwable actualException =
                catchThrowable(() -> persistenceServiceUnderTest.findProductByBarcode(DEFAULT_BARCODE, negativeLimit));

            // then
            assertThat(actualException).isNotNull();
            assertThat(actualException).isExactlyInstanceOf(IllegalArgumentException.class);
            assertThat(actualException.getMessage()).isEqualTo("limit must be a positive number or zero");

            verify(priceRepository, never()).findAllByProductBarcode(eq(DEFAULT_BARCODE), any(Pageable.class));
            verify(domainMapper, never()).toProductListWithManyPrices(anyList());
            verify(productExternalService, never()).fetchByBarcode(eq(DEFAULT_BARCODE));
            verify(priceRepository, never()).save(any(Price.class));
            verify(domainMapper, never()).mapToPrice(any(ProductWithLatestPrice.class));
        }

        @Test
        @DisplayName("When the limit is zero then should return a product with all its prices")
        void should_return_a_product_with_all_its_prices() {
            // given
            final int limit = 0;
            final Pageable pageRequest = PageRequest.ofSize(Integer.MAX_VALUE);

            given(priceRepository.findAllByProductBarcode(eq(DEFAULT_BARCODE), eq(pageRequest)))
                .willReturn(pricesForTesting);

            given(domainMapper.toProductWithManyPrices(eq(pricesForTesting)))
                .willReturn(getDefaultProductWithManyPrices(pricesForTesting));

            // when
            final ProductWithManyPrices actualProduct = persistenceServiceUnderTest.findProductByBarcode(DEFAULT_BARCODE, 0);

            // then
            assertThat(actualProduct).isNotNull();
            assertThat(actualProduct.getPrices()).hasSize(16);

            verify(priceRepository, times(1)).findAllByProductBarcode(anyString(), any(Pageable.class));
            verify(domainMapper, times(1)).toProductWithManyPrices(anyList());
            verify(productExternalService, never()).fetchByBarcode(anyString());
            verify(priceRepository, never()).save(any(Price.class));
            verify(domainMapper, never()).mapToPrice(any());
        }

        @Test
        @DisplayName("When limit is seven then should return a product with seven prices")
        void should_return_a_product_with_a_few_prices() {
            // given
            final int limit = 7;
            final Pageable pageRequest = PageRequest.ofSize(limit);
            final List<Price> listWithSevenPrices = pricesForTesting.stream().limit(limit).collect(Collectors.toList());

            given(priceRepository.findAllByProductBarcode(eq(DEFAULT_BARCODE), eq(pageRequest)))
                .willReturn(listWithSevenPrices);

            given(domainMapper.toProductWithManyPrices(eq(listWithSevenPrices)))
                .willReturn(getDefaultProductWithManyPrices(listWithSevenPrices));

            // when
            final ProductWithManyPrices actualProduct = persistenceServiceUnderTest.findProductByBarcode(DEFAULT_BARCODE, 7);

            // then
            assertThat(actualProduct).isNotNull();
            assertThat(actualProduct.getPrices()).hasSize(7);

            verify(priceRepository, times(1)).findAllByProductBarcode(anyString(), any(Pageable.class));
            verify(domainMapper, times(1)).toProductWithManyPrices(anyList());
            verify(productExternalService, never()).fetchByBarcode(anyString());
            verify(priceRepository, never()).save(any(Price.class));
            verify(domainMapper, never()).mapToPrice(any());
        }

        @Test
        @DisplayName("When a product cannot be found in the db then should look in the external service")
        void should_return_a_product_from_external_service() {
            // given
            final Price aPrice = pricesForTesting.get(0);
            final ProductWithLatestPrice aProduct = getProductListWithLatestPrice(1).get(0);

            given(priceRepository.findAllByProductBarcode(eq(DEFAULT_BARCODE), eq(PageRequest.ofSize(Integer.MAX_VALUE))))
                .willReturn(Collections.emptyList());

            given(productExternalService.fetchByBarcode(eq(DEFAULT_BARCODE)))
                .willReturn(Optional.of(aProduct));

            // when
            final ProductWithLatestPrice actualProduct =
                persistenceServiceUnderTest.findProductByBarcode(DEFAULT_BARCODE, 0);

            // then
            assertThat(actualProduct).isNotNull();
            assertThat(actualProduct).extracting("latestPrice").isNotNull();
            assertThat(actualProduct).extracting("latestPrice.value").isNotNull();
            assertThat(actualProduct).extracting("latestPrice.value").isEqualTo(new BigDecimal("16.90"));
            assertThat(actualProduct).isNotNull();

            verify(priceRepository, times(1)).findAllByProductBarcode(anyString(), any(Pageable.class));
            verify(domainMapper, never()).toProductWithManyPrices(anyList());
            verify(productExternalService, times(1)).fetchByBarcode(anyString());
            verify(priceRepository, times(1)).save(isNull());
            verify(domainMapper, times(1)).mapToPrice(any(ProductWithLatestPrice.class));
        }

        @Test
        @DisplayName("Should throw an exception when no products can be found in both places")
        void when_cannot_find_a_product_should_throw_an_exception() {
            // given
            given(priceRepository.findAllByProductBarcode(eq(DEFAULT_BARCODE), eq(PageRequest.ofSize(Integer.MAX_VALUE))))
                    .willReturn(Collections.emptyList());

            given(productExternalService.fetchByBarcode(eq(DEFAULT_BARCODE))).willReturn(Optional.empty());

            // when
            final Throwable actualThrowable = catchThrowable(() ->
                    persistenceServiceUnderTest.findProductByBarcode(DEFAULT_BARCODE, 0)
            );

            // then
            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isExactlyInstanceOf(ResponseStatusException.class);
            assertThat(((ResponseStatusException) actualThrowable).getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(((ResponseStatusException) actualThrowable).getReason()).isEqualTo("Product not found");

            verify(priceRepository, times(1)).findAllByProductBarcode(anyString(), any(Pageable.class));
            verify(domainMapper, never()).toProductWithManyPrices(anyList());
            verify(productExternalService, times(1)).fetchByBarcode(anyString());
            verify(domainMapper, never()).mapToPrice(any(ProductWithLatestPrice.class));
            verify(priceRepository, never()).save(isNotNull());
        }
    }

    @Test
    @DisplayName("Testing findAllProducts method")
    void should_return_all_products_with_all_their_prices() {
        // given
        given(priceRepository.findAll()).willReturn(pricesForTesting);
        given(domainMapper.toProductListWithManyPrices(eq(pricesForTesting)))
            .willReturn(List.of(getDefaultProductWithManyPrices(pricesForTesting)));

        // when
        final List<ProductWithManyPrices> actualListOfProducts = persistenceServiceUnderTest.findAllProducts();

        // then
        assertThat(actualListOfProducts).isNotNull();
        assertThat(actualListOfProducts).hasSize(1);
        assertThat(actualListOfProducts.get(0).getPrices()).hasSize(16);

        verify(priceRepository, times(1)).findAll();
        verify(domainMapper, times(1)).toProductListWithManyPrices(anyList());
    }

    @Test
    @DisplayName("Testing findAllPagedProducts method")
    void should_return_a_list_of_paged_products() {
        // given
        final Page<UUID> page = new PageImpl<>(List.of(
            UUID.fromString("04f3dfdd-c811-4cc7-8e82-62d8406ad32c"),
            UUID.fromString("d55960d3-c270-4e5c-baa3-7923a5254365"),
            UUID.fromString("3bf5543e-923d-46ff-ae36-127575fd30f8"),
            UUID.fromString("758c7235-abd7-4a3e-b94b-87e610ce385e")
        ));

        given(productRepository.findAllId(eq(PageRequest.ofSize(10)))).willReturn(page);
        given(domainMapper.toProductListWithManyPrices(anyList())).willReturn(
            List.of(
                getDefaultProductWithManyPrices(getSomePrices()),
                getDefaultProductWithManyPrices(getSomePrices()),
                getDefaultProductWithManyPrices(getSomePrices()),
                getDefaultProductWithManyPrices(getSomePrices())
            )
        );
        given(priceRepository.findAllByProductId(eq(page.getContent()))).willReturn(pricesForTesting);

        // when
        final Paged<List<ProductWithManyPrices>> actualPagedList =
            persistenceServiceUnderTest.findAllPagedProducts(PageRequest.ofSize(10));

        //then
        assertThat(actualPagedList).isNotNull();
        assertThat(actualPagedList).extracting("currentPage").isEqualTo(0);
        assertThat(actualPagedList).extracting("totalPages").isEqualTo(1);
        assertThat(actualPagedList).extracting("numberOfItems").isEqualTo(4);
        assertThat(actualPagedList).extracting("hasNext").isEqualTo(false);
        assertThat(actualPagedList.getContent()).hasSize(4);

        verify(productRepository, times(1)).findAllId(any(Pageable.class));
        verify(domainMapper, times(1)).toProductListWithManyPrices(anyList());
        verify(priceRepository, times(1)).findAllByProductId(anyList());
    }
}
