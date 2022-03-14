package com.xyz.ean.service;

import com.xyz.ean.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.BDDMockito.mock;

class ProductServiceTest {

    private ProductService productServiceUnderTest;
    private ForeignProductHttpService foreignProductHttpServiceMock;
    private DomainMapper domainMapperMock;
    private ProductRepository productRepositoryMock;

    @BeforeEach
    void setUp() {
        this.foreignProductHttpServiceMock = mock(ForeignProductHttpService.class);
        this.domainMapperMock = mock(DomainMapper.class);
        this.productRepositoryMock = mock(ProductRepository.class);
        this.productServiceUnderTest = new ProductService(this.productRepositoryMock, this.foreignProductHttpServiceMock, this.domainMapperMock);
    }

}