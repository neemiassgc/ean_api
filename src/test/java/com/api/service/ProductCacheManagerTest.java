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

    private List<Product> getProductsByIndexes(final int ...indexes) {
        final Product[] productsToReturn = new Product[indexes.length];
        for (int i = 0; i < productsToReturn.length; i++)
            productsToReturn[i] = Resources.PRODUCTS_SAMPLE.get(indexes[i]);
        return Arrays.asList(productsToReturn);
    }
}