package com.cms.complaints.repository;

import com.cms.complaints.entity.ComplaintHistory;
import com.cms.complaints.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintHistoryRepository extends JpaRepository<ComplaintHistory, Long> {
    List<ComplaintHistory> findByComplaintOrderByCreatedAtDesc(Complaint complaint);
}
