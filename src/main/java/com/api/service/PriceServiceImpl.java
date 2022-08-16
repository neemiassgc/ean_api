package com.api.service;

import com.api.entity.Price;
import com.api.repository.PriceRepository;
import com.api.service.interfaces.PriceService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional(readOnly = true)
public class PriceServiceImpl implements PriceService {

    private final PriceRepository priceRepository;

    @Override
    public Price findById(@NonNull UUID id) {
        return priceRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Price not found"));
    }

    @Override
    public List<Price> findByProductBarcode(@NonNull String barcode, @NonNull Sort sort) {
        final List<Price> prices = priceRepository.findByProductBarcode(barcode, sort);
        if (prices.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        return prices;
    }

    @Override
    public List<Price> findByProductBarcode(@NonNull String barcode, @NonNull Pageable pageable) {
        final List<Price> prices = priceRepository.findByProductBarcode(barcode, pageable);
        if (prices.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        return prices;
    }
}
