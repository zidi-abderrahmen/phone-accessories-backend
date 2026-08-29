package com.ia.backend.user.controller;

import com.ia.backend.user.dto.register.UserRegisterRequest;
import com.ia.backend.user.dto.response.UserResponse;
import com.ia.backend.user.dto.role.UserRoleRequest;
import com.ia.backend.user.dto.role.UserRoleResponse;
import com.ia.backend.user.service.UserManagementService;
import com.ia.backend.user.service.UserRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;
    private final UserRoleService userRoleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) Boolean blocked,
            @RequestParam(required = false) Boolean deleted,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                userManagementService.getAllUsers(pageable, blocked, deleted)
        );
    }

    @PostMapping("/admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> createAdmin(@Valid @RequestBody UserRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userManagementService.createAdmin(request));
    }

    @PutMapping("/{id}/blocked/{blocked}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<UserResponse> updateUserBlockedStatus(@PathVariable String id, @PathVariable boolean blocked) {
        return ResponseEntity.ok(userManagementService.updateUserBlockedStatus(id, blocked));
    }

    @PatchMapping("/{id}/deleted/{deleted}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> updateUserDeletedStatus(@PathVariable String id, @PathVariable boolean deleted) {
        userManagementService.updateUserDeletedStatus(id, deleted);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<UserRoleResponse>> getAllRoles() {
        return ResponseEntity.ok(userRoleService.getAllRoles());
    }

    @GetMapping("/roles/deleted")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<UserRoleResponse>> getAllByDeleted() {
        return ResponseEntity.ok(userRoleService.getAllByDeleted(true));
    }

    @GetMapping("/roles/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<UserRoleResponse>> getAllNotDeleted() {
        return ResponseEntity.ok(userRoleService.getAllByDeleted(false));
    }

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<UserRoleResponse> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(userRoleService.getRoleById(id));
    }

    @PostMapping("/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserRoleResponse> createRole(@Valid @RequestBody UserRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userRoleService.createRole(request));
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserRoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody UserRoleRequest request) {
        return ResponseEntity.ok(userRoleService.updateRole(id, request));
    }

    @DeleteMapping("/roles/soft/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> softDeleteRole(@PathVariable Long id) {
        userRoleService.softDeleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/roles/hard/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> hardDeleteRole(@PathVariable Long id) {
        userRoleService.hardDeleteRole(id);
        return ResponseEntity.noContent().build();
    }
}