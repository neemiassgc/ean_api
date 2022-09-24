package com.api.service;

import com.api.entity.Product;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.api.projection.Projection.SimpleProduct;

@Service
public class DomainMapper {

    public SimpleProduct mapToSimpleProduct(@NonNull final Product product) {
        return new SimpleProduct() {
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
        };
    }

    public List<SimpleProduct> mapToSimpleProductList(List<Product> productList) {
        return productList
            .stream()
            .map(this::mapToSimpleProduct)
            .collect(Collectors.toList());
    }
}
