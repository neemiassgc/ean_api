package com.api.projection;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;

@RequiredArgsConstructor
@Getter
@Builder
@ToString
public final class PriceWithInstant {

    private final BigDecimal value;
    private final Instant instant;
}
