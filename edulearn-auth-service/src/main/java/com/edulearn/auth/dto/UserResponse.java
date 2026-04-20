package com.edulearn.auth.dto;

import com.edulearn.auth.enums.ApprovalStatus;
import com.edulearn.auth.enums.AuthProvider;
import com.edulearn.auth.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {
    private UUID userId;
    private String fullName;
    private String email;
    private Role role;
    private AuthProvider provider;
    private ApprovalStatus approvalStatus;
    private String mobile;
    private String bio;
    private String profilePicUrl;
    private LocalDateTime createdAt;
}