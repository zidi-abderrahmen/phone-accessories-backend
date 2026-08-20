package com.ia.backend.service;

import com.ia.backend.dto.cart.CartResponse;
import com.ia.backend.dto.cart.item.CartItemRequest;
import com.ia.backend.dto.cart.item.CartItemResponse;
import com.ia.backend.dto.cart.item.UpdateCartItemRequest;
import com.ia.backend.entity.Accessory;
import com.ia.backend.entity.User;
import com.ia.backend.entity.cart.Cart;
import com.ia.backend.entity.cart.CartItem;
import com.ia.backend.exception.AccessDeniedException;
import com.ia.backend.exception.NotFoundException;
import com.ia.backend.mapper.AccessoryMapper;
import com.ia.backend.mapper.CartItemMapper;
import com.ia.backend.mapper.UserMapper;
import com.ia.backend.repository.AccessoryRepository;
import com.ia.backend.repository.cart.CartItemRepository;
import com.ia.backend.repository.cart.CartRepository;
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
    public void clearCart(Long cartId) {
        User currentUser = userService.getCurrentUserEntity();
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