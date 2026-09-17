package com.ia.backend.order.service;

import com.ia.backend.order.dto.OrderRequest;
import com.ia.backend.order.dto.OrderResponse;
import com.ia.backend.accessory.entity.Accessory;
import com.ia.backend.user.entity.User;
import com.ia.backend.cart.entity.Cart;
import com.ia.backend.cart.entity.CartItem;
import com.ia.backend.common.enums.OrderStatus;
import com.ia.backend.order.enums.PaymentMethod;
import com.ia.backend.order.enums.PaymentStatus;
import com.ia.backend.order.enums.ShippingMethod;
import com.ia.backend.order.entity.Order;
import com.ia.backend.order.entity.OrderItem;
import com.ia.backend.common.exception.NotFoundException;
import com.ia.backend.order.mapper.OrderMapper;
import com.ia.backend.order.payment.MockPaymentGateway;
import com.ia.backend.order.payment.PaymentResult;
import com.ia.backend.cart.repository.CartRepository;
import com.ia.backend.order.repository.OrderRepository;
import com.ia.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ia.backend.common.exception.BadRequestException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserService userService;
    private final OrderMapper orderMapper;
    private final MockPaymentGateway paymentGateway;

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrdersHistory() {
        User currentUser = getCurrentUser();

        log.info("Fetching orders for user: {}", currentUser.getId());
        List<Order> orders = orderRepository.findAllByUserIdWithItems(currentUser.getId());

        log.info("Found {} orders for user: {}", orders.size(), currentUser.getId());
        return orderMapper.toResponses(orders);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        User currentUser = getCurrentUser();

        log.info("Fetching order with id: {} for user: {}", id, currentUser.getId());
        Order order = orderRepository.findByIdAndUserIdWithItems(id, currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        User currentUser = getCurrentUser();

        Cart cart = cartRepository.findByUser_Id(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Cannot create an order from an empty cart");
        }

        if (currentUser.getPhoneNumber() == null || currentUser.getPhoneNumber().isEmpty()) {
            currentUser.setPhoneNumber(request.customerPhoneNumber());
        }

        for (CartItem cartItem : cart.getCartItems()) {
            Accessory accessory = cartItem.getAccessory();
            if (accessory.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for: " + accessory.getTitle());
            }

            accessory.setStock(accessory.getStock() - cartItem.getQuantity());
        }

        Order order = orderMapper.toEntity(request);
        order.setUser(currentUser);
        order.setStatus(OrderStatus.PENDING);

        Set<OrderItem> orderItems = cart.getCartItems()
                .stream()
                .map(cartItem -> OrderItem.builder()
                        .order(order)
                        .accessory(cartItem.getAccessory())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getAccessory().getPrice())
                        .build())
                .collect(Collectors.toSet());

        order.setItems(orderItems);

        BigDecimal subtotal = orderItems.stream()
                .map(item -> item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fee = BigDecimal.valueOf(
                request.shippingMethod() == ShippingMethod.STANDARD ? 7 : 15);

        order.setTotalAmount(subtotal.add(fee));

        order.setPaymentStatus(PaymentStatus.PENDING);
        if (request.paymentMethod() != PaymentMethod.CASH_ON_DELIVERY) {
            PaymentResult payment = paymentGateway.charge(
                    request.paymentMethod(), order.getTotalAmount());

            if (payment.status() == PaymentStatus.FAILED) {
                throw new BadRequestException(
                        "Payment was declined. Please try another payment method.");
            }

            order.setPaymentStatus(payment.status());
            order.setPaymentReference(payment.reference());
            order.setPaidAt(payment.paidAt());
        }

        Order savedOrder = orderRepository.save(order);
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return orderMapper.toResponse(savedOrder);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = getValidatedPendingOrder(
                id,
                OrderStatus.PENDING,
                "Cannot cancel an order that is already ");

        order.getItems().forEach(item -> {
            Accessory accessory = item.getAccessory();
            accessory.setStock(accessory.getStock() + item.getQuantity());
        });

        order.setStatus(OrderStatus.CANCELLED);

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.info("Refunding mock payment for cancelled order {} (reference={})",
                    order.getId(), order.getPaymentReference());
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = getValidatedPendingOrder(
                id,
                OrderStatus.CANCELLED,
                "Cannot delete an order that is already ");

        order.getItems().forEach(item -> {
            Accessory accessory = item.getAccessory();
            accessory.setStock(accessory.getStock() + item.getQuantity());
        });

        orderRepository.delete(order);
    }

    private Order getValidatedPendingOrder(Long id, OrderStatus status, String errorMessage) {
        User currentUser = getCurrentUser();

        Order order = orderRepository.findByIdAndUserIdWithItems(id, currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getStatus() != status) {
            throw new BadRequestException(
                    errorMessage + order.getStatus());
        }

        return order;
    }

    private User getCurrentUser() {
        log.info("Getting current user");
        return userService.getCurrentUserEntity();
    }
}