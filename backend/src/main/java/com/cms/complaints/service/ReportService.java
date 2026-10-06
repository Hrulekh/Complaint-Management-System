package com.cms.complaints.service;

import com.cms.complaints.dto.ReportSummaryDto;
import com.cms.complaints.dto.TrendDataDto;
import com.cms.complaints.entity.Complaint;
import com.cms.complaints.entity.ComplaintStatus;
import com.cms.complaints.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {
    @Autowired
    private ComplaintRepository complaintRepository;

    public ReportSummaryDto generateSummaryReport() {
        ReportSummaryDto report = new ReportSummaryDto();

        report.setSubmittedCount(complaintRepository.countByStatus(ComplaintStatus.SUBMITTED));
        report.setAssignedCount(complaintRepository.countByStatus(ComplaintStatus.ASSIGNED));
        report.setInProgressCount(complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS));
        report.setResolvedCount(complaintRepository.countByStatus(ComplaintStatus.RESOLVED));
        report.setClosedCount(complaintRepository.countByStatus(ComplaintStatus.CLOSED));
        report.setReopenedCount(complaintRepository.countByStatus(ComplaintStatus.REOPENED));

        long overdueCount = complaintRepository.findOverdueNotEscalated(LocalDateTime.now()).size();
        report.setOverdueCount(overdueCount);

        double avgResolutionHours = calculateAverageResolutionTime();
        report.setAverageResolutionHours(avgResolutionHours);

        double avgRating = calculateAverageFeedbackRating();
        report.setAverageFeedbackRating(avgRating);

        return report;
    }

    public List<TrendDataDto> generateTrendReport(int days) {
        List<TrendDataDto> trends = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusDays(days);

        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            LocalDateTime startOfDay = currentDate.atStartOfDay();
            LocalDateTime endOfDay = currentDate.atTime(23, 59, 59);

            String dateStr = currentDate.format(DateTimeFormatter.ISO_DATE);
            long createdCount = 0;
            long resolvedCount = 0;

            trends.add(new TrendDataDto(dateStr, createdCount, resolvedCount));
        }

        return trends;
    }

    private double calculateAverageResolutionTime() {
        try {
            List<Complaint> resolved = complaintRepository.findByStatus(ComplaintStatus.RESOLVED,
                    org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent();

            if (resolved.isEmpty()) return 0;

            double totalHours = resolved.stream()
                    .filter(c -> c.getCreatedAt() != null && c.getResolvedAt() != null)
                    .mapToDouble(c -> java.time.temporal.ChronoUnit.HOURS.between(
                            c.getCreatedAt(), c.getResolvedAt()))
                    .sum();

            return totalHours / resolved.size();
        } catch (Exception e) {
            return 0;
        }
    }

    private double calculateAverageFeedbackRating() {
        try {
            List<Complaint> allComplaints = complaintRepository.findAll();
            List<Integer> ratings = allComplaints.stream()
                    .filter(c -> c.getFeedbackRating() != null)
                    .map(Complaint::getFeedbackRating)
                    .collect(Collectors.toList());

            if (ratings.isEmpty()) return 0;
            return ratings.stream().mapToDouble(Integer::doubleValue).average().orElse(0);
        } catch (Exception e) {
            return 0;
        }
    }
}
