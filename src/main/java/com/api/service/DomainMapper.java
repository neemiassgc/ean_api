package com.api.service;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.pojo.DomainUtils;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.api.projection.Projection.*;

@Service
public class DomainMapper {

    public Price mapToPrice(@NonNull ProductWithLatestPrice productWithLatestPrice) {
        final Product product = new Product();
        product.setDescription(productWithLatestPrice.getDescription());
        product.setSequenceCode(productWithLatestPrice.getSequenceCode());
        product.setBarcode(productWithLatestPrice.getBarcode());

        return new Price(productWithLatestPrice.getLatestPrice().getPrice().doubleValue(), product);
    }

    public ProductBase toProductWithLatestPrice(final Price price) {
        Objects.requireNonNull(price, "Price cannot be null");

        return DomainUtils
            .productWithLatestPriceBuilder()
            .description(price.getProduct().getDescription())
            .barcode(price.getProduct().getBarcode())
            .sequenceCode(price.getProduct().getSequenceCode())
            .latestPrice(new PriceWithInstant(BigDecimal.valueOf(price.getPrice()), price.getInstant()))
            .build();
    }

    public List<ProductBase> toProductListWithManyPrices(final List<Price> prices) {
        Objects.requireNonNull(prices, "Prices cannot be null");

        final Map<Product, List<Price>> mapOfProducts = prices.stream()
            .collect(Collectors.groupingBy(Price::getProduct, HashMap::new, Collectors.toList()));

        return mapOfProducts.values().stream().map(this::toProductWithManyPrices).collect(Collectors.toList());
    }

    public ProductBase toProductWithManyPrices(final List<Price> priceList) {
        Objects.requireNonNull(priceList, "Price cannot be null");
        final Product product = priceList.get(0).getProduct();

        return DomainUtils.productWithManyPricesBuilder()
            .description(product.getDescription())
            .barcode(product.getBarcode())
            .sequenceCode(product.getSequenceCode())
            .prices(
                priceList.stream().map(price ->
                    new PriceWithInstant(BigDecimal.valueOf(price.getPrice()), price.getInstant()))
                    .collect(Collectors.toList()))
            .build();
    }
}
