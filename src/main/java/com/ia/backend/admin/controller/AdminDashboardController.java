package com.ia.backend.admin.controller;

import com.ia.backend.admin.dto.AdminDashboardResponse;
import com.ia.backend.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/order-limit/{orderListLimit}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard(@PathVariable int orderListLimit) {
        return ResponseEntity.ok(adminDashboardService.getAdminDashboard(orderListLimit));
    }
}