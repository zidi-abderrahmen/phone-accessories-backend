package com.ia.backend.service;

import com.ia.backend.dto.reftoken.RefreshTokenRequest;
import com.ia.backend.dto.user.UserLoginRequest;
import com.ia.backend.dto.user.UserLoginResponse;
import com.ia.backend.dto.user.UserRegisterRequest;
import com.ia.backend.dto.user.UserResponse;
import com.ia.backend.entity.EmailVerification;
import com.ia.backend.entity.RefreshToken;
import com.ia.backend.entity.User;
import com.ia.backend.entity.UserRole;
import com.ia.backend.exception.AlreadyExistException;
import com.ia.backend.exception.NotFoundException;
import com.ia.backend.mapper.AuthMapper;
import com.ia.backend.repository.RefreshTokenRepository;
import com.ia.backend.repository.UserRepository;
import com.ia.backend.repository.UserRoleRepository;
import com.ia.backend.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

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

        return authMapper.toUserResponse(userRepository.save(newUser));
    }

    public UserLoginResponse login(UserLoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("Authenticated user not found in database."));

        String jwt = jwtUtils.generateTokenFromUsername(user.getEmail());

        String refreshToken = UUID.randomUUID().toString();

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        UserResponse userResponse = authMapper.toUserResponse(user);

        return new UserLoginResponse(jwt, refreshToken, userResponse);
    }

    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken())
                .ifPresent(refreshTokenRepository::delete);
    }
}