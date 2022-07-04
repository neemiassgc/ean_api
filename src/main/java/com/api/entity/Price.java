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

    private Double value;

    @Column(name = "instant", columnDefinition = "TIMESTAMPTZ")
    private Instant instant = Instant.now();

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_product_prices"))
    private Product product;

    public Price(final Double value, final Product product) {
        this(value, Instant.now(), product);
    }

    public Price(final Double value, final Instant instant, final Product product) {
        this(null, value, instant, product);
    }

    @Override
    public String toString() {
        return String.format("Price{id=%s, value=%s, created=%s, product=%s}", id, value, instant, product);
    }
}
