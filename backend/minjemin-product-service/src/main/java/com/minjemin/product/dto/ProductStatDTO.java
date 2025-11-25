package com.minjemin.product.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class ProductStatDTO {
    private Integer totalProduct;
    private List<ItemDTO> dataProducts;
}
