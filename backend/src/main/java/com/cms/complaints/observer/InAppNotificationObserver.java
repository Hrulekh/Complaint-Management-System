package com.cms.complaints.observer;

import com.cms.complaints.entity.Notification;
import com.cms.complaints.entity.User;
import com.cms.complaints.repository.NotificationRepository;
import com.cms.complaints.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InAppNotificationObserver implements ComplaintObserver {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void update(ComplaintEvent event) {
        switch (event.getType()) {
            case CREATED:
                notifyComplainant(event, "Your complaint has been filed. Keep this number: " +
                                 event.getComplaint().getTicketId());
                break;
            case ASSIGNED:
                notifyComplainant(event, "Your complaint has been assigned to " + event.getActorName());
                notifyAdminsAndStaff(event, event.getActorName() + " has been assigned to complaint " +
                                    event.getComplaint().getTicketId());
                break;
            case STATUS_CHANGED:
                notifyComplainant(event, "Your complaint status changed to " +
                                 event.getComplaint().getStatus());
                notifyAdminsAndStaff(event, "Status changed to " + event.getComplaint().getStatus());
                break;
            case RESOLVED:
                notifyComplainant(event, "Your complaint has been resolved. " +
                                 (event.getDetails() != null ? event.getDetails() : ""));
                notifyAdminsAndStaff(event, "Complaint resolved by " + event.getActorName());
                break;
            case CLOSED:
                notifyComplainant(event, "Your complaint has been closed.");
                notifyAdminsAndStaff(event, "Complaint closed by " + event.getActorName());
                break;
            case REOPENED:
                notifyComplainant(event, "Your complaint has been reopened.");
                notifyAdminsAndStaff(event, "Complaint reopened by " + event.getActorName());
                break;
            case ESCALATED:
                notifyAdminsAndStaff(event, "Complaint " + event.getComplaint().getTicketId() +
                                    " is overdue and has been escalated");
                break;
            case FEEDBACK_GIVEN:
                notifyAdminsAndStaff(event, "Feedback received for complaint " +
                                    event.getComplaint().getTicketId());
                break;
        }
    }

    private void notifyComplainant(ComplaintEvent event, String message) {
        User complainant = event.getComplaint().getCreatedBy();
        if (complainant != null) {
            Notification notification = new Notification(complainant, event.getComplaint(),
                                                        event.getType().name(), message);
            notificationRepository.save(notification);
        }
    }

    private void notifyAdminsAndStaff(ComplaintEvent event, String message) {
        if (event.getComplaint().getAssignedTo() != null) {
            Notification notification = new Notification(event.getComplaint().getAssignedTo(),
                                                        event.getComplaint(),
                                                        event.getType().name(), message);
            notificationRepository.save(notification);
        }
    }
}
