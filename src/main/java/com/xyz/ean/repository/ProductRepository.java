package com.xyz.ean.repository;

import com.xyz.ean.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT pro, pri FROM Product pro JOIN FETCH pro.prices pri WHERE pro.eanCode = ?1 ORDER BY pri.created DESC")
    Optional<Product> findByEanCode(final String eanCode);
}
