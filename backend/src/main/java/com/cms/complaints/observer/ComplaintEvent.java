package com.cms.complaints.observer;

import com.cms.complaints.entity.ActionType;
import com.cms.complaints.entity.Complaint;
import java.time.LocalDateTime;

public class ComplaintEvent {
    public enum EventType {
        CREATED, ASSIGNED, REASSIGNED, STATUS_CHANGED, REMARK_ADDED,
        RESOLVED, REOPENED, CLOSED, ESCALATED, FEEDBACK_GIVEN
    }

    private EventType type;
    private Complaint complaint;
    private Long actorId;
    private String actorName;
    private LocalDateTime timestamp;
    private String details;

    public ComplaintEvent(EventType type, Complaint complaint, Long actorId, String actorName) {
        this.type = type;
        this.complaint = complaint;
        this.actorId = actorId;
        this.actorName = actorName;
        this.timestamp = LocalDateTime.now();
    }

    public EventType getType() { return type; }
    public Complaint getComplaint() { return complaint; }
    public Long getActorId() { return actorId; }
    public String getActorName() { return actorName; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
