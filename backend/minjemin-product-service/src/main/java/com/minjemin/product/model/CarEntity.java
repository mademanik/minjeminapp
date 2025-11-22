package com.minjemin.product.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class CarEntity {
    private String name;
    private String color;
    private Long price;
}
