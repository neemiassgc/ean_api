package com.api.service;

import com.api.entity.Price;
import static com.api.projection.Projection.*;
import com.api.repository.PriceRepository;
import com.api.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PersistenceServiceTest {

    private PersistenceService persistenceServiceUnderTest;
    private PriceRepository priceRepository;
    private ProductRepository productRepository;
    private ProductExternalService productExternalService;
    private DomainMapper domainMapper;

    private final String DEFAULT_BARCODE = "7891000055120";

    @BeforeAll
    void init() {
        this.priceRepository = mock(PriceRepository.class);
        this.productRepository = mock(ProductRepository.class);
        this.productExternalService = mock(ProductExternalService.class);
        this.domainMapper = mock(DomainMapper.class);
        persistenceServiceUnderTest =  new PersistenceService(productRepository, priceRepository, productExternalService, domainMapper);
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
