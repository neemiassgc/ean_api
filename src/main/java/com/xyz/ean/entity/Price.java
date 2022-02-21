package com.xyz.ean.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Calendar;
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

    private Calendar created_at;

    @OneToMany(mappedBy = "prices")
    private Product product;
}
