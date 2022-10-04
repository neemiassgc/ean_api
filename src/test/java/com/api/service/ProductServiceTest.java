package com.api.service;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.projection.SimpleProductWithStatus;
import com.api.repository.ProductRepository;
import com.api.service.interfaces.ProductExternalService;
import com.api.service.interfaces.ProductService;
import org.hibernate.validator.constraints.time.DurationMax;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
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
