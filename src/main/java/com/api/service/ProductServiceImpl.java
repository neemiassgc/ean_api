package com.api.service;

import com.api.entity.Product;
import com.api.projection.SimpleProductWithStatus;
import com.api.repository.ProductRepository;
import com.api.service.interfaces.ProductExternalService;
import com.api.service.interfaces.ProductService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.BiFunction;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductExternalService productExternalService;
    private final CacheManager<Product, UUID> productCacheManager;
    private final long[] totalOfItems = new long[1];

    @Transactional(propagation = Propagation.REQUIRED)
    public SimpleProductWithStatus getByBarcodeAndSaveIfNecessary(@NonNull final String barcode) {
        final Optional<List<Product>> productListOptional =
            productCacheManager.sync(
                barcode,
                () -> productRepository.findByBarcode(barcode).map(List::of).orElse(Collections.emptyList())
            );

        if (productListOptional.isPresent())
            return productListOptional.get().get(0).toSimpleProductWithStatus(HttpStatus.OK);

        final Product newProduct = productExternalService.fetchByBarcode(barcode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        save(newProduct);
        return newProduct.toSimpleProductWithStatus(HttpStatus.CREATED);
    }

    @Override
    public void save(@NonNull final Product product) {
        productRepository.save(product);
        productCacheManager.evictAll();
    }

    @Override
    public List<Product> findAllWithLatestPrice() {
        return productCacheManager
            .sync("withLatestPrice", productRepository::findAllWithLastPrice)
            .orElse(Collections.emptyList());
    }

    @Override
    public List<Product> findAll(@NonNull Sort sort) {
        return productCacheManager
            .sync(sort.toString(), () -> productRepository.findAll(sort))
            .orElse(Collections.emptyList());
    }

    @Override
    public Page<Product> findAll(@NonNull Pageable pageable) {
        final String key = String.format("pag=%s-%s", pageable.getPageNumber(), pageable.getPageSize());
        final List<Product> listOfProducts = productCacheManager
            .sync(key, () -> {
                final Page<Product> productPage = productRepository.findAll(pageable);
                totalOfItems[0] = productPage.getTotalElements();
                return productPage.getContent();
            })
            .orElse(Collections.emptyList());
        return new PageImpl<>(listOfProducts, pageable, totalOfItems[0]);
    }

    @Override
    public Page<Product> findAllByDescriptionIgnoreCaseContaining(@NonNull String description, @NonNull Pageable pageable) {
        return getAllBySettings(description, pageable, productRepository::findAllByDescriptionIgnoreCaseContaining);
    }

    @Override
    public Page<Product> findAllByDescriptionIgnoreCaseStartingWith(String description, Pageable pageable) {
        return getAllBySettings(description, pageable, productRepository::findAllByDescriptionIgnoreCaseStartingWith);
    }

    @Override
    public Page<Product> findAllByDescriptionIgnoreCaseEndingWith(String description, Pageable pageable) {
        return getAllBySettings(description, pageable, productRepository::findAllByDescriptionIgnoreCaseEndingWith);
    }

    private Page<Product> getAllBySettings(
        final String expression,
        final Pageable pageable,
        final BiFunction<String, Pageable, Page<Product>> pageBiFunction
    ) {
        if (expression.isEmpty()) return new PageImpl<>(Collections.emptyList());
        final String key = String.format("pag=%s-%s", pageable.getPageNumber(), pageable.getPageSize());
        final List<Product> listOfProducts = productCacheManager
            .sync(key, () -> {
                final Page<Product> productPage = pageBiFunction.apply(expression, pageable);
                totalOfItems[0] = productPage.getTotalElements();
                return productPage.getContent();
            })
            .orElse(Collections.emptyList());
        return new PageImpl<>(listOfProducts, pageable, totalOfItems[0]);
    }
}