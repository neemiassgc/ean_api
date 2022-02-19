package com.xyz.ean.service;

import com.xyz.ean.entity.Price;
import com.xyz.ean.repository.PriceRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PriceService {

    private final PriceRepository priceRepository;

    private Price save(@NonNull final Price price) {
        return priceRepository.save(price);
    }
}
