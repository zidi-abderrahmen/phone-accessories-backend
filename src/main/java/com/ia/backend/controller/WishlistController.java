package com.ia.backend.controller;

import com.ia.backend.dto.wishlist.WishlistResponse;
import com.ia.backend.dto.wishlist.items.WishlistItemResponse;
import com.ia.backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<WishlistResponse> getMyWishlist() {
        return ResponseEntity.ok(wishlistService.getMyWishlist());
    }

    @PostMapping("/items/{accessoryId}")
    public ResponseEntity<WishlistItemResponse> addToWishlist(
            @PathVariable Long accessoryId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wishlistService.addToWishlist(accessoryId));
    }

    @DeleteMapping("/items/{wishId}")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable Long wishId
    ) {
        wishlistService.removeFromWishlist(wishId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items")
    public ResponseEntity<Void> clearWishlist() {
        wishlistService.clearWishlist();
        return ResponseEntity.noContent().build();
    }
}