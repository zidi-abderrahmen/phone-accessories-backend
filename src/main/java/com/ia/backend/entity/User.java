package com.ia.backend.entity;

import com.ia.backend.entity.cart.Cart;
import com.ia.backend.entity.order.Order;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder @ToString(exclude = { "password", "refreshTokens", "emailVerification" })
public class User {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 150)
    private String firstName;

    @Column(length = 150)
    private String lastName;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(length = 50)
    private String phoneNumber;

    @Column(nullable = false, length = 250)
    private String password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_roles_mapping",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<UserRole> roles = new HashSet<>();

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = false;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private EmailVerification emailVerification;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private ResetPassword resetPassword;

    @Builder.Default
    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY)
    private Set<Order> orders = new HashSet<>();

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean blocked = false;

    @Version
    private Integer version;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

}