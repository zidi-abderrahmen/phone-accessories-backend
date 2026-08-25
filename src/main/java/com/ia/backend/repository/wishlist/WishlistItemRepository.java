package com.ia.backend.repository.wishlist;

import com.ia.backend.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    int deleteByWishlist_UserIdAndAccessory_Id(String userId, Long accessoryId);
    boolean existsByWishlist_UserIdAndAccessory_Id(String userId, Long accessoryId);
    void deleteAllByWishlist_UserId(String userId);
}