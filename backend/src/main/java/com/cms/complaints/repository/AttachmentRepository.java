package com.cms.complaints.repository;

import com.cms.complaints.entity.Attachment;
import com.cms.complaints.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByComplaint(Complaint complaint);
    long countByComplaint(Complaint complaint);
}
