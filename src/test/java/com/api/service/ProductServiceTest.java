package com.api.service;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.repository.ProductRepository;
import com.api.service.interfaces.ProductExternalService;
import com.api.service.interfaces.ProductService;
import org.hibernate.validator.constraints.time.DurationMax;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
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
