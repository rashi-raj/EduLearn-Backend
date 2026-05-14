package com.edulearn.assessment.controller;

import com.edulearn.assessment.dto.*;
import com.edulearn.assessment.service.AssessmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssessmentController.class)
@DisplayName("AssessmentController Unit Tests")
class CourseControllerTest { // Wait, I named it CourseControllerTest by mistake in the previous write_to_file. Let's fix class name.

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssessmentService assessmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private QuizRequest createValidQuizRequest() {
        QuizRequest request = new QuizRequest();
        request.setCourseId(UUID.randomUUID());
        request.setTitle("Java Basics");
        request.setDescription("Test description");
        request.setTimeLimitMinutes(30);
        request.setPassingScore(60.0);
        request.setMaxAttempts(3);
        return request;
    }

    private QuestionRequest createValidQuestionRequest() {
        QuestionRequest request = new QuestionRequest();
        request.setText("What is Java?");
        request.setType("MCQ");
        request.setCorrectAnswer("Programming Language");
        request.setMarks(10);
        request.setOrderIndex(1);
        return request;
    }

    @Test
    @DisplayName("POST /api/v1/quizzes: creates a quiz")
    void createQuiz_shouldReturnCreated() throws Exception {
        QuizRequest request = createValidQuizRequest();
        
        QuizResponse response = QuizResponse.builder().quizId(UUID.randomUUID()).title("Java Basics").build();
        when(assessmentService.createQuiz(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Java Basics"));
    }

    @Test
    @DisplayName("POST /api/v1/quizzes/{id}/questions: adds a question")
    void addQuestion_shouldReturnCreated() throws Exception {
        UUID quizId = UUID.randomUUID();
        QuestionRequest request = createValidQuestionRequest();
        
        QuestionResponse response = QuestionResponse.builder().questionId(UUID.randomUUID()).text("What is Java?").build();
        when(assessmentService.addQuestion(eq(quizId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/quizzes/{quizId}/questions", quizId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("What is Java?"));
    }

    @Test
    @DisplayName("GET /api/v1/quizzes/course/{id}: returns quizzes")
    void getQuizzesByCourse_shouldReturnList() throws Exception {
        UUID courseId = UUID.randomUUID();
        when(assessmentService.getQuizzesByCourse(courseId)).thenReturn(List.of(new QuizResponse()));

        mockMvc.perform(get("/api/v1/quizzes/course/{courseId}", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/attempts/{id}/submit: submits an attempt")
    void submitAttempt_shouldReturnCreated() throws Exception {
        UUID quizId = UUID.randomUUID();
        AttemptRequest request = new AttemptRequest();
        request.setStudentId(UUID.randomUUID());
        request.setAnswers(List.of());
        
        AttemptResponse response = AttemptResponse.builder().attemptId(UUID.randomUUID()).score(100.0).build();
        when(assessmentService.submitAttempt(eq(quizId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/attempts/{quizId}/submit", quizId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(100.0));
    }

    @Test
    @DisplayName("GET /api/v1/attempts/student/{id}: returns attempts")
    void getAttemptsByStudent_shouldReturnList() throws Exception {
        UUID studentId = UUID.randomUUID();
        when(assessmentService.getAttemptsByStudent(studentId)).thenReturn(List.of(new AttemptResponse()));

        mockMvc.perform(get("/api/v1/attempts/student/{studentId}", studentId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/attempts/best-score: returns best score")
    void getBestScore_shouldReturnScore() throws Exception {
        UUID studentId = UUID.randomUUID();
        UUID quizId = UUID.randomUUID();
        when(assessmentService.getBestScore(studentId, quizId)).thenReturn(85.0);

        mockMvc.perform(get("/api/v1/attempts/best-score")
                .param("studentId", studentId.toString())
                .param("quizId", quizId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("85.0"));
    }

    @Test
    @DisplayName("PUT /api/v1/quizzes/{id}: updates a quiz")
    void updateQuiz_shouldReturnOk() throws Exception {
        UUID quizId = UUID.randomUUID();
        QuizRequest request = createValidQuizRequest();
        
        when(assessmentService.updateQuiz(eq(quizId), any())).thenReturn(new QuizResponse());

        mockMvc.perform(put("/api/v1/quizzes/{quizId}", quizId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/quizzes/{id}: deletes a quiz")
    void deleteQuiz_shouldReturnNoContent() throws Exception {
        UUID quizId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/quizzes/{quizId}", quizId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/v1/quizzes/{id}/publish: publishes a quiz")
    void publishQuiz_shouldReturnOk() throws Exception {
        UUID quizId = UUID.randomUUID();
        QuizPublishRequest request = new QuizPublishRequest();
        request.setPublished(true);
        
        when(assessmentService.publishQuiz(eq(quizId), eq(true))).thenReturn(new QuizResponse());

        mockMvc.perform(patch("/api/v1/quizzes/{quizId}/publish", quizId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/quizzes/ping: health check")
    void ping_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/quizzes/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("Assessment service is working"));
    }
}
