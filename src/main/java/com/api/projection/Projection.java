package com.api.projection;

import com.api.projection.deserializer.ProductBaseDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class Projection {

    private Projection() {}

    public interface ProductBase {
        String getDescription();
        String getBarcode();
        Integer getSequenceCode();
    }

    @JsonDeserialize(using = ProductBaseDeserializer.class)
    public interface ProductWithLatestPrice extends ProductBase {
        PriceWithInstant getLatestPrice();
    }

    public interface ProductWithManyPrices extends ProductBase {
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
