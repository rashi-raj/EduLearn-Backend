package com.edulearn.lesson.controller;

import com.edulearn.lesson.dto.*;
import com.edulearn.lesson.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Lesson Controller", description = "APIs for managing lessons and resources inside courses")
@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @Operation(summary = "Add a new lesson to a course")
    @PostMapping
    public ResponseEntity<LessonResponse> addLesson(@Valid @RequestBody LessonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.addLesson(request));
    }

    @Operation(summary = "Get all lessons for a course")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LessonResponse>> getLessonsByCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(lessonService.getLessonsByCourse(courseId));
    }

    @Operation(summary = "Get lesson by ID")
    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonResponse> getLessonById(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(lessonService.getLessonById(lessonId));
    }

    @Operation(summary = "Update a lesson")
    @PutMapping("/{lessonId}")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable UUID lessonId,
            @Valid @RequestBody LessonRequest request
    ) {
        return ResponseEntity.ok(lessonService.updateLesson(lessonId, request));
    }

    @Operation(summary = "Delete a lesson")
    @DeleteMapping("/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable UUID lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reorder lessons within a course")
    @PatchMapping("/course/{courseId}/reorder")
    public ResponseEntity<List<LessonResponse>> reorderLessons(
            @PathVariable UUID courseId,
            @Valid @RequestBody List<LessonOrderUpdateRequest> requests
    ) {
        return ResponseEntity.ok(lessonService.reorderLessons(courseId, requests));
    }

    @Operation(summary = "Add a resource to a lesson")
    @PostMapping("/{lessonId}/resources")
    public ResponseEntity<ResourceResponse> addResource(
            @PathVariable UUID lessonId,
            @Valid @RequestBody ResourceRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.addResource(lessonId, request));
    }

    @Operation(summary = "Remove a resource")
    @DeleteMapping("/resources/{resourceId}")
    public ResponseEntity<Void> removeResource(@PathVariable UUID resourceId) {
        lessonService.removeResource(resourceId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get preview lessons for a course")
    @GetMapping("/course/{courseId}/preview")
    public ResponseEntity<List<LessonResponse>> getPreviewLessons(@PathVariable UUID courseId) {
        return ResponseEntity.ok(lessonService.getPreviewLessons(courseId));
    }

    @Operation(summary = "Health check endpoint")
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Lesson service is working");
    }
}