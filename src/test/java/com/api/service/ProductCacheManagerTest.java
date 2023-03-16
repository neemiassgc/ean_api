package com.api.service;

import com.api.Resources;
import com.api.entity.Product;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

public final class ProductCacheManagerTest {

    private CacheManager<Product, UUID> productCacheManager;

    @BeforeEach
    void setup() {
        productCacheManager = new CacheManager<>(Comparator.comparing(Product::getDescription), Product::getId);
    }

    @Test
    void given_a_key_should_return_all_products_from_cache_in_the_order() {
        final List<Product> eightProducts = getProductsByIndexes(0, 10, 4, 7, 9, 2, 3, 11);
        final String key = "eight";
        productCacheManager.put(key, eightProducts);

        final Optional<List<Product>> actualProducts = productCacheManager.get(key);

        assertThat(actualProducts).isNotNull();
        assertThat(actualProducts).isPresent();
        assertThat(actualProducts.get()).satisfies(products -> {
            assertThat(products).hasSize(8);
            assertThat(products).extracting(Product::getDescription)
                .containsExactly(
                    "ACHOC PO NESCAU 800G", "MAIONESE QUERO 210G TP", "BISC WAFER TODDY 132G CHOC",
                    "CAFE UTAM 500G", "LIMP M.USO OMO 500ML DESINF HERBAL", "BALA GELATINA FINI 500G BURGUER",
                    "BISC ROSQ MARILAN 350G INT", "MILHO VDE PREDILECTA 170G LT"
                );
        });
    }

    private List<Product> getProductsByIndexes(final int ...indexes) {
        final Product[] productsToReturn = new Product[indexes.length];
        for (int i = 0; i < productsToReturn.length; i++)
            productsToReturn[i] = Resources.PRODUCTS_SAMPLE.get(indexes[i]);
        return Arrays.asList(productsToReturn);
    }
}