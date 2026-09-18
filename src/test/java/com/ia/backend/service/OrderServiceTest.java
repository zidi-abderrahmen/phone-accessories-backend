package com.ia.backend.service;

import com.ia.backend.accessory.entity.Accessory;
import com.ia.backend.cart.entity.Cart;
import com.ia.backend.cart.entity.CartItem;
import com.ia.backend.cart.repository.CartRepository;
import com.ia.backend.common.exception.BadRequestException;
import com.ia.backend.common.exception.NotFoundException;
import com.ia.backend.common.enums.OrderStatus;
import com.ia.backend.order.dto.OrderRequest;
import com.ia.backend.order.enums.PaymentMethod;
import com.ia.backend.order.enums.ShippingMethod;
import com.ia.backend.order.entity.Order;
import com.ia.backend.order.mapper.OrderMapper;
import com.ia.backend.order.repository.OrderRepository;
import com.ia.backend.order.service.OrderService;
import com.ia.backend.user.entity.User;
import com.ia.backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserService userService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Cart cart;
    private Accessory accessoryA;
    private Accessory accessoryB;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("user-1");
        user.setPhoneNumber("12345678");

        accessoryA = accessory("10.00", 5, "Case");
        accessoryB = accessory("20.00", 5, "Charger");

        CartItem itemA = CartItem.builder().accessory(accessoryA).quantity(1).build();
        CartItem itemB = CartItem.builder().accessory(accessoryB).quantity(2).build();

        cart = new Cart();
        itemA.setCart(cart);
        itemB.setCart(cart);
        cart.setCartItems(new ArrayList<>(List.of(itemA, itemB)));
    }

    @Test
    void createOrder_standardShipping_chargesFlatFeeOnce() {
        OrderRequest request = request(ShippingMethod.STANDARD);
        stubHappyPath(request);

        orderService.createOrder(request);

        // subtotal = (10.00 x 1) + (20.00 x 2) = 50.00, flat standard fee = 7.00
        assertThat(capturedTotal()).isEqualByComparingTo("57.00");
    }

    @Test
    void createOrder_expressShipping_chargesFlatFeeOnce() {
        OrderRequest request = request(ShippingMethod.EXPRESS);
        stubHappyPath(request);

        orderService.createOrder(request);

        // subtotal = 50.00, flat express fee = 15.00
        assertThat(capturedTotal()).isEqualByComparingTo("65.00");
    }

    @Test
    void createOrder_doesNotChargeShippingFeePerLineItem() {
        OrderRequest request = request(ShippingMethod.STANDARD);
        stubHappyPath(request);

        orderService.createOrder(request);

        // regression guard: the previous, buggy behaviour added the fee per line item
        // (10 + 7) + (40 + 7) = 64.00 — the flat fee must NOT reproduce this.
        assertThat(capturedTotal()).isNotEqualByComparingTo("64.00");
    }

    @Test
    void createOrder_decrementsStockForEachItem() {
        OrderRequest request = request(ShippingMethod.STANDARD);
        stubHappyPath(request);

        orderService.createOrder(request);

        assertThat(accessoryA.getStock()).isEqualTo(4); // 5 - 1
        assertThat(accessoryB.getStock()).isEqualTo(3); // 5 - 2
    }

    @Test
    void createOrder_withEmptyCart_throwsBadRequest() {
        cart.setCartItems(new ArrayList<>());
        when(userService.getCurrentUserEntity()).thenReturn(user);
        when(cartRepository.findByUser_Id("user-1")).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.createOrder(request(ShippingMethod.STANDARD)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty cart");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_withInsufficientStock_throwsBadRequest() {
        accessoryA.setStock(0);
        when(userService.getCurrentUserEntity()).thenReturn(user);
        when(cartRepository.findByUser_Id("user-1")).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.createOrder(request(ShippingMethod.STANDARD)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void advanceOrderStatus_advancesStepByStepToDelivered() {
        Order order = Order.builder().id(1L).status(OrderStatus.PENDING).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.advanceOrderStatus(1L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);

        orderService.advanceOrderStatus(1L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

        orderService.advanceOrderStatus(1L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void advanceOrderStatus_whenAlreadyDelivered_throwsBadRequest() {
        Order order = Order.builder().id(1L).status(OrderStatus.DELIVERED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.advanceOrderStatus(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("delivered");
    }

    @Test
    void advanceOrderStatus_whenCancelled_throwsBadRequest() {
        Order order = Order.builder().id(1L).status(OrderStatus.CANCELLED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.advanceOrderStatus(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    void advanceOrderStatus_whenOrderNotFound_throwsNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.advanceOrderStatus(99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------
    private void stubHappyPath(OrderRequest request) {
        when(userService.getCurrentUserEntity()).thenReturn(user);
        when(cartRepository.findByUser_Id("user-1")).thenReturn(Optional.of(cart));
        when(orderMapper.toEntity(request)).thenReturn(Order.builder().build());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(null);
    }

    private BigDecimal capturedTotal() {
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        return captor.getValue().getTotalAmount();
    }

    private static Accessory accessory(String price, int stock, String title) {
        Accessory accessory = new Accessory();
        accessory.setTitle(title);
        accessory.setPrice(new BigDecimal(price));
        accessory.setStock(stock);
        return accessory;
    }

    private static OrderRequest request(ShippingMethod shippingMethod) {
        return new OrderRequest(
                "Jane Doe",
                "jane@example.com",
                "12345678",
                "1 Main St",
                "Tunis",
                "1000",
                "Tunisia",
                PaymentMethod.CASH_ON_DELIVERY,
                shippingMethod,
                ""
        );
    }
}
