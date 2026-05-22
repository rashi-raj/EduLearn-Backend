package com.edulearn.auth.repository;

import com.edulearn.auth.entity.User;
import com.edulearn.auth.enums.ApprovalStatus;
import com.edulearn.auth.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByRole(Role role);

    List<User> findAllByRoleAndApprovalStatus(Role role, ApprovalStatus approvalStatus);
}