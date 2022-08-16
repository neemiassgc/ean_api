package com.api.service.interfaces;

import com.api.entity.Product;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<Product> findAllWithLastPrice();

    Optional<Product> findByBarcode(@NonNull String barcode);

    Product processByBarcode(@NonNull final String barcode);
}
