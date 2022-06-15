package com.api.entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Price {

    @Id
    @GeneratedValue
    private UUID id;

    private Double price;

    @Column(name = "instant", columnDefinition = "TIMESTAMPTZ")
    private Instant instant = Instant.now();

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_product_prices"))
    private Product product;

    public Price(final Double price, final Product product) {
        this(price, Instant.now(), product);
    }

    public Price(final Double price, final Instant instant, final Product product) {
        this(null, price, instant, product);
    }

    @Override
    public String toString() {
        return String.format("Price{id=%s, price=%s, created=%s, product=%s}", id, price, instant, product);
    }
}
