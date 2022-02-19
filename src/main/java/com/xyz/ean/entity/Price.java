package com.xyz.ean.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Calendar;
import java.util.UUID;

@Entity
@Table(name = "prices")
public class Price {

    @Id
    @GeneratedValue
    private UUID id;

    private double price;

    private Calendar created_at;
}
