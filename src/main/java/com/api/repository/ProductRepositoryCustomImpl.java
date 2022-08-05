package com.api.repository;

import com.api.entity.Product;
import com.api.service.ProductExternalService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Service
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    @Setter(onMethod_ = @Autowired, onParam_ = @Lazy)
    private ProductRepository productRepository;

    private final ProductExternalService productExternalService;

    @Transactional(propagation = Propagation.REQUIRED)
    public Product processByBarcode(@NonNull final String barcode) {
        final Optional<Product> productOptional = productRepository.findByBarcode(barcode);

        if (productOptional.isPresent()) return productOptional.get();

        // Save a new product
        final Product newProduct = productExternalService.fetchByBarcode(barcode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        return this.productRepository.save(newProduct);
    }
}