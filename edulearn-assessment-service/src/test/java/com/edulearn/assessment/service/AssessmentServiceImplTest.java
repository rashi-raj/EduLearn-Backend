package com.edulearn.assessment.service;

import com.edulearn.assessment.dto.QuizRequest;
import com.edulearn.assessment.dto.QuizResponse;
import com.edulearn.assessment.entity.Quiz;
import com.edulearn.assessment.repository.AttemptRepository;
import com.edulearn.assessment.repository.QuestionRepository;
import com.edulearn.assessment.repository.QuizRepository;
import com.edulearn.assessment.service.impl.AssessmentServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceImplTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AttemptRepository attemptRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AssessmentServiceImpl assessmentService;

    private UUID courseId;
    private UUID quizId;
    private QuizRequest quizRequest;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        quizId = UUID.randomUUID();

        quizRequest = new QuizRequest();
        quizRequest.setCourseId(courseId);
        quizRequest.setTitle("Java Basics Quiz");
        quizRequest.setDescription("Test your Java knowledge");
        quizRequest.setTimeLimitMinutes(30);
        quizRequest.setPassingScore(60.0);
        quizRequest.setMaxAttempts(3);

        quiz = Quiz.builder()
                .quizId(quizId)
                .courseId(courseId)
                .title("Java Basics Quiz")
                .description("Test your Java knowledge")
                .timeLimitMinutes(30)
                .passingScore(60.0)
                .maxAttempts(3)
                .isPublished(false)
                .build();
    }

    @Test
    void createQuiz_shouldCreateAndReturnQuiz() {
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz);
        when(questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId)).thenReturn(Collections.emptyList());

        QuizResponse response = assessmentService.createQuiz(quizRequest);

        assertNotNull(response);
        assertEquals(courseId, response.getCourseId());
        assertEquals("Java Basics Quiz", response.getTitle());
        assertFalse(response.getIsPublished());
        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    void getQuizById_shouldReturnQuiz_whenExists() {
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId)).thenReturn(Collections.emptyList());

        QuizResponse response = assessmentService.getQuizById(quizId);

        assertNotNull(response);
        assertEquals(quizId, response.getQuizId());
        assertEquals("Java Basics Quiz", response.getTitle());
    }

    @Test
    void getQuizById_shouldThrowException_whenNotFound() {
        when(quizRepository.findById(quizId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> assessmentService.getQuizById(quizId));

        assertEquals("Quiz not found", exception.getMessage());
    }

    @Test
    void getQuizzesByCourse_shouldReturnList() {
        when(quizRepository.findByCourseId(courseId)).thenReturn(List.of(quiz));
        when(questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId)).thenReturn(Collections.emptyList());

        List<QuizResponse> responses = assessmentService.getQuizzesByCourse(courseId);

        assertEquals(1, responses.size());
        assertEquals("Java Basics Quiz", responses.get(0).getTitle());
    }

    @Test
    void deleteQuiz_shouldDeleteQuizAndQuestions() {
        when(quizRepository.existsById(quizId)).thenReturn(true);
        when(questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId)).thenReturn(Collections.emptyList());

        assessmentService.deleteQuiz(quizId);

        verify(quizRepository).deleteById(quizId);
    }

    @Test
    void deleteQuiz_shouldThrowException_whenNotFound() {
        when(quizRepository.existsById(quizId)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> assessmentService.deleteQuiz(quizId));

        assertEquals("Quiz not found", exception.getMessage());
        verify(quizRepository, never()).deleteById(any());
    }

    @Test
    void publishQuiz_shouldUpdatePublishStatus() {
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId)).thenReturn(Collections.emptyList());

        QuizResponse response = assessmentService.publishQuiz(quizId, true);

        assertNotNull(response);
        assertTrue(response.getIsPublished());
        verify(quizRepository).save(any(Quiz.class));
    }
}
