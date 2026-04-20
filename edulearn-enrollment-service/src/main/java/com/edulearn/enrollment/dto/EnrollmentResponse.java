package com.edulearn.enrollment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class EnrollmentResponse {

    private UUID enrollmentId;
    private UUID studentId;
    private UUID courseId;

    private String studentName;
    private String studentEmail;

    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private String status;
    private Double progressPercent;
    private Boolean certificateIssued;
    private LocalDateTime expiresAt;
    private Boolean accessExpired;
}