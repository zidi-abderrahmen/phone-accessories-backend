package com.ia.backend.accessory.controller;

import com.ia.backend.accessory.dto.AccessoryRequest;
import com.ia.backend.accessory.dto.AccessoryResponse;
import com.ia.backend.accessory.dto.SearchRequest;
import com.ia.backend.accessory.service.AccessoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accessories") // Don't add '/api' because it's already added in the application.properties
@RequiredArgsConstructor
public class AccessoryController {

    private final AccessoryService accessoryService;

    @GetMapping
    public ResponseEntity<Page<AccessoryResponse>> getAllAccessories(Pageable pageable) {
        return ResponseEntity.ok(accessoryService.getAllAccessories(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccessoryResponse> getAccessoryById(@PathVariable Long id) {
        return ResponseEntity.ok(accessoryService.getAccessoryById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<AccessoryResponse> createAccessory(@Valid @RequestBody AccessoryRequest accessoryRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accessoryService.createAccessory(accessoryRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<AccessoryResponse> updateAccessory(@PathVariable Long id, @Valid @RequestBody AccessoryRequest accessoryRequest) {
        return ResponseEntity.ok(accessoryService.updateAccessory(id, accessoryRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteAccessory(@PathVariable Long id) {
        accessoryService.deleteAccessory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AccessoryResponse>> searchAccessories(
            SearchRequest request,
            @PageableDefault(size = 12, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(accessoryService.filterAccessories(request, pageable));
    }
}