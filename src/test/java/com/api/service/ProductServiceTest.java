package com.api.service;

import com.api.repository.ProductRepository;
import com.api.service.interfaces.ProductExternalService;
import com.api.service.interfaces.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;

import static org.mockito.Mockito.mock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductServiceTest {

    private ProductService productServiceImplUnderTest;
    private ProductRepository productRepositoryMock;
    private ProductExternalService productExternalServiceMock;

    @BeforeEach
    void setup() {
        productExternalServiceMock = mock(ProductExternalService.class);
        productRepositoryMock = mock(ProductRepository.class);
        productServiceImplUnderTest = new ProductServiceImpl(productRepositoryMock, productExternalServiceMock);
    }

    @Nested
    private final class GetByBarcodeAndSaveIfNecessaryTest {

    }
}
