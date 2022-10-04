package com.api.service;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.projection.SimpleProductWithStatus;
import com.api.repository.ProductRepository;
import com.api.service.interfaces.ProductExternalService;
import com.api.service.interfaces.ProductService;
import org.hibernate.validator.constraints.time.DurationMax;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

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
    class GetByBarcodeAndSaveIfNecessaryTest {

        @Test
        @DisplayName("Should throw NullPointerException")
        void if_barcode_is_null_then_should_throw_an_exception() {
            final Throwable actualThrowable =
                    catchThrowable(() -> productServiceImplUnderTest.getByBarcodeAndSaveIfNecessary(null));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(NullPointerException.class);

            verifyNoInteractions(productRepositoryMock);
        }

        @Test
        @DisplayName("Should return a product from db")
        void when_a_product_exist_in_db_then_should_return_it() {
            final String targetBarcode = "7891000055120";
            final Product expectedProduct = Resources.PRODUCT_LIST.get(0);
            given(productRepositoryMock.findByBarcode(eq(targetBarcode)))
                .willReturn(Optional.of(expectedProduct));

            final SimpleProductWithStatus actualSimpleProductWithStatus =
                productServiceImplUnderTest.getByBarcodeAndSaveIfNecessary(targetBarcode);

            assertThat(actualSimpleProductWithStatus).isNotNull();
            assertThat(actualSimpleProductWithStatus.getHttpStatus()).isEqualTo(HttpStatus.OK);
            assertThat(actualSimpleProductWithStatus.getSimpleProduct()).satisfies(simpleProduct -> {
                assertThat(simpleProduct.getDescription()).isEqualTo("ACHOC PO NESCAU 800G");
                assertThat(simpleProduct.getBarcode()).isEqualTo("7891000055120");
                assertThat(simpleProduct.getSequenceCode()).isEqualTo(29250);
            });

            verify(productRepositoryMock, times(1)).findByBarcode(eq(targetBarcode));
            verify(productRepositoryMock, only()).findByBarcode(eq(targetBarcode));
        }

        @Test
        @DisplayName("Should return a product from an external api")
        void when_a_product_does_not_exist_in_db_then_should_return_from_an_external_api() {
            final String targetBarcode = "7891000055120";
            final Product expectedProduct = Resources.PRODUCT_LIST.get(0);
            given(productRepositoryMock.findByBarcode(eq(targetBarcode)))
                .willReturn(Optional.empty());
            given(productExternalServiceMock.fetchByBarcode(eq(targetBarcode)))
                .willReturn(Optional.of(expectedProduct));
            given(productRepositoryMock.save(eq(expectedProduct)))
                .willAnswer(answer -> answer.getArgument(0, Product.class));

            final SimpleProductWithStatus actualSimpleProductWithStatus =
                productServiceImplUnderTest.getByBarcodeAndSaveIfNecessary(targetBarcode);

            assertThat(actualSimpleProductWithStatus).isNotNull();
            assertThat(actualSimpleProductWithStatus.getHttpStatus()).isEqualTo(HttpStatus.CREATED);
            assertThat(actualSimpleProductWithStatus.getSimpleProduct()).satisfies(simpleProduct -> {
                assertThat(simpleProduct.getDescription()).isEqualTo("ACHOC PO NESCAU 800G");
                assertThat(simpleProduct.getBarcode()).isEqualTo("7891000055120");
                assertThat(simpleProduct.getSequenceCode()).isEqualTo(29250);
            });

            verify(productRepositoryMock, times(1)).findByBarcode(eq(targetBarcode));
            verify(productExternalServiceMock, times(1)).fetchByBarcode(eq(targetBarcode));
            verify(productRepositoryMock, times(1)).save(eq(expectedProduct));
        }

        @Test
        @DisplayName("Should throw an ResponseStatusException | NOT FOUND")
        void when_a_product_is_not_found_then_should_throw_an_exception() {
            final String nonExistentBarcodeAnywhere = "7891000055345";
            given(productRepositoryMock.findByBarcode(eq(nonExistentBarcodeAnywhere)))
                .willReturn(Optional.empty());
            given(productExternalServiceMock.fetchByBarcode(eq(nonExistentBarcodeAnywhere)))
                .willReturn(Optional.empty());

            final Throwable actualThrowable =
                catchThrowable(() -> productServiceImplUnderTest.getByBarcodeAndSaveIfNecessary(nonExistentBarcodeAnywhere));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(ResponseStatusException.class);
            assertThat((ResponseStatusException) actualThrowable).satisfies(exception -> {
                assertThat(exception.getReason()).isEqualTo("Product not found");
                assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            });

            verify(productRepositoryMock, times(1)).findByBarcode(eq(nonExistentBarcodeAnywhere));
            verify(productExternalServiceMock, times(1)).fetchByBarcode(eq(nonExistentBarcodeAnywhere));
            verifyNoMoreInteractions(productRepositoryMock, productExternalServiceMock);
        }
    }

    @Test
    @DisplayName("Should return all products available")
    void should_return_all_products_available() {
        given(productRepositoryMock.findAllWithLastPrice())
            .willReturn(Resources.PRODUCT_LIST);

        final List<Product> actualProducts = productServiceImplUnderTest.findAllWithLatestPrice();

        assertThat(actualProducts).hasSize(3);
        assertThat(actualProducts).extracting(Product::getPrices).hasSize(3);

        verify(productRepositoryMock, times(1)).findAllWithLastPrice();
        verify(productRepositoryMock, only()).findAllWithLastPrice();
    }

    @Nested
    class FindAllTest {

    }

    private static final class Resources {

        private static final List<Product> PRODUCT_LIST = List.of(
            Product.builder()
                .description("ACHOC PO NESCAU 800G")
                .barcode("7891000055120")
                .sequenceCode(29250)
                .build(),
            Product.builder()
                .description("AMENDOIM SALG CROKISSIMO 400G PIMENTA")
                .barcode("7896336010058")
                .sequenceCode(120983)
                .build(),
            Product.builder()
                .description("BALA GELATINA FINI 500G BURGUER")
                .barcode("7896336010058")
                .sequenceCode(93556)
                .build()
        );

        static {
            PRODUCT_LIST.get(0)
                .addPrice(new Price(new BigDecimal("16.98")))
                .addPrice(new Price(new BigDecimal("11.54")))
                .addPrice(new Price(new BigDecimal("9")));

            PRODUCT_LIST.get(1)
                .addPrice(new Price(new BigDecimal("5.2")))
                .addPrice(new Price(new BigDecimal("1.39")))
                .addPrice(new Price(new BigDecimal("8.75")));

            PRODUCT_LIST.get(2)
                .addPrice(new Price(new BigDecimal("6.11")))
                .addPrice(new Price(new BigDecimal("2.49")))
                .addPrice(new Price(new BigDecimal("6.96")));
        }
    }
}
