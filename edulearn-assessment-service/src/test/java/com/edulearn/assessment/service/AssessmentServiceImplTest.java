package com.edulearn.assessment.service;

import com.edulearn.assessment.dto.*;
import com.edulearn.assessment.entity.Attempt;
import com.edulearn.assessment.entity.Question;
import com.edulearn.assessment.entity.Quiz;
import com.edulearn.assessment.event.NotificationEventPublisher;
import com.edulearn.assessment.repository.AttemptRepository;
import com.edulearn.assessment.repository.QuestionRepository;
import com.edulearn.assessment.repository.QuizRepository;
import com.edulearn.assessment.service.impl.AssessmentServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("AssessmentServiceImpl Unit Tests")
class AssessmentServiceImplTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AttemptRepository attemptRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NotificationEventPublisher notificationEventPublisher;

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

    @Test
    void addQuestion_shouldAddAndReturnQuestion() {
        QuestionRequest request = new QuestionRequest();
        request.setText("What is Java?");
        request.setCorrectAnswer("Programming Language");
        request.setMarks(10);

        Question question = Question.builder()
                .questionId(UUID.randomUUID())
                .quizId(quizId)
                .text("What is Java?")
                .correctAnswer("Programming Language")
                .marks(10)
                .build();

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        QuestionResponse response = assessmentService.addQuestion(quizId, request);

        assertNotNull(response);
        assertEquals("What is Java?", response.getText());
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    void submitAttempt_shouldCalculateScoreAndSave() throws JsonProcessingException {
        UUID studentId = UUID.randomUUID();
        UUID qId = UUID.randomUUID();
        
        AttemptRequest request = new AttemptRequest();
        request.setStudentId(studentId);
        AnswerRequest answer = new AnswerRequest();
        answer.setQuestionId(qId);
        answer.setAnswer("Java");
        request.setAnswers(List.of(answer));

        Question question = Question.builder()
                .questionId(qId)
                .correctAnswer("Java")
                .marks(10)
                .build();

        Attempt savedAttempt = Attempt.builder()
                .attemptId(UUID.randomUUID())
                .score(10.0)
                .passed(true)
                .build();

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(attemptRepository.countByStudentIdAndQuizId(studentId, quizId)).thenReturn(0L);
        when(questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId)).thenReturn(List.of(question));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(attemptRepository.save(any(Attempt.class))).thenReturn(savedAttempt);

        AttemptResponse response = assessmentService.submitAttempt(quizId, request);

        assertNotNull(response);
        assertEquals(10.0, response.getScore());
        assertTrue(response.getPassed());
        verify(notificationEventPublisher).publish(any());
    }

    @Test
    void submitAttempt_shouldThrowException_whenMaxAttemptsExceeded() {
        UUID studentId = UUID.randomUUID();
        AttemptRequest request = new AttemptRequest();
        request.setStudentId(studentId);

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(attemptRepository.countByStudentIdAndQuizId(studentId, quizId)).thenReturn(3L);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> assessmentService.submitAttempt(quizId, request));

        assertEquals("Maximum attempts exceeded", exception.getMessage());
    }

    @Test
    void getAttemptsByStudent_shouldReturnList() {
        UUID studentId = UUID.randomUUID();
        when(attemptRepository.findByStudentId(studentId)).thenReturn(Collections.emptyList());

        List<AttemptResponse> responses = assessmentService.getAttemptsByStudent(studentId);

        assertTrue(responses.isEmpty());
    }

    @Test
    void getBestScore_shouldReturnScore() {
        UUID studentId = UUID.randomUUID();
        Attempt attempt = Attempt.builder().score(85.0).build();
        when(attemptRepository.findTopByStudentIdAndQuizIdOrderByScoreDesc(studentId, quizId))
                .thenReturn(Optional.of(attempt));

        Double score = assessmentService.getBestScore(studentId, quizId);

        assertEquals(85.0, score);
    }

    @Test
    void updateQuiz_shouldUpdateAndReturnQuiz() {
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz);
        when(questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId)).thenReturn(Collections.emptyList());

        QuizResponse response = assessmentService.updateQuiz(quizId, quizRequest);

        assertNotNull(response);
        verify(quizRepository).save(any(Quiz.class));
    }
}
