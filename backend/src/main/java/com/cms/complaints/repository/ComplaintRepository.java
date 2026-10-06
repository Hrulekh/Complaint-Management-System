package com.cms.complaints.repository;

import com.cms.complaints.entity.Complaint;
import com.cms.complaints.entity.ComplaintStatus;
import com.cms.complaints.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Optional<Complaint> findByTicketId(String ticketId);

    Page<Complaint> findByCreatedBy(User createdBy, Pageable pageable);

    Page<Complaint> findByAssignedTo(User assignedTo, Pageable pageable);

    @Query("SELECT c FROM Complaint c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:categoryId IS NULL OR c.category.id = :categoryId) AND " +
           "(:priorityId IS NULL OR c.priority.id = :priorityId) AND " +
           "(:assigneeId IS NULL OR c.assignedTo.id = :assigneeId) AND " +
           "(:fromDate IS NULL OR c.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR c.createdAt <= :toDate) AND " +
           "(:search IS NULL OR c.ticketId LIKE CONCAT('%', :search, '%') OR " +
           "c.title LIKE CONCAT('%', :search, '%') OR " +
           "c.description LIKE CONCAT('%', :search, '%'))")
    Page<Complaint> findWithFilters(
        @Param("status") ComplaintStatus status,
        @Param("categoryId") Long categoryId,
        @Param("priorityId") Long priorityId,
        @Param("assigneeId") Long assigneeId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("search") String search,
        Pageable pageable
    );

    @Query("SELECT c FROM Complaint c WHERE " +
           "c.dueAt < :now AND c.status NOT IN ('RESOLVED', 'CLOSED') AND c.escalated = false")
    List<Complaint> findOverdueNotEscalated(@Param("now") LocalDateTime now);

    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);

    long countByStatus(ComplaintStatus status);

    long countByCategory_Id(Long categoryId);

    long countByPriority_Id(Long priorityId);
}
