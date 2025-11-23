package com.minjemin.product.service;

import com.minjemin.product.dto.ItemDTO;

import java.util.List;

public interface ItemService {
    ItemDTO createItem(ItemDTO dto, String userId);
    List<ItemDTO> getMyItems(String userId, String name, Double minPrice, Double maxPrice);
    ItemDTO getItemById(Long id);
    ItemDTO updateItemById(Long id, ItemDTO dto);
    void deleteItemById(Long id);
}
