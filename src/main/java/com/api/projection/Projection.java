package com.api.projection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;

public final class Projection {

    private Projection() {}

    public interface SimpleProduct {
        String getDescription();
        String getBarcode();
        Integer getSequenceCode();
    }

    public interface Paged<T> {
        int getCurrentPage();
        int getTotalPages();
        int getNumberOfItems();
        boolean getHasNext();
        T getContent();
    }

    @Getter
    @RequiredArgsConstructor
    @ToString
    public static class PriceWithInstant {
        final BigDecimal value;
        final Instant instant;
    }
}
