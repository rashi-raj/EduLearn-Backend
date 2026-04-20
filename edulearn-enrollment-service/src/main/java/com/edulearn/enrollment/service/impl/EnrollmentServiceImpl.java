package com.edulearn.enrollment.service.impl;

import com.edulearn.enrollment.dto.EnrollmentRequest;
import com.edulearn.enrollment.dto.EnrollmentResponse;
import com.edulearn.enrollment.entity.Enrollment;
import com.edulearn.enrollment.event.NotificationEvent;
import com.edulearn.enrollment.event.NotificationEventPublisher;
import com.edulearn.enrollment.repository.EnrollmentRepository;
import com.edulearn.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_EXPIRED = "EXPIRED";

    private final EnrollmentRepository enrollmentRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    @Override
    public EnrollmentResponse enroll(EnrollmentRequest request) {
        log.info("Attempting enrollment for studentId={} and courseId={}", request.getStudentId(), request.getCourseId());

        if (enrollmentRepository.existsByStudentIdAndCourseId(request.getStudentId(), request.getCourseId())) {
            log.warn("Duplicate enrollment attempt for studentId={} and courseId={}", request.getStudentId(), request.getCourseId());
            throw new RuntimeException("Student is already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .studentId(request.getStudentId())
                .courseId(request.getCourseId())
                .studentName(request.getStudentName() != null ? request.getStudentName() : "Learner")
                .studentEmail(request.getStudentEmail() != null ? request.getStudentEmail() : "")
                .enrolledAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMonths(8))
                .accessExpired(false)
                .status(STATUS_ACTIVE)
                .progressPercent(0.0)
                .certificateIssued(false)
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Enrollment saved successfully with enrollmentId={}", savedEnrollment.getEnrollmentId());

        notificationEventPublisher.publish(NotificationEvent.builder()
                .eventType("ENROLLMENT_CREATED")
                .userId(request.getStudentId().toString())
                .title("Enrollment Successful!")
                .message("You have been enrolled in a new course. Start learning now!")
                .build());

        return mapToResponse(savedEnrollment);
    }

    @Override
    public void unenroll(UUID studentId, UUID courseId) {
        log.info("Attempting unenroll for studentId={} and courseId={}", studentId, courseId);

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollment.setStatus(STATUS_CANCELLED);
        enrollmentRepository.save(enrollment);
        log.info("Enrollment cancelled for studentId={} and courseId={}", studentId, courseId);
    }

    @Override
    public List<EnrollmentResponse> getEnrollmentsByStudent(UUID studentId) {
        log.debug("Fetching enrollments by studentId={}", studentId);
        return enrollmentRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<EnrollmentResponse> getEnrollmentsByCourse(UUID courseId) {
        log.debug("Fetching enrollments by courseId={}", courseId);
        return enrollmentRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public EnrollmentResponse updateProgress(UUID studentId, UUID courseId, Double progressPercent) {
        log.info("Updating progress for studentId={}, courseId={}, progressPercent={}", studentId, courseId, progressPercent);

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollment.setProgressPercent(progressPercent);

        if (progressPercent >= 100.0) {
            enrollment.setStatus(STATUS_COMPLETED);
            enrollment.setCompletedAt(LocalDateTime.now());
            log.info("Enrollment marked completed automatically for studentId={} and courseId={}", studentId, courseId);
        }

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    public EnrollmentResponse markComplete(UUID studentId, UUID courseId) {
        log.info("Marking complete for studentId={} and courseId={}", studentId, courseId);

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollment.setProgressPercent(100.0);
        enrollment.setStatus(STATUS_COMPLETED);
        enrollment.setCompletedAt(LocalDateTime.now());

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    public boolean isEnrolled(UUID studentId, UUID courseId) {
        log.debug("Checking isEnrolled for studentId={} and courseId={}", studentId, courseId);

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElse(null);

        if (enrollment == null) {
            return false;
        }

        return !STATUS_CANCELLED.equalsIgnoreCase(enrollment.getStatus());
    }

    @Override
    public boolean hasActiveAccess(UUID studentId, UUID courseId) {
        log.debug("Checking active access for studentId={} and courseId={}", studentId, courseId);

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        if (STATUS_CANCELLED.equalsIgnoreCase(enrollment.getStatus())) {
            log.info("Access denied because enrollment is cancelled for studentId={} and courseId={}", studentId, courseId);
            return false;
        }

        if (enrollment.getExpiresAt() == null) {
            return true;
        }

        boolean active = enrollment.getExpiresAt().isAfter(LocalDateTime.now());

        enrollment.setAccessExpired(!active);
        if (!active && !STATUS_COMPLETED.equalsIgnoreCase(enrollment.getStatus())) {
            enrollment.setStatus(STATUS_EXPIRED);
        }

        enrollmentRepository.save(enrollment);
        log.debug("Active access result for studentId={} and courseId={} is {}", studentId, courseId, active);
        return active;
    }

    @Override
    public long getEnrollmentCount(UUID courseId) {
        log.debug("Getting enrollment count for courseId={}", courseId);
        return enrollmentRepository.countByCourseId(courseId);
    }

    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        boolean expired = enrollment.getExpiresAt() != null
                && enrollment.getExpiresAt().isBefore(LocalDateTime.now());

        return EnrollmentResponse.builder()
                .enrollmentId(enrollment.getEnrollmentId())
                .studentId(enrollment.getStudentId())
                .courseId(enrollment.getCourseId())
                .studentName(enrollment.getStudentName())
                .studentEmail(enrollment.getStudentEmail())
                .enrolledAt(enrollment.getEnrolledAt())
                .completedAt(enrollment.getCompletedAt())
                .expiresAt(enrollment.getExpiresAt())
                .accessExpired(expired)
                .status(enrollment.getStatus())
                .progressPercent(enrollment.getProgressPercent())
                .certificateIssued(enrollment.getCertificateIssued())
                .build();
    }
}
