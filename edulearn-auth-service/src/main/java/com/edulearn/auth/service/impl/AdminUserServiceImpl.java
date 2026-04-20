package com.edulearn.auth.service.impl;

import com.edulearn.auth.dto.UserResponse;
import com.edulearn.auth.entity.User;
import com.edulearn.auth.enums.ApprovalStatus;
import com.edulearn.auth.enums.Role;
import com.edulearn.auth.repository.UserRepository;
import com.edulearn.auth.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    private UserResponse map(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .provider(user.getProvider())
                .approvalStatus(user.getApprovalStatus())
                .mobile(user.getMobile())
                .bio(user.getBio())
                .profilePicUrl(user.getProfilePicUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public List<UserResponse> getPendingInstructors() {
        return userRepository.findAllByRoleAndApprovalStatus(Role.INSTRUCTOR, ApprovalStatus.PENDING)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public List<UserResponse> getApprovedInstructors() {
        return userRepository.findAllByRoleAndApprovalStatus(Role.INSTRUCTOR, ApprovalStatus.APPROVED)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public List<UserResponse> getRejectedInstructors() {
        return userRepository.findAllByRoleAndApprovalStatus(Role.INSTRUCTOR, ApprovalStatus.REJECTED)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public UserResponse approveInstructor(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.INSTRUCTOR) {
            throw new RuntimeException("Only instructor accounts can be approved.");
        }

        user.setApprovalStatus(ApprovalStatus.APPROVED);
        return map(userRepository.save(user));
    }

    @Override
    public UserResponse rejectInstructor(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.INSTRUCTOR) {
            throw new RuntimeException("Only instructor accounts can be rejected.");
        }

        user.setApprovalStatus(ApprovalStatus.REJECTED);
        return map(userRepository.save(user));
    }
}