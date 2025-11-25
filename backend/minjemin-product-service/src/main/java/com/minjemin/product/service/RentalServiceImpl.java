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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
    public RentalDTO createRental(RentalDTO dto, String borrowerId, String borrowerName) {
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
                .borrowerName(borrowerName)
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
    public List<RentalDTO> getMyRentals(String borrowerId, String name, String status) {
        String normalizedName = (name == null || name.isBlank()) ? null : name.toLowerCase();
        String normalizedStatus = (status == null || status.isBlank()) ? null : status.toLowerCase();

        return rentalRepository.findByBorrowerId(borrowerId)
                .stream()
                .filter(rental -> normalizedName == null
                        || rental.getItem().getName().toLowerCase().contains(normalizedName))
                .filter(rental -> normalizedStatus == null
                        || rental.getStatus().toString().toLowerCase().contains(normalizedStatus))
                .map(rentalMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RentalDTO> getMyRentalsPage(String borrowerId, String name, String status, Pageable pageable) {
        String normalizedName = (name == null || name.isBlank()) ? null : name.toLowerCase();
        String normalizedStatus = (status == null || status.isBlank()) ? null : status.toLowerCase();

        List<Rental> allRentals = rentalRepository.findByBorrowerId(borrowerId);

        List<RentalDTO> filteredDtos = allRentals.stream()
                .filter(rental -> normalizedName == null
                        || rental.getItem().getName().toLowerCase().contains(normalizedName))
                .filter(rental -> normalizedStatus == null
                        || rental.getStatus().toString().toLowerCase().contains(normalizedStatus))
                .map(rentalMapper::toDto)
                .collect(Collectors.toList());

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;

        List<RentalDTO> pageList;

        if (filteredDtos.size() < startItem) {
            pageList = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, filteredDtos.size());
            pageList = filteredDtos.subList(startItem, toIndex);
        }

        return new PageImpl<>(pageList, pageable, filteredDtos.size());
    }

    @Override
    public Page<RentalDTO> getMyRentalsPageDb(String borrowerId, String name, String status, Pageable pageable) {
        Specification<Rental> spec = RentalSpecification.filterRentals(borrowerId, name, status);
        Page<Rental> rentalPage = rentalRepository.findAll(spec, pageable);
        return rentalPage.map(rentalMapper::toDto);
    }

    @Override
    public List<RentalDTO> getRequestRentals(String ownerId, String name, String status) {
        String normalizedName = (name == null || name.isBlank()) ? null : name.toLowerCase();
        String normalizedStatus = (status == null || status.isBlank()) ? null : status.toLowerCase();

        return rentalRepository.findByItem_OwnerId(ownerId)
                .stream()
                .filter(rental -> normalizedName == null
                        || rental.getItem().getName().toLowerCase().contains(normalizedName))
                .filter(rental -> normalizedStatus == null
                        || rental.getStatus().toString().toLowerCase().contains(normalizedStatus))
                .map(rentalMapper::toDto)
                .collect(Collectors.toList());
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

    @Override
    public void deleteRentalById(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rental not found"));

        rentalRepository.delete(rental);
    }
}
