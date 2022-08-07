package com.api.repository;

import com.api.entity.Price;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional(readOnly = true)
public class PriceRepositoryIT {

    @Autowired
    private PriceRepository priceRepository;

    @Test
    void should_return_a_list_of_prices_of_a_product() {
        final String barcode = "7896036093085";
        final List<Price> priceList = priceRepository.findByProductBarcode(barcode, Sort.by("instant").descending());
        final Iterator<BigDecimal> valuesToCheckInOrder = List.of(
            new BigDecimal("3.50"),
            new BigDecimal("2.50"),
            new BigDecimal("5.49")
        ).iterator();

        assertThat(priceList).hasSize(3);
        assertThat(priceList).allSatisfy(priceUnderTest ->
            assertThat(priceUnderTest.getValue()).isEqualTo(valuesToCheckInOrder.next())
        );
    }

    @Test
    void should_return_only_two_prices_of_a_product() {
        final String barcode = "7896036093085";
        final List<Price> priceList =
            priceRepository.findByProductBarcode(barcode, PageRequest.ofSize(2).withSort(Sort.by("instant").descending()));
        final Iterator<BigDecimal> valuesToCheckInOrder = List.of(
                new BigDecimal("3.50"),
                new BigDecimal("2.50")
        ).iterator();

        assertThat(priceList).hasSize(2);
        assertThat(priceList).allSatisfy(priceUnderTest ->
            assertThat(priceUnderTest.getValue()).isEqualTo(valuesToCheckInOrder.next())
        );
    }
}