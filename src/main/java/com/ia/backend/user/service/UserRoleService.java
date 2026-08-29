package com.ia.backend.user.service;

import com.ia.backend.user.dto.role.UserRoleRequest;
import com.ia.backend.user.dto.role.UserRoleResponse;
import com.ia.backend.user.entity.UserRole;
import com.ia.backend.common.exception.AlreadyExistException;
import com.ia.backend.common.exception.NotFoundException;
import com.ia.backend.user.mapper.UserRoleMapper;
import com.ia.backend.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRoleMapper userRoleMapper;

    @Transactional(readOnly = true)
    public List<UserRoleResponse> getAllRoles() {
        log.debug("Fetching all roles");
        return userRoleRepository.findAll()
                .stream()
                .map(userRoleMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserRoleResponse> getAllByDeleted(boolean deleted) {
        log.debug("Fetching all roles by deleted: {}", deleted);
        return userRoleRepository.findAllByDeleted(deleted)
                .stream()
                .map(userRoleMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserRoleResponse getRoleById(Long id) {
        log.debug("Fetching role with id: {}", id);
        return userRoleRepository.findById(id)
                .map(userRoleMapper::toDto)
                .orElseThrow(() -> {
                    logError();
                    return new NotFoundException("Role not found");
                });
    }

    @Transactional
    public UserRoleResponse createRole(UserRoleRequest request) {
        log.debug("Creating new role: {}", request.name());
        if (userRoleRepository.existsByName(request.name())) {
            log.error("Role already exists");
            throw new AlreadyExistException("Role already exists");
        }

        log.debug("Creating new role: {}", request.name());
        UserRole newRole = userRoleMapper.toEntity(request);
        if (request.deleted()) {
            newRole.setDeletedAt(LocalDateTime.now());
        }
        userRoleRepository.save(newRole);

        log.info("Created new role with id: {}", newRole.getId());
        return userRoleMapper.toDto(newRole);
    }

    @Transactional
    public UserRoleResponse updateRole(Long id, UserRoleRequest request) {
        UserRole existingRole = exitingRoleById(id);

        if (!existingRole.getName().equalsIgnoreCase(request.name())
                && userRoleRepository.existsByNameIgnoreCase(request.name())) {
            log.error("Role name already exists");
            throw new AlreadyExistException("Role name already exists");
        }

        if (request.deleted()) {
            existingRole.setDeletedAt(LocalDateTime.now());
        } else {
            existingRole.setDeletedAt(null);
        }
        userRoleMapper.updateRole(request, existingRole);

        return userRoleMapper.toDto(existingRole);
    }

    @Transactional
    public void softDeleteRole(Long id) {
        log.debug("Deleting role with id: {}", id);
        UserRole existingRole = exitingRoleById(id);

        log.debug("Marking role as deleted: {}", existingRole.getName());
        existingRole.setDeleted(true);
        existingRole.setDeletedAt(LocalDateTime.now());
        userRoleRepository.save(existingRole);
    }

    @Transactional
    public void hardDeleteRole(Long id) {
        if (!userRoleRepository.existsById(id)) {
            logError();
            throw new NotFoundException("Role not found");
        }

        userRoleRepository.deleteById(id);
        log.info("Deleted role with id: {}", id);
    }

    private UserRole exitingRoleById(Long id) {
        return userRoleRepository.findById(id)
                .orElseThrow(() -> {
                    logError();
                    return new NotFoundException("Role not found");
                });
    }

    private void logError() {
        log.error("Role not found");
    }
}