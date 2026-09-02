package com.ia.backend.user.service;

import com.ia.backend.user.password.dto.request.ForgotPasswordRequest;
import com.ia.backend.user.password.dto.request.ResetPasswordRequest;
import com.ia.backend.user.verification.entity.EmailVerification;
import com.ia.backend.user.verification.repository.EmailVerificationRepository;
import com.ia.backend.user.verification.service.EmailService;
import com.ia.backend.user.entity.RefreshToken;
import com.ia.backend.user.repository.RefreshTokenRepository;
import com.ia.backend.user.password.entity.ResetPassword;
import com.ia.backend.user.password.repository.ResetPasswordRepository;
import com.ia.backend.user.dto.login.UserLoginRequest;
import com.ia.backend.user.dto.login.UserLoginResponse;
import com.ia.backend.user.dto.register.UserRegisterRequest;
import com.ia.backend.user.dto.response.UserResponse;
import com.ia.backend.user.verification.dto.VerifyEmailRequest;
import com.ia.backend.user.verification.dto.EmailResponse;
import com.ia.backend.common.exception.AlreadyExistException;
import com.ia.backend.common.exception.ExpiredException;
import com.ia.backend.common.exception.NotFoundException;
import com.ia.backend.user.mapper.UserMapper;
import com.ia.backend.user.entity.User;
import com.ia.backend.user.entity.UserRole;
import com.ia.backend.user.repository.UserRepository;
import com.ia.backend.user.repository.UserRoleRepository;
import com.ia.backend.common.util.JwtUtils;
import com.ia.backend.user.util.TokenHasherUtils;
import com.ia.backend.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;
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
            log.error("Email already exists.");
            throw new AlreadyExistException("Email already exists.");
        }

        UserRole userRole = userRoleRepository.findByName("USER")
                .orElseThrow(() -> {
                    log.error("User role not found.");
                    return new NotFoundException("User role not found.");
                });

        User newUser = userMapper.toUser(request);
        newUser.setRoles(Set.of(userRole));
        newUser.setPassword(passwordEncoder.encode(request.password()));

        String rawToken = UUID.randomUUID().toString();
        EmailVerification emailVerification = EmailVerification.builder()
                .token(tokenHasher.hash(rawToken))
                .user(newUser)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        newUser.setEmailVerification(emailVerification);

        log.info("Saving user: {}", newUser);
        User savedUser = userRepository.save(newUser);

        String verificationLink = baseUrl + "/verify-email?token=" + rawToken;
        emailService.sendEmail(
                savedUser.getEmail(),
                (savedUser.getFirstName() + " " + savedUser.getLastName()),
                verificationLink,
                false);

        log.info("Email sent successfully.");
        return userMapper.toUserResponse(savedUser);
    }

    public UserLoginResponse login(UserLoginRequest request) {
        log.info("Logging in user with email: {}", request.email());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserPrincipal principal = (UserPrincipal) Objects.requireNonNull(authentication.getPrincipal());

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found with email: " + principal.getUsername()));

        if (user.isDeleted()) {
            log.error("User is deleted.");
            throw new BadCredentialsException("User is deleted. Please contact support for assistance.");
        }

        if (!user.isEnabled()) {
            log.error("User is disabled.");
            throw new BadCredentialsException("User is disabled. Please contact support for assistance.");
        }

        UserResponse userResponse = userMapper.toUserResponse(user);

        log.info("User logged in successfully.");
        return new UserLoginResponse(tokenService.issueTokens(user, request.rememberMe()), userResponse);
    }

    @Transactional
    public EmailResponse verifyEmail(VerifyEmailRequest request) {
        log.info("Verifying email with token: {}", request.token());
        String hashedToken = tokenHasher.hash(request.token());

        EmailVerification verification = emailVerificationRepository.findByToken(hashedToken)
                .orElseThrow(() -> new NotFoundException("Invalid verification token."));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.error("Verification token has expired.");
            throw new ExpiredException("Verification token has expired.");
        }

        User user = verification.getUser();
        if (user.isEnabled()) {
            log.error("Email already verified.");
            throw new AlreadyExistException("Email already verified.");
        }

        user.setEnabled(true);
        user.setEmailVerification(null);
        userRepository.save(user);

        log.info("Email verified successfully.");
        return new EmailResponse("Email verified successfully.");
    }

    public void logout(String refreshToken, String accessToken) {
        String hashedRefreshToken = tokenHasher.hash(refreshToken);

        log.info("Logging out user with hashed refresh token: {}", hashedRefreshToken);
        RefreshToken existingRefreshToken = refreshTokenRepository.findByToken(hashedRefreshToken)
                .orElseThrow(() -> new BadCredentialsException("Refresh token not found or has expired."));

        String userEmail = jwtUtils.getUsernameFromJwtToken(accessToken);

        if (!existingRefreshToken.getUser().getEmail().equals(userEmail)) {
            log.error("Unauthorized access attempt.");
            throw new BadCredentialsException("Unauthorized.");
        }

        log.info("Refresh token found and user is authorized. Deleting refresh token.");
        refreshTokenRepository.delete(existingRefreshToken);
    }

    @Transactional
    public EmailResponse forgotPassword(ForgotPasswordRequest request) {
        log.info("Sending reset password email to user with email: {}", request.email());
        userRepository.findByEmail(request.email()).ifPresent(existingUser -> {

            log.info("Reset password email sent to user: {}", existingUser.getEmail());
            String rawToken = UUID.randomUUID().toString();
            String hashedToken = tokenHasher.hash(rawToken);

            log.info("Creating reset password token: {}", hashedToken);
            ResetPassword resetPassword = resetPasswordRepository.findByUser(existingUser)
                    .orElseGet(() -> ResetPassword.builder()
                            .user(existingUser)
                            .build());

            log.info("Saving reset password token: {}", hashedToken);
            resetPassword.setToken(hashedToken);
            resetPassword.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            resetPassword.setVerified(false);

            resetPasswordRepository.save(resetPassword);

            log.info("Reset password email sent to user: {}", existingUser.getEmail());
            String verificationLink = baseUrl + "/reset-password?token=" + rawToken;
            emailService.sendEmail(
                    existingUser.getEmail(),
                    existingUser.getFirstName() + " " + existingUser.getLastName(),
                    verificationLink,
                    true);
        });

        return new EmailResponse("If an account exists with this email, a reset link has been sent.");
    }

    @Transactional
    public EmailResponse resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password for user with token: {}", request.token());
        String hashedToken = tokenHasher.hash(request.token());
        ResetPassword resetPassword = resetPasswordRepository.findByToken(hashedToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid reset password token."));

        log.info("Deleting reset password token: {}", request.token());
        resetPasswordRepository.delete(resetPassword);

        if (resetPassword.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.error("Reset password token has expired.");
            throw new BadCredentialsException("Reset password token has expired.");
        }

        User existingUser = resetPassword.getUser();
        existingUser.setPassword(passwordEncoder.encode(request.newPassword()));

        existingUser.setResetPassword(null);

        log.info("Resetting password for user: {}", existingUser.getEmail());
        userRepository.save(existingUser);
        resetPasswordRepository.flush();

        log.info("Password reset successfully.");
        return new EmailResponse("Password reset successfully.");
    }
}