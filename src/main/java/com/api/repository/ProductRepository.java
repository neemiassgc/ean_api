package com.api.repository;

import com.api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByBarcode(String barcode);

    @Query(
        "SELECT pro FROM Product pro JOIN FETCH pro.prices pri WHERE pri.instant IN "+
        "(SELECT MAX(pri2.instant) FROM Price pri2 GROUP BY pri2.product) "+
        "ORDER BY pro.description ASC"
    )
    List<Product> findAllWithLastPrice();

    Page<Product> findAllByDescriptionIgnoreCaseContaining(String description, Pageable pageable);
}