package com.api.service;

import com.api.entity.Product;
import com.api.repository.ProductRepository;
import com.api.repository.ProductRepositoryCustomImpl;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductRepositoryCustomImplTest {

    private ProductRepositoryCustomImpl productRepositoryCustomImplUnderTest;
    private ProductRepository productRepository;
    private ProductExternalService productExternalService;

    // resources for testing
    private Product defaultProduct;

    @BeforeAll
    void init() {
        this.defaultProduct = Product.builder()
            .description("OLEO MARIA")
            .barcode("7896036093085")
            .sequenceCode(1184)
            .build();
    }

    @BeforeEach
    void preset() {
        this.productRepository = mock(ProductRepository.class);
        this.productExternalService = mock(ProductExternalService.class);
        this.productRepositoryCustomImplUnderTest =
            new ProductRepositoryCustomImpl(this.productExternalService);
        this.productRepositoryCustomImplUnderTest.setProductRepository(productRepository);
    }

    @DisplayName("When a product already exists in DB then returns it - processByBarcode")
    @Test
    void should_return_a_product_from_db() {
        // given
        final String targetBarcode = defaultProduct.getBarcode();
        given(productRepository.findByBarcode(eq(targetBarcode)))
            .willReturn(Optional.of(defaultProduct));

        // when
        final Product actualProduct = productRepositoryCustomImplUnderTest.processByBarcode(targetBarcode);

        // then
       assertThat(actualProduct).isEqualTo(defaultProduct);

       verify(productRepository, times(1)).findByBarcode(eq(targetBarcode));
       verify(productRepository, only()).findByBarcode(eq(targetBarcode));
    }

    @DisplayName("When a product does not exist in DB then checks in the external service  - processByBarcode")
    @Test
    void should_return_a_product_from_the_external_service() {
        // given
        final String targetBarcode = defaultProduct.getBarcode();
        given(productRepository.findByBarcode(eq(targetBarcode))).willReturn(Optional.empty());
        given(productExternalService.fetchByBarcode(eq(targetBarcode)))
            .willReturn(Optional.of(defaultProduct));
        given(productRepository.save(eq(defaultProduct)))
            .willAnswer(invocation ->  invocation.getArgument(0, Product.class));

        // when
        final Product actualProduct = productRepositoryCustomImplUnderTest.processByBarcode(targetBarcode);

        // then
        assertThat(actualProduct).isEqualTo(defaultProduct);

        verify(productRepository, times(1)).findByBarcode(eq(targetBarcode));
        verify(productExternalService, times(1)).fetchByBarcode(eq(targetBarcode));
        verify(productRepository, times(1)).save(eq(defaultProduct));
    }
}
