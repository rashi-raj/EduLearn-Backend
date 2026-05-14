package com.edulearn.course.controller;

import com.edulearn.course.dto.CourseApprovalActionRequest;
import com.edulearn.course.dto.CourseRequest;
import com.edulearn.course.dto.CourseResponse;
import com.edulearn.course.dto.PublishCourseRequest;
import com.edulearn.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Course Controller", description = "APIs for course creation, approval, publishing and management")
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "Create a new course")
    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(request));
    }

    @Operation(summary = "Get all courses")
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @Operation(summary = "Get course by ID")
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable UUID courseId) {
        return ResponseEntity.ok(courseService.getCourseById(courseId));
    }

    @Operation(summary = "Get courses by category")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseResponse>> getCoursesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(courseService.getCoursesByCategory(category));
    }

    @Operation(summary = "Get courses by instructor")
    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<CourseResponse>> getCoursesByInstructor(@PathVariable UUID instructorId) {
        return ResponseEntity.ok(courseService.getCoursesByInstructor(instructorId));
    }

    @Operation(summary = "Get courses by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<CourseResponse>> getCoursesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(courseService.getCoursesByStatus(status));
    }

    @Operation(summary = "Search courses by keyword")
    @GetMapping("/search")
    public ResponseEntity<List<CourseResponse>> searchCourses(@RequestParam String keyword) {
        return ResponseEntity.ok(courseService.searchCourses(keyword));
    }

    @Operation(summary = "Update a course")
    @PutMapping("/{courseId}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable UUID courseId,
            @Valid @RequestBody CourseRequest request
    ) {
        return ResponseEntity.ok(courseService.updateCourse(courseId, request));
    }

    @Operation(summary = "Submit course for admin approval")
    @PatchMapping("/{courseId}/submit-for-approval")
    public ResponseEntity<CourseResponse> submitCourseForApproval(@PathVariable UUID courseId) {
        return ResponseEntity.ok(courseService.submitCourseForApproval(courseId));
    }

    @Operation(summary = "Publish or unpublish a course")
    @PatchMapping("/{courseId}/publish")
    public ResponseEntity<CourseResponse> publishCourse(
            @PathVariable UUID courseId,
            @Valid @RequestBody PublishCourseRequest request
    ) {
        return ResponseEntity.ok(courseService.publishCourse(courseId, request.getPublished()));
    }

    @Operation(summary = "Approve a course")
    @PutMapping("/{courseId}/approve")
    public ResponseEntity<CourseResponse> approveCourse(@PathVariable UUID courseId) {
        System.out.println(">>> CONTROLLER: Received /approve request for " + courseId);
        return ResponseEntity.ok(courseService.reviewCourse(courseId, "APPROVE"));
    }

    @Operation(summary = "Reject a course")
    @PutMapping("/{courseId}/reject")
    public ResponseEntity<CourseResponse> rejectCourse(@PathVariable UUID courseId) {
        System.out.println(">>> CONTROLLER: Received /reject request for " + courseId);
        return ResponseEntity.ok(courseService.reviewCourse(courseId, "REJECT"));
    }

    @Operation(summary = "Delete a course")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Health check endpoint")
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Course service is working");
    }
    
    @Operation(summary = "Get all published courses")
    @GetMapping("/published")
    public ResponseEntity<List<CourseResponse>> getPublishedCourses() {
        return ResponseEntity.ok(courseService.getPublishedCourses());
    }
}