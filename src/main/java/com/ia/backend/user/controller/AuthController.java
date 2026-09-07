package com.ia.backend.user.controller;

import com.ia.backend.user.dto.login.LoginResult;
import com.ia.backend.user.password.dto.ForgotPasswordRequest;
import com.ia.backend.user.password.dto.ResetPasswordRequest;
import com.ia.backend.user.dto.refreshtoken.RefreshTokenResponse;
import com.ia.backend.user.dto.login.UserLoginRequest;
import com.ia.backend.user.dto.register.UserRegisterRequest;
import com.ia.backend.user.dto.response.UserResponse;
import com.ia.backend.user.verification.dto.VerifyEmailRequest;
import com.ia.backend.user.verification.dto.EmailResponse;
import com.ia.backend.user.service.AuthService;
import com.ia.backend.user.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    @Value("${application.security.jwt.expiration}")
    private int expirationJwt;

    @Value("${application.security.jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Value("${application.security.jwt.remember-me-expiration}")
    private long rememberMeExpirationMs;

    @Value("${application.security.cookies.secure}")
    private boolean cookieSecure;

    @Value("${application.security.cookies.same-site}")
    private String cookieSameSite;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletResponse response
    ) {
        LoginResult result = authService.login(request);

        generateCookies(
                result.tokens().accessToken(),
                result.tokens().refreshToken(),
                response,
                request.rememberMe()
        );

        return ResponseEntity.ok(result.user());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Void> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(c -> "refresh_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (refreshToken == null) {
            throw new BadCredentialsException("Refresh token not found");
        }

        RefreshTokenResponse refreshResponse = tokenService.refreshToken(
                refreshToken
        );

        generateCookies(
                refreshResponse.accessToken(),
                refreshResponse.refreshToken(),
                response,
                refreshResponse.rememberMe()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<EmailResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(authService.verifyEmail(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<EmailResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<EmailResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Cookie[] cookies = Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]);

        String accessToken = Arrays.stream(cookies)
                .filter(c -> "access_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        String refreshToken = Arrays.stream(cookies)
                .filter(c -> "refresh_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (refreshToken == null && accessToken == null) {
            clearCookie(response, "access_token", "/");
            clearCookie(response, "refresh_token", "/api/auth/refresh-token");
            return ResponseEntity.noContent().build();
        }

        authService.logout(refreshToken, accessToken);

        clearCookie(response, "access_token", "/");
        clearCookie(response, "refresh_token", "/api/auth/refresh-token");

        return ResponseEntity.noContent().build();
    }

    private void clearCookie(HttpServletResponse response, String name, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path(path)
                .maxAge(Duration.ZERO)
                .sameSite(cookieSameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void generateCookies(String accessToken, String refreshToken, HttpServletResponse response, boolean rememberMe) {
        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofMillis(expirationJwt))
                .sameSite(cookieSameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        long rememberMeExpiration = rememberMe ? rememberMeExpirationMs : refreshExpirationMs;
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/auth/refresh-token")
                .maxAge(Duration.ofMillis(rememberMeExpiration))
                .sameSite(cookieSameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}