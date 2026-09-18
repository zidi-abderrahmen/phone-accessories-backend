package com.ia.backend.integration;

import com.ia.backend.accessory.entity.Accessory;
import com.ia.backend.accessory.repository.AccessoryRepository;
import com.ia.backend.cart.dto.CartItemRequest;
import com.ia.backend.category.entity.Category;
import com.ia.backend.category.repository.CategoryRepository;
import com.ia.backend.common.enums.OrderStatus;
import com.ia.backend.order.dto.OrderRequest;
import com.ia.backend.order.dto.OrderResponse;
import com.ia.backend.order.enums.PaymentMethod;
import com.ia.backend.order.enums.PaymentStatus;
import com.ia.backend.order.enums.ShippingMethod;
import com.ia.backend.order.payment.MockPaymentGateway;
import com.ia.backend.order.payment.PaymentResult;
import com.ia.backend.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderFlowIntegrationTest extends IntegrationTestBase {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccessoryRepository accessoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoSpyBean
    private MockPaymentGateway paymentGateway;

    @Test
    void createOrder_chargesFlatShippingFee_andDecrementsStock() throws Exception {
        Accessory accessory = createAccessory("50.00", 3);
        Session session = registerVerifyAndLogin(uniqueEmail("order"));

        addToCart(session, accessory.getId(), 2);
        OrderResponse order = placeOrder(session, ShippingMethod.STANDARD);

        // subtotal 2 x 50.00 = 100.00 plus a single flat standard fee of 7.00
        assertThat(order.totalAmount()).isEqualByComparingTo("107.00");
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.paymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(order.paymentReference()).isNull();
        assertThat(order.paidAt()).isNull();
        assertThat(order.items()).hasSize(1);
        order.items().forEach(item -> {
            assertThat(item.quantity()).isEqualTo(2);
            assertThat(item.unitPrice()).isEqualByComparingTo("50.00");
        });

        assertThat(currentStock(accessory)).isEqualTo(1);
    }

    @Test
    void createOrder_expressShipping_chargesFlatFeeOnce() throws Exception {
        Accessory accessory = createAccessory("20.00", 5);
        Session session = registerVerifyAndLogin(uniqueEmail("express"));

        addToCart(session, accessory.getId(), 1);
        OrderResponse order = placeOrder(session, ShippingMethod.EXPRESS);

        // subtotal 20.00 plus a single flat express fee of 15.00
        assertThat(order.totalAmount()).isEqualByComparingTo("35.00");
    }

    @Test
    void createOrder_withOnlinePayment_isMarkedPaidWithReference() throws Exception {
        Accessory accessory = createAccessory("20.00", 5);
        Session session = registerVerifyAndLogin(uniqueEmail("card"));

        addToCart(session, accessory.getId(), 1);
        OrderResponse order = placeOrder(session, PaymentMethod.CREDIT_CARD, ShippingMethod.STANDARD);

        assertThat(order.paymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(order.paymentReference()).startsWith("MOCK-");
        assertThat(order.paidAt()).isNotNull();
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void cancelPaidOrder_marksPaymentRefunded() throws Exception {
        Accessory accessory = createAccessory("20.00", 5);
        Session session = registerVerifyAndLogin(uniqueEmail("refund"));

        addToCart(session, accessory.getId(), 1);
        OrderResponse order = placeOrder(session, PaymentMethod.PAYPAL, ShippingMethod.STANDARD);
        assertThat(order.paymentStatus()).isEqualTo(PaymentStatus.PAID);

        mockMvc.perform(put("/orders/" + order.id() + "/cancel")
                        .cookie(session.accessToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/orders/" + order.id()).cookie(session.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.paymentStatus").value("REFUNDED"));
    }

    @Test
    void createOrder_whenPaymentDeclined_returns400AndCreatesNoOrder() throws Exception {
        Accessory accessory = createAccessory("20.00", 3);
        Session session = registerVerifyAndLogin(uniqueEmail("declined"));

        addToCart(session, accessory.getId(), 2);

        doReturn(PaymentResult.failed())
                .when(paymentGateway).charge(any(PaymentMethod.class), any());

        mockMvc.perform(post("/orders")
                        .cookie(session.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(PaymentMethod.CREDIT_CARD, ShippingMethod.STANDARD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Payment was declined. Please try another payment method."));

        mockMvc.perform(get("/orders").cookie(session.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createOrder_fromEmptyCart_returns400() throws Exception {
        Accessory accessory = createAccessory("10.00", 5);
        Session session = registerVerifyAndLogin(uniqueEmail("empty-cart"));

        long cartItemId = addToCart(session, accessory.getId(), 1);
        mockMvc.perform(delete("/carts/my-cart/cart-item/" + cartItemId)
                        .cookie(session.accessToken()))
                .andExpect(status().isNoContent());
        flushAndClear();

        mockMvc.perform(post("/orders")
                        .cookie(session.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(PaymentMethod.CASH_ON_DELIVERY, ShippingMethod.STANDARD)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_withInsufficientStock_returns400() throws Exception {
        Accessory accessory = createAccessory("10.00", 1);
        Session session = registerVerifyAndLogin(uniqueEmail("out-of-stock"));

        addToCart(session, accessory.getId(), 1);

        Accessory sold = accessoryRepository.findById(accessory.getId()).orElseThrow();
        sold.setStock(0);
        accessoryRepository.saveAndFlush(sold);

        mockMvc.perform(post("/orders")
                        .cookie(session.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(PaymentMethod.CASH_ON_DELIVERY, ShippingMethod.STANDARD)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_withoutCart_returns404() throws Exception {
        Session session = registerVerifyAndLogin(uniqueEmail("no-cart"));

        mockMvc.perform(post("/orders")
                        .cookie(session.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(PaymentMethod.CASH_ON_DELIVERY, ShippingMethod.STANDARD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void orderHistory_containsOwnOrder_andIsScopedToTheUser() throws Exception {
        Accessory accessory = createAccessory("10.00", 5);
        Session owner = registerVerifyAndLogin(uniqueEmail("owner"));
        addToCart(owner, accessory.getId(), 1);
        OrderResponse order = placeOrder(owner, ShippingMethod.STANDARD);

        mockMvc.perform(get("/orders").cookie(owner.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(order.id()));

        Session intruder = registerVerifyAndLogin(uniqueEmail("intruder"));
        mockMvc.perform(get("/orders/" + order.id()).cookie(intruder.accessToken()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/orders").cookie(intruder.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void cancelOrder_restoresStock_andMarksOrderCancelled() throws Exception {
        Accessory accessory = createAccessory("10.00", 3);
        Session session = registerVerifyAndLogin(uniqueEmail("cancel"));
        addToCart(session, accessory.getId(), 2);
        OrderResponse order = placeOrder(session, ShippingMethod.STANDARD);

        assertThat(currentStock(accessory)).isEqualTo(1);

        mockMvc.perform(put("/orders/" + order.id() + "/cancel")
                        .cookie(session.accessToken()))
                .andExpect(status().isNoContent());

        assertThat(currentStock(accessory)).isEqualTo(3);
        mockMvc.perform(get("/orders/" + order.id()).cookie(session.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void deleteCancelledOrder_removesIt() throws Exception {
        Accessory accessory = createAccessory("10.00", 2);
        Session session = registerVerifyAndLogin(uniqueEmail("delete"));
        addToCart(session, accessory.getId(), 1);
        OrderResponse order = placeOrder(session, ShippingMethod.STANDARD);

        mockMvc.perform(put("/orders/" + order.id() + "/cancel")
                        .cookie(session.accessToken()))
                .andExpect(status().isNoContent());

        int stockAfterCancel = accessoryRepository.findById(accessory.getId())
                .orElseThrow()
                .getStock();
        assertThat(stockAfterCancel).isEqualTo(2);

        mockMvc.perform(delete("/orders/" + order.id()).cookie(session.accessToken()))
                .andExpect(status().isNoContent());

        int stockAfterDelete = accessoryRepository.findById(accessory.getId())
                .orElseThrow()
                .getStock();
        assertThat(stockAfterDelete).isEqualTo(stockAfterCancel);

        mockMvc.perform(get("/orders/" + order.id()).cookie(session.accessToken()))
                .andExpect(status().isNotFound());
    }

    private Accessory createAccessory(String price, int stock) {
        Category category = categoryRepository.save(Category.builder()
                .name("category-" + UUID.randomUUID())
                .description("Integration test category")
                .imageUrl("https://example.com/category.png")
                .build());

        return accessoryRepository.save(Accessory.builder()
                .title("Test Case")
                .description("Integration test accessory")
                .imageUrl("https://example.com/accessory.png")
                .price(new BigDecimal(price))
                .stock(stock)
                .category(category)
                .productCode("PC" + UUID.randomUUID().toString().replace("-", "").substring(0, 8))
                .build());
    }

    private long addToCart(Session session, long accessoryId, int quantity) throws Exception {
        MvcResult result = mockMvc.perform(post("/carts/my-cart")
                        .cookie(session.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CartItemRequest(accessoryId, quantity))))
                .andExpect(status().isCreated())
                .andReturn();

        flushAndClear();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private OrderResponse placeOrder(Session session, ShippingMethod shippingMethod) throws Exception {
        return placeOrder(session, PaymentMethod.CASH_ON_DELIVERY, shippingMethod);
    }

    private OrderResponse placeOrder(
            Session session, PaymentMethod paymentMethod, ShippingMethod shippingMethod)
            throws Exception {
        MvcResult result = mockMvc.perform(post("/orders")
                        .cookie(session.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(paymentMethod, shippingMethod)))
                .andExpect(status().isOk())
                .andReturn();

        flushAndClear();
        return objectMapper.readValue(result.getResponse().getContentAsString(), OrderResponse.class);
    }

    private String orderJson(PaymentMethod paymentMethod, ShippingMethod shippingMethod)
            throws Exception {
        return objectMapper.writeValueAsString(new OrderRequest(
                "Jane Doe",
                "jane@example.com",
                "+21612345678",
                "1 Main St",
                "Tunis",
                "1000",
                "Tunisia",
                paymentMethod,
                shippingMethod,
                ""
        ));
    }

    private int currentStock(Accessory accessory) {
        return accessoryRepository.findById(accessory.getId()).orElseThrow().getStock();
    }
}
