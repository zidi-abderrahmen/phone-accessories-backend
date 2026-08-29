package com.ia.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Table(name = "refresh_tokens")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder @ToString(exclude = "token")
public class RefreshToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500, unique = true)
    private String token; // Random UUID but hashed

    @Builder.Default
    @Column(nullable = false)
    private boolean valid = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Builder.Default
    @Column(nullable = false)
    private boolean rememberMe = false;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}