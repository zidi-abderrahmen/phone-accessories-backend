package com.ia.backend.service;

import com.ia.backend.dto.reftoken.RefreshTokenRequest;
import com.ia.backend.dto.reftoken.RefreshTokenResponse;
import com.ia.backend.dto.user.UserLoginRequest;
import com.ia.backend.dto.user.UserLoginResponse;
import com.ia.backend.dto.user.UserRegisterRequest;
import com.ia.backend.dto.user.UserResponse;
import com.ia.backend.dto.verifemail.VerifyEmailRequest;
import com.ia.backend.dto.verifemail.VerifyEmailResponse;
import com.ia.backend.entity.EmailVerification;
import com.ia.backend.entity.RefreshToken;
import com.ia.backend.entity.User;
import com.ia.backend.entity.UserRole;
import com.ia.backend.exception.AlreadyExistException;
import com.ia.backend.exception.NotFoundException;
import com.ia.backend.mapper.AuthMapper;
import com.ia.backend.repository.EmailVerificationRepository;
import com.ia.backend.repository.RefreshTokenRepository;
import com.ia.backend.repository.UserRepository;
import com.ia.backend.repository.UserRoleRepository;
import com.ia.backend.util.JwtUtils;
import com.ia.backend.util.TokenHasherUtils;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.CloseableThreadContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

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

    @Value("${application.frontend.url}")
    private String baseUrl;

    @Value("${application.security.jwt.refresh-expiration-ms}")
    private int refreshExpirationMs;

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

        String verificationLink = baseUrl + "/api/auth/verify?token=" + emailVerification.getToken();
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFirstName(), verificationLink);

        return authMapper.toUserResponse(savedUser);
    }

    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("Authenticated user not found."));

        String jwt = jwtUtils.generateTokenFromUsername(user.getEmail());

        String rawRefreshToken = UUID.randomUUID().toString();
        String hashedRefreshToken = tokenHasher.hash(rawRefreshToken);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(hashedRefreshToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs)))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        UserResponse userResponse = authMapper.toUserResponse(user);

        return new UserLoginResponse(new RefreshTokenResponse(jwt, rawRefreshToken), userResponse);
    }

    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken existedRefreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BadCredentialsException("Refresh token not found or has expired."));

        refreshTokenRepository.delete(existedRefreshToken);

        if (existedRefreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Refresh token not found or has expired.");
        }

        User user = existedRefreshToken.getUser();

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account is disabled.");
        }

        String jwt = jwtUtils.generateTokenFromUsername(user.getEmail());
        String rawRefreshToken = UUID.randomUUID().toString();
        String hashedRefreshToken = tokenHasher.hash(rawRefreshToken);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(hashedRefreshToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs)))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return new RefreshTokenResponse(jwt, rawRefreshToken);
    }

    @Transactional
    public VerifyEmailResponse verifyEmail(VerifyEmailRequest request) {
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

        return new VerifyEmailResponse("Email verified successfully.");
    }

    public void logout(RefreshTokenRequest request, String authorization) {
        String hashedRefreshToken = tokenHasher.hash(request.refreshToken());

        RefreshToken refreshToken = refreshTokenRepository.findByToken(hashedRefreshToken)
                .orElseThrow(() -> new BadCredentialsException("Refresh token not found or has expired."));

        String jwt = authorization.replace("Bearer ", "");
        String userEmail = jwtUtils.getUsernameFromJwtToken(jwt);

        if (!refreshToken.getUser().getEmail().equals(userEmail)) {
            throw new BadCredentialsException("Unauthorized.");
        }

        refreshTokenRepository.delete(refreshToken);
    }
}