package com.ia.backend.controller;

import com.ia.backend.dto.email.password.ForgotPasswordRequest;
import com.ia.backend.dto.email.password.ResetPasswordRequest;
import com.ia.backend.dto.me.MeResponse;
import com.ia.backend.dto.reftoken.RefreshTokenResponse;
import com.ia.backend.dto.user.UserLoginRequest;
import com.ia.backend.dto.user.UserLoginResponse;
import com.ia.backend.dto.user.UserRegisterRequest;
import com.ia.backend.dto.user.UserResponse;
import com.ia.backend.dto.email.VerifyEmailRequest;
import com.ia.backend.dto.email.EmailResponse;
import com.ia.backend.service.AuthService;
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

    @Value("${application.security.jwt.expiration-ms}")
    private int expirationJwt;

    @Value("${application.security.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${application.security.jwt.remember-me-expiration-ms}")
    private long rememberMeExpirationMs;

    @Value("${application.security.cookie.secure:false}")
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

        RefreshTokenResponse refreshResponse = authService.refreshToken(
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

    @GetMapping("/verify")
    public ResponseEntity<EmailResponse> verifyEmail(@ModelAttribute VerifyEmailRequest request) {
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

    @GetMapping("/me")
    public ResponseEntity<MeResponse> getCurrentUser() {
        return ResponseEntity.ok(authService.getMe());
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
        cookie.setSecure(true);
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