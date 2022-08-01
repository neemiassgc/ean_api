package com.api.repository;

import com.api.entity.Price;
import com.api.projection.ProjectionFactory;
import com.api.service.DomainMapper;
import com.api.service.ProductExternalService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static com.api.projection.Projection.*;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Service
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    @Setter(onMethod_ = @Autowired, onParam_ = @Lazy)
    private ProductRepository productRepository;

    private final PriceRepository priceRepository;
    private final ProductExternalService productExternalService;
    private final DomainMapper domainMapper;

    public <I extends ProductBase> I findProductByBarcode(@NonNull final String barcode, int limit) {
        if (limit < 0) throw new IllegalArgumentException("limit must be a positive number or zero");

        final List<Price> priceList =
        priceRepository.findAllByProductBarcode(
            barcode,
            PageRequest.ofSize(limit == 0 ? Integer.MAX_VALUE : limit)
        );

        if (!priceList.isEmpty())
            return (I) domainMapper.toProductWithManyPrices(priceList);

        // Save a new product
        final ProductBase productBase = productExternalService.fetchByBarcode(barcode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        this.saveProductWithPrice((ProductWithLatestPrice) productBase);

        return (I) productBase;
    }

    public List<ProductWithManyPrices> findAllProducts() {
        final List<Price> priceList = priceRepository.findAll();
        return domainMapper.toProductListWithManyPrices(priceList);
    }

    public Paged<List<ProductWithManyPrices>> findAllPagedProducts(@NonNull final Pageable pageable) {
        final Page<UUID> page = productRepository.findAllId(pageable);
        final List<ProductWithManyPrices> productWithManyPricesList =
            domainMapper.toProductListWithManyPrices(priceRepository.findAllByProductId(page.getContent()));

        return ProjectionFactory.paged(page, productWithManyPricesList);
    }

    public List<ProductWithLatestPrice> findAllProductsWithLatestPrice() {
        return domainMapper.toProductListWithLatestPrice(priceRepository.findAllLatestPrice());
    }

    public void saveProductWithPrice(@NonNull final ProductWithLatestPrice productWithLatestPrice) {
        final Price priceToSave = domainMapper.mapToPrice(productWithLatestPrice);
        priceRepository.save(priceToSave);
    }
}