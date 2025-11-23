package com.minjemin.product.mapper;

import com.minjemin.product.dto.RentalDTO;
import com.minjemin.product.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface RentalMapper {
    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.name", target = "itemName")
    RentalDTO toDto(Rental rental);
}
