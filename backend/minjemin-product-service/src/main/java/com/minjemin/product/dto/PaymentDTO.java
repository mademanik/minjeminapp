package com.minjemin.product.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class PaymentDTO {
    private Long id;
    private Double amount;
    private LocalDateTime paidAt;
    private Long rentalId;
}
