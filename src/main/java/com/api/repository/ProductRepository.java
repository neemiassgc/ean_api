package com.api.repository;

import com.api.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByBarcode(final String barcode);

    List<Product> findAllByOrderByDescriptionAsc();
}
