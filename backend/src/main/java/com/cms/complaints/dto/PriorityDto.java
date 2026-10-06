package com.cms.complaints.dto;

public class PriorityDto {
    private Long id;
    private String name;
    private Integer level;
    private Integer slaHours;
    private Boolean active;

    public PriorityDto() {}

    public PriorityDto(Long id, String name, Integer level, Integer slaHours, Boolean active) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.slaHours = slaHours;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Integer getSlaHours() { return slaHours; }
    public void setSlaHours(Integer slaHours) { this.slaHours = slaHours; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
