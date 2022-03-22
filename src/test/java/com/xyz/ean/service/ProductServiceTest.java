package com.xyz.ean.service;

import com.xyz.ean.dto.StandardProductDTO;
import com.xyz.ean.entity.Price;
import com.xyz.ean.entity.Product;
import com.xyz.ean.repository.ProductRepository;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.mockito.BDDMockito.*;

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

    @Test
    public void shouldReturnAProductFromDBIfItExistsInTheDatabase_saveByEanCode() {
        //given
        final Product standardProduct = new Product();
        standardProduct.setEanCode("1234567890123");
        standardProduct.setDescription("Standard Product Description");
        standardProduct.addPrice(new Price(10.0));
        given(productRepositoryMock.findByEanCode(anyString())).willReturn(Optional.of(standardProduct));

        //when
        final Product actualProduct = productServiceUnderTest.saveByEanCode("1234567890123");

        //then
        assertThat(actualProduct).isNotNull();

        verify(productRepositoryMock, times(1)).findByEanCode(anyString());
        verify(productRepositoryMock, only()).findByEanCode(anyString());
        verify(foreignProductHttpServiceMock, never()).fetchByEanCode(anyString());
        verify(domainMapperMock, never()).mapToProduct(any(StandardProductDTO.class));
    }

    @Test
    public void shouldReturnAProductFromExternalApiIfItDoesNotExistInTheDB_saveByEanCode() {
        //given
        given(productRepositoryMock.findByEanCode(anyString())).willReturn(Optional.empty());
        given(foreignProductHttpServiceMock.fetchByEanCode(anyString())).willReturn(Optional.of(new StandardProductDTO()));
        given(domainMapperMock.mapToProduct(any(StandardProductDTO.class))).willReturn(new Product());

        //when
        final Product actualProduct = productServiceUnderTest.saveByEanCode("1234567890123");

        //then
        assertThat(actualProduct).isNotNull();

        verify(productRepositoryMock, times(1)).findByEanCode(anyString());
        verify(foreignProductHttpServiceMock, times(1)).fetchByEanCode(anyString());
        verify(domainMapperMock, times(1)).mapToProduct(any(StandardProductDTO.class));
    }

    @Test
    public void shouldThrowAnExceptionIfProductDoesNotExistInTheDBOrExternalApi_saveByEanCode() {
        //given
        given(productRepositoryMock.findByEanCode(anyString())).willReturn(Optional.empty());
        given(foreignProductHttpServiceMock.fetchByEanCode(anyString())).willReturn(Optional.empty());

        //when
        final Throwable actualException = catchThrowable(() -> productServiceUnderTest.saveByEanCode("1234567890123"));

        //then
        assertThat(actualException).isNotNull();
        assertThat(actualException).isInstanceOf(ResponseStatusException.class);
        assertThat(((ResponseStatusException) actualException).getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(productRepositoryMock, times(1)).findByEanCode(anyString());
        verify(foreignProductHttpServiceMock, times(1)).fetchByEanCode(anyString());
        verify(domainMapperMock, never()).mapToProduct(any(StandardProductDTO.class));
    }

}