package com.cms.complaints.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreatePriorityRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Level is required")
    private Integer level;

    private Integer slaHours;

    public CreatePriorityRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Integer getSlaHours() { return slaHours; }
    public void setSlaHours(Integer slaHours) { this.slaHours = slaHours; }
}
