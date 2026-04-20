package com.edulearn.enrollment.controller;

import com.edulearn.enrollment.dto.EnrollmentRequest;
import com.edulearn.enrollment.dto.EnrollmentResponse;
import com.edulearn.enrollment.dto.ProgressUpdateRequest;
import com.edulearn.enrollment.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Enrollment Controller", description = "APIs for enrollment management and progress tracking")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Operation(summary = "Enroll student in a course")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Enrollment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or already enrolled")
    })
    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
        log.info("Received enroll request for studentId={} and courseId={}", request.getStudentId(), request.getCourseId());
        EnrollmentResponse response = enrollmentService.enroll(request);
        log.info("Enrollment created successfully with enrollmentId={}", response.getEnrollmentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Unenroll student from a course")
    @DeleteMapping
    public ResponseEntity<Void> unenroll(
            @Parameter(description = "Student ID") @RequestParam UUID studentId,
            @Parameter(description = "Course ID") @RequestParam UUID courseId
    ) {
        log.info("Received unenroll request for studentId={} and courseId={}", studentId, courseId);
        enrollmentService.unenroll(studentId, courseId);
        log.info("Enrollment cancelled for studentId={} and courseId={}", studentId, courseId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get enrollments by student ID")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentResponse>> getByStudent(@PathVariable UUID studentId) {
        log.debug("Fetching enrollments for studentId={}", studentId);
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudent(studentId));
    }

    @Operation(summary = "Get enrollments by course ID")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<EnrollmentResponse>> getByCourse(@PathVariable UUID courseId) {
        log.debug("Fetching enrollments for courseId={}", courseId);
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourse(courseId));
    }

    @Operation(summary = "Check whether student has active access to a course")
    @GetMapping("/has-access")
    public ResponseEntity<Boolean> hasActiveAccess(
            @RequestParam UUID studentId,
            @RequestParam UUID courseId
    ) {
        log.debug("Checking active access for studentId={} and courseId={}", studentId, courseId);
        return ResponseEntity.ok(enrollmentService.hasActiveAccess(studentId, courseId));
    }

    @Operation(summary = "Update enrollment progress")
    @PatchMapping("/progress")
    public ResponseEntity<EnrollmentResponse> updateProgress(
            @RequestParam UUID studentId,
            @RequestParam UUID courseId,
            @Valid @RequestBody ProgressUpdateRequest request
    ) {
        log.info("Updating progress for studentId={}, courseId={}, progressPercent={}", studentId, courseId, request.getProgressPercent());
        return ResponseEntity.ok(
                enrollmentService.updateProgress(studentId, courseId, request.getProgressPercent())
        );
    }

    @Operation(summary = "Mark enrollment as completed")
    @PatchMapping("/complete")
    public ResponseEntity<EnrollmentResponse> markComplete(
            @RequestParam UUID studentId,
            @RequestParam UUID courseId
    ) {
        log.info("Marking enrollment complete for studentId={} and courseId={}", studentId, courseId);
        return ResponseEntity.ok(enrollmentService.markComplete(studentId, courseId));
    }

    @Operation(summary = "Check whether student is enrolled in a course")
    @GetMapping("/is-enrolled")
    public ResponseEntity<Boolean> isEnrolled(
            @RequestParam UUID studentId,
            @RequestParam UUID courseId
    ) {
        log.debug("Checking enrollment for studentId={} and courseId={}", studentId, courseId);
        return ResponseEntity.ok(enrollmentService.isEnrolled(studentId, courseId));
    }

    @Operation(summary = "Get enrollment count by course ID")
    @GetMapping("/count/{courseId}")
    public ResponseEntity<Long> getEnrollmentCount(@PathVariable UUID courseId) {
        log.debug("Getting enrollment count for courseId={}", courseId);
        return ResponseEntity.ok(enrollmentService.getEnrollmentCount(courseId));
    }

    @Operation(summary = "Health check for enrollment service")
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.debug("Ping endpoint invoked");
        return ResponseEntity.ok("Enrollment service is working");
    }
}
