package com.api.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Collectors;

@Entity(name = "Product")
@Table(
    name = "products",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_barcode", columnNames = "barcode"),
        @UniqueConstraint(name = "uk_sequence_code", columnNames = "sequence_code")
    }
)
@NamedEntityGraph(name = "prices_entity_graph", attributeNodes = @NamedAttributeNode("prices")) // It defines which entities need to be fetched in join query
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String description;

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("instant DESC")
    private List<Price> prices = new Vector<>();

    @Column(name = "barcode", nullable = false, length = 13)
    private String barcode;

    @Column(name = "sequence_code", nullable = false)
    private Integer sequenceCode;

    @Builder
    public Product(final String description, final String barcode, final Integer sequenceCode, final Price... prices) {
        this.description = description;
        this.barcode = barcode;
        this.sequenceCode = sequenceCode;
        this.addPrice(prices);
    }

    public void addPrice(final Price... prices) {
        for (Price price : prices) {
            this.prices.add(price);
            price.setProduct(this);
        }
    }

    public void remove(final Price... prices) {
        for (Price price : prices) {
            this.prices.remove(price);
            price.setProduct(null);
        }
    }

    @Override
    public String toString() {
        return String.format(
            "Product{id=%s, description=%s, eanCode=%s, sequenceCode=%s, prices=%s}",
            id,
            description,
                barcode,
            sequenceCode,
            this.prices.stream().map(price -> price.getPrice().toString()).collect(Collectors.joining(","))
        );
    }
}
