package com.cms.complaints.service;

import com.cms.complaints.dto.NotificationDto;
import com.cms.complaints.entity.Notification;
import com.cms.complaints.entity.User;
import com.cms.complaints.exception.ResourceNotFoundException;
import com.cms.complaints.repository.NotificationRepository;
import com.cms.complaints.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::mapToDto);
    }

    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return notificationRepository.countByUserAndReadFlag(user, false);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setReadFlag(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        notificationRepository.findByUserAndReadFlag(user, false)
                .forEach(n -> {
                    n.setReadFlag(true);
                    notificationRepository.save(n);
                });
    }

    private NotificationDto mapToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setEventType(notification.getEventType());
        dto.setMessage(notification.getMessage());
        dto.setReadFlag(notification.getReadFlag());
        dto.setCreatedAt(notification.getCreatedAt());
        if (notification.getComplaint() != null) {
            dto.setComplaintId(notification.getComplaint().getId());
            dto.setTicketId(notification.getComplaint().getTicketId());
        }
        return dto;
    }
}
