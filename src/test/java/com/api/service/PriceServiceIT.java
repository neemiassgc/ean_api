package com.api.service;

import com.api.repository.PriceRepository;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional(readOnly = true)
public class PriceServiceIT {

    @Autowired
    private PriceRepository priceRepository;

    @Nested
    final class FindByIdTest {

    }
}