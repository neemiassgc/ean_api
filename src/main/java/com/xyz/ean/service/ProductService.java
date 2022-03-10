package com.xyz.ean.service;

import com.xyz.ean.entity.Price;
import com.xyz.ean.entity.Product;
import com.xyz.ean.repository.ProductRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductService {

    private final ProductRepository  productRepository;
    private final ForeignProductHttpService foreignProductHttpService;

    public Product saveByEanCode(@NonNull final String eanCode) {
        final Product productToReturn = productRepository.findByEanCode(eanCode)
            .or(() -> {
                final Optional<Product> fetchedProduct = foreignProductHttpService.fetchByEanCode(eanCode);
                fetchedProduct.ifPresent(productRepository::save);
                return fetchedProduct;
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        this.pricesOrdering(productToReturn);
        return productToReturn;
    }

    public Product save(@NonNull final Product product) {
        return productRepository.save(product);
    }

    public Product findByEanCode(@NonNull final String eanCode) {
        return productRepository.findByEanCode(eanCode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public List<Product> findAll() {
        return productRepository.findAll().stream().peek(this::pricesOrdering).collect(Collectors.toList());
    }

    private void pricesOrdering(final Product product) {
        final List<Price> orderedPrices = product
            .getPrices()
            .stream()
            .sorted(Comparator.comparing(Price::getCreated).reversed())
            .limit(2)
            .collect(Collectors.toList());

        product.setPrices(orderedPrices);
    }
}
