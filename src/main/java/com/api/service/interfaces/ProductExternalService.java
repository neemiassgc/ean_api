package com.api.service.interfaces;

import com.api.entity.Product;

import java.util.Optional;

public interface ProductExternalService {

    Optional<Product> fetchByBarcode(String barcode);
}