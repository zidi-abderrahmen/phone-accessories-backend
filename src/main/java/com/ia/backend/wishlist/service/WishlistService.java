package com.ia.backend.wishlist.service;

import com.ia.backend.wishlist.dto.WishlistResponse;
import com.ia.backend.wishlist.dto.items.WishlistItemResponse;
import com.ia.backend.accessory.entity.Accessory;
import com.ia.backend.user.entity.User;
import com.ia.backend.wishlist.entity.Wishlist;
import com.ia.backend.wishlist.entity.WishlistItem;
import com.ia.backend.common.exception.AlreadyExistException;
import com.ia.backend.common.exception.NotFoundException;
import com.ia.backend.wishlist.mapper.WishlistItemMapper;
import com.ia.backend.wishlist.mapper.WishlistMapper;
import com.ia.backend.accessory.repository.AccessoryRepository;
import com.ia.backend.wishlist.repository.WishlistItemRepository;
import com.ia.backend.wishlist.repository.WishlistRepository;
import com.ia.backend.user.service.UserService;
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