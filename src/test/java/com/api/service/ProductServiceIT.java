package com.api.service;

import com.api.entity.Product;
import com.api.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class ProductServiceIT {

    @Autowired private ProductService productService;
    @Autowired private ProductRepository productRepository;

    private static final String BARCODE_FOR_DB = "7896036093085";
    private static final String BARCODE_FOR_INTEGRATION_API = "7898215151784";
    private static final String NON_EXISTING_BARCODE = "7898215151785";
    
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
}
