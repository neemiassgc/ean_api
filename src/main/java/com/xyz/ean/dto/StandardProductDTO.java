package com.xyz.ean.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public final class StandardProductDTO {

    private double price;
    private String description;
    private int sequence;
    private String eanCode;
}
