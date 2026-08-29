package com.ia.backend.cart.repository;

import com.ia.backend.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndAccessoryId(Long cartId, Long accessoryId);
    Optional<CartItem> findByIdAndCart_User_Id(Long id, String userId);
    void deleteAllByCartId(Long cartId);
}