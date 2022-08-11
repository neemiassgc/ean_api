package com.api.entity;

import com.api.projection.deserializer.ProductDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;

import javax.persistence.*;
import java.util.*;

@JsonDeserialize(using = ProductDeserializer.class)
@Entity(name = "Product")
@Table(
    name = "products",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_barcode", columnNames = "barcode"),
        @UniqueConstraint(name = "uk_sequence_code", columnNames = "sequence_code")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(nullable = false)
    private String description;

    @Column(name = "barcode", nullable = false, length = 13)
    private String barcode;

    @Column(name = "sequence_code", nullable = false)
    private Integer sequenceCode;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<Price> prices = new ArrayList<>();

    @Builder
    public Product(final String description, final String barcode, final Integer sequenceCode) {
        this.description = description;
        this.barcode = barcode;
        this.sequenceCode = sequenceCode;
    }

    @Override
    public String toString() {
        return String.format(
            "Product{id=%s, description=%s, barcode=%s, sequenceCode=%s}",
            id,
            description,
            barcode,
            sequenceCode
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, barcode, sequenceCode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Product)) return false;

        final Product that = (Product) obj;
        return Objects.equals(this.description, that.getDescription())
            && Objects.equals(this.barcode, that.getBarcode())
            && Objects.equals(this.sequenceCode, that.getSequenceCode());
    }

    public Product addPrice(@NonNull final Price price) {
        if (!Objects.isNull(price.getProduct()))
            throw new IllegalStateException("Product is already assigned to a Price");

        price.setProduct(this);
        this.prices.add(price);

        return this;
    }

    public boolean removePrice(@NonNull final Price price) {
        price.setProduct(null);
        return this.prices.remove(price);
    }

    public List<Price> getPrices() {
        return Collections.unmodifiableList(prices);
    }
}
