package com.xyz.ean.service;

import com.xyz.ean.dto.InputItemDTO;
import com.xyz.ean.entity.Price;
import com.xyz.ean.entity.Product;
import com.xyz.ean.repository.ProductRepository;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.*;

@SuppressWarnings("ConstantConditions")
class ProductServiceTest {

    private ProductService productServiceUnderTest;
    private ForeignProductHttpService foreignProductHttpServiceMock;
    private DomainMapper domainMapperMock;
    private ProductRepository productRepositoryMock;

    private static Product getDefaultProduct() {
        final Product product = new Product();
        product.setEanCode("1234567890123");
        product.setDescription("Default Product Description");
        product.addPrice(new Price(10.0));
        product.setSequenceCode(417304);
        return product;
    }

    @BeforeEach
    void setUp() {
        this.foreignProductHttpServiceMock = mock(ForeignProductHttpService.class);
        this.domainMapperMock = mock(DomainMapper.class);
        this.productRepositoryMock = mock(ProductRepository.class);
        this.productServiceUnderTest = new ProductService(this.productRepositoryMock, this.foreignProductHttpServiceMock, this.domainMapperMock);
    }

    @Test
    void shouldReturnAProductFromDBIfItExistsInTheDatabase_saveByEanCode() {
        //given
        given(productRepositoryMock.findByEanCode(anyString())).willReturn(Optional.of(getDefaultProduct()));

        //when
        final Product actualProduct = productServiceUnderTest.saveByEanCode("1234567890123");

        //then
        assertThat(actualProduct).isNotNull();

        verify(productRepositoryMock, times(1)).findByEanCode(anyString());
        verify(productRepositoryMock, only()).findByEanCode(anyString());
        verify(foreignProductHttpServiceMock, never()).fetchByEanCode(anyString());
        verify(domainMapperMock, never()).mapToProduct(any(InputItemDTO.class));
    }

    @Test
    void shouldReturnAProductFromExternalApiIfItDoesNotExistInTheDB_saveByEanCode() {
        //given
        final InputItemDTO inputItemDTO = InputItemDTO.builder()
            .description("Default Product Description")
            .eanCode("1234567890123")
            .currentPrice(10.0)
            .sequence(417304)
            .build();


        given(productRepositoryMock.findByEanCode(anyString())).willReturn(Optional.empty());
        given(foreignProductHttpServiceMock.fetchByEanCode(anyString())).willReturn(Optional.of(inputItemDTO));
        given(domainMapperMock.mapToProduct(any(InputItemDTO.class))).willReturn(new Product());

        //when
        final Product actualProduct = productServiceUnderTest.saveByEanCode("1234567890123");

        //then
        assertThat(actualProduct).isNotNull();

        verify(productRepositoryMock, times(1)).findByEanCode(anyString());
        verify(foreignProductHttpServiceMock, times(1)).fetchByEanCode(anyString());
        verify(domainMapperMock, times(1)).mapToProduct(any(InputItemDTO.class));
    }

    @Test
    void shouldThrowAnExceptionIfProductDoesNotExistInTheDBOrExternalApi_saveByEanCode() {
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
        verify(domainMapperMock, never()).mapToProduct(any(InputItemDTO.class));
    }

    @Test
    void givenAValidProductThenShouldSaveTheProduct_save() {
        //given
        given(productRepositoryMock.save(any(Product.class))).will(invocation -> invocation.getArgument(0));

        //when
        final Product actualProduct = productServiceUnderTest.save(getDefaultProduct());

        //then
        assertThat(actualProduct).isNotNull();
        assertThat(actualProduct.getPrices()).isNotNull();
        assertThat(actualProduct.getPrices()).hasSize(1);

        verify(productRepositoryMock, times(1)).save(any(Product.class));
    }

    @Test
    void givenANullProductThenShouldThrowAnException_save() {
        //given
        given(productRepositoryMock.save(any(Product.class))).will(invocation -> null);

        //when
        final Throwable actualException = catchThrowable(() -> productServiceUnderTest.save(null));

        //then
        assertThat(actualException).isNotNull();
        assertThat(actualException).isInstanceOf(NullPointerException.class);

        verify(productRepositoryMock, never()).save(any(Product.class));
    }

    @Test
    void givenAnExistentEanCodeThenShouldReturnAProduct_findByEanCode() {
        //given
        given(productRepositoryMock.findByEanCode(anyString())).willReturn(Optional.of(getDefaultProduct()));

        //when
        final Product actualProduct = productServiceUnderTest.findByEanCode("1234567890123");

        //then
        assertThat(actualProduct).isNotNull();

        verify(productRepositoryMock, times(1)).findByEanCode(anyString());
        verify(productRepositoryMock, only()).findByEanCode(anyString());
    }

    @Test
    void givenANonExistentEanCodeThenShouldThrowAnException_findByEanCode() {
        //given
        given(productRepositoryMock.findByEanCode(anyString())).willReturn(Optional.empty());

        //when
        final Throwable actualException = catchThrowable(() -> productServiceUnderTest.findByEanCode("1234567890123"));

        //then
        assertThat(actualException).satisfies(throwable -> {
            assertThat(throwable).isNotNull();
            assertThat(throwable).isInstanceOf(ResponseStatusException.class);
            assertThat(((ResponseStatusException) throwable).getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        });

        verify(productRepositoryMock, times(1)).findByEanCode(anyString());
        verify(productRepositoryMock, only()).findByEanCode(anyString());
    }

    @Test
    void givenANullEanCodeThenShouldThrowAnException_findByEanCode() {
        //given
        given(productRepositoryMock.findByEanCode(anyString())).willReturn(Optional.empty());

        //when
        final Throwable actualException = catchThrowable(() -> productServiceUnderTest.findByEanCode(null));

        //then
        assertThat(actualException).satisfies(throwable -> {
            assertThat(throwable).isNotNull();
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        });

        verify(productRepositoryMock, never()).findByEanCode(anyString());
    }

    @Test
    void ifThereAreAnyProductsInTheDBThenShouldReturnAllProducts_findAll() {
        //given
        final List<Product> existentProducts = List.of(
            getDefaultProduct(), getDefaultProduct(), getDefaultProduct(), getDefaultProduct()
        );

        given(productRepositoryMock.findAll()).willReturn(existentProducts);

        //when
        final List<Product> actualProducts = productServiceUnderTest.findAll();

        //then
        assertThat(actualProducts).isNotNull();
        assertThat(actualProducts).hasSize(4);
        assertThat(actualProducts).allSatisfy(product -> assertThat(product).isNotNull());

        verify(productRepositoryMock, times(1)).findAll();
        verify(productRepositoryMock, only()).findAll();
    }

    @Test
    void ifThereAreNoProductsInTheDBThenShouldReturnAnEmptyList_findAll() {
        //given
        given(productRepositoryMock.findAll()).willReturn(Collections.emptyList());

        //when
        final List<Product> actualProducts = productServiceUnderTest.findAll();

        //then
        assertThat(actualProducts).isNotNull();
        assertThat(actualProducts).isEmpty();

        verify(productRepositoryMock, times(1)).findAll();
        verify(productRepositoryMock, only()).findAll();
    }
}