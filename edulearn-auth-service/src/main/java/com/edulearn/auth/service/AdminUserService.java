package com.edulearn.auth.service;

import com.edulearn.auth.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface AdminUserService {
    List<UserResponse> getPendingInstructors();
    List<UserResponse> getApprovedInstructors();
    List<UserResponse> getRejectedInstructors();
    List<UserResponse> getAllUsers();
    UserResponse approveInstructor(UUID userId);
    UserResponse rejectInstructor(UUID userId);
}