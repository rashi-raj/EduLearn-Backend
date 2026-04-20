package com.edulearn.assessment.controller;

import com.edulearn.assessment.dto.*;
import com.edulearn.assessment.service.AssessmentService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Assessment Controller", description = "APIs for quiz management, questions, and student attempts")
public class AssessmentController {

    private final AssessmentService assessmentService;

    @Operation(summary = "Create a new quiz")
    @PostMapping("/api/v1/quizzes")
    public ResponseEntity<QuizResponse> createQuiz(@Valid @RequestBody QuizRequest request) {
        log.info("Creating quiz for courseId={}", request.getCourseId());
        QuizResponse response = assessmentService.createQuiz(request);
        log.info("Quiz created with quizId={}", response.getQuizId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Add a question to a quiz")
    @PostMapping("/api/v1/quizzes/{quizId}/questions")
    public ResponseEntity<QuestionResponse> addQuestion(
            @PathVariable UUID quizId,
            @Valid @RequestBody QuestionRequest request
    ) {
        log.info("Adding question to quizId={}", quizId);
        return ResponseEntity.status(HttpStatus.CREATED).body(assessmentService.addQuestion(quizId, request));
    }

    @Operation(summary = "Get all quizzes for a course")
    @GetMapping("/api/v1/quizzes/course/{courseId}")
    public ResponseEntity<List<QuizResponse>> getQuizzesByCourse(@PathVariable UUID courseId) {
        log.debug("Fetching quizzes for courseId={}", courseId);
        return ResponseEntity.ok(assessmentService.getQuizzesByCourse(courseId));
    }

    @Operation(summary = "Get quiz by ID")
    @GetMapping("/api/v1/quizzes/{quizId}")
    public ResponseEntity<QuizResponse> getQuizById(@PathVariable UUID quizId) {
        log.debug("Fetching quiz quizId={}", quizId);
        return ResponseEntity.ok(assessmentService.getQuizById(quizId));
    }

    @Operation(summary = "Submit a quiz attempt")
    @PostMapping("/api/v1/attempts/{quizId}/submit")
    public ResponseEntity<AttemptResponse> submitAttempt(
            @PathVariable UUID quizId,
            @Valid @RequestBody AttemptRequest request
    ) {
        log.info("Submitting attempt for quizId={} by studentId={}", quizId, request.getStudentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(assessmentService.submitAttempt(quizId, request));
    }

    @Operation(summary = "Get all attempts by student")
    @GetMapping("/api/v1/attempts/student/{studentId}")
    public ResponseEntity<List<AttemptResponse>> getAttemptsByStudent(@PathVariable UUID studentId) {
        log.debug("Fetching attempts for studentId={}", studentId);
        return ResponseEntity.ok(assessmentService.getAttemptsByStudent(studentId));
    }

    @Operation(summary = "Get best score for a student on a quiz")
    @GetMapping("/api/v1/attempts/best-score")
    public ResponseEntity<Double> getBestScore(
            @RequestParam UUID studentId,
            @RequestParam UUID quizId
    ) {
        log.debug("Getting best score for studentId={}, quizId={}", studentId, quizId);
        return ResponseEntity.ok(assessmentService.getBestScore(studentId, quizId));
    }

    @Operation(summary = "Update an existing quiz")
    @PutMapping("/api/v1/quizzes/{quizId}")
    public ResponseEntity<QuizResponse> updateQuiz(
            @PathVariable UUID quizId,
            @Valid @RequestBody QuizRequest request
    ) {
        log.info("Updating quizId={}", quizId);
        return ResponseEntity.ok(assessmentService.updateQuiz(quizId, request));
    }

    @Operation(summary = "Delete a quiz")
    @DeleteMapping("/api/v1/quizzes/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable UUID quizId) {
        log.info("Deleting quizId={}", quizId);
        assessmentService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Publish or unpublish a quiz")
    @PatchMapping("/api/v1/quizzes/{quizId}/publish")
    public ResponseEntity<QuizResponse> publishQuiz(
            @PathVariable UUID quizId,
            @Valid @RequestBody QuizPublishRequest request
    ) {
        log.info("Setting publish status for quizId={} to {}", quizId, request.getPublished());
        return ResponseEntity.ok(assessmentService.publishQuiz(quizId, request.getPublished()));
    }

    @Operation(summary = "Health check for assessment service")
    @GetMapping("/api/v1/quizzes/ping")
    public ResponseEntity<String> ping() {
        log.debug("Ping endpoint invoked");
        return ResponseEntity.ok("Assessment service is working");
    }
}