package com.ia.backend.repository.cart;

import com.ia.backend.entity.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndAccessoryId(Long cartId, Long accessoryId);
    Optional<CartItem> findByIdAndCart_User_Id(Long id, String userId);
    void deleteAllByCartId(Long cartId);
}