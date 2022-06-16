package com.api.projection;

import com.api.projection.deserializer.ProductWithLatestPriceDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.util.List;

public final class Projection {

    private Projection() {}

    @JsonDeserialize(using = ProductWithLatestPriceDeserializer.class)
    public interface ProductWithLatestPrice {
        String getDescription();
        String getBarcode();
        Integer getSequenceCode();
        BigDecimal getLatestPrice();
    }

    public interface ProductWithAllPrices {
        String getDescription();
        String getBarcode();
        Integer getSequenceCode();
        List<BigDecimal> getPrices();
    }
}
