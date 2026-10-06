package com.cms.complaints.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AssignComplaintRequest {
    @NotNull(message = "Staff ID is required")
    private Long staffId;

    public AssignComplaintRequest() {}

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
}
