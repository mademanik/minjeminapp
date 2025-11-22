package com.minjemin.product.controller;

import com.minjemin.product.model.Payment;
import com.minjemin.product.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/rental/{rentalId}")
    public Payment pay(@PathVariable Long rentalId,
                       @RequestParam Double amount,
                       @AuthenticationPrincipal Jwt jwt) {
        String payerId = jwt.getClaim("sub");
        return paymentService.createPayment(rentalId, payerId, amount);
    }
}
