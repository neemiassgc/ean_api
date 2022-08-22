package com.api.projection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class Projection {

    private Projection() {}

    public interface SimpleProduct {
        String getDescription();
        String getBarcode();
        Integer getSequenceCode();
    }

    @Getter
    @RequiredArgsConstructor
    @ToString
    public static class PriceWithInstant {
        final BigDecimal value;
        final Instant instant;
    }
}
