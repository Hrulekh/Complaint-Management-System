package com.cms.complaints.controller;

import com.cms.complaints.dto.CategoryDto;
import com.cms.complaints.dto.CreateCategoryRequest;
import com.cms.complaints.dto.CreatePriorityRequest;
import com.cms.complaints.dto.PriorityDto;
import com.cms.complaints.dto.ReportSummaryDto;
import com.cms.complaints.dto.TrendDataDto;
import com.cms.complaints.dto.UserDto;
import com.cms.complaints.entity.Role;
import com.cms.complaints.security.UserPrincipal;
import com.cms.complaints.service.AdminService;
import com.cms.complaints.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin management endpoints")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private ReportService reportService;

    @PostMapping("/categories")
    @Operation(summary = "Create a new category")
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        CategoryDto category = adminService.createCategory(request.getName(), request.getDescription());
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update a category")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long id,
            @RequestBody CreateCategoryRequest request) {
        CategoryDto category = adminService.updateCategory(id, request.getName(),
                request.getDescription(), null);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = adminService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/priorities")
    @Operation(summary = "Create a new priority")
    public ResponseEntity<PriorityDto> createPriority(
            @Valid @RequestBody CreatePriorityRequest request) {
        PriorityDto priority = adminService.createPriority(request.getName(),
                request.getLevel(), request.getSlaHours());
        return new ResponseEntity<>(priority, HttpStatus.CREATED);
    }

    @PutMapping("/priorities/{id}")
    @Operation(summary = "Update a priority")
    public ResponseEntity<PriorityDto> updatePriority(
            @PathVariable Long id,
            @RequestBody CreatePriorityRequest request) {
        PriorityDto priority = adminService.updatePriority(id, request.getName(),
                request.getLevel(), request.getSlaHours(), null);
        return ResponseEntity.ok(priority);
    }

    @GetMapping("/priorities")
    @Operation(summary = "Get all priorities")
    public ResponseEntity<List<PriorityDto>> getAllPriorities() {
        List<PriorityDto> priorities = adminService.getAllPriorities();
        return ResponseEntity.ok(priorities);
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        Page<UserDto> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Update user role")
    public ResponseEntity<UserDto> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role) {
        UserDto user = adminService.updateUserRole(id, Role.valueOf(role));
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/users/{id}/active")
    @Operation(summary = "Activate or deactivate user")
    public ResponseEntity<UserDto> updateUserActive(
            @PathVariable Long id,
            @RequestParam Boolean active) {
        UserDto user = adminService.updateUserActive(id, active);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/staff")
    @Operation(summary = "Get active staff members (for assignment)")
    public ResponseEntity<List<UserDto>> getStaffMembers() {
        List<UserDto> staff = adminService.getStaffMembers();
        return ResponseEntity.ok(staff);
    }

    @GetMapping("/reports/summary")
    @Operation(summary = "Get complaint summary report")
    public ResponseEntity<ReportSummaryDto> getSummaryReport() {
        ReportSummaryDto report = reportService.generateSummaryReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/trend")
    @Operation(summary = "Get complaint trend report")
    public ResponseEntity<List<TrendDataDto>> getTrendReport(
            @RequestParam(defaultValue = "30") int days) {
        List<TrendDataDto> trend = reportService.generateTrendReport(days);
        return ResponseEntity.ok(trend);
    }
}
