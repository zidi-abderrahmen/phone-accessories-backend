package com.ia.backend.controller;

import com.ia.backend.dto.user.role.UserRoleRequest;
import com.ia.backend.dto.user.role.UserRoleResponse;
import com.ia.backend.service.UserRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<UserRoleResponse>> getAllRoles() {
        return ResponseEntity.ok(userRoleService.getAllRoles());
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<UserRoleResponse>> getAllByDeleted() {
        return ResponseEntity.ok(userRoleService.getAllByDeleted(true));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<UserRoleResponse>> getAllNotDeleted() {
        return ResponseEntity.ok(userRoleService.getAllByDeleted(false));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<UserRoleResponse> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(userRoleService.getRoleById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserRoleResponse> createRole(@Valid @RequestBody UserRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userRoleService.createRole(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserRoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody UserRoleRequest request) {
        return ResponseEntity.ok(userRoleService.updateRole(id, request));
    }

    @DeleteMapping("/soft/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> softDeleteRole(@PathVariable Long id) {
        userRoleService.softDeleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/hard/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> hardDeleteRole(@PathVariable Long id) {
        userRoleService.hardDeleteRole(id);
        return ResponseEntity.noContent().build();
    }
}