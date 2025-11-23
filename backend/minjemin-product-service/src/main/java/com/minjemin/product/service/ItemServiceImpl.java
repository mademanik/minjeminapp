package com.minjemin.product.service;

import com.minjemin.product.dto.ItemDTO;
import com.minjemin.product.exception.BadRequestException;
import com.minjemin.product.exception.NotFoundException;
import com.minjemin.product.mapper.ItemMapper;
import com.minjemin.product.model.Item;
import com.minjemin.product.model.Rental;
import com.minjemin.product.model.RentalStatus;
import com.minjemin.product.repository.ItemRepository;
import com.minjemin.product.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final RentalRepository rentalRepository;

    @Override
    public ItemDTO createItem(ItemDTO dto, String userId) {
        Item item = itemMapper.toEntity(dto);
        item.setOwnerId(userId);
        item.setAvailable(true);
        Item saved = itemRepository.save(item);
        return itemMapper.toDto(saved);
    }

    @Override
    public List<ItemDTO> getMyItems(String userId, String name, Double minPrice, Double maxPrice) {
        return itemRepository.findByOwnerId(userId)
                .stream()
                .filter(item -> name == null || item.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(item -> minPrice == null || item.getPricePerDay() >= minPrice)
                .filter(item -> maxPrice == null || item.getPricePerDay() <= maxPrice)
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDTO getItemById(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("item not found"));
        return itemMapper.toDto(item);
    }

    @Override
    public ItemDTO updateItemById(Long id, ItemDTO dto) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("item not found"));
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPricePerDay(dto.getPricePerDay());
        item.setAvailable(dto.isAvailable());
        item.setStock(dto.getStock());

        Item saved = itemRepository.save(item);
        return itemMapper.toDto(saved);
    }

    @Override
    public void deleteItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        List<Rental> rentals = rentalRepository.findByItem_IdAndStatusNotIn(id,
                List.of(RentalStatus.COMPLETED, RentalStatus.CANCELLED));

        if (!rentals.isEmpty()) {
            throw new BadRequestException("Item still used in Rentals");
        }

        itemRepository.delete(item);
        rentalRepository.deleteByItem_Id(id);
    }
}
