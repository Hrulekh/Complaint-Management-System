package com.cms.complaints.service;

import com.cms.complaints.dto.ReportSummaryDto;
import com.cms.complaints.dto.TrendDataDto;
import com.cms.complaints.entity.Category;
import com.cms.complaints.entity.Complaint;
import com.cms.complaints.entity.ComplaintStatus;
import com.cms.complaints.repository.CategoryRepository;
import com.cms.complaints.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {
    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public ReportSummaryDto generateSummaryReport() {
        ReportSummaryDto report = new ReportSummaryDto();

        long submitted = complaintRepository.countByStatus(ComplaintStatus.SUBMITTED);
        long assigned = complaintRepository.countByStatus(ComplaintStatus.ASSIGNED);
        long inProgress = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long closed = complaintRepository.countByStatus(ComplaintStatus.CLOSED);
        long reopened = complaintRepository.countByStatus(ComplaintStatus.REOPENED);

        report.setSubmittedCount(submitted);
        report.setAssignedCount(assigned);
        report.setInProgressCount(inProgress);
        report.setResolvedCount(resolved);
        report.setClosedCount(closed);
        report.setReopenedCount(reopened);

        long total = submitted + assigned + inProgress + resolved + closed + reopened;
        report.setTotalComplaints(total);

        long open = submitted + assigned + inProgress + reopened;
        report.setOpenComplaints(open);

        long overdueCount = complaintRepository.findOverdueNotEscalated(LocalDateTime.now()).size();
        report.setOverdueCount(overdueCount);

        double avgResolutionHours = calculateAverageResolutionTime();
        report.setAverageResolutionHours(avgResolutionHours);

        double avgRating = calculateAverageFeedbackRating();
        report.setAverageFeedbackRating(avgRating);

        // Category breakdown
        List<ReportSummaryDto.CategoryCount> byCategory = new ArrayList<>();
        List<Category> categories = categoryRepository.findAll();
        for (Category cat : categories) {
            long count = complaintRepository.countByCategory_Id(cat.getId());
            if (count > 0) {
                byCategory.add(new ReportSummaryDto.CategoryCount(cat.getName(), count));
            }
        }
        report.setByCategory(byCategory);

        return report;
    }

    public List<TrendDataDto> generateTrendReport(int days) {
        List<TrendDataDto> trends = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusDays(days);

        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
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
                    .mapToDouble(c -> ChronoUnit.HOURS.between(
                            c.getCreatedAt(), c.getResolvedAt()))
                    .sum();

            long validCount = resolved.stream()
                    .filter(c -> c.getCreatedAt() != null && c.getResolvedAt() != null)
                    .count();

            return validCount > 0 ? totalHours / validCount : 0;
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
