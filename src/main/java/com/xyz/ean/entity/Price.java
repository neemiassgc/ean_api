package com.xyz.ean.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "prices")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Price {

    @Id
    @GeneratedValue
    private UUID id;

    private double price;

    private LocalDate created;

    @ManyToOne
    private Product product;
}
