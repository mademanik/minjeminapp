package com.minjemin.product.repository;

import com.minjemin.product.model.Rental;
import com.minjemin.product.model.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByBorrowerId(String borrowerId);
    List<Rental> findByItem_OwnerId(String ownerId);
    List<Rental> findByBorrowerIdAndItem_NameContainingIgnoreCaseAndStatus(
            String borrowerId, String name, RentalStatus status);
    List<Rental> findByItem_IdAndStatusNotIn(Long itemId, List<RentalStatus> excludedStatuses);
    void deleteByItem_Id(Long itemId);
}
