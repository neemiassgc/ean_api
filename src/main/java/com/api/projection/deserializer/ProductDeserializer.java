package com.api.projection.deserializer;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.pojo.DomainUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.Objects;

public final class ProductDeserializer extends StdDeserializer<Product> {

    public ProductDeserializer() {
        this(null);
    }

    public ProductDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    @Nullable
    public Product deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final JsonNode jsonNode = p.getCodec().readTree(p);

        final JsonNode item = jsonNode.get("item");

        if (!Objects.nonNull(item)) throw new IllegalStateException("Item node does not exist");

        if (item.get(5).get("value").asText().isEmpty())
            throw new IllegalStateException("Item name is empty");

        final Price latestPrice =
            new Price(DomainUtils.parsePrice(item.get(4).get("value").asText()));

        return Product.builder()
            .description(item.get(1).get("value").asText())
            .barcode(item.get(5).get("value").asText())
            .sequenceCode(item.get(2).get("value").asInt())
            .build()
            .addPrice(latestPrice);
    }
}
