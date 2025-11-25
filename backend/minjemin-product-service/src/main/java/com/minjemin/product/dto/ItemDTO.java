package com.minjemin.product.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ItemDTO {
    private Long id;
    private String name;
    private String description;
    private Double pricePerDay;
    private boolean available;
    private String ownerId;
    private Integer stock;
}
