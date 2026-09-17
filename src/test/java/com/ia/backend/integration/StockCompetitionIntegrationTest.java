package com.ia.backend.integration;

import com.ia.backend.accessory.entity.Accessory;
import com.ia.backend.accessory.repository.AccessoryRepository;
import com.ia.backend.cart.entity.Cart;
import com.ia.backend.cart.entity.CartItem;
import com.ia.backend.cart.repository.CartRepository;
import com.ia.backend.category.entity.Category;
import com.ia.backend.category.repository.CategoryRepository;
import com.ia.backend.common.util.JwtUtils;
import com.ia.backend.order.dto.OrderRequest;
import com.ia.backend.order.entity.Order;
import com.ia.backend.order.enums.PaymentMethod;
import com.ia.backend.order.enums.ShippingMethod;
import com.ia.backend.order.repository.OrderRepository;
import com.ia.backend.user.entity.User;
import com.ia.backend.user.entity.UserRole;
import com.ia.backend.user.repository.UserRepository;
import com.ia.backend.user.repository.UserRoleRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Reproduces the real optimistic-locking race on the accessory stock.
 *
 * <p>Two customers try to buy the last unit at the same time. Both requests read
 * {@code stock = 1} and {@code version = 1}, then the test pauses each request right at
 * {@code OrderRepository.save(...)} (via a spy and a {@link CyclicBarrier}) so both
 * transactions reach the point of no return before either commits. Whichever commits
 * second writes against a stale {@code @Version} and must be rejected with HTTP 409,
 * while the other succeeds and the stock lands on zero.</p>
 *
 * <p>Deliberately <b>not</b> {@code @Transactional}: the test transaction would never
 * commit, so the version conflict could never occur. State is cleaned up in
 * {@link #cleanUp()}.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StockCompetitionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccessoryRepository accessoryRepository;

    @Autowired
    private CartRepository cartRepository;

    @MockitoSpyBean
    private OrderRepository orderRepository;

    private Accessory accessory;
    private User racerA;
    private User racerB;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(Category.builder()
                .name("race-" + UUID.randomUUID())
                .description("Stock competition category")
                .imageUrl("https://example.com/category.png")
                .build());

        accessory = accessoryRepository.save(Accessory.builder()
                .title("Last One In Stock")
                .description("Single unit available")
                .imageUrl("https://example.com/accessory.png")
                .price(new BigDecimal("100.00"))
                .stock(1)
                .category(category)
                .productCode("RC" + UUID.randomUUID().toString().replace("-", "").substring(0, 8))
                .build());

        UserRole userRole = userRoleRepository.findByName("USER").orElseThrow();
        racerA = createUserWithCart(userRole, "racer-a");
        racerB = createUserWithCart(userRole, "racer-b");
    }

    @AfterEach
    void cleanUp() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        userRepository.deleteAll(userRepository.findAll().stream()
                .filter(user -> user.getEmail().endsWith("@test.example"))
                .toList());
        accessoryRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void concurrentOrdersForLastUnit_oneSucceeds_andOneIsRejectedWith409() throws Exception {
        CyclicBarrier commitBarrier = new CyclicBarrier(2);
        Answer<?> realSave = Mockito.mockingDetails(orderRepository)
                .getMockCreationSettings()
                .getDefaultAnswer();
        doAnswer(invocation -> {
            Object saved = realSave.answer(invocation);
            commitBarrier.await(15, TimeUnit.SECONDS);
            return saved;
        }).when(orderRepository).save(any(Order.class));

        Cookie cookieA = accessCookie(racerA);
        Cookie cookieB = accessCookie(racerB);
        String body = orderJson();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);

            Future<MvcResult> orderA = executor.submit(order(cookieA, body, ready, start));
            Future<MvcResult> orderB = executor.submit(order(cookieB, body, ready, start));

            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            int statusA = orderA.get(30, TimeUnit.SECONDS).getResponse().getStatus();
            int statusB = orderB.get(30, TimeUnit.SECONDS).getResponse().getStatus();

            assertThat(List.of(statusA, statusB)).containsExactlyInAnyOrder(200, 409);
        } finally {
            executor.shutdownNow();
        }

        assertThat(currentStock()).isZero();

        int ordersForRacerA = orderRepository.findAllByUserIdWithItems(racerA.getId()).size();
        int ordersForRacerB = orderRepository.findAllByUserIdWithItems(racerB.getId()).size();
        assertThat(ordersForRacerA + ordersForRacerB).isEqualTo(1);
    }

    private Callable<MvcResult> order(Cookie cookie, String body, CountDownLatch ready, CountDownLatch start) {
        return () -> {
            ready.countDown();
            start.await(10, TimeUnit.SECONDS);
            return mockMvc.perform(post("/orders")
                            .cookie(cookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andReturn();
        };
    }

    private User createUserWithCart(UserRole role, String prefix) {
        User user = userRepository.save(User.builder()
                .firstName("Race")
                .lastName("Tester")
                .email(prefix + "-" + UUID.randomUUID() + "@test.example")
                .password("password-not-used-for-token-authentication")
                .phoneNumber("+21612345678")
                .roles(Set.of(role))
                .enabled(true)
                .build());

        Cart cart = Cart.builder().user(user).build();
        cart.getCartItems().add(CartItem.builder()
                .cart(cart)
                .accessory(accessory)
                .quantity(1)
                .build());
        cartRepository.save(cart);

        return user;
    }

    private Cookie accessCookie(User user) {
        return new Cookie("access_token", jwtUtils.generateTokenFromUsername(user.getEmail()));
    }

    private String orderJson() throws Exception {
        return objectMapper.writeValueAsString(new OrderRequest(
                "Race Tester",
                "race@test.example",
                "+21612345678",
                "1 Main St",
                "Tunis",
                "1000",
                "Tunisia",
                PaymentMethod.CASH_ON_DELIVERY,
                ShippingMethod.STANDARD,
                ""
        ));
    }

    private int currentStock() {
        return accessoryRepository.findById(accessory.getId()).orElseThrow().getStock();
    }
}
