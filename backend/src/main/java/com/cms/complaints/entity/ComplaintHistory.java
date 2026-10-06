package com.cms.complaints.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_history", indexes = {
    @Index(name = "idx_complaint_id", columnList = "complaint_id"),
    @Index(name = "idx_action_type", columnList = "action_type")
})
public class ComplaintHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus fromStatus;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;

    @Column(columnDefinition = "LONGTEXT")
    private String remark;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ComplaintHistory() {}

    public ComplaintHistory(Complaint complaint, ActionType actionType, User performedBy) {
        this.complaint = complaint;
        this.actionType = actionType;
        this.performedBy = performedBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Complaint getComplaint() { return complaint; }
    public void setComplaint(Complaint complaint) { this.complaint = complaint; }

    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }

    public ComplaintStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(ComplaintStatus fromStatus) { this.fromStatus = fromStatus; }

    public ComplaintStatus getToStatus() { return toStatus; }
    public void setToStatus(ComplaintStatus toStatus) { this.toStatus = toStatus; }

    public User getPerformedBy() { return performedBy; }
    public void setPerformedBy(User performedBy) { this.performedBy = performedBy; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
