package com.xyz.ean.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
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

    private LocalDate created = LocalDate.now();

    @ManyToOne
    private Product product;

    public Price(final double price) {
        this(null, price, LocalDate.now(), null);
    }

    @Override
    public String toString() {
        return String.format("Price{id=%s, price=%s, created=%s, product=%s}", id, price, created, product);
    }
}
