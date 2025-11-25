package com.minjemin.product.service;

import com.minjemin.product.dto.RentalDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RentalService {
    RentalDTO createRental(RentalDTO dto, String borrowerId, String borrowerName);
    List<RentalDTO> getMyRentals(String borrowerId, String name, String status);
    Page<RentalDTO> getMyRentalsPage(String borrowerId, String name, String status, Pageable pageable);
    Page<RentalDTO> getMyRentalsPageDb(String borrowerId, String name, String status, Pageable pageable);
    List<RentalDTO> getRequestRentals(String ownerId, String name, String status);

    RentalDTO approveRental(Long rentalId, String ownerId);
    RentalDTO startRental(Long rentalId, String borrowerId);
    RentalDTO completeRental(Long rentalId, String actorId);
    RentalDTO cancelRental(Long rentalId, String actorId);
    RentalDTO getById(Long id);
    void deleteRentalById(Long id);
}
