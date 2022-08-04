package com.api.service;

import static com.api.projection.Projection.*;

import com.api.entity.Price;
import com.api.entity.Product;
import lombok.NonNull;
import org.springframework.stereotype.Service;

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

    public PriceWithInstant mapToPriceWithInstant(@NonNull final Price price) {
        return new PriceWithInstant(price.getValue(), price.getInstant());
    }
}
