package com.minjemin.product.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Long id;
    private Double amount;
    private LocalDateTime paidAt;
    private Long rentalId;
}
