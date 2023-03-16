package com.api.service;

import com.api.entity.Product;
import org.junit.jupiter.api.BeforeEach;

import java.util.Comparator;
import java.util.UUID;

public final class ProductCacheManagerTest {

    private CacheManager<Product, UUID> productCacheManager;

    @BeforeEach
    void setup() {
        productCacheManager = new CacheManager<>(Comparator.comparing(Product::getDescription), Product::getId);
    }
}
