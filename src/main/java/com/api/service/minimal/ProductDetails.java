package com.api.service.minimal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public final class ProductDetails {
    private final String description;
    private final BigDecimal oldPrice;
    private final BigDecimal newPrice;
}
