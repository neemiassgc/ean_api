package com.api.service;

import com.api.dto.InputItemDTO;
import com.api.entity.Price;
import com.api.entity.Product;
import com.api.repository.ProductRepository;

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
        product.setBarCode("1234567890123");
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
    void should_return_a_product_from_db_if_it_exists_in_the_database_saveByEanCode() {
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
    void should_return_a_product_from_external_api_if_it_does_not_exist_in_the_db_saveByEanCode() {
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
    void should_throw_an_exception_if_product_does_not_exist_in_the_db_or_external_api_saveByEanCode() {
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
    void given_a_valid_product_then_should_save_the_product_save() {
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
    void given_a_null_product_then_should_throw_an_exception_save() {
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
    void given_an_existent_bar_code_then_should_return_a_product_findByEanCode() {
        final String barCode = "1234567890123";

        //given
        given(productRepositoryMock.findByEanCode(anyString())).willReturn(Optional.of(getDefaultProduct()));

        //when
        final Product actualProduct = productServiceUnderTest.findByEanCode(barCode);

        //then
        assertThat(actualProduct).isNotNull();

        verify(productRepositoryMock, times(1)).findByEanCode(anyString());
        verify(productRepositoryMock, only()).findByEanCode(anyString());
    }

    @Test
    void given_a_non_existent_bar_code_then_should_throw_an_exception_findByEanCode() {
        final String barCode = "1234567890123";

        //given
        given(productRepositoryMock.findByEanCode(anyString())).willReturn(Optional.empty());

        //when
        final Throwable actualException = catchThrowable(() -> productServiceUnderTest.findByEanCode(barCode));

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
    void given_a_null_bar_code_then_should_throw_an_exception_findByEanCode() {
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
    void if_there_are_any_products_in_the_db_then_should_return_all_products_findAll() {
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
    void if_there_are_no_products_in_the_db_then_should_return_an_empty_list_findAll() {
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