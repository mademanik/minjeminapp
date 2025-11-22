package com.minjemin.product.mapper;

import com.minjemin.product.dto.ItemDTO;
import com.minjemin.product.model.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDTO toDto(Item item);

    @Mapping(target = "id", ignore = true)
    Item toEntity(ItemDTO dto);
}
