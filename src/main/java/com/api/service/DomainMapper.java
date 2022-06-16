package com.api.service;

import com.api.projection.ProductResponseDTO;
import com.api.entity.Price;
import com.api.entity.Product;
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

    public List<Projection.ProductWithAllPrices> toProductWithAllPrices(final List<Price> prices) {
        Objects.requireNonNull(prices, "Prices cannot be null");

        final Map<Product, List<Price>> mapOfProducts = prices.stream()
            .collect(Collectors.groupingBy(Price::getProduct, HashMap::new, Collectors.toList()));

        return mapOfProducts.entrySet().stream().map(entry -> {
            final Product product = entry.getKey();
            return new Projection.ProductWithAllPrices() {
                @Override
                public String getDescription() {
                    return product.getDescription();
                }

                @Override
                public String getBarcode() {
                    return product.getBarcode();
                }

                @Override
                public Integer getSequenceCode() {
                    return product.getSequenceCode();
                }

                @Override
                public List<BigDecimal> getPrices() {
                    return entry.getValue().stream().map(price -> new BigDecimal(price.getPrice()+"")).collect(Collectors.toList());
                }
            };
        }).collect(Collectors.toList());
    }

    public List<ProductResponseDTO> mapToDtoList(final List<Product> products) {
        Objects.requireNonNull(products, "Products cannot be null");

        return products.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }
}
