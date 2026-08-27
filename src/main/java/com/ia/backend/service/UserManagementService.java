package com.ia.backend.service;

import com.ia.backend.dto.user.UserRegisterRequest;
import com.ia.backend.dto.user.UserResponse;
import com.ia.backend.entity.User;
import com.ia.backend.entity.UserRole;
import com.ia.backend.exception.AlreadyExistException;
import com.ia.backend.exception.NotFoundException;
import com.ia.backend.mapper.UserMapper;
import com.ia.backend.repository.UserRepository;
import com.ia.backend.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable, Boolean blocked, Boolean deleted) {
        log.debug("Fetching all users");
        return userRepository.findAllByBlockedAndDeleted(blocked, deleted, pageable)
                .map(userMapper::toUserResponse);
    }

    @Transactional
    public UserResponse createAdmin(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            log.error("Email already exists");
            throw new AlreadyExistException("Email already exists");
        }

        UserRole adminRole = userRoleRepository.findByName("ADMIN")
                .orElseThrow(() -> {
                    log.error("Admin role not found");
                    return new NotFoundException("Admin role not found");
                });

        User newUser = userMapper.toUser(request);
        Set<UserRole> roles = new HashSet<>();
        roles.add(adminRole);
        newUser.setRoles(roles);
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setEnabled(true);

        userRepository.save(newUser);
        return userMapper.toUserResponse(newUser);
    }

    @Transactional
    public UserResponse updateUserBlockedStatus(String id, boolean blocked) {
        User existingUser = getExistingUser(id);

        existingUser.setBlocked(blocked);

        return userMapper.toUserResponse(existingUser);
    }

    @Transactional
    public void updateUserDeletedStatus(String id, boolean deleted) {
        User existingUser = getExistingUser(id);
        existingUser.setDeletedAt(deleted ? LocalDateTime.now() : null);
        existingUser.setDeleted(deleted);
    }

    protected User getExistingUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with id {} not found", id);
                    return new NotFoundException("User not found");
                });
    }
}