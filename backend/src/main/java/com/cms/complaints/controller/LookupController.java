package com.cms.complaints.controller;

import com.cms.complaints.dto.CategoryDto;
import com.cms.complaints.dto.PriorityDto;
import com.cms.complaints.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public lookup endpoints — accessible to all authenticated users (COMPLAINANT, STAFF, ADMIN).
 * These expose read-only reference data needed by forms (categories / priorities).
 */
@RestController
@RequestMapping("/lookup")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Lookup", description = "Reference data for complaint forms (categories and priorities)")
public class LookupController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/categories")
    @Operation(summary = "Get all active categories (available to all authenticated users)")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        return ResponseEntity.ok(adminService.getAllCategories());
    }

    @GetMapping("/priorities")
    @Operation(summary = "Get all active priorities ordered by level (available to all authenticated users)")
    public ResponseEntity<List<PriorityDto>> getPriorities() {
        return ResponseEntity.ok(adminService.getAllPriorities());
    }
}
