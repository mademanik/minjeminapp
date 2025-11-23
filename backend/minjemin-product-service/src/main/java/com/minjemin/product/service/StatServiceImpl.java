package com.minjemin.product.service;

import com.minjemin.product.dto.ItemDTO;
import com.minjemin.product.dto.ProductStatDTO;
import com.minjemin.product.dto.RentalDTO;
import com.minjemin.product.dto.RentalStatDTO;
import com.minjemin.product.mapper.ItemMapper;
import com.minjemin.product.mapper.RentalMapper;
import com.minjemin.product.model.Item;
import com.minjemin.product.model.Rental;
import com.minjemin.product.model.RentalStatus;
import com.minjemin.product.repository.ItemRepository;
import com.minjemin.product.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;

    @Override
    public ProductStatDTO totalProduct() {
        List<Item> items = itemRepository.findAll();
        List<ItemDTO> itemDTOS = items.stream().map(itemMapper::toDto).toList();
        return ProductStatDTO.builder()
                .totalProduct(items.size())
                .dataProducts(itemDTOS)
                .build();
    }

    @Override
    public RentalStatDTO totalRental() {
        List<Rental> rentals = rentalRepository.findAll();
        List<RentalDTO> rentalDTOS = rentals.stream().map(rentalMapper::toDto).toList();

        Map<RentalStatus, Long> statusCount = rentals.stream()
                .collect(Collectors.groupingBy(Rental::getStatus, Collectors.counting()));

        return RentalStatDTO.builder()
                .totalRental(rentals.size())
                .dataRentals(rentalDTOS)
                .statuses(statusCount)
                .build();
    }
}
