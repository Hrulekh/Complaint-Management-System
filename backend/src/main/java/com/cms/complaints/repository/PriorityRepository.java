package com.cms.complaints.repository;

import com.cms.complaints.entity.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriorityRepository extends JpaRepository<Priority, Long> {
    Optional<Priority> findByName(String name);
    List<Priority> findByActive(Boolean active);
    List<Priority> findByOrderByLevel();
}
