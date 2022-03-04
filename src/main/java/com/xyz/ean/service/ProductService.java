package com.xyz.ean.service;

import com.xyz.ean.entity.Product;
import com.xyz.ean.repository.ProductRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductService {

    private final ProductRepository  productRepository;
    private final ForeignProductHttpService foreignProductHttpService;

    public Product saveByEanCode(@NonNull final String eanCode) {
        final Product product = foreignProductHttpService.fetchByEanCode(eanCode)
            .orElseThrow(() -> new IllegalStateException("Product not found"));

        return productRepository.save(product);
    }
}
