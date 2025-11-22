package com.minjemin.product.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RentalDTO {
    private Long id;
    private Long itemId;
    private String borrowerId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalPrice;
    private String status;
    private String approvedBy;
    private Boolean paid;
}
