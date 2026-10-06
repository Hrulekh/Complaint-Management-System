package com.cms.complaints.repository;

import com.cms.complaints.entity.User;
import com.cms.complaints.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRoleAndActive(Role role, Boolean active);
    List<User> findByActive(Boolean active);
}
