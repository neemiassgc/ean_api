package com.api.service.minimal;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public final class ProductDetails {
    private final String description;
    private final BigDecimal oldPrice;
    private final BigDecimal newPrice;
}
