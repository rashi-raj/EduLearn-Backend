package com.edulearn.enrollment.service;

import com.edulearn.enrollment.dto.EnrollmentRequest;
import com.edulearn.enrollment.dto.EnrollmentResponse;

import java.util.List;
import java.util.UUID;

public interface EnrollmentService {

    EnrollmentResponse enroll(EnrollmentRequest request);

    void unenroll(UUID studentId, UUID courseId);

    List<EnrollmentResponse> getEnrollmentsByStudent(UUID studentId);

    List<EnrollmentResponse> getEnrollmentsByCourse(UUID courseId);

    EnrollmentResponse updateProgress(UUID studentId, UUID courseId, Double progressPercent);

    EnrollmentResponse markComplete(UUID studentId, UUID courseId);

    boolean isEnrolled(UUID studentId, UUID courseId);
    
    boolean hasActiveAccess(UUID studentId, UUID courseId);

    long getEnrollmentCount(UUID courseId);
}