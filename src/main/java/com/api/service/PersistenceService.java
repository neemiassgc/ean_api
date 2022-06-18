package com.api.service;

import com.api.repository.PriceRepository;
import com.api.repository.ProductRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static com.api.projection.Projection.ProductWithLatestPrice;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PersistenceService {

    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;
    private final ProductExternalService productExternalService;
    private final DomainMapper domainMapper;

    public ProductWithLatestPrice findProductByBarcode(@NonNull final String barcode) {
        return productRepository.findByBarcodeWithLatestPrice(barcode)
            .or(() -> {
                final Optional<ProductWithLatestPrice> optionalProductWithLatestPrice = productExternalService.fetchByEanCode(barcode);
                optionalProductWithLatestPrice
                    .ifPresent(productWithLatestPrice -> priceRepository.save(domainMapper.mapToPrice(productWithLatestPrice)));

                return optionalProductWithLatestPrice;
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }
}