package com.cms.complaints.repository;

import com.cms.complaints.entity.Notification;
import com.cms.complaints.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Notification> findByUserAndReadFlagOrderByCreatedAtDesc(User user, Boolean readFlag, Pageable pageable);

    long countByUserAndReadFlag(User user, Boolean readFlag);

    @Modifying
    @Query("UPDATE Notification n SET n.readFlag = :readFlag WHERE n.user = :user")
    void updateReadFlagByUser(@Param("user") User user, @Param("readFlag") Boolean readFlag);

    List<Notification> findByUserAndReadFlag(User user, Boolean readFlag);
}
