package com.api.repository;

import com.api.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT pro, pri FROM #{#entityName} pro JOIN FETCH pro.prices pri WHERE pro.barcode = ?1 ORDER BY pri.instant DESC")
    Optional<Product> findByBarcode(final String barcode);

    @NonNull
    @EntityGraph(value = "prices_entity_graph")
    List<Product> findAllByOrderByDescriptionAsc();
}
