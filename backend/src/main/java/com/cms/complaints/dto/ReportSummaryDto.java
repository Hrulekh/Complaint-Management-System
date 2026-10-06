package com.cms.complaints.dto;

public class ReportSummaryDto {
    private long submittedCount;
    private long assignedCount;
    private long inProgressCount;
    private long resolvedCount;
    private long closedCount;
    private long reopenedCount;
    private long overdueCount;
    private double averageResolutionHours;
    private double averageFeedbackRating;

    public ReportSummaryDto() {}

    public long getSubmittedCount() { return submittedCount; }
    public void setSubmittedCount(long submittedCount) { this.submittedCount = submittedCount; }

    public long getAssignedCount() { return assignedCount; }
    public void setAssignedCount(long assignedCount) { this.assignedCount = assignedCount; }

    public long getInProgressCount() { return inProgressCount; }
    public void setInProgressCount(long inProgressCount) { this.inProgressCount = inProgressCount; }

    public long getResolvedCount() { return resolvedCount; }
    public void setResolvedCount(long resolvedCount) { this.resolvedCount = resolvedCount; }

    public long getClosedCount() { return closedCount; }
    public void setClosedCount(long closedCount) { this.closedCount = closedCount; }

    public long getReopenedCount() { return reopenedCount; }
    public void setReopenedCount(long reopenedCount) { this.reopenedCount = reopenedCount; }

    public long getOverdueCount() { return overdueCount; }
    public void setOverdueCount(long overdueCount) { this.overdueCount = overdueCount; }

    public double getAverageResolutionHours() { return averageResolutionHours; }
    public void setAverageResolutionHours(double averageResolutionHours) { this.averageResolutionHours = averageResolutionHours; }

    public double getAverageFeedbackRating() { return averageFeedbackRating; }
    public void setAverageFeedbackRating(double averageFeedbackRating) { this.averageFeedbackRating = averageFeedbackRating; }
}
