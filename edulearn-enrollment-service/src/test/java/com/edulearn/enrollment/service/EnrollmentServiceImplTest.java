package com.edulearn.enrollment.service;

import com.edulearn.enrollment.dto.EnrollmentRequest;
import com.edulearn.enrollment.dto.EnrollmentResponse;
import com.edulearn.enrollment.entity.Enrollment;
import com.edulearn.enrollment.repository.EnrollmentRepository;
import com.edulearn.enrollment.service.impl.EnrollmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    private UUID studentId;
    private UUID courseId;
    private EnrollmentRequest request;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        courseId = UUID.randomUUID();

        request = new EnrollmentRequest();
        request.setStudentId(studentId);
        request.setCourseId(courseId);
        request.setStudentName("Rashi");
        request.setStudentEmail("rashi@example.com");
    }

    @Test
    void enroll_shouldCreateEnrollment_whenStudentNotAlreadyEnrolled() {
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment enrollment = invocation.getArgument(0);
            enrollment.setEnrollmentId(UUID.randomUUID());
            return enrollment;
        });

        EnrollmentResponse response = enrollmentService.enroll(request);

        assertNotNull(response);
        assertEquals(studentId, response.getStudentId());
        assertEquals(courseId, response.getCourseId());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals(0.0, response.getProgressPercent());
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enroll_shouldThrowException_whenStudentAlreadyEnrolled() {
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> enrollmentService.enroll(request));

        assertEquals("Student is already enrolled in this course", exception.getMessage());
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void updateProgress_shouldMarkCompleted_whenProgressIsHundred() {
        Enrollment enrollment = buildEnrollment("ACTIVE", 50.0, LocalDateTime.now().plusDays(10));
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EnrollmentResponse response = enrollmentService.updateProgress(studentId, courseId, 100.0);

        assertEquals("COMPLETED", response.getStatus());
        assertEquals(100.0, response.getProgressPercent());
        assertNotNull(response.getCompletedAt());
    }

    @Test
    void hasActiveAccess_shouldReturnFalseAndExpireEnrollment_whenAccessExpired() {
        Enrollment enrollment = buildEnrollment("ACTIVE", 75.0, LocalDateTime.now().minusDays(1));
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = enrollmentService.hasActiveAccess(studentId, courseId);

        assertFalse(result);
        assertTrue(enrollment.getAccessExpired());
        assertEquals("EXPIRED", enrollment.getStatus());
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void isEnrolled_shouldReturnFalse_whenEnrollmentIsCancelled() {
        Enrollment enrollment = buildEnrollment("CANCELLED", 0.0, LocalDateTime.now().plusDays(2));
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)).thenReturn(Optional.of(enrollment));

        boolean enrolled = enrollmentService.isEnrolled(studentId, courseId);

        assertFalse(enrolled);
    }

    private Enrollment buildEnrollment(String status, Double progress, LocalDateTime expiresAt) {
        return Enrollment.builder()
                .enrollmentId(UUID.randomUUID())
                .studentId(studentId)
                .courseId(courseId)
                .studentName("Rashi")
                .studentEmail("rashi@example.com")
                .enrolledAt(LocalDateTime.now().minusDays(5))
                .expiresAt(expiresAt)
                .accessExpired(false)
                .status(status)
                .progressPercent(progress)
                .certificateIssued(false)
                .build();
    }
}
