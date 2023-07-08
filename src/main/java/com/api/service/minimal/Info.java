package com.api.service.minimal;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public final class Info {
    private final List<ProductDetails> productDetailsList;
    private final int totalOfProducts;
    private final long elapsedTimeInSeconds;
}
