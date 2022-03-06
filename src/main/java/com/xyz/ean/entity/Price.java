package com.xyz.ean.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Price {

    @Id
    @GeneratedValue
    private UUID id;

    private Double price;

    @Column(columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime created = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_product_prices_product"))
    private Product product;

    public Price(final double price) {
        this(null, price, LocalDateTime.now(), null);
    }

    @Override
    public String toString() {
        return String.format("Price{id=%s, price=%s, created=%s, product=%s}", id, price, created, product);
    }
}
