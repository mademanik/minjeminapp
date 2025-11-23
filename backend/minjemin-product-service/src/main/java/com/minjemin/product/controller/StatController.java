package com.minjemin.product.controller;

import com.minjemin.product.dto.ProductStatDTO;
import com.minjemin.product.dto.RentalStatDTO;
import com.minjemin.product.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/stats")
@RestController
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    @GetMapping("/products")
    public ResponseEntity<ProductStatDTO> productStatistics() {
        ProductStatDTO response = statService.totalProduct();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rentals")
    public ResponseEntity<RentalStatDTO> rentalStatistics() {
        RentalStatDTO response = statService.totalRental();
        return ResponseEntity.ok(response);
    }
}
