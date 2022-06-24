package com.api.projection;

import lombok.Builder;

import java.util.List;

public class ProjectionFactory {
    @Builder(builderMethodName = "productWithManyPricesBuilder")
    public static Projection.ProductWithManyPrices productWithManyPrices(
            final String description,
            final String barcode,
            final Integer sequenceCode,
            final List<Projection.PriceWithInstant> prices
    ) {
        return new Projection.ProductWithManyPrices() {
            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public String getBarcode() {
                return barcode;
            }

            @Override
            public Integer getSequenceCode() {
                return sequenceCode;
            }

            @Override
            public List<Projection.PriceWithInstant> getPrices() {
                return prices;
            }
        };
    }

    @Builder(builderMethodName = "productWithLatestPriceBuilder")
    public static Projection.ProductWithLatestPrice productWithLatestPrice(
            final String description,
            final String barcode,
            final Integer sequenceCode,
            final Projection.PriceWithInstant latestPrice
    ) {
        return new Projection.ProductWithLatestPrice() {
            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public String getBarcode() {
                return barcode;
            }

            @Override
            public Integer getSequenceCode() {
                return sequenceCode;
            }

            @Override
            public Projection.PriceWithInstant getLatestPrice() {
                return latestPrice;
            }
        };
    }
}
