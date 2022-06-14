package com.api.repository;

import com.api.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByBarcode(final String barcode);

    @NonNull
    @EntityGraph(value = "prices_entity_graph")
    List<Product> findAllByOrderByDescriptionAsc();
}
