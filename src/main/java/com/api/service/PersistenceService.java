package com.api.service;

import com.api.entity.Price;
import static com.api.projection.Projection.*;
import com.api.repository.PriceRepository;
import com.api.repository.ProductRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PersistenceService {

    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;
    private final ProductExternalService productExternalService;
    private final DomainMapper domainMapper;

    public <I extends ProductBase> I findProductByBarcode(@NonNull final String barcode, int limit) {
        final List<Price> priceList =
        priceRepository.findAllByProductBarcode(
            barcode,
            PageRequest.ofSize(limit == 0 ? Integer.MAX_VALUE : limit)
        );

        if (!priceList.isEmpty())
            return (I) domainMapper.toProductWithManyPrices(priceList);

        final ProductBase productBase = productExternalService.fetchByBarcode(barcode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        priceRepository.save(domainMapper.mapToPrice((ProductWithLatestPrice) productBase));

        return (I) productBase;
    }

    public List<ProductWithManyPrices> findAllProducts() {
        final List<Price> priceList = priceRepository.findAll();
        return domainMapper.toProductListWithManyPrices(priceList);
    }

    public List<ProductWithLatestPrice> findAllProductsWithLatestPrice() {
        return domainMapper.toProductListWithLatestPrice(priceRepository.findAllLatestPrice());
    }

    public void saveProductWithPrice(final ProductWithLatestPrice productWithLatestPrice) {
        priceRepository.save(domainMapper.mapToPrice(productWithLatestPrice));
    }
}