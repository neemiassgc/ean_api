package com.api.service;

import com.api.entity.Product;
import com.api.repository.ProductRepository;
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
    private final ProductExternalService productExternalService;
    private final DomainMapper domainMapper;

    public Product saveByBarcode(@NonNull final String barcode) {
        return productRepository.findByBarcode(barcode)
            .or(() -> {
                final Optional<Product> fetchedProduct = this.productExternalService
                    .fetchByEanCode(barcode)
                    .map(this.domainMapper::mapToProduct);

                fetchedProduct.ifPresent(productRepository::save);

                return fetchedProduct;
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public Product save(@NonNull final Product product) {
        return productRepository.save(product);
    }

    public Product findByBarcode(@NonNull final String barcode) {
        return productRepository.findByBarcode(barcode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public List<Product> findAllByOrderByDescriptionAsc() {
        return productRepository.findAllByOrderByDescriptionAsc();
    }

    public List<Product> saveAll(@NonNull final List<Product> products) {
        return productRepository.saveAllAndFlush(products);
    }
}
