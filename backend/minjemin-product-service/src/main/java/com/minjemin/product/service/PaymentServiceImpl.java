package com.minjemin.product.service;

import com.minjemin.product.exception.BadRequestException;
import com.minjemin.product.exception.NotFoundException;
import com.minjemin.product.model.Payment;
import com.minjemin.product.model.Rental;
import com.minjemin.product.repository.PaymentRepository;
import com.minjemin.product.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final RentalRepository rentalRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public Payment createPayment(Long rentalId, String payerId, Double amount) {
        Rental r = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental not found"));

        if (!r.getBorrowerId().equals(payerId)) {
            throw new BadRequestException("Only borrower can pay for rental");
        }

        if (Boolean.TRUE.equals(r.getPaid())) {
            throw new BadRequestException("Rental already paid");
        }

        // mock external payment: create Payment record
        Payment p = Payment.builder()
                .rental(r)
                .amount(amount)
                .paymentRef("MOCK-" + System.currentTimeMillis())
                .build();

        Payment saved = paymentRepository.save(p);

        // set rental paid
        r.setPaid(true);
        rentalRepository.save(r);

        return saved;
    }
}
