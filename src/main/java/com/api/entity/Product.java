package com.api.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collections;
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
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String description;

    @Column(name = "barcode", nullable = false, length = 13)
    private String barcode;

    @Column(name = "sequence_code", nullable = false)
    private Integer sequenceCode;

    @Builder
    public Product(final String description, final String barcode, final Integer sequenceCode) {
        this.description = description;
        this.barcode = barcode;
        this.sequenceCode = sequenceCode;
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
