package com.xyz.ean.service;

import com.xyz.ean.dto.ProductResponseDTO;
import com.xyz.ean.entity.Price;
import com.xyz.ean.entity.Product;
import com.xyz.ean.dto.DomainResponse;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class DomainMapper {

    public Product mapToProduct(final DomainResponse domainResponse) {
        final Product product = new Product();
        final Price price = new Price();
        product.setDescription(domainResponse.getDescription());
        product.setSequenceCode(domainResponse.getSequence());
        product.setEanCode(domainResponse.getEanCode());
        price.setPrice(domainResponse.getPrice());
        product.addPrice(price);
        return product;
    }

    public ProductResponseDTO mapToDto(final Product product) {
        return ProductResponseDTO.builder()
            .description(product.getDescription())
            .eanCode(product.getEanCode())
            .sequenceCode(product.getSequenceCode())
            .prices(product.getPrices().stream().map(Price::getPrice).collect(Collectors.toList()))
            .build();
    }
}
