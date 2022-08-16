package com.api.service;

import com.api.entity.Product;
import com.api.repository.ProductRepository;
import com.api.service.interfaces.ProductService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductExternalService productExternalService;

    @Transactional(propagation = Propagation.REQUIRED)
    public Product getByBarcode(@NonNull final String barcode) {
        final Optional<Product> productOptional = productRepository.findByBarcode(barcode);

        if (productOptional.isPresent()) return productOptional.get();

        // Save a new product
        final Product newProduct = productExternalService.fetchByBarcode(barcode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        return this.productRepository.save(newProduct);
    }

    @Override
    public List<Product> findAllWithLastPrice() {
        return productRepository.findAllWithLastPrice();
    }

    @Override
    public Optional<Product> findByBarcode(@NonNull String barcode) {
        return productRepository.findByBarcode(barcode);
    }

    @Override
    public List<Product> findAll(@NonNull Sort sort) {
        return productRepository.findAll(sort);
    }

    @Override
    public Page<Product> findAll(@NonNull Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void save(@NonNull Product product) {
        productRepository.save(product);
    }
}