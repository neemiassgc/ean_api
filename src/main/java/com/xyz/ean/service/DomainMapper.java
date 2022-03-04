package com.xyz.ean.service;

import com.xyz.ean.entity.Price;
import com.xyz.ean.entity.Product;
import com.xyz.ean.dto.DomainResponse;
import org.springframework.stereotype.Service;

@Service
public class EntityMapper {

    public Product mapProduct(final DomainResponse domainResponse) {
        final Product product = new Product();
        final Price price = new Price();
        product.setDescription(domainResponse.getDescription());
        product.setSequenceCode(domainResponse.getSequence());
        product.setEanCode(domainResponse.getEanCode());
        price.setPrice(domainResponse.getPrice());
        product.addPrice(price);
        return product;
    }
}
