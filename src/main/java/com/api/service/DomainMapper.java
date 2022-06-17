package com.api.service;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.pojo.DomainUtils;
import com.api.projection.Projection;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DomainMapper {

    public Price mapToPrice(final Projection.ProductWithLatestPrice productWithLatestPrice) {
        Objects.requireNonNull(productWithLatestPrice, "InputItemDTO cannot be null");

        final Product product = new Product();

        product.setDescription(productWithLatestPrice.getDescription());
        product.setSequenceCode(productWithLatestPrice.getSequenceCode());
        product.setBarcode(productWithLatestPrice.getBarcode());

        return new Price(productWithLatestPrice.getLatestPrice().doubleValue(), product);
    }

    public Projection.ProductWithLatestPrice mapToProductWithLatestPrice(final Price price) {
        Objects.requireNonNull(price, "Price cannot be null");

        return DomainUtils
            .productWithLatestPriceBuilder()
            .description(price.getProduct().getDescription())
            .barcode(price.getProduct().getBarcode())
            .sequenceCode(price.getProduct().getSequenceCode())
            .latestPrice(BigDecimal.valueOf(price.getPrice()))
            .build();
    }

    public List<Projection.ProductWithAllPrices> toProductListWithAllPrices(final List<Price> prices) {
        Objects.requireNonNull(prices, "Prices cannot be null");

        final Map<Product, List<Price>> mapOfProducts = prices.stream()
            .collect(Collectors.groupingBy(Price::getProduct, HashMap::new, Collectors.toList()));

        return mapOfProducts.entrySet().stream().map(entry -> {
            final Product product = entry.getKey();

            return DomainUtils.productWithAllPricesBuilder()
                .description(product.getDescription())
                .barcode(product.getBarcode())
                .sequenceCode(product.getSequenceCode())
                .prices(entry.getValue().stream().map(price -> new BigDecimal(price.getPrice() + "")).collect(Collectors.toList()))
                .build();
        }).collect(Collectors.toList());
    }
}
