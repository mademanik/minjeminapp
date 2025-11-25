package com.minjemin.product.controller;

import com.minjemin.product.dto.RentalDTO;
import com.minjemin.product.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping
    public RentalDTO create(@RequestBody RentalDTO dto, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("sub");
        String username = jwt.getClaim("preferred_username");
        return rentalService.createRental(dto, userId, username);
    }

    @GetMapping("/my")
    public List<RentalDTO> getMy(@AuthenticationPrincipal Jwt jwt,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) String status) {
        String userId = jwt.getClaim("sub");
        return rentalService.getMyRentals(userId, name, status);
    }

    @GetMapping("/my/page")
    public Page<RentalDTO> getMyPage(@AuthenticationPrincipal Jwt jwt,
                                     @RequestParam(required = false) String name,
                                     @RequestParam(required = false) String status, Pageable pageable) {
        String userId = jwt.getClaim("sub");
        return rentalService.getMyRentalsPage(userId, name, status, pageable);
    }

    @GetMapping("/my/pagedb")
    public Page<RentalDTO> getMyPageDb(@AuthenticationPrincipal Jwt jwt,
                                     @RequestParam(required = false) String name,
                                     @RequestParam(required = false) String status, Pageable pageable) {
        String userId = jwt.getClaim("sub");
        return rentalService.getMyRentalsPageDb(userId, name, status, pageable);
    }

    @GetMapping("/request")
    public List<RentalDTO> getRequestRentals(@AuthenticationPrincipal Jwt jwt,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) String status) {
        String ownerId = jwt.getClaim("sub");
        return rentalService.getRequestRentals(ownerId, name, status);
    }

    @PostMapping("/{id}/approve")
    public RentalDTO approve(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("sub");
        return rentalService.approveRental(id, userId);
    }

    @PostMapping("/{id}/start")
    public RentalDTO start(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("sub");
        return rentalService.startRental(id, userId);
    }

    @PostMapping("/{id}/complete")
    public RentalDTO complete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("sub");
        return rentalService.completeRental(id, userId);
    }

    @PostMapping("/{id}/cancel")
    public RentalDTO cancel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("sub");
        return rentalService.cancelRental(id, userId);
    }

    @GetMapping("/{id}")
    public RentalDTO getById(@PathVariable Long id) {
        return rentalService.getById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRentalById(@PathVariable Long id) {
        rentalService.deleteRentalById(id);
        return ResponseEntity.noContent().build();
    }
}
