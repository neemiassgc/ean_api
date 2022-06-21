package com.api.projection.deserializer;

import com.api.pojo.DomainUtils;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

import static com.api.projection.Projection.PriceWithInstant;
import static com.api.projection.Projection.ProductBase;

public final class ProductBaseDeserializer extends StdDeserializer<ProductBase> {

    public ProductBaseDeserializer() {
        this(null);
    }

    public ProductBaseDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    @Nullable
    public ProductBase deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        final JsonNode jsonNode = p.getCodec().readTree(p);

        final JsonNode item = jsonNode.get("item");

        if (Objects.isNull(item)) return null;

        if (item.get(5).get("value").asText().isEmpty()) throw new IllegalStateException("Item name is empty");

        final PriceWithInstant latestPrice =
            new PriceWithInstant(DomainUtils.parsePrice(item.get(4).get("value").asText()), Instant.now());

        return DomainUtils.productWithLatestPriceBuilder()
            .description(item.get(1).get("value").asText())
            .barcode(item.get(5).get("value").asText())
            .sequenceCode(item.get(2).get("value").asInt())
            .latestPrice(latestPrice)
            .build();
    }
}
