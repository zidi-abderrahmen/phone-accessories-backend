package com.ia.backend.service;

import com.ia.backend.dto.wishlist.WishlistResponse;
import com.ia.backend.dto.wishlist.items.WishlistItemResponse;
import com.ia.backend.entity.Accessory;
import com.ia.backend.entity.User;
import com.ia.backend.entity.Wishlist;
import com.ia.backend.entity.WishlistItem;
import com.ia.backend.exception.AlreadyExistException;
import com.ia.backend.exception.NotFoundException;
import com.ia.backend.mapper.wishlist.WishlistItemMapper;
import com.ia.backend.mapper.wishlist.WishlistMapper;
import com.ia.backend.repository.AccessoryRepository;
import com.ia.backend.repository.wishlist.WishlistItemRepository;
import com.ia.backend.repository.wishlist.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final UserService userService;
    private final WishlistMapper wishlistMapper;
    private final WishlistItemMapper wishlistItemMapper;
    private final AccessoryRepository accessoryRepository;

    @Transactional
    public WishlistResponse getMyWishlist() {
        log.info("Fetching current user's wishlist");
        User currentUser = userService.getCurrentUserEntity();

        return wishlistMapper.toResponse(createWishlist(currentUser));
    }

    @Transactional
    public WishlistItemResponse addToWishlist(Long accessoryId) {
        User currentUser = userService.getCurrentUserEntity();

        Accessory accessory = accessoryRepository.findById(accessoryId)
                .orElseThrow(() -> new NotFoundException("Accessory not found"));

        Wishlist wishlist = createWishlist(currentUser);

        if (wishlistItemRepository.existsByWishlist_UserIdAndAccessory_Id(
                currentUser.getId(),
                accessory.getId())) {
            throw new AlreadyExistException("Accessory already exists in wishlist");
        }

        WishlistItem wishlistItem = WishlistItem.builder()
                .wishlist(wishlist)
                .accessory(accessory)
                .build();

        log.info("Adding accessory {} to wishlist {}", accessory.getId(), wishlist.getId());
        return wishlistItemMapper.toResponse(
                wishlistItemRepository.save(wishlistItem)
        );
    }

    @Transactional
    public void removeFromWishlist(Long accessoryId) {
        log.info("Removing accessory {} from wishlist", accessoryId);
        User currentUser = userService.getCurrentUserEntity();
        int response = wishlistItemRepository.deleteByWishlist_UserIdAndAccessory_Id(currentUser.getId(), accessoryId);

        if (response == 0) {
            log.error("Accessory not found in wishlist");
            throw new NotFoundException("Accessory not found in wishlist");
        }
    }

    @Transactional
    public void clearWishlist() {
        log.info("Clearing current user's wishlist");
        User currentUser = userService.getCurrentUserEntity();
        wishlistItemRepository.deleteAllByWishlist_UserId(currentUser.getId());
    }

    private Wishlist createWishlist(User currentUser) {
        return wishlistRepository.findByUserId(currentUser.getId())
                        .orElseGet(() -> {
                            log.info("Creating new wishlist for user: {}", currentUser.getId());
                            Wishlist newWishlist = Wishlist.builder().user(currentUser).build();
                            wishlistRepository.save(newWishlist);
                            return newWishlist;
                        });
    }
}