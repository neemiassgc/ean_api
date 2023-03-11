package com.api.job;

import com.api.repository.PriceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PricingJobIT {

    @Autowired private PricingJob pricingJob;
    @Autowired private PriceRepository priceRepository;

    @Test
    void should_save_different_unequal_prices() {
        pricingJob.execute();

        final long actualAmountOfPrices = priceRepository.count();

        final int amountOfPrices = 66;
        assertThat(actualAmountOfPrices).isGreaterThanOrEqualTo(amountOfPrices);
    }
}