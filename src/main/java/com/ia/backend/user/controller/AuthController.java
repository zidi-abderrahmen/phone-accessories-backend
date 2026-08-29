package com.ia.backend.user.controller;

import com.ia.backend.password.dto.request.ForgotPasswordRequest;
import com.ia.backend.password.dto.request.ResetPasswordRequest;
import com.ia.backend.user.dto.refreshtoken.RefreshTokenResponse;
import com.ia.backend.user.dto.login.UserLoginRequest;
import com.ia.backend.user.dto.login.UserLoginResponse;
import com.ia.backend.user.dto.register.UserRegisterRequest;
import com.ia.backend.user.dto.response.UserResponse;
import com.ia.backend.email.dto.VerifyEmailRequest;
import com.ia.backend.email.dto.EmailResponse;
import com.ia.backend.user.service.AuthService;
import com.ia.backend.user.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletResponse response
    ) {
        UserLoginResponse userLoginResponse = authService.login(request);

        generateCookies(
                userLoginResponse.tokens().accessToken(),
                userLoginResponse.tokens().refreshToken(),
                response,
                request.rememberMe()
        );

        return ResponseEntity.ok(userLoginResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("refresh_token".equals(c.getName())) {
                    refreshToken = c.getValue();
                    break;
                }
            }
        }

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

        return ResponseEntity.ok(refreshResponse);
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
        String accessToken = null;
        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt_token".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                }
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        authService.logout(refreshToken, accessToken);

        clearCookie(response, "jwt_token");
        clearCookie(response, "refresh_token");

        return ResponseEntity.noContent().build();
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void generateCookies(String accessToken, String refreshToken, HttpServletResponse response, boolean rememberMe) {
        Cookie accessCookie = new Cookie("jwt_token",
                accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(cookieSecure);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(expirationJwt / 1000);
        accessCookie.setAttribute("SameSite", "Strict");
        response.addCookie(accessCookie);

        int rememberMeMaxAge = rememberMe ? (int) (rememberMeExpirationMs / 1000) : (int) (refreshExpirationMs / 1000);

        Cookie refreshCookie = new Cookie("refresh_token",
                refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(cookieSecure);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(rememberMeMaxAge);
        refreshCookie.setAttribute("SameSite", "Strict");
        response.addCookie(refreshCookie);
    }
}