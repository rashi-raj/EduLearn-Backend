package com.edulearn.progress.controller;

import com.edulearn.progress.dto.CertificateResponse;
import com.edulearn.progress.dto.CompleteLessonRequest;
import com.edulearn.progress.dto.ProgressResponse;
import com.edulearn.progress.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Progress Controller", description = "APIs for tracking lesson completion and course progress")
public class ProgressController {

    private final ProgressService progressService;

    @Operation(summary = "Mark a lesson as completed")
    @PostMapping("/complete-lesson")
    public ResponseEntity<Void> completeLesson(@RequestBody @Valid CompleteLessonRequest request) {
        log.info("Completing lesson for studentId={}, courseId={}, lessonId={}", request.getStudentId(), request.getCourseId(), request.getLessonId());
        progressService.completeLesson(
                request.getStudentId(),
                request.getCourseId(),
                request.getLessonId()
        );
        log.info("Lesson completed successfully");
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get progress for a student in a course")
    @GetMapping
    public ResponseEntity<ProgressResponse> getProgress(
            @RequestParam UUID studentId,
            @RequestParam UUID courseId
    ) {
        log.debug("Fetching progress for studentId={}, courseId={}", studentId, courseId);
        return ResponseEntity.ok(
                progressService.getProgress(studentId, courseId)
        );
    }

    @Operation(summary = "Health check for progress service")
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.debug("Ping endpoint invoked");
        return ResponseEntity.ok("Progress service is working");
    }

    @Operation(summary = "Get certificate for a student in a course")
    @GetMapping("/certificate")
    public ResponseEntity<CertificateResponse> getCertificate(
            @RequestParam UUID studentId,
            @RequestParam UUID courseId
    ) {
        log.info("Fetching certificate for studentId={}, courseId={}", studentId, courseId);
        try {
            return ResponseEntity.ok(progressService.getCertificate(studentId, courseId));
        } catch (RuntimeException e) {
            log.warn("Certificate not found for studentId={}, courseId={}", studentId, courseId);
            return ResponseEntity.notFound().build();
        }
    }
}