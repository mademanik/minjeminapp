package com.minjemin.product.service;

import com.minjemin.product.dto.RentalDTO;
import com.minjemin.product.exception.BadRequestException;
import com.minjemin.product.exception.NotFoundException;
import com.minjemin.product.mapper.RentalMapper;
import com.minjemin.product.model.Item;
import com.minjemin.product.model.Rental;
import com.minjemin.product.model.RentalStatus;
import com.minjemin.product.repository.ItemRepository;
import com.minjemin.product.repository.RentalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final ItemRepository itemRepository;
    private final RentalMapper rentalMapper;

    @Override
    @Transactional
    public RentalDTO createRental(RentalDTO dto, String borrowerId) {
        // validasi basic
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new BadRequestException("startDate and endDate required");
        }
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("startDate must be before or equal endDate");
        }

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new RuntimeException("item not found"));

        // owner can't borrow it own goods
        if (item.getOwnerId().equals(borrowerId)) {
            throw new BadRequestException("Owner cannot borrow their own item");
        }

        // cek stock
        if (item.getStock() == null || item.getStock() <= 0) {
            throw new BadRequestException("Item out of stock");
        }

        // total price if not provided
        if (dto.getTotalPrice() == null) {
            long days = dto.getEndDate().toEpochDay() - dto.getStartDate().toEpochDay() + 1;
            dto.setTotalPrice(item.getPricePerDay() * days);
        }

        Rental rental = Rental.builder()
                .item(item)
                .borrowerId(borrowerId)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .totalPrice(dto.getTotalPrice())
                .status(RentalStatus.PENDING)
                .paid(false)
                .build();

        Rental saved = rentalRepository.save(rental);
        return rentalMapper.toDto(saved);
    }

    @Override
    public List<RentalDTO> getMyRentals(String borrowerId) {
        return rentalRepository.findByBorrowerId(borrowerId)
                .stream().map(rentalMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public RentalDTO approveRental(Long rentalId, String ownerId) {
        Rental r = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental not found"));

        // only owner can approve
        if (!r.getItem().getOwnerId().equals(ownerId)) {
            throw new BadRequestException("Only owner can approve rental");
        }

        if (r.getStatus() != RentalStatus.PENDING) {
            throw new BadRequestException("Rental must be in PENDING to approve");
        }

        // reduce stock
        Item item = r.getItem();
        if (item.getStock() <= 0) throw new BadRequestException("Item out of stock");
        item.setStock(item.getStock() - 1);
        if (item.getStock() == 0) item.setAvailable(false);
        itemRepository.save(item);

        r.setStatus(RentalStatus.APPROVED);
        r.setApprovedBy(ownerId);
        rentalRepository.save(r);
        return rentalMapper.toDto(r);
    }

    @Override
    public RentalDTO startRental(Long rentalId, String borrowerId) {
        Rental r = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental not found"));

        if (!r.getBorrowerId().equals(borrowerId)) {
            throw new BadRequestException("Only borrower can start rental (pickup)");
        }

        if (r.getStatus() != RentalStatus.APPROVED) {
            throw new BadRequestException("Rental must be APPROVED to start");
        }

        // optional: check paid
        if (Boolean.FALSE.equals(r.getPaid())) {
            throw new BadRequestException("Rental must be paid before starting");
        }

        r.setStatus(RentalStatus.ONGOING);
        rentalRepository.save(r);
        return rentalMapper.toDto(r);
    }

    @Override
    @Transactional
    public RentalDTO completeRental(Long rentalId, String actorId) {
        Rental r = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental not found"));

        if (!r.getItem().getOwnerId().equals(actorId) && !r.getBorrowerId().equals(actorId)) {
            throw new BadRequestException("Only owner or borrower can complete rental");
        }

        if (r.getStatus() != RentalStatus.ONGOING) {
            throw new BadRequestException("Rental must be ONGOING to complete");
        }

        r.setStatus(RentalStatus.COMPLETED);

        // restore stock
        Item item = r.getItem();
        item.setStock(item.getStock() + 1);
        item.setAvailable(true);
        itemRepository.save(item);

        rentalRepository.save(r);
        return rentalMapper.toDto(r);
    }

    @Override
    @Transactional
    public RentalDTO cancelRental(Long rentalId, String actorId) {
        Rental r = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental not found"));

        // borrower can cancel when PENDING; owner can cancel too
        if (r.getStatus() == RentalStatus.PENDING) {
            r.setStatus(RentalStatus.CANCELLED);
            rentalRepository.save(r);
            return rentalMapper.toDto(r);
        }

        throw new BadRequestException("Only PENDING rentals can be cancelled");
    }

    @Override
    public RentalDTO getById(Long id) {
        return rentalMapper.toDto(rentalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rental not found")));
    }
}
