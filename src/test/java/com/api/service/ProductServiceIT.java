package com.api.service;

import com.api.entity.Product;
import com.api.projection.SimpleProductWithStatus;
import com.api.repository.PriceRepository;
import com.api.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest
@Transactional(readOnly = true)
public class ProductServiceImplIT {

    @Autowired
    private ProductServiceImpl productServiceImplUnderTest;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PriceRepository priceRepository;
}