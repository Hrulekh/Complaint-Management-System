package com.cms.complaints.dto;

import com.cms.complaints.entity.ActionType;
import com.cms.complaints.entity.ComplaintStatus;
import java.time.LocalDateTime;

public class HistoryDto {
    private Long id;
    private ActionType actionType;
    private ComplaintStatus fromStatus;
    private ComplaintStatus toStatus;
    private UserDto performedBy;
    private String remark;
    private LocalDateTime createdAt;

    public HistoryDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }

    public ComplaintStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(ComplaintStatus fromStatus) { this.fromStatus = fromStatus; }

    public ComplaintStatus getToStatus() { return toStatus; }
    public void setToStatus(ComplaintStatus toStatus) { this.toStatus = toStatus; }

    public UserDto getPerformedBy() { return performedBy; }
    public void setPerformedBy(UserDto performedBy) { this.performedBy = performedBy; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
