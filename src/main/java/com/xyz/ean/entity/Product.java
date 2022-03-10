package com.xyz.ean.entity;

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
        @UniqueConstraint(name = "uk_ean", columnNames = "ean_code"),
        @UniqueConstraint(name = "uk_sequence_code", columnNames = "sequence_code")
    }
)
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
    private List<Price> prices = new Vector<>();

    @Column(name = "ean_code", nullable = false, length = 13)
    private String eanCode;

    @Column(name = "sequence_code", nullable = false)
    private Integer sequenceCode;

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
            eanCode,
            sequenceCode,
            this.prices.stream().map(price -> price.getPrice().toString()).collect(Collectors.joining(","))
        );
    }
}
