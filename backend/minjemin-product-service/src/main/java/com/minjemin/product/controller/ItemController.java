package com.minjemin.product.controller;

import com.minjemin.product.dto.ItemDTO;
import com.minjemin.product.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDTO create(@RequestBody ItemDTO dto, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("sub");
        return itemService.createItem(dto, userId);
    }

    @GetMapping("/my")
    public List<ItemDTO> myItems(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("sub");
        return itemService.getMyItems(userId);
    }

    @GetMapping("/{id}")
    public ItemDTO getById(@PathVariable Long id) {
        return itemService.getItemById(id);
    }
}
