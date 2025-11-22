package com.minjemin.product.service;

import com.minjemin.product.dto.ItemDTO;

import java.util.List;

public interface ItemService {
    ItemDTO createItem(ItemDTO dto, String userId);
    List<ItemDTO> getMyItems(String userId);
    ItemDTO getItemById(Long id);
}
