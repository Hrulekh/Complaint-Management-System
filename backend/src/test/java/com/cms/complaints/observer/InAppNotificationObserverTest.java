package com.cms.complaints.observer;

import com.cms.complaints.entity.*;
import com.cms.complaints.repository.NotificationRepository;
import com.cms.complaints.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InAppNotificationObserverTest {
    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InAppNotificationObserver observer;

    private Complaint complaint;
    private User complainant;
    private User staff;

    @BeforeEach
    void setUp() {
        complainant = new User("Complainant", "user@cms.local", "hash", "9876543210", Role.COMPLAINANT);
        complainant.setId(1L);

        staff = new User("Staff", "staff@cms.local", "hash", "9876543210", Role.STAFF);
        staff.setId(2L);

        Category category = new Category("Test", "Test");
        category.setId(1L);

        Priority priority = new Priority("High", 3, 24);
        priority.setId(1L);

        complaint = new Complaint("CMP-2026-000001", "Title", "Description", category, priority, complainant);
        complaint.setId(1L);
        complaint.setAssignedTo(staff);
    }

    @Test
    void testObserverNotifiesOnComplaintCreated() {
        ComplaintEvent event = new ComplaintEvent(ComplaintEvent.EventType.CREATED, complaint, 1L, "Complainant");

        observer.update(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());

        Notification notification = captor.getValue();
        assert notification.getUser().equals(complainant);
        assert notification.getMessage().contains("filed");
    }

    @Test
    void testObserverNotifiesOnAssignment() {
        ComplaintEvent event = new ComplaintEvent(ComplaintEvent.EventType.ASSIGNED, complaint, 2L, "Admin");

        observer.update(event);

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void testObserverNotifiesOnStatusChange() {
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        ComplaintEvent event = new ComplaintEvent(ComplaintEvent.EventType.STATUS_CHANGED, complaint, 2L, "Staff");

        observer.update(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());
    }

    @Test
    void testObserverNotifiesOnResolution() {
        ComplaintEvent event = new ComplaintEvent(ComplaintEvent.EventType.RESOLVED, complaint, 2L, "Staff");
        event.setDetails("Issue resolved successfully");

        observer.update(event);

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }
}
