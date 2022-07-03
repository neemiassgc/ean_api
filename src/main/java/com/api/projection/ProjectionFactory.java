package com.api.projection;

import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

import static com.api.projection.Projection.*;

public class ProjectionFactory {
    @Builder(builderMethodName = "productWithManyPricesBuilder")
    public static ProductWithManyPrices productWithManyPrices(
            final String description,
            final String barcode,
            final Integer sequenceCode,
            final List<PriceWithInstant> prices
    ) {
        return new ProductWithManyPrices() {
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
            public List<PriceWithInstant> getPrices() {
                return prices;
            }
        };
    }

    @Builder(builderMethodName = "productWithLatestPriceBuilder")
    public static ProductWithLatestPrice productWithLatestPrice(
            final String description,
            final String barcode,
            final Integer sequenceCode,
            final PriceWithInstant latestPrice
    ) {
        return new ProductWithLatestPrice() {
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
            public PriceWithInstant getLatestPrice() {
                return latestPrice;
            }
        };
    }

    public static <T> Paged<T> paged(Page<?> page, final T content) {
        return new Paged<T>() {
            @Override
            public int getCurrentPage() {
                return page.getNumber();
            }

            @Override
            public int getTotalPages() {
                return page.getTotalPages();
            }

            @Override
            public int getNumberOfItems() {
                return page.getNumberOfElements();
            }

            @Override
            public boolean getHasNext() {
                return page.hasNext();
            }

            @Override
            public T getContent() {
                return content;
            }
        };
    }

}
