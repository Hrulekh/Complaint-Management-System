package com.cms.complaints.service;

import com.cms.complaints.entity.*;
import com.cms.complaints.exception.ConflictException;
import com.cms.complaints.exception.ResourceNotFoundException;
import com.cms.complaints.observer.ComplaintEvent;
import com.cms.complaints.observer.ComplaintEventPublisher;
import com.cms.complaints.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LifecycleServiceTest {
    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ComplaintHistoryRepository historyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ComplaintEventPublisher eventPublisher;

    @InjectMocks
    private LifecycleService lifecycleService;

    private Complaint complaint;
    private User staff;
    private User admin;
    private Category category;
    private Priority priority;

    @BeforeEach
    void setUp() {
        category = new Category("Test Category", "Test");
        category.setId(1L);

        priority = new Priority("High", 3, 24);
        priority.setId(1L);

        staff = new User("Staff Name", "staff@cms.local", "hash", "9876543210", Role.STAFF);
        staff.setId(2L);

        admin = new User("Admin Name", "admin@cms.local", "hash", "9876543210", Role.ADMIN);
        admin.setId(3L);

        User complainant = new User("Complainant Name", "user@cms.local", "hash", "9876543210", Role.COMPLAINANT);
        complainant.setId(1L);

        complaint = new Complaint("CMP-2026-000001", "Test Title", "Test Description",
                                 category, priority, complainant);
        complaint.setId(1L);
        complaint.setStatus(ComplaintStatus.SUBMITTED);
    }

    @Test
    void testValidStatusTransitions() {
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(historyRepository.save(any())).thenReturn(new ComplaintHistory());

        lifecycleService.updateStatus(1L, ComplaintStatus.ASSIGNED, "Assigning", 2L);

        assertEquals(ComplaintStatus.ASSIGNED, complaint.getStatus());
        verify(eventPublisher).publish(any(ComplaintEvent.class));
    }

    @Test
    void testInvalidStatusTransition() {
        complaint.setStatus(ComplaintStatus.CLOSED);
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));

        assertThrows(ConflictException.class,
            () -> lifecycleService.updateStatus(1L, ComplaintStatus.ASSIGNED, "Invalid", 2L));
    }

    @Test
    void testResolveComplaintRequiresSummary() {
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));

        lifecycleService.resolveComplaint(1L, "Resolution details", 2L);

        assertEquals(ComplaintStatus.RESOLVED, complaint.getStatus());
        assertEquals("Resolution details", complaint.getResolutionSummary());
        assertNotNull(complaint.getResolvedAt());
    }

    @Test
    void testClosedComplaintCannotBeModified() {
        complaint.setStatus(ComplaintStatus.CLOSED);
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));

        assertThrows(ConflictException.class,
            () -> lifecycleService.updateStatus(1L, ComplaintStatus.REOPENED, null, 2L));
    }

    @Test
    void testReopenComplaint() {
        complaint.setStatus(ComplaintStatus.RESOLVED);
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        lifecycleService.reopenComplaint(1L, "Not satisfied", 1L);

        assertEquals(ComplaintStatus.REOPENED, complaint.getStatus());
        verify(eventPublisher).publish(any(ComplaintEvent.class));
    }

    @Test
    void testFeedbackOnlyAfterResolution() {
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));

        assertThrows(ConflictException.class,
            () -> lifecycleService.addFeedback(1L, 5, "Great service", 1L));
    }

    @Test
    void testFeedbackOnceOnly() {
        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setFeedbackRating(4);
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));

        assertThrows(ConflictException.class,
            () -> lifecycleService.addFeedback(1L, 5, "Another feedback", 1L));
    }
}
