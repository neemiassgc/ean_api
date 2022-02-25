package com.xyz.ean.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

@Entity(name = "Product")
@Table(name = "products")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Price> prices;

    @Column(name = "ean_code", unique = true, nullable = false, length = 13)
    private String eanCode;

    public Product(final UUID id, final String name, final String eanCode) {
        this(id, name, new Vector<>(), eanCode);
    }
}
