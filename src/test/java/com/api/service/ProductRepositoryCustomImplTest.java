package com.api.service;

import com.api.repository.ProductRepository;
import com.api.repository.ProductRepositoryCustomImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import static org.mockito.Mockito.mock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductRepositoryCustomImplTest {

    private ProductRepositoryCustomImpl productRepositoryCustomImplUnderTest;
    private ProductRepository productRepository;
    private ProductExternalService productExternalService;

    // resources for testing
    private final String DEFAULT_BARCODE = "7891000055120";

    @BeforeAll
    void init() {

    }

    @BeforeEach
    void preset() {
        this.productRepository = mock(ProductRepository.class);
        this.productExternalService = mock(ProductExternalService.class);
        this.productRepositoryCustomImplUnderTest =
            new ProductRepositoryCustomImpl(this.productExternalService);
        this.productRepositoryCustomImplUnderTest.setProductRepository(productRepository);
    }
}
