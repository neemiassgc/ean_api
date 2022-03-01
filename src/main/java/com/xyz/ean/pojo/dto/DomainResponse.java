package com.xyz.ean.pojo.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public final class DomainResponse {

    private double price;
    private String description;
    private int sequence;
    private String eanCode;
}
