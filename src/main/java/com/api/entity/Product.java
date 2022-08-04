package com.api.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;
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

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Price> prices;

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
        return Objects.equals(this.description, that.description)
            && Objects.equals(this.barcode, that.barcode)
            && Objects.equals(this.sequenceCode, that.sequenceCode);
    }
}
