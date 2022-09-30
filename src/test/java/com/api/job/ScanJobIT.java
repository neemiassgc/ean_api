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

    @Test
    void should_save_different_unequal_prices() {
        scanJob.execute(null);

        final long actualAmountOfPrices = priceRepository.count();

        final int amountOfPrices = 66;
        assertThat(actualAmountOfPrices).isGreaterThanOrEqualTo(amountOfPrices);
    }
}