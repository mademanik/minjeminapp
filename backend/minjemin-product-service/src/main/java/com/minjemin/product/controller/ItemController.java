package com.minjemin.product.controller;

import com.minjemin.product.dto.ItemDTO;
import com.minjemin.product.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public List<ItemDTO> myItems(@AuthenticationPrincipal Jwt jwt,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) Double minPrice,
                                 @RequestParam(required = false) Double maxPrice) {
        String userId = jwt.getClaim("sub");
        return itemService.getMyItems(userId, name, minPrice, maxPrice);
    }

    @GetMapping("")
    public List<ItemDTO> getAllItems() {
        return itemService.getAllItems();
    }

    @GetMapping("/{id}")
    public ItemDTO getById(@PathVariable Long id) {
        return itemService.getItemById(id);
    }

    @PutMapping("/{id}")
    public ItemDTO updateItemById(@PathVariable Long id, @RequestBody ItemDTO dto) {
        return itemService.updateItemById(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemById(@PathVariable Long id) {
        itemService.deleteItemById(id);
        return ResponseEntity.noContent().build();
    }
}
