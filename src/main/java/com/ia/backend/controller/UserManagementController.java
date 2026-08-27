package com.ia.backend.controller;

import com.ia.backend.dto.user.UserRegisterRequest;
import com.ia.backend.dto.user.UserResponse;
import com.ia.backend.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

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
}