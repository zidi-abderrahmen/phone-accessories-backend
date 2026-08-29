package com.ia.backend.user.service;

import com.ia.backend.user.dto.refreshtoken.RefreshTokenResponse;
import com.ia.backend.user.entity.RefreshToken;
import com.ia.backend.user.entity.User;
import com.ia.backend.common.exception.ExpiredException;
import com.ia.backend.common.exception.NotFoundException;
import com.ia.backend.user.repository.RefreshTokenRepository;
import com.ia.backend.common.util.JwtUtils;
import com.ia.backend.user.util.TokenHasherUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHasherUtils tokenHasher;

    @Value("${application.security.jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Value("${application.security.jwt.remember-me-expiration}")
    private long rememberMeExpirationMs;

    @Transactional
    public RefreshTokenResponse issueTokens(User user, boolean rememberMe) {
        String jwt = jwtUtils.generateTokenFromUsername(user.getEmail());
        String rawRefreshToken = UUID.randomUUID().toString();
        String hashedRefreshToken = tokenHasher.hash(rawRefreshToken);

        long expirationMs = rememberMe ? rememberMeExpirationMs : refreshExpirationMs;

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(hashedRefreshToken)
                .user(user)
                .rememberMe(rememberMe)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(expirationMs)))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return new RefreshTokenResponse(jwt, rawRefreshToken, rememberMe);
    }

    @Transactional(noRollbackFor = BadCredentialsException.class)
    public RefreshTokenResponse refreshToken(String rawRefreshToken) {
        String hashedRefreshToken = tokenHasher.hash(rawRefreshToken);
        RefreshToken existedRefreshToken = refreshTokenRepository.findByToken(hashedRefreshToken)
                .orElseThrow(() -> new NotFoundException("Refresh token not found or has expired."));

        boolean rememberMe = existedRefreshToken.isRememberMe();
        User user = existedRefreshToken.getUser();

        refreshTokenRepository.delete(existedRefreshToken);

        if (existedRefreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ExpiredException("Refresh token not found or has expired.");
        }

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account is disabled.");
        }

        return issueTokens(user, rememberMe);
    }
}