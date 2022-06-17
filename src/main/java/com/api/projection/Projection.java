package com.api.projection;

import com.api.projection.deserializer.ProductWithLatestPriceDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class Projection {

    private Projection() {}

    private interface ProductBase {
        String getDescription();
        String getBarcode();
        Integer getSequenceCode();
    }

    @JsonDeserialize(using = ProductWithLatestPriceDeserializer.class)
    public interface ProductWithLatestPrice extends ProductBase {
        PriceWithInstant getLatestPrice();
    }

    public interface ProductWithAllPrices extends ProductBase {
        List<PriceWithInstant> getPrices();
    }

    @Getter
    @RequiredArgsConstructor
    @ToString
    public static class PriceWithInstant {
        final BigDecimal price;
        final Instant instant;
    }
}
