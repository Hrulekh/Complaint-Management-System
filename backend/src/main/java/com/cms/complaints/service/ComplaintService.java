package com.cms.complaints.service;

import com.cms.complaints.dto.ComplaintDto;
import com.cms.complaints.dto.HistoryDto;
import com.cms.complaints.entity.*;
import com.cms.complaints.exception.BadRequestException;
import com.cms.complaints.exception.ConflictException;
import com.cms.complaints.exception.ResourceNotFoundException;
import com.cms.complaints.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ComplaintService {
    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintHistoryRepository historyRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PriorityRepository priorityRepository;

    public ComplaintDto createComplaint(String title, String description, Long categoryId,
                                       Long priorityId, Long createdById) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Priority priority = priorityRepository.findById(priorityId)
                .orElseThrow(() -> new ResourceNotFoundException("Priority not found"));
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String ticketId = generateTicketId();
        Complaint complaint = new Complaint(ticketId, title, description, category, priority, createdBy);
        complaint.setDueAt(LocalDateTime.now().plusHours(priority.getSlaHours()));

        Complaint saved = complaintRepository.save(complaint);

        ComplaintHistory history = new ComplaintHistory(saved, ActionType.CREATED, createdBy);
        historyRepository.save(history);

        return mapToDto(saved);
    }

    public ComplaintDto getComplaintById(Long id, Long userId, Role role) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        if (!canAccessComplaint(complaint, userId, role)) {
            throw new ResourceNotFoundException("Complaint not found");
        }

        return mapToDto(complaint);
    }

    public ComplaintDto getComplaintByTicketId(String ticketId, Long userId, Role role) {
        Complaint complaint = complaintRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        if (userId != null && !canAccessComplaint(complaint, userId, role)) {
            throw new ResourceNotFoundException("Complaint not found");
        }

        return mapToDto(complaint);
    }

    public Page<ComplaintDto> getMyComplaints(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return complaintRepository.findByCreatedBy(user, pageable).map(this::mapToDto);
    }

    public Page<ComplaintDto> getAssignedComplaints(Long staffId, Pageable pageable) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return complaintRepository.findByAssignedTo(staff, pageable).map(this::mapToDto);
    }

    public Page<ComplaintDto> searchComplaints(ComplaintStatus status, Long categoryId, Long priorityId,
                                               Long assigneeId, LocalDateTime fromDate, LocalDateTime toDate,
                                               String search, Pageable pageable) {
        return complaintRepository.findWithFilters(status, categoryId, priorityId, assigneeId,
                                                   fromDate, toDate, search, pageable)
                .map(this::mapToDto);
    }

    public List<HistoryDto> getComplaintHistory(Long complaintId, Long userId, Role role) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        if (!canAccessComplaint(complaint, userId, role)) {
            throw new ResourceNotFoundException("Complaint not found");
        }

        return historyRepository.findByComplaintOrderByCreatedAtDesc(complaint)
                .stream()
                .map(this::mapHistoryToDto)
                .collect(Collectors.toList());
    }

    private String generateTicketId() {
        int year = LocalDateTime.now().getYear();
        long count = complaintRepository.count() + 1;
        return String.format("CMP-%d-%06d", year, count);
    }

    private boolean canAccessComplaint(Complaint complaint, Long userId, Role role) {
        if (role == Role.ADMIN) return true;
        if (role == Role.STAFF) {
            return complaint.getAssignedTo() != null && complaint.getAssignedTo().getId().equals(userId);
        }
        return complaint.getCreatedBy().getId().equals(userId);
    }

    private ComplaintDto mapToDto(Complaint complaint) {
        ComplaintDto dto = new ComplaintDto();
        dto.setId(complaint.getId());
        dto.setTicketId(complaint.getTicketId());
        dto.setTitle(complaint.getTitle());
        dto.setDescription(complaint.getDescription());
        dto.setStatus(complaint.getStatus());
        dto.setCreatedAt(complaint.getCreatedAt());
        dto.setUpdatedAt(complaint.getUpdatedAt());
        dto.setResolvedAt(complaint.getResolvedAt());
        dto.setClosedAt(complaint.getClosedAt());
        dto.setResolutionSummary(complaint.getResolutionSummary());
        dto.setFeedbackRating(complaint.getFeedbackRating());
        dto.setFeedbackComment(complaint.getFeedbackComment());
        dto.setDueAt(complaint.getDueAt());

        if (complaint.getCategory() != null) {
            dto.setCategory(new CategoryDto(complaint.getCategory().getId(),
                    complaint.getCategory().getName(),
                    complaint.getCategory().getDescription(),
                    complaint.getCategory().getActive()));
        }

        if (complaint.getPriority() != null) {
            dto.setPriority(new PriorityDto(complaint.getPriority().getId(),
                    complaint.getPriority().getName(),
                    complaint.getPriority().getLevel(),
                    complaint.getPriority().getSlaHours(),
                    complaint.getPriority().getActive()));
        }

        if (complaint.getCreatedBy() != null) {
            dto.setCreatedBy(mapUserToDto(complaint.getCreatedBy()));
        }

        if (complaint.getAssignedTo() != null) {
            dto.setAssignedTo(mapUserToDto(complaint.getAssignedTo()));
        }

        List<Attachment> attachments = attachmentRepository.findByComplaint(complaint);
        dto.setAttachments(attachments.stream()
                .map(a -> {
                    AttachmentDto adto = new AttachmentDto();
                    adto.setId(a.getId());
                    adto.setFileName(a.getFileName());
                    adto.setStoredName(a.getStoredName());
                    adto.setContentType(a.getContentType());
                    adto.setSizeBytes(a.getSizeBytes());
                    adto.setUploadedAt(a.getUploadedAt());
                    if (a.getUploadedBy() != null) {
                        adto.setUploadedBy(mapUserToDto(a.getUploadedBy()));
                    }
                    return adto;
                })
                .collect(Collectors.toList()));

        return dto;
    }

    private HistoryDto mapHistoryToDto(ComplaintHistory history) {
        HistoryDto dto = new HistoryDto();
        dto.setId(history.getId());
        dto.setActionType(history.getActionType());
        dto.setFromStatus(history.getFromStatus());
        dto.setToStatus(history.getToStatus());
        dto.setRemark(history.getRemark());
        dto.setCreatedAt(history.getCreatedAt());
        if (history.getPerformedBy() != null) {
            dto.setPerformedBy(mapUserToDto(history.getPerformedBy()));
        }
        return dto;
    }

    private UserDto mapUserToDto(User user) {
        return new UserDto(user.getId(), user.getFullName(), user.getEmail(),
                          user.getPhone(), user.getRole(), user.getActive());
    }

    public List<Complaint> findOverdueComplaints() {
        return complaintRepository.findOverdueNotEscalated(LocalDateTime.now());
    }
}
