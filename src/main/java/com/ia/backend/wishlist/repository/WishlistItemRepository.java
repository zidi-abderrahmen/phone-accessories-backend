package com.ia.backend.wishlist.repository;

import com.ia.backend.wishlist.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    int deleteByWishlist_UserIdAndAccessory_Id(String userId, Long accessoryId);
    boolean existsByWishlist_UserIdAndAccessory_Id(String userId, Long accessoryId);
    void deleteAllByWishlist_UserId(String userId);
}