package com.api.repository;

import com.api.entity.Product;
import lombok.NonNull;

public interface ProductRepositoryCustom {

    Product processByBarcode(@NonNull final String barcode);
}