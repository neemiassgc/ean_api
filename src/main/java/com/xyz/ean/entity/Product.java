package com.xyz.ean.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

@Entity(name = "Product")
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String description;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Price> prices = new Vector<>();

    @Column(name = "ean_code", unique = true, nullable = false, length = 13)
    private String eanCode;

    @Column(name = "sequence_code", unique = true, nullable = false)
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
}
