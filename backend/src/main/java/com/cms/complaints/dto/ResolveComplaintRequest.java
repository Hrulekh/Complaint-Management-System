package com.cms.complaints.dto;

import jakarta.validation.constraints.NotBlank;

public class ResolveComplaintRequest {
    @NotBlank(message = "Resolution summary is required")
    private String resolutionSummary;

    public ResolveComplaintRequest() {}

    public String getResolutionSummary() { return resolutionSummary; }
    public void setResolutionSummary(String resolutionSummary) { this.resolutionSummary = resolutionSummary; }
}
