package com.minjemin.product.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class RentalDTO {
    private Long id;
    private Long itemId;
    private String itemName;
    private String borrowerId;
    private String borrowerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalPrice;
    private String status;
    private String approvedBy;
    private Boolean paid;
}
