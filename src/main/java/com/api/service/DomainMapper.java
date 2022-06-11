package com.api.service;

import com.api.dto.InputItemDTO;
import com.api.dto.ProductResponseDTO;
import com.api.entity.Price;
import com.api.entity.Product;
import com.api.pojo.DomainUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DomainMapper {

    public Product mapToProduct(final InputItemDTO inputItemDTO) {
        Objects.requireNonNull(inputItemDTO, "InputItemDTO cannot be null");

        final Product product = new Product();
        final Price price = new Price();
        product.setDescription(inputItemDTO.getDescription());
        product.setSequenceCode(inputItemDTO.getSequence());
        product.setBarcode(inputItemDTO.getBarcode());
        price.setPrice(inputItemDTO.getCurrentPrice());
        product.addPrice(price);
        return product;
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
    }

    public List<ProductResponseDTO> mapToDtoList(final List<Product> products) {
        Objects.requireNonNull(products, "Products cannot be null");

        return products.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }
}
