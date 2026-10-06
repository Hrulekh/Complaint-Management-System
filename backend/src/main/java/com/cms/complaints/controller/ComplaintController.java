package com.cms.complaints.controller;

import com.cms.complaints.dto.ComplaintDto;
import com.cms.complaints.dto.CreateComplaintRequest;
import com.cms.complaints.dto.HistoryDto;
import com.cms.complaints.entity.Role;
import com.cms.complaints.security.UserPrincipal;
import com.cms.complaints.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/complaints")
@Tag(name = "Complaints", description = "Complaint management endpoints")
public class ComplaintController {
    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private com.cms.complaints.service.LifecycleService lifecycleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPLAINANT', 'STAFF', 'ADMIN')")
    @Operation(summary = "Create a new complaint")
    public ResponseEntity<ComplaintDto> createComplaint(
            @Valid @RequestBody CreateComplaintRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ComplaintDto complaint = complaintService.createComplaint(
                request.getTitle(),
                request.getDescription(),
                request.getCategoryId(),
                request.getPriorityId(),
                principal.getId()
        );
        return new ResponseEntity<>(complaint, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('COMPLAINANT', 'STAFF', 'ADMIN')")
    @Operation(summary = "Get my complaints")
    public ResponseEntity<Page<ComplaintDto>> getMyComplaints(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        Page<ComplaintDto> complaints = complaintService.getMyComplaints(principal.getId(), pageable);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPLAINANT', 'STAFF', 'ADMIN')")
    @Operation(summary = "Get complaint by ID")
    public ResponseEntity<ComplaintDto> getComplaintById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        ComplaintDto complaint = complaintService.getComplaintById(id, principal.getId(),
                principal.getRole() != null ? Role.valueOf(principal.getRole()) : Role.COMPLAINANT);
        return ResponseEntity.ok(complaint);
    }

    @GetMapping("/ticket/{ticketId}")
    @Operation(summary = "Get complaint by ticket ID (public tracking)")
    public ResponseEntity<ComplaintDto> getComplaintByTicketId(
            @PathVariable String ticketId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        Role role = principal != null && principal.getRole() != null ?
                   Role.valueOf(principal.getRole()) : null;
        ComplaintDto complaint = complaintService.getComplaintByTicketId(ticketId, userId, role);
        return ResponseEntity.ok(complaint);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Search complaints (staff/admin)")
    public ResponseEntity<Page<ComplaintDto>> searchComplaints(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long priorityId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<ComplaintDto> complaints = complaintService.searchComplaints(
                status != null ? com.cms.complaints.entity.ComplaintStatus.valueOf(status) : null,
                categoryId, priorityId, assigneeId, fromDate, toDate, search, pageable
        );
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('COMPLAINANT', 'STAFF', 'ADMIN')")
    @Operation(summary = "Get complaint history")
    public ResponseEntity<List<HistoryDto>> getComplaintHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<HistoryDto> history = complaintService.getComplaintHistory(id, principal.getId(),
                principal.getRole() != null ? Role.valueOf(principal.getRole()) : Role.COMPLAINANT);
        return ResponseEntity.ok(history);
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign complaint to staff (admin only)")
    public ResponseEntity<Void> assignComplaint(
            @PathVariable Long id,
            @Valid @RequestBody com.cms.complaints.dto.AssignComplaintRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        lifecycleService.assignComplaint(id, request.getStaffId(), principal.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Update complaint status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody com.cms.complaints.dto.UpdateComplaintStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        lifecycleService.updateStatus(id,
                com.cms.complaints.entity.ComplaintStatus.valueOf(request.getStatus()),
                request.getRemark(), principal.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Resolve complaint with summary")
    public ResponseEntity<Void> resolveComplaint(
            @PathVariable Long id,
            @Valid @RequestBody com.cms.complaints.dto.ResolveComplaintRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        lifecycleService.resolveComplaint(id, request.getResolutionSummary(), principal.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Close resolved complaint")
    public ResponseEntity<Void> closeComplaint(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        lifecycleService.closeComplaint(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reopen")
    @PreAuthorize("hasAnyRole('COMPLAINANT', 'ADMIN')")
    @Operation(summary = "Reopen resolved complaint")
    public ResponseEntity<Void> reopenComplaint(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserPrincipal principal) {
        lifecycleService.reopenComplaint(id, reason, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/feedback")
    @PreAuthorize("hasRole('COMPLAINANT')")
    @Operation(summary = "Provide feedback on resolved complaint")
    public ResponseEntity<Void> provideFeedback(
            @PathVariable Long id,
            @Valid @RequestBody com.cms.complaints.dto.FeedbackRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        lifecycleService.addFeedback(id, request.getRating(), request.getComment(), principal.getId());
        return ResponseEntity.noContent().build();
    }
}
