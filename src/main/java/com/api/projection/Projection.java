package com.api.projection;

import com.api.projection.deserializer.ProductWithLatestPriceDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;

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

    @JsonDeserialize(using = ProductWithLatestPriceDeserializer.class)
    public interface ProductWithLatestPrice extends ProductBase {

        @Value("#{new com.api.projection.Projection.PriceWithInstant(new java.math.BigDecimal(target.latestPrice.toString()), target.instant)}")
        PriceWithInstant getLatestPrice();
    }

    public interface ProductWithManyPrices extends ProductBase {
        List<PriceWithInstant> getPrices();
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
        final BigDecimal price;
        final Instant instant;
    }
}
