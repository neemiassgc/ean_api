package com.api.service;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.pojo.DomainUtils;
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

    public Price mapToPrice(final ProductWithLatestPrice productWithLatestPrice) {
        Objects.requireNonNull(productWithLatestPrice, "InputItemDTO cannot be null");

        final Product product = new Product();

        product.setDescription(productWithLatestPrice.getDescription());
        product.setSequenceCode(productWithLatestPrice.getSequenceCode());
        product.setBarcode(productWithLatestPrice.getBarcode());

        return new Price(productWithLatestPrice.getLatestPrice().getPrice().doubleValue(), product);
    }

    public ProductWithLatestPrice mapToProductWithLatestPrice(final Price price) {
        Objects.requireNonNull(price, "Price cannot be null");

        return DomainUtils
            .productWithLatestPriceBuilder()
            .description(price.getProduct().getDescription())
            .barcode(price.getProduct().getBarcode())
            .sequenceCode(price.getProduct().getSequenceCode())
            .latestPrice(new PriceWithInstant(BigDecimal.valueOf(price.getPrice()), price.getInstant()))
            .build();
    }

    public List<ProductWithAllPrices> toProductListWithAllPrices(final List<Price> prices) {
        Objects.requireNonNull(prices, "Prices cannot be null");

        final Map<Product, List<Price>> mapOfProducts = prices.stream()
            .collect(Collectors.groupingBy(Price::getProduct, HashMap::new, Collectors.toList()));

        return mapOfProducts.values().stream().map(this::toProductWithAllPrices).collect(Collectors.toList());
    }

    public ProductWithAllPrices toProductWithAllPrices(final List<Price> priceList) {
        Objects.requireNonNull(priceList, "Price cannot be null");
        final Product product = priceList.get(0).getProduct();

        return DomainUtils.productWithAllPricesBuilder()
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
