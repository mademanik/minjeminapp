package com.minjemin.product.service;

import com.minjemin.product.dto.ItemDTO;
import com.minjemin.product.exception.NotFoundException;
import com.minjemin.product.model.Item;
import com.minjemin.product.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public ItemDTO createItem(ItemDTO dto, String userId) {
        Item item = Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .pricePerDay(dto.getPricePerDay())
                .available(true)
                .ownerId(userId)
                .stock(dto.getStock())
                .build();

        Item saved = itemRepository.save(item);
        dto.setId(saved.getId());
        dto.setOwnerId(userId);
        dto.setAvailable(true);
        return dto;
    }

    @Override
    public List<ItemDTO> getMyItems(String userId) {
        return itemRepository.findByOwnerId(userId)
                .stream()
                .map(item -> {
                    ItemDTO dto = new ItemDTO();
                    dto.setId(item.getId());
                    dto.setName(item.getName());
                    dto.setDescription(item.getDescription());
                    dto.setPricePerDay(item.getPricePerDay());
                    dto.setOwnerId(item.getOwnerId());
                    dto.setAvailable(item.getAvailable());
                    dto.setStock(item.getStock());
                    return dto;
                }).collect(Collectors.toList());
    }

    @Override
    public ItemDTO getItemById(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("item not found"));

        ItemDTO dto = new ItemDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPricePerDay(item.getPricePerDay());
        dto.setOwnerId(item.getOwnerId());
        dto.setAvailable(item.getAvailable());
        dto.setStock(item.getStock());

        return dto;
    }
}
