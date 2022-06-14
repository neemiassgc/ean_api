package com.api.service;

import com.api.projection.InputItemDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductExternalServiceIT {

    @Autowired private ProductExternalService productExternalServiceUnderTest;

    @Test
    void given_an_existing_barcode_then_return_a_dto() {
        // Give
        String existingBarcode = "7891962057620";

        // When
        final Optional<InputItemDTO> actualResult = productExternalServiceUnderTest.fetchByEanCode(existingBarcode);

        // Then
        assertThat(actualResult).isNotNull();
        assertThat(actualResult).isPresent();
        assertThat(actualResult).get().extracting("description").isEqualTo("PAO BAUDUC 400G INTE");
        assertThat(actualResult).get().extracting("barcode").isEqualTo(existingBarcode);
        assertThat(actualResult).get().extracting("sequence").isEqualTo(134262);
    }

    @Test
    void given_an_non_existing_barcode_then_return_an_empty_optional() {
        // Give
        String nonExistingBarcode = "7891962057621";

        // When
        final Optional<InputItemDTO> actualResult = productExternalServiceUnderTest.fetchByEanCode(nonExistingBarcode);

        // Then
        assertThat(actualResult).isNotNull();
        assertThat(actualResult).isEmpty();
    }
}