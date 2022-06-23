package com.api.service;

import com.api.entity.Price;
import com.api.repository.PriceRepository;
import com.api.repository.ProductRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.api.projection.Projection.ProductBase;
import static com.api.projection.Projection.ProductWithLatestPrice;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PersistenceService {

    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;
    private final ProductExternalService productExternalService;
    private final DomainMapper domainMapper;

    public ProductBase findProductByBarcode(@NonNull final String barcode, int limit) {
        final List<Price> priceList =
        priceRepository.findAllByProductBarcode(
            barcode,
            PageRequest.ofSize(limit == 0 ? Integer.MAX_VALUE : limit)
        );

        if (!priceList.isEmpty())
            return domainMapper.toProductWithManyPrices(priceList);

        final ProductBase productBase = productExternalService.fetchByBarcode(barcode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        priceRepository.save(domainMapper.mapToPrice((ProductWithLatestPrice) productBase));

        return productBase;
    }

    public List<ProductBase> findAllProducts() {
        final List<Price> priceList = priceRepository.findAll();
        return domainMapper.toProductListWithManyPrices(priceList);
    }

    public <I extends ProductBase> List<I> findAllProductsWithLatestPrice() {
        //noinspection unchecked
        return (List<I>) domainMapper.toProductListWithLatestPrice(priceRepository.findAllLatestPrice());
    }

    public void saveProductWithPrice(final ProductWithLatestPrice productWithLatestPrice) {
        priceRepository.save(domainMapper.mapToPrice(productWithLatestPrice));
    }

    public ProductBase findProductByBarcode(@NonNull final String barcode) {
        return findProductByBarcode(barcode, 0);
    }
}