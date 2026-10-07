package com.cms.complaints.dto;

import java.util.List;

public class ReportSummaryDto {
    private long totalComplaints;
    private long submittedCount;
    private long assignedCount;
    private long inProgressCount;
    private long resolvedCount;
    private long closedCount;
    private long reopenedCount;
    private long openComplaints;
    private long overdueCount;
    private double averageResolutionHours;
    private double averageFeedbackRating;
    private List<CategoryCount> byCategory;

    public ReportSummaryDto() {}

    // totalComplaints
    public long getTotalComplaints() { return totalComplaints; }
    public void setTotalComplaints(long totalComplaints) { this.totalComplaints = totalComplaints; }

    // submittedCount
    public long getSubmittedCount() { return submittedCount; }
    public void setSubmittedCount(long submittedCount) { this.submittedCount = submittedCount; }

    // assignedCount
    public long getAssignedCount() { return assignedCount; }
    public void setAssignedCount(long assignedCount) { this.assignedCount = assignedCount; }

    // inProgressCount
    public long getInProgressCount() { return inProgressCount; }
    public void setInProgressCount(long inProgressCount) { this.inProgressCount = inProgressCount; }

    // resolvedCount
    public long getResolvedCount() { return resolvedCount; }
    public void setResolvedCount(long resolvedCount) { this.resolvedCount = resolvedCount; }

    // closedCount
    public long getClosedCount() { return closedCount; }
    public void setClosedCount(long closedCount) { this.closedCount = closedCount; }

    // reopenedCount
    public long getReopenedCount() { return reopenedCount; }
    public void setReopenedCount(long reopenedCount) { this.reopenedCount = reopenedCount; }

    // openComplaints (submitted + assigned + in_progress + reopened)
    public long getOpenComplaints() { return openComplaints; }
    public void setOpenComplaints(long openComplaints) { this.openComplaints = openComplaints; }

    // overdueCount
    public long getOverdueCount() { return overdueCount; }
    public void setOverdueCount(long overdueCount) { this.overdueCount = overdueCount; }

    // averageResolutionHours
    public double getAverageResolutionHours() { return averageResolutionHours; }
    public void setAverageResolutionHours(double averageResolutionHours) { this.averageResolutionHours = averageResolutionHours; }

    // averageFeedbackRating
    public double getAverageFeedbackRating() { return averageFeedbackRating; }
    public void setAverageFeedbackRating(double averageFeedbackRating) { this.averageFeedbackRating = averageFeedbackRating; }

    // byCategory
    public List<CategoryCount> getByCategory() { return byCategory; }
    public void setByCategory(List<CategoryCount> byCategory) { this.byCategory = byCategory; }

    /**
     * Inner class for category-wise breakdown.
     */
    public static class CategoryCount {
        private String name;
        private long count;

        public CategoryCount() {}

        public CategoryCount(String name, long count) {
            this.name = name;
            this.count = count;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }
}
