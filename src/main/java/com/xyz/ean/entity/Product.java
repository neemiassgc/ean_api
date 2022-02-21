package com.xyz.ean.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
    private UUID id;

    private String name;

    @ManyToOne(cascade = CascadeType.ALL)
    private List<Price> prices;

    @Column(name = "ean_code", unique = true, nullable = false, length = 13)
    private String eanCode;
}
