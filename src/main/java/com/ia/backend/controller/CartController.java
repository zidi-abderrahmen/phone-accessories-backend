package com.ia.backend.controller;

import com.ia.backend.dto.cart.CartResponse;
import com.ia.backend.dto.cart.item.CartItemRequest;
import com.ia.backend.dto.cart.item.CartItemResponse;
import com.ia.backend.dto.cart.item.UpdateCartItemRequest;
import com.ia.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts/my-cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    @PostMapping
    public ResponseEntity<CartItemResponse> addItemToCart(@Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItemToCart(request));
    }

    @PutMapping("/cart-item/{id}")
    public ResponseEntity<CartItemResponse> updateItemInCart(@PathVariable Long id, @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateCartItemQuantity(id, request));
    }

    @DeleteMapping("/cart-item/{id}")
    public ResponseEntity<Void> removeItemFromCart(@PathVariable Long id) {
        cartService.removeItemFromCart(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cart/{id}")
    public ResponseEntity<Void> clearCart(@PathVariable Long id) {
        cartService.clearCart(id);
        return ResponseEntity.noContent().build();
    }

}