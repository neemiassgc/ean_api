package com.api.service;

import com.api.entity.Price;
import com.api.projection.ProjectionFactory;
import com.api.repository.PriceRepository;
import com.api.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.api.projection.Projection.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
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
    }
}
