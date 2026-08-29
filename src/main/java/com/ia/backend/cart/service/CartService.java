package com.ia.backend.cart.service;

import com.ia.backend.cart.dto.CartResponse;
import com.ia.backend.cart.dto.items.CartItemRequest;
import com.ia.backend.cart.dto.items.CartItemResponse;
import com.ia.backend.cart.dto.items.UpdateCartItemRequest;
import com.ia.backend.accessory.entity.Accessory;
import com.ia.backend.user.entity.User;
import com.ia.backend.cart.entity.Cart;
import com.ia.backend.cart.entity.CartItem;
import com.ia.backend.common.exception.AccessDeniedException;
import com.ia.backend.common.exception.NotFoundException;
import com.ia.backend.accessory.mapper.AccessoryMapper;
import com.ia.backend.cart.mapper.CartItemMapper;
import com.ia.backend.user.mapper.UserMapper;
import com.ia.backend.accessory.repository.AccessoryRepository;
import com.ia.backend.cart.repository.CartItemRepository;
import com.ia.backend.cart.repository.CartRepository;
import com.ia.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final AccessoryRepository accessoryRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final UserMapper userMapper;
    private final CartItemMapper cartItemMapper;
    private final AccessoryMapper accessoryMapper;

    @Transactional
    public CartResponse getMyCart() {
        log.info("Getting current user");
        User currentUser = userService.getCurrentUserEntity();

        log.info("Fetching cart for user: {}", currentUser.getId());
        Cart existingOrNewCart = cartRepository.findByUser_Id(currentUser.getId())
                .orElseGet(() -> {
                    log.info("Creating new cart for user: {}", currentUser.getId());
                    Cart newCart = Cart.builder().user(currentUser).build();
                    cartRepository.save(newCart);
                    return newCart;
                });

        List<CartItemResponse> cartItemResponses = existingOrNewCart.getCartItems()
                .stream()
                .map(cartItemMapper::toResponse)
                .toList();

        return new CartResponse(
                existingOrNewCart.getId(),
                userMapper.toUserResponse(currentUser),
                cartItemResponses,
                existingOrNewCart.getCreatedAt(),
                existingOrNewCart.getUpdatedAt()
        );
    }

    @Transactional
    public CartItemResponse addItemToCart(CartItemRequest request) {
        User currentUser = userService.getCurrentUserEntity();

        Cart existingCart = cartRepository.findByUser_Id(currentUser.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(currentUser).build()));

        if (!existingCart.getUser().getId().equals(currentUser.getId())) {
            log.error("User {} attempted to modify cart {} they do not own.", currentUser.getId(), existingCart.getId());
            throw new AccessDeniedException("You do not have access to this cart.");
        }

        if (request.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        Accessory existingAccessory = accessoryRepository.findById(request.accessoryId())
                .orElseThrow(() -> {
                    log.error("Accessory not found.");
                    return new NotFoundException("Accessory not found.");
                });

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndAccessoryId(existingCart.getId(), existingAccessory.getId());

        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.quantity();

            if (newQuantity > existingAccessory.getStock()) {
                throw new IllegalArgumentException("Requested quantity exceeds available stock.");
            }
            cartItem.setQuantity(newQuantity);
            log.debug("Merged quantity into existing cart item: {}", cartItem.getId());
        } else {
            if (request.quantity() > existingAccessory.getStock()) {
                throw new IllegalArgumentException("Requested quantity exceeds available stock.");
            }
            cartItem = cartItemMapper.toEntity(request);
            cartItem.setAccessory(existingAccessory);
            cartItem.setCart(existingCart);
            log.debug("Creating new cart item: {}", cartItem);
        }

        cartItemRepository.save(cartItem);
        log.debug("Cart item saved successfully.");

        return  new CartItemResponse(
                cartItem.getId(),
                accessoryMapper.toDto(existingAccessory),
                cartItem.getQuantity()
        );
    }

    @Transactional
    public CartItemResponse updateCartItemQuantity(Long id, UpdateCartItemRequest request) {
        User currentUser = userService.getCurrentUserEntity();

        CartItem cartItem = getCartItem(id, currentUser.getId());

        if (request.newQuantity() > cartItem.getAccessory().getStock()) {
            log.error("Requested quantity exceeds available stock.");
            throw new IllegalArgumentException("Requested quantity exceeds available stock.");
        }

        cartItem.setQuantity(request.newQuantity());

        return new CartItemResponse(
                cartItem.getId(),
                accessoryMapper.toDto(cartItem.getAccessory()),
                cartItem.getQuantity()
        );
    }

    @Transactional
    public void removeItemFromCart(Long cartItemId) {
        User currentUser = userService.getCurrentUserEntity();

        CartItem cartItem = getCartItem(cartItemId, currentUser.getId());

        cartItemRepository.delete(cartItem);
        log.debug("Cart item {} removed successfully.", cartItemId);
    }

    @Transactional
    public void clearCart() {
        User currentUser = userService.getCurrentUserEntity();
        Long cartId = currentUser.getCart().getId();
        Cart existingCart = cartRepository.findById(cartId)
                .orElseThrow(() -> {
                    log.error("Cart not found.");
                    return new NotFoundException("Cart not found.");
                });

        if (!existingCart.getUser().getId().equals(currentUser.getId())) {
            log.error("User {} attempted to clear cart {} they do not own.", currentUser.getId(), cartId);
            throw new AccessDeniedException("You do not have access to this cart.");
        }

        cartItemRepository.deleteAllByCartId(existingCart.getId());
        log.debug("Cart {} cleared successfully.", cartId);
    }

    @Transactional
    protected CartItem getCartItem(Long cartItemId, String currentUserId) {
        return cartItemRepository.findByIdAndCart_User_Id(cartItemId, currentUserId)
                .orElseThrow(() -> {
                    log.error("Cart item not found.");
                    return new NotFoundException("Cart item not found.");
                });
    }
}