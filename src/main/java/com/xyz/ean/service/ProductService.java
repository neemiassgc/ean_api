package com.xyz.ean.service;

import com.xyz.ean.entity.Product;
import com.xyz.ean.repository.ProductRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductService {

    private final ProductRepository  productRepository;
    private final ForeignProductHttpService foreignProductHttpService;

    public Product saveByEanCode(@NonNull final String eanCode) {
        return productRepository.findByEanCode(eanCode)
            .or(() -> {
                final Optional<Product> fetchedProduct = foreignProductHttpService.fetchByEanCode(eanCode);
                fetchedProduct.ifPresent(productRepository::save);
                return fetchedProduct;
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }
}
