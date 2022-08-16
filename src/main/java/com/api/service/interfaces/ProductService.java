package com.api.service.interfaces;

import com.api.entity.Product;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<Product> findAllWithLastPrice();

    Optional<Product> findByBarcode(@NonNull String barcode);

    Product processByBarcode(@NonNull final String barcode);

    List<Product> findAll(Sort sort);

    Page<Product> findAll(Pageable pageable);

    void save(Product product);
}