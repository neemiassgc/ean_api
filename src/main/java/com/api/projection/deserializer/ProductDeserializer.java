package com.api.projection.deserializer;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.component.DomainUtils;
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

    private void verifyIfTheSessionIsValid(final JsonNode item) {
        if (!Objects.nonNull(item))
            // Possibly, the session is not valid, and it's necessary to create a new one
            throw new IllegalStateException("Item node does not exist");
    }

    private void verifyIfTheProductExist(final JsonNode item) {
        if (item.get(5).get("value").asText().isEmpty())
            throw new IllegalStateException("Item name is empty"); // When a product doesn't exist
    }

    @Override
    @Nullable
    public Product deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final JsonNode jsonNode = p.getCodec().readTree(p);
        final JsonNode item = jsonNode.get("item");

        verifyIfTheSessionIsValid(item);
        verifyIfTheProductExist(item);

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
