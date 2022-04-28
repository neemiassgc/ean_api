package com.api.service;

import com.api.dto.InputItemDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ForeignProductHttpServiceIT {

    @Autowired private ForeignProductHttpService foreignProductHttpServiceUnderTest;

    @Test
    void given_an_existing_barcode_then_return_a_dto() {
        // Give
        String existingBarcode = "7891962057620";

        // When
        final Optional<InputItemDTO> actualResult = foreignProductHttpServiceUnderTest.fetchByEanCode(existingBarcode);

        // Then
        assertThat(actualResult).isNotNull();
        assertThat(actualResult).isPresent();
        assertThat(actualResult).get().extracting("description").isEqualTo("PAO BAUDUC 400G INTE");
        assertThat(actualResult).get().extracting("eanCode").isEqualTo(existingBarcode);
        assertThat(actualResult).get().extracting("sequence").isEqualTo(134262);
    }
}