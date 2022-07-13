package com.api.job;

import com.api.repository.PriceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScanJobIT {

    @Autowired private ScanJob scanJob;
    @Autowired private PriceRepository priceRepository;

    private final int AMOUNT_OF_PRICES = 67;

    @Test
    void should_save_different_unequal_prices() {
        scanJob.execute(null);

        final long actualAmountOfPrices = priceRepository.count();

        assertThat(actualAmountOfPrices).isGreaterThan(AMOUNT_OF_PRICES);
    }
}