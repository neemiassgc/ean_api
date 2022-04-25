package com.api.service;

import com.api.entity.Price;
import com.api.repository.PriceRepository;
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
