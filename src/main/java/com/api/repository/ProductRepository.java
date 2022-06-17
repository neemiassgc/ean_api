package com.api.repository;

import com.api.entity.Product;
import com.api.projection.Projection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByBarcode(final String barcode);

    @Query(
        "SELECT pro.barcode AS barcode, pro.description AS description, pro.sequenceCode AS sequenceCode, " +
        "pri.price AS latestPrice, pri.instant AS instant FROM Price pri JOIN pri.product pro WHERE pro.barcode = ?1 " +
        "AND pri.instant = (SELECT MAX(pr.instant) FROM Price pr WHERE pr.product.barcode = ?1 GROUP BY pr.product)"
    )
    Optional<Projection.ProductWithLatestPrice> findByBarcodeWithLatestPrice(final String barcode);

    List<Product> findAllByOrderByDescriptionAsc();
}
