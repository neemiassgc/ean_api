package com.api.service;

import com.api.entity.Price;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
    }

    @Test
    void should_fetch_a_product_from_the_external_api_save_it_in_the_db_and_return_it_saveByBarcode() {
        final Product actualProduct = productService.saveByBarcode(BARCODE_FOR_INTEGRATION_API);
        final long actualCount = productRepository.count();

        assertThat(actualProduct).isNotNull();
        assertThat(actualProduct).extracting("barcode").isEqualTo(BARCODE_FOR_INTEGRATION_API);
        assertThat(actualProduct).extracting("description").isEqualTo("CR LEITE PIRACANJUBA");
        assertThat(actualProduct).extracting("sequenceCode").isEqualTo(109727);
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

    @Test
    void should_save_a_new_product_save() {
        // given
        final Price[] pricesToBind = new Price[] {
            new Price(10.9, Instant.now()),
            new Price(8.75, Instant.now().minus(2, ChronoUnit.DAYS)) // 2 days ago
        };
        final Product productToSave = new Product();
        productToSave.setBarcode("1234567890123");
        productToSave.setDescription("Testing product");
        productToSave.setSequenceCode(12345);
        productToSave.addPrice(pricesToBind);

        // when
        final Product actualProduct = productService.save(productToSave);
        final long actualCount = productRepository.count();

        // then
        assertThat(actualProduct).isNotNull();
        assertThat(actualProduct).extracting("barcode").isEqualTo("1234567890123");
        assertThat(actualProduct).extracting("description").isEqualTo("Testing product");
        assertThat(actualProduct).extracting("sequenceCode").isEqualTo(12345);
        assertThat(actualProduct.getPrices()).hasSize(2);
        assertThat(actualProduct.getPrices()).extracting("price").containsExactly(10.9, 8.75);
        assertThat(actualCount).isEqualTo(PRODUCT_ACTUAL_COUNT + 1); // 5 expected products
    }

    @Test
    void should_return_a_product_from_db_findByBarcode() {
        final Product fetchedActualProduct = productService.findByBarcode(BARCODE_FOR_DB);

        assertThat(fetchedActualProduct).isNotNull();
        assertThat(fetchedActualProduct).extracting("barcode").isEqualTo(BARCODE_FOR_DB);
        assertThat(fetchedActualProduct).extracting("description").isEqualTo("OLEO MARIA");
        assertThat(fetchedActualProduct).extracting("sequenceCode").isEqualTo(1184);
        assertThat(fetchedActualProduct).extracting("id").isEqualTo(UUID.fromString("3f30dc5c-5ce1-4556-a648-de8e55b0f6be"));
        assertThat(fetchedActualProduct.getPrices()).hasSize(3);
        assertThat(fetchedActualProduct.getPrices()).extracting("price").containsExactly(3.5, 2.5, 5.49);
    }

    @Test
    void should_throw_an_exception_when_barcode_is_not_found_findByBarcode() {
        final Throwable actualThrowable = catchThrowable(() -> productService.findByBarcode(NON_EXISTING_BARCODE));

        assertThat(actualThrowable).isNotNull();
        assertThat(actualThrowable).isInstanceOf(ResponseStatusException.class);
        assertThat(Objects.castIfBelongsToType(actualThrowable, ResponseStatusException.class)).satisfies(throwable -> {
           assertThat(throwable.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
           assertThat(throwable.getReason()).isEqualTo("Product not found");
        });
    }

    @Test
    void should_return_all_products_from_db_findAll() {
        final List<Product> fetchedActualProducts = productService.findAllByOrderByDescriptionAsc();

        assertThat(fetchedActualProducts).isNotNull();
        assertThat(fetchedActualProducts).hasSize((int) PRODUCT_ACTUAL_COUNT);
        assertThat(fetchedActualProducts).allSatisfy(product -> assertThat(product).isNotNull());
    }

    @Test
    void should_save_all_the_products_saveAll() {
        // given
        final List<Product> productsToSave = List.of(
            Product.builder()
                .description("Testing product").barcode("1234567890123")
                .sequenceCode(12345).prices(new Price[] {new Price(8.34)}).build(),
            Product.builder().description("Testing product").barcode("1234567890124")
                .sequenceCode(12346).prices(new Price[] {new Price(7.12)}).build(),
            Product.builder().description("Testing product").barcode("1234567890125")
                .sequenceCode(12347).prices(new Price[] {new Price(5.6)}).build()
        );

        // when
        final List<Product> actualProducts = productService.saveAll(productsToSave);
        final long actualCount = productRepository.count();

        // then
        assertThat(actualProducts).isNotNull();
        assertThat(actualProducts).hasSize(3);
        assertThat(actualCount).isEqualTo(PRODUCT_ACTUAL_COUNT + 3); // 7 expected products
    }
}
