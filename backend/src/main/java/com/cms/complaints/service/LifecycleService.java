package com.cms.complaints.service;

import com.cms.complaints.entity.*;
import com.cms.complaints.exception.ConflictException;
import com.cms.complaints.exception.ResourceNotFoundException;
import com.cms.complaints.observer.ComplaintEvent;
import com.cms.complaints.observer.ComplaintEventPublisher;
import com.cms.complaints.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LifecycleService {
    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintHistoryRepository historyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintEventPublisher eventPublisher;

    @Transactional
    public void assignComplaint(Long complaintId, Long staffId, Long adminId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (complaint.getStatus() != ComplaintStatus.SUBMITTED &&
            complaint.getStatus() != ComplaintStatus.REOPENED) {
            throw new ConflictException("Complaint cannot be assigned in " + complaint.getStatus() + " status");
        }

        User previousAssignee = complaint.getAssignedTo();
        complaint.setAssignedTo(staff);
        complaint.setStatus(ComplaintStatus.ASSIGNED);
        complaint.setUpdatedAt(LocalDateTime.now());

        ComplaintHistory history = new ComplaintHistory(complaint,
            previousAssignee != null ? ActionType.REASSIGNED : ActionType.ASSIGNED, admin);
        history.setFromStatus(complaint.getStatus());
        history.setToStatus(ComplaintStatus.ASSIGNED);
        historyRepository.save(history);

        ComplaintEvent event = new ComplaintEvent(
            previousAssignee != null ? ComplaintEvent.EventType.REASSIGNED : ComplaintEvent.EventType.ASSIGNED,
            complaint, adminId, admin.getFullName());
        eventPublisher.publish(event);
    }

    @Transactional
    public void updateStatus(Long complaintId, ComplaintStatus newStatus, String remark, Long userId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ComplaintStatus oldStatus = complaint.getStatus();
        validateStatusTransition(oldStatus, newStatus);

        complaint.setStatus(newStatus);
        complaint.setUpdatedAt(LocalDateTime.now());
        if (newStatus == ComplaintStatus.RESOLVED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }

        ComplaintHistory history = new ComplaintHistory(complaint, ActionType.STATUS_CHANGED, user);
        history.setFromStatus(oldStatus);
        history.setToStatus(newStatus);
        history.setRemark(remark);
        historyRepository.save(history);

        ComplaintEvent event = new ComplaintEvent(ComplaintEvent.EventType.STATUS_CHANGED, complaint, userId, user.getFullName());
        event.setDetails("Status changed from " + oldStatus + " to " + newStatus);
        eventPublisher.publish(event);
    }

    @Transactional
    public void resolveComplaint(Long complaintId, String resolutionSummary, Long userId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (complaint.getStatus() != ComplaintStatus.IN_PROGRESS) {
            throw new ConflictException("Only in-progress complaints can be resolved");
        }

        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setResolutionSummary(resolutionSummary);
        complaint.setResolvedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());

        ComplaintHistory history = new ComplaintHistory(complaint, ActionType.RESOLVED, user);
        history.setFromStatus(ComplaintStatus.IN_PROGRESS);
        history.setToStatus(ComplaintStatus.RESOLVED);
        history.setRemark(resolutionSummary);
        historyRepository.save(history);

        ComplaintEvent event = new ComplaintEvent(ComplaintEvent.EventType.RESOLVED, complaint, userId, user.getFullName());
        event.setDetails(resolutionSummary);
        eventPublisher.publish(event);
    }

    @Transactional
    public void closeComplaint(Long complaintId, Long userId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new ConflictException("Only resolved complaints can be closed");
        }

        complaint.setStatus(ComplaintStatus.CLOSED);
        complaint.setClosedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());

        ComplaintHistory history = new ComplaintHistory(complaint, ActionType.CLOSED, user);
        history.setFromStatus(ComplaintStatus.RESOLVED);
        history.setToStatus(ComplaintStatus.CLOSED);
        historyRepository.save(history);

        ComplaintEvent event = new ComplaintEvent(ComplaintEvent.EventType.CLOSED, complaint, userId, user.getFullName());
        eventPublisher.publish(event);
    }

    @Transactional
    public void reopenComplaint(Long complaintId, String reason, Long userId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (complaint.getStatus() == ComplaintStatus.CLOSED) {
            throw new ConflictException("Closed complaints cannot be reopened by non-admins");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED &&
            complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new ConflictException("Only resolved or closed complaints can be reopened");
        }

        complaint.setStatus(ComplaintStatus.REOPENED);
        complaint.setUpdatedAt(LocalDateTime.now());

        ComplaintHistory history = new ComplaintHistory(complaint, ActionType.REOPENED, user);
        history.setFromStatus(complaint.getStatus());
        history.setToStatus(ComplaintStatus.REOPENED);
        history.setRemark(reason);
        historyRepository.save(history);

        ComplaintEvent event = new ComplaintEvent(ComplaintEvent.EventType.REOPENED, complaint, userId, user.getFullName());
        event.setDetails(reason);
        eventPublisher.publish(event);
    }

    @Transactional
    public void addFeedback(Long complaintId, Integer rating, String comment, Long userId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        if (complaint.getFeedbackRating() != null) {
            throw new ConflictException("Feedback already provided for this complaint");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED &&
            complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new ConflictException("Feedback can only be given for resolved or closed complaints");
        }

        complaint.setFeedbackRating(rating);
        complaint.setFeedbackComment(comment);
        complaint.setUpdatedAt(LocalDateTime.now());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ComplaintHistory history = new ComplaintHistory(complaint, ActionType.FEEDBACK_GIVEN, user);
        history.setRemark("Rating: " + rating + (comment != null ? ", Comment: " + comment : ""));
        historyRepository.save(history);

        ComplaintEvent event = new ComplaintEvent(ComplaintEvent.EventType.FEEDBACK_GIVEN, complaint, userId, user.getFullName());
        eventPublisher.publish(event);
    }

    @Scheduled(fixedDelay = 900000)
    @Transactional
    public void checkAndEscalateOverdueComplaints() {
        List<Complaint> overdueComplaints = complaintRepository.findOverdueNotEscalated(LocalDateTime.now());

        for (Complaint complaint : overdueComplaints) {
            complaint.setEscalated(true);
            complaint.setUpdatedAt(LocalDateTime.now());
            complaintRepository.save(complaint);

            ComplaintEvent event = new ComplaintEvent(ComplaintEvent.EventType.ESCALATED, complaint,
                                                     complaint.getId(), "System");
            eventPublisher.publish(event);
        }
    }

    private void validateStatusTransition(ComplaintStatus from, ComplaintStatus to) {
        if (from == ComplaintStatus.CLOSED) {
            throw new ConflictException("Closed complaints cannot be modified");
        }

        boolean validTransition = false;
        switch (from) {
            case SUBMITTED:
                validTransition = to == ComplaintStatus.ASSIGNED;
                break;
            case ASSIGNED:
                validTransition = to == ComplaintStatus.IN_PROGRESS;
                break;
            case IN_PROGRESS:
                validTransition = to == ComplaintStatus.RESOLVED;
                break;
            case RESOLVED:
                validTransition = to == ComplaintStatus.CLOSED || to == ComplaintStatus.REOPENED;
                break;
            case REOPENED:
                validTransition = to == ComplaintStatus.ASSIGNED || to == ComplaintStatus.IN_PROGRESS;
                break;
        }

        if (!validTransition) {
            throw new ConflictException("Invalid status transition from " + from + " to " + to);
        }
    }
}
