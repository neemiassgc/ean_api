package com.api.service;

import com.api.repository.PriceRepository;
import com.api.repository.ProductRepository;
import org.junit.jupiter.api.*;

import static org.mockito.Mockito.mock;

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
    private class FindProductByBarcodeTest {
        
    }
}
