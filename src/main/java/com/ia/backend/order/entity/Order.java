package com.ia.backend.order.entity;

import com.ia.backend.user.entity.User;
import com.ia.backend.common.enums.OrderStatus;
import com.ia.backend.order.enums.PaymentMethod;
import com.ia.backend.order.enums.ShippingMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Entity @Table(name = "orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<OrderItem> items;

    @Column(nullable = false, length = 300)
    private String customerFullName;

    @Column(length = 150)
    private String customerEmail;

    @Column(nullable = false, length = 50)
    private String customerPhoneNumber;

    @Column(nullable = false, length = 350)
    private String customerStreet;

    @Column(nullable = false, length = 50)
    private String customerCity;

    @Column(nullable = false, length = 50)
    private String customerPostalCode;

    @Column(nullable = false, length = 50)
    private String customerCountry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ShippingMethod shippingMethod;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
}