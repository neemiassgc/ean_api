package com.xyz.ean.service;

import com.xyz.ean.dto.StandardProductDTO;
import com.xyz.ean.dto.ProductResponseDTO;
import com.xyz.ean.entity.Price;
import com.xyz.ean.entity.Product;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DomainMapper {

    public Product mapToProduct(final StandardProductDTO standardProductDTO) {
        final Product product = new Product();
        final Price price = new Price();
        product.setDescription(standardProductDTO.getDescription());
        product.setSequenceCode(standardProductDTO.getSequence());
        product.setEanCode(standardProductDTO.getEanCode());
        price.setPrice(standardProductDTO.getPrice());
        product.addPrice(price);
        return product;
    }

    public ProductResponseDTO mapToDto(final Product product) {
        return ProductResponseDTO.builder()
            .description(product.getDescription())
            .eanCode(product.getEanCode())
            .sequenceCode(product.getSequenceCode())
            .prices(product
                .getPrices()
                .stream()
                .map(ProductResponseDTO.PriceDateTime::new)
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
