package com.minjemin.product.service;

import com.minjemin.product.dto.RentalDTO;
import com.minjemin.product.exception.BadRequestException;
import com.minjemin.product.mapper.RentalMapper;
import com.minjemin.product.model.Item;
import com.minjemin.product.model.Rental;
import com.minjemin.product.model.RentalStatus;
import com.minjemin.product.repository.ItemRepository;
import com.minjemin.product.repository.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RentalServiceImplTest {
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private RentalMapper rentalMapper;

    @InjectMocks
    private RentalServiceImpl rentalService;

    private final String TEST_BORROWER_ID = "borrower123";
    private final String TEST_OWNER_ID = "owner456";
    private final Long TEST_ITEM_ID = 1L;
    private final Long TEST_RENTAL_ID = 100L;
    private Item testItem;
    private Rental testRental;
    private RentalDTO testRentalDTO;

    @BeforeEach
    void setUp() {
        // Setup Item
        testItem = Item.builder()
                .id(TEST_ITEM_ID)
                .ownerId(TEST_OWNER_ID)
                .stock(5)
                .pricePerDay(Double.valueOf(1000L))
                .name("Laptop Gaming")
                .available(true)
                .build();

        // Setup Rental
        testRental = Rental.builder()
                .id(TEST_RENTAL_ID)
                .item(testItem)
                .borrowerId(TEST_BORROWER_ID)
                .borrowerName("Budi")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .totalPrice((double) 3000L)
                .status(RentalStatus.PENDING)
                .paid(false)
                .build();

        // Setup DTO
        testRentalDTO = RentalDTO.builder()
                .itemId(TEST_ITEM_ID)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .totalPrice((double) 3000L)
                .status("PENDING")
                .build();
    }

    // --------------------------------------------------------------------------------
    // Tests for createRental
    // --------------------------------------------------------------------------------

    @Test
    void createRental_Success() {
        // Arrange
        when(itemRepository.findById(TEST_ITEM_ID)).thenReturn(Optional.of(testItem));
        when(rentalRepository.save(any(Rental.class))).thenReturn(testRental);
        when(rentalMapper.toDto(any(Rental.class))).thenReturn(testRentalDTO);

        // Act
        RentalDTO result = rentalService.createRental(testRentalDTO, TEST_BORROWER_ID, "Budi");

        // Assert
        assertNotNull(result);
        assertEquals(3000L, result.getTotalPrice());
        verify(rentalRepository, times(1)).save(any(Rental.class));
    }

    // --------------------------------------------------------------------------------
    // Tests for getMyRentalsPageDb (Pagination & DB Filtering)
    // --------------------------------------------------------------------------------

    @Test
    void getMyRentalsPageDb_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Rental> rentalPage = new PageImpl<>(Collections.singletonList(testRental), pageable, 1);
        Page<RentalDTO> dtoPage = new PageImpl<>(Collections.singletonList(testRentalDTO), pageable, 1);

        Specification<Rental> mockSpec = mock(Specification.class);

        when(rentalRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(rentalPage);
        when(rentalMapper.toDto(any(Rental.class))).thenReturn(testRentalDTO);

        // Act
        Page<RentalDTO> result = rentalService.getMyRentalsPageDb(TEST_BORROWER_ID, "laptop", "pending", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("PENDING", result.getContent().get(0).getStatus());

        // Repository should call with Specification
        verify(rentalRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    // --------------------------------------------------------------------------------
    // Tests for getMyRentalsPage (In-Memory Pagination)
    // --------------------------------------------------------------------------------

    @Test
    void getMyRentalsPage_Success_FilteringAndPaging() {
        // Arrange
        // create 3 Rental: 2 matching, 1 not matching (status: CANCELLED)
        Rental rental1 = testRental.toBuilder().id(1L).status(RentalStatus.PENDING).build();
        Rental rental2 = testRental.toBuilder().id(2L).status(RentalStatus.PENDING).build();
        Rental rental3 = testRental.toBuilder().id(3L).status(RentalStatus.CANCELLED).build();

        List<Rental> allRentals = Arrays.asList(rental1, rental2, rental3);

        when(rentalRepository.findByBorrowerId(TEST_BORROWER_ID)).thenReturn(allRentals);

        // Mock Mapper: only return DTO. Filter status "pending"
        when(rentalMapper.toDto(rental1)).thenReturn(testRentalDTO.toBuilder().id(1L).status("PENDING").build());
        when(rentalMapper.toDto(rental2)).thenReturn(testRentalDTO.toBuilder().id(2L).status("PENDING").build());
        //when(rentalMapper.toDto(rental3)).thenReturn(testRentalDTO.toBuilder().id(3L).status("CANCELLED").build());

        // Pageable: page 0, Size 1
        Pageable pageable = PageRequest.of(0, 1);

        // Act
        // Filter: name="laptop" (match), status="pending" (match 2 rental)
        Page<RentalDTO> result = rentalService.getMyRentalsPage(TEST_BORROWER_ID, "laptop", "pending", pageable);

        // Assert
        assertEquals(2, result.getTotalElements()); // Total elemen yang difilter adalah 2
        assertEquals(1, result.getContent().size()); // Ukuran halaman adalah 1
        assertEquals(0, result.getNumber()); // Halaman yang diambil adalah 0
        assertEquals(1L, result.getContent().get(0).getId()); // Rental pertama yang diambil
    }

    // --------------------------------------------------------------------------------
    // Tests for approveRental
    // --------------------------------------------------------------------------------

    @Test
    void approveRental_Success() {
        // Arrange
        testRental.setStatus(RentalStatus.PENDING);
        testItem.setStock(5);

        when(rentalRepository.findById(TEST_RENTAL_ID)).thenReturn(Optional.of(testRental));
        when(rentalMapper.toDto(any(Rental.class))).thenReturn(testRentalDTO.toBuilder().status("APPROVED").build());

        // Act
        RentalDTO result = rentalService.approveRental(TEST_RENTAL_ID, TEST_OWNER_ID);

        // Assert
        assertEquals("APPROVED", result.getStatus());
        // Stock harus berkurang 1
        verify(itemRepository, times(1)).save(argThat(item -> item.getStock() == 4));
        verify(rentalRepository, times(1)).save(argThat(rental -> rental.getStatus() == RentalStatus.APPROVED));
    }

    @Test
    void approveRental_Failure_NotOwner() {
        // Arrange
        when(rentalRepository.findById(TEST_RENTAL_ID)).thenReturn(Optional.of(testRental));

        // Act & Assert (not ownerId)
        assertThrows(BadRequestException.class, () ->
                rentalService.approveRental(TEST_RENTAL_ID, "someoneElse")
        );
    }

    @Test
    void approveRental_Failure_NotPending() {
        // Arrange
        testRental.setStatus(RentalStatus.APPROVED); // already APPROVED
        when(rentalRepository.findById(TEST_RENTAL_ID)).thenReturn(Optional.of(testRental));

        // Act & Assert
        assertThrows(BadRequestException.class, () ->
                rentalService.approveRental(TEST_RENTAL_ID, TEST_OWNER_ID)
        );
    }

    // --------------------------------------------------------------------------------
    // Tests for startRental
    // --------------------------------------------------------------------------------

    @Test
    void startRental_Success() {
        // Arrange
        testRental.setStatus(RentalStatus.APPROVED);
        testRental.setPaid(true);
        when(rentalRepository.findById(TEST_RENTAL_ID)).thenReturn(Optional.of(testRental));
        when(rentalMapper.toDto(any(Rental.class))).thenReturn(testRentalDTO.toBuilder().status("ONGOING").build());

        // Act
        RentalDTO result = rentalService.startRental(TEST_RENTAL_ID, TEST_BORROWER_ID);

        // Assert
        assertEquals("ONGOING", result.getStatus());
        verify(rentalRepository, times(1)).save(argThat(rental -> rental.getStatus() == RentalStatus.ONGOING));
    }

    @Test
    void startRental_Failure_MustBeApproved() {
        // Arrange
        testRental.setStatus(RentalStatus.PENDING);
        when(rentalRepository.findById(TEST_RENTAL_ID)).thenReturn(Optional.of(testRental));

        // Act & Assert
        assertThrows(BadRequestException.class, () ->
                rentalService.startRental(TEST_RENTAL_ID, TEST_BORROWER_ID)
        );
    }

    @Test
    void startRental_Failure_MustBePaid() {
        // Arrange
        testRental.setStatus(RentalStatus.APPROVED);
        testRental.setPaid(false); // not paid yet
        when(rentalRepository.findById(TEST_RENTAL_ID)).thenReturn(Optional.of(testRental));

        // Act & Assert
        assertThrows(BadRequestException.class, () ->
                rentalService.startRental(TEST_RENTAL_ID, TEST_BORROWER_ID)
        );
    }

    // --------------------------------------------------------------------------------
    // Tests for completeRental
    // --------------------------------------------------------------------------------

    @Test
    void completeRental_Success_ByOwner() {
        // Arrange
        testRental.setStatus(RentalStatus.ONGOING);
        testItem.setStock(0); // Stock 0 sebelum restore
        when(rentalRepository.findById(TEST_RENTAL_ID)).thenReturn(Optional.of(testRental));
        when(rentalMapper.toDto(any(Rental.class))).thenReturn(testRentalDTO.toBuilder().status("COMPLETED").build());

        // Act
        RentalDTO result = rentalService.completeRental(TEST_RENTAL_ID, TEST_OWNER_ID);

        // Assert
        assertEquals("COMPLETED", result.getStatus());

        // Stock should increment
        verify(itemRepository, times(1)).save(argThat(item -> item.getStock() == 1 && item.getAvailable()));
        verify(rentalRepository, times(1)).save(argThat(rental -> rental.getStatus() == RentalStatus.COMPLETED));
    }

    @Test
    void completeRental_Failure_NotOngoing() {
        // Arrange
        testRental.setStatus(RentalStatus.APPROVED);
        when(rentalRepository.findById(TEST_RENTAL_ID)).thenReturn(Optional.of(testRental));

        // Act & Assert
        assertThrows(BadRequestException.class, () ->
                rentalService.completeRental(TEST_RENTAL_ID, TEST_OWNER_ID)
        );
    }

    // --------------------------------------------------------------------------------
    // Tests for cancelRental
    // --------------------------------------------------------------------------------

    @Test
    void cancelRental_Success_WhenPending() {
        // Arrange
        testRental.setStatus(RentalStatus.PENDING);
        when(rentalRepository.findById(TEST_RENTAL_ID)).thenReturn(Optional.of(testRental));
        when(rentalMapper.toDto(any(Rental.class))).thenReturn(testRentalDTO.toBuilder().status("CANCELLED").build());

        // Act
        RentalDTO result = rentalService.cancelRental(TEST_RENTAL_ID, TEST_BORROWER_ID);

        // Assert
        assertEquals("CANCELLED", result.getStatus());
        verify(rentalRepository, times(1)).save(argThat(rental -> rental.getStatus() == RentalStatus.CANCELLED));
    }

    @Test
    void cancelRental_Failure_WhenApproved() {
        // Arrange
        testRental.setStatus(RentalStatus.APPROVED);
        when(rentalRepository.findById(TEST_RENTAL_ID)).thenReturn(Optional.of(testRental));

        // Act & Assert
        assertThrows(BadRequestException.class, () ->
                rentalService.cancelRental(TEST_RENTAL_ID, TEST_BORROWER_ID)
        );
    }
}
