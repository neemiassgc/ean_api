package com.api.service;

import com.api.projection.ProductResponseDTO;
import com.api.entity.Price;
import com.api.entity.Product;
import com.api.projection.Projection;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public ProductResponseDTO mapToDto(final Product product) {
        Objects.requireNonNull(product, "Product cannot be null");

        return ProductResponseDTO.builder()
            .description(product.getDescription())
            .barcode(product.getBarcode())
            .sequenceCode(product.getSequenceCode())
            .priceInstants(product
                .getPrices()
                .stream()
                .map(ProductResponseDTO.PriceInstant::from)
                .collect(Collectors.toList())
            )
            .build();

        return null;
    }

    public List<ProductResponseDTO> mapToDtoList(final List<Product> products) {
        Objects.requireNonNull(products, "Products cannot be null");

        return products.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }
}
