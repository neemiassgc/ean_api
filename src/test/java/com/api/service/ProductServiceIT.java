package com.api.service;

import com.api.entity.Product;
import com.api.repository.ProductRepository;
import org.assertj.core.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class ProductServiceIT {

    @Autowired private ProductService productService;
    @Autowired private ProductRepository productRepository;

    private static final String BARCODE_FOR_DB = "7896036093085";
    private static final String BARCODE_FOR_INTEGRATION_API = "7898215151784";
    private static final String NON_EXISTING_BARCODE = "7898215151785";
    private static final long PRODUCT_ACTUAL_COUNT = 4;
    
    @Test
    void should_return_a_product_from_the_database_saveByBarcode() {
        final Product actualProduct = productService.saveByBarcode(BARCODE_FOR_DB);

        assertThat(actualProduct).isNotNull();
        assertThat(actualProduct).extracting("barcode").isEqualTo(BARCODE_FOR_DB);
        assertThat(actualProduct).extracting("description").isEqualTo("OLEO MARIA");
        assertThat(actualProduct).extracting("sequenceCode").isEqualTo(1184);
        assertThat(actualProduct).extracting("id").isEqualTo(UUID.fromString("3f30dc5c-5ce1-4556-a648-de8e55b0f6be"));
        assertThat(actualProduct.getPrices()).hasSize(3);
        assertThat(actualProduct.getPrices()).extracting("price").containsExactly(3.5, 2.5, 5.49);
    }

    @Test
    void should_fetch_a_product_from_the_external_api_save_it_in_the_db_and_return_it_saveByBarcode() {
        final Product actualProduct = productService.saveByBarcode(BARCODE_FOR_INTEGRATION_API);
        final long actualCount = productRepository.count();

        assertThat(actualProduct).isNotNull();
        assertThat(actualProduct).extracting("barcode").isEqualTo(BARCODE_FOR_INTEGRATION_API);
        assertThat(actualProduct).extracting("description").isEqualTo("CR LEITE PIRACANJUBA");
        assertThat(actualProduct).extracting("sequenceCode").isEqualTo(109727);
        assertThat(actualProduct.getPrices()).hasSize(1);
        assertThat(actualCount).isEqualTo(PRODUCT_ACTUAL_COUNT + 1); // 5 expected products
    }

    @Test
    void should_throw_an_exception_when_barcode_is_not_found_saveByBarcode() {
        final Throwable actualThrowable = catchThrowable(() -> productService.saveByBarcode(NON_EXISTING_BARCODE));
        final long actualCount = productRepository.count();

        assertThat(actualThrowable).isNotNull();
        assertThat(actualThrowable).isInstanceOf(ResponseStatusException.class);
        assertThat(Objects.castIfBelongsToType(actualThrowable, ResponseStatusException.class)).satisfies(throwable -> {
           assertThat(throwable.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
           assertThat(throwable.getReason()).isEqualTo("Product not found");
        });
        assertThat(actualCount).isEqualTo(PRODUCT_ACTUAL_COUNT);
    }
}
