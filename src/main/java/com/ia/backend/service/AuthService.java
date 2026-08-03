package com.ia.backend.service;

import com.ia.backend.dto.email.password.ForgotPasswordRequest;
import com.ia.backend.dto.email.password.ResetPasswordRequest;
import com.ia.backend.dto.me.MeResponse;
import com.ia.backend.dto.user.UserLoginRequest;
import com.ia.backend.dto.user.UserLoginResponse;
import com.ia.backend.dto.user.UserRegisterRequest;
import com.ia.backend.dto.user.UserResponse;
import com.ia.backend.dto.email.VerifyEmailRequest;
import com.ia.backend.dto.email.EmailResponse;
import com.ia.backend.entity.*;
import com.ia.backend.exception.AlreadyExistException;
import com.ia.backend.exception.NotFoundException;
import com.ia.backend.mapper.AuthMapper;
import com.ia.backend.repository.*;
import com.ia.backend.util.JwtUtils;
import com.ia.backend.util.TokenHasherUtils;
import com.ia.backend.util.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final TokenHasherUtils tokenHasher;
    private final ResetPasswordRepository resetPasswordRepository;
    private final TokenService tokenService;

    @Value("${application.frontend.url}")
    private String baseUrl;

    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new AlreadyExistException("Email already exists.");
        }

        UserRole userRole = userRoleRepository.findByName("USER")
                .orElseThrow(() -> new NotFoundException("User role not found."));

        User newUser = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(userRole))
                .build();

        EmailVerification emailVerification = EmailVerification.builder()
                .token(UUID.randomUUID().toString())
                .user(newUser)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        newUser.setEmailVerification(emailVerification);

        User savedUser = userRepository.save(newUser);

        String verificationLink = baseUrl + "/verify?token=" + emailVerification.getToken();
        emailService.sendEmail(savedUser.getEmail(), savedUser.getFirstName(), verificationLink, false);

        return authMapper.toUserResponse(savedUser);
    }

    public UserLoginResponse login(UserLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = ((UserPrincipal) Objects.requireNonNull(authentication.getPrincipal())).user();

        UserResponse userResponse = authMapper.toUserResponse(user);

        return new UserLoginResponse(tokenService.issueTokens(user, request.rememberMe()), userResponse);
    }

    @Transactional
    public EmailResponse verifyEmail(VerifyEmailRequest request) {
        EmailVerification verification = emailVerificationRepository.findByToken(request.token())
                .orElseThrow(() -> new BadCredentialsException("Invalid verification token."));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            emailVerificationRepository.delete(verification);
            throw new BadCredentialsException("Verification token has expired.");
        }

        User user = verification.getUser();

        if (user.isEnabled()) {
            emailVerificationRepository.delete(verification);
            throw new AlreadyExistException("Email already verified.");
        }

        user.setEnabled(true);

        emailVerificationRepository.delete(verification);

        return new EmailResponse("Email verified successfully.");
    }

    public void logout(String refreshToken, String accessToken) {
        String hashedRefreshToken = tokenHasher.hash(refreshToken);

        RefreshToken existingRefreshToken = refreshTokenRepository.findByToken(hashedRefreshToken)
                .orElseThrow(() -> new BadCredentialsException("Refresh token not found or has expired."));

        String userEmail = jwtUtils.getUsernameFromJwtToken(accessToken);

        if (!existingRefreshToken.getUser().getEmail().equals(userEmail)) {
            throw new BadCredentialsException("Unauthorized.");
        }

        refreshTokenRepository.delete(existingRefreshToken);
    }

    @Transactional
    public EmailResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(existingUser -> {
            resetPasswordRepository.deleteAllByUser(existingUser);
            resetPasswordRepository.flush();

            String rawToken = UUID.randomUUID().toString();
            String hashedToken = tokenHasher.hash(rawToken);

            ResetPassword newResetPassword = ResetPassword.builder()
                    .token(hashedToken)
                    .user(existingUser)
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .build();

            existingUser.setResetPassword(newResetPassword);
            userRepository.save(existingUser);

            String verificationLink = baseUrl + "/reset-password?token=" + rawToken;
            emailService.sendEmail(existingUser.getEmail(), existingUser.getFirstName(), verificationLink, true);
        });

        return new EmailResponse("If an account exists with this email, a reset link has been sent.");
    }

    @Transactional
    public EmailResponse resetPassword(ResetPasswordRequest request) {
        String hashedToken = tokenHasher.hash(request.token());
        ResetPassword resetPassword = resetPasswordRepository.findByToken(hashedToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid reset password token."));

        if (resetPassword.getExpiresAt().isBefore(LocalDateTime.now())) {
            resetPasswordRepository.delete(resetPassword);
            throw new BadCredentialsException("Reset password token has expired.");
        }

        User existingUser = resetPassword.getUser();
        existingUser.setPassword(passwordEncoder.encode(request.newPassword()));

        existingUser.setResetPassword(null);

        userRepository.save(existingUser);
        resetPasswordRepository.delete(resetPassword);
        resetPasswordRepository.flush();

        return new EmailResponse("Password reset successfully.");
    }

    public MeResponse getMe() {
        UserDetails userDetails = getUserDetails();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<String> roles = user.getRoles()
                .stream()
                .map(UserRole::getName)
                .collect(Collectors.toSet());

        return new MeResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                roles,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private static @NonNull UserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BadCredentialsException("User is not authenticated.");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        if (userDetails == null) {
            throw new UsernameNotFoundException("User details not found");
        }
        return userDetails;
    }
}