package com.api.service;

import com.api.entity.Price;
import com.api.repository.PriceRepository;
import com.api.service.interfaces.PriceService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional(readOnly = true)
public class PriceServiceImpl implements PriceService {

    private final PriceRepository priceRepository;
    private final CacheManager<Price, UUID> priceCacheManager;

    @Override
    public Price findById(@NonNull UUID id) {
        return priceCacheManager.sync(id.toString(),
            () -> priceRepository.findById(id).map(List::of).orElse(Collections.emptyList())
        )
        .map(l -> l.get(0))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Price not found"));
    }

    @Override
    public List<Price> findByProductBarcode(@NonNull String barcode, @NonNull Sort sort) {
        final Optional<List<Price>> optionalPrices =
               priceCacheManager.sync(barcode+sort, () -> priceRepository.findByProductBarcode(barcode, sort));
        if (optionalPrices.isPresent()) return optionalPrices.get();
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
    }

    @Override
    public List<Price> findByProductBarcode(@NonNull String barcode, @NonNull Pageable pageable) {
        final Optional<List<Price>> optionalOfPrices = priceCacheManager.sync(
            generateLink(barcode, pageable),
            () -> priceRepository.findByProductBarcode(barcode, pageable)
        );
        if (optionalOfPrices.isPresent()) return optionalOfPrices.get();
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
    }

    private String generateLink(final String barcode, final Pageable pageable) {
        return String.format("%s-pag=%s-%s", barcode, pageable.getPageNumber(), pageable.getPageSize());
    }
}