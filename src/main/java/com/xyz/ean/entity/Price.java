package com.xyz.ean.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
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

    @Column(columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime created = LocalDateTime.now();

    @ManyToOne
    private Product product;

    public Price(final double price) {
        this(null, price, LocalDateTime.now(), null);
    }

    @Override
    public String toString() {
        return String.format("Price{id=%s, price=%s, created=%s, product=%s}", id, price, created, product);
    }
}
