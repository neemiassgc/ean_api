package com.api.service;

import com.api.dto.InputItemDTO;
import com.api.dto.ProductResponseDTO;
import com.api.entity.Price;
import com.api.entity.Product;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DomainMapper {

    public Product mapToProduct(final InputItemDTO inputItemDTO) {
        final Product product = new Product();
        final Price price = new Price();
        product.setDescription(inputItemDTO.getDescription());
        product.setSequenceCode(inputItemDTO.getSequence());
        product.setBarcode(inputItemDTO.getEanCode());
        price.setPrice(inputItemDTO.getCurrentPrice());
        product.addPrice(price);
        return product;
    }

    public ProductResponseDTO mapToDto(final Product product) {
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
        return products.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }
}
