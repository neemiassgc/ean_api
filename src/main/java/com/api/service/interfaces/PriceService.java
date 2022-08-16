package com.api.service.interfaces;

import com.api.entity.Price;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface PriceService {

    Price findById(UUID id);

    List<Price> findByProductBarcode(String barcode, Sort sort);

    List<Price> findByProductBarcode(String barcode, Pageable pageable);
}
