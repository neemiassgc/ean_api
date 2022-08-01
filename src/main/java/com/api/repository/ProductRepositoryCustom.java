package com.api.repository;

import com.api.projection.Projection;
import lombok.NonNull;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductRepositoryCustom {

    <I extends Projection.ProductBase> I findProductByBarcode(@NonNull final String barcode, int limit);

    List<Projection.ProductWithManyPrices> findAllProducts();

    Projection.Paged<List<Projection.ProductWithManyPrices>> findAllPagedProducts(@NonNull final Pageable pageable);

    List<Projection.ProductWithLatestPrice> findAllProductsWithLatestPrice();

    void saveProductWithPrice(@NonNull final Projection.ProductWithLatestPrice productWithLatestPrice);
}
