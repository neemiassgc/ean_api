package com.api.projection;

import com.api.projection.deserializer.ProductWithLatestPriceDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
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
        BigDecimal getLatestPrice();
    }

    public interface ProductWithAllPrices extends ProductBase {
        List<BigDecimal> getPrices();
    }
}
