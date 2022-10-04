package com.api.service;

import com.api.repository.PriceRepository;
import com.api.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional(readOnly = true)
public class ProductServiceIT {

    @Autowired
    private ProductServiceImpl productServiceImplUnderTest;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PriceRepository priceRepository;
}