package com.ia.backend.service;

import com.ia.backend.dto.user.UserResponse;
import com.ia.backend.dto.user.password.ChangePasswordRequest;
import com.ia.backend.dto.user.password.ChangePasswordResponse;
import com.ia.backend.dto.user.profile.UpdateProfileRequest;
import com.ia.backend.entity.User;
import com.ia.backend.exception.AlreadyExistException;
import com.ia.backend.mapper.UserMapper;
import com.ia.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public ChangePasswordResponse changePassword(ChangePasswordRequest request) {
        User user = getCurrentUserEntity();

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            log.error("Old password does not match.");
            throw new IllegalArgumentException("Old password does not match.");
        }

        log.info("Changing password for user: {}", user.getEmail());
        user.setPassword(passwordEncoder.encode(request.newPassword()));

        return new ChangePasswordResponse(
                userMapper.toUserResponse(user),
                "Password changed successfully."
        );
    }

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUserEntity();

        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                log.error("Email already exists.");
                throw new AlreadyExistException("Email already exists.");
            }
        }

        log.info("Updating profile for user: {}", user.getEmail());
        userMapper.updateUser(request, user);
        user.setEmail(request.email());

        return userMapper.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        User user = getCurrentUserEntity();

        log.info("User details retrieved successfully.");
        return userMapper.toUserResponse(user);
    }

    public User getCurrentUserEntity() {
        UserDetails userDetails = getUserDetails();

        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> {
                    log.error("User not found");
                    return new UsernameNotFoundException("User not found");
                });
    }

    private static @NonNull UserDetails getUserDetails() {
        log.info("Getting user details from security context.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            log.error("User is not authenticated.");
            throw new BadCredentialsException("User is not authenticated.");
        }

        log.info("User is authenticated.");
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        if (userDetails == null) {
            log.error("User details not found.");
            throw new UsernameNotFoundException("User details not found");
        }

        return userDetails;
    }
}