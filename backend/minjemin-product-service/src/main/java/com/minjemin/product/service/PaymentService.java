package com.minjemin.product.service;

import com.minjemin.product.model.Payment;

public interface PaymentService {
    Payment createPayment(Long rentalId, String payerId, Double amount);
}
