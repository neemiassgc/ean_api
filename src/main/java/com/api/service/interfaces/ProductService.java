package com.api.service.interfaces;

import com.api.entity.Product;
import com.api.projection.SimpleProductWithStatus;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface ProductService {

    List<Product> findAllWithLatestPrice();

    SimpleProductWithStatus getByBarcodeAndSaveIfNecessary(@NonNull final String barcode);

    List<Product> findAll(Sort sort);

    Page<Product> findAll(Pageable pageable);

    void save(Product product);

    Page<Product> findAllByDescriptionIgnoreCaseContaining(String description, Pageable pageable);

    Page<Product> findAllByDescriptionIgnoreCaseStartingWith(String description, Pageable pageable);

    Page<Product> findAllByDescriptionIgnoreCaseEndingWith(String description, Pageable pageable);
}