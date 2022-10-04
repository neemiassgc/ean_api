package com.api.service;

import com.api.entity.Price;
import com.api.service.interfaces.PriceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional(readOnly = true)
public class PriceServiceIT {

    @Autowired
    private PriceService priceService;

    @Nested
    final class FindByIdTest {

        @Test
        @DisplayName("Should return a price")
        void when_id_exist_the_should_return_a_price() {
            final UUID existentUuid = UUID.fromString("9423f8be-2a4f-4baa-b457-6a904bf633f0");

            final Price actualPrice = priceService.findById(existentUuid);

            assertThat(actualPrice).isNotNull();
            assertThat(actualPrice.getValue()).isEqualTo(new BigDecimal("12.70"));
        }
    }
}