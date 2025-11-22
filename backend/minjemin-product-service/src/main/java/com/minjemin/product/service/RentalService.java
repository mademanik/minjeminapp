package com.minjemin.product.service;

import com.minjemin.product.dto.RentalDTO;

import java.util.List;

public interface RentalService {
    RentalDTO createRental(RentalDTO dto, String borrowerId);
    List<RentalDTO> getMyRentals(String borrowerId);

    RentalDTO approveRental(Long rentalId, String ownerId);
    RentalDTO startRental(Long rentalId, String borrowerId);
    RentalDTO completeRental(Long rentalId, String actorId);
    RentalDTO cancelRental(Long rentalId, String actorId);
    RentalDTO getById(Long id);
}
