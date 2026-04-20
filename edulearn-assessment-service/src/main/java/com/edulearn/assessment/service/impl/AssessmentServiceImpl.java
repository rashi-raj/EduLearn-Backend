package com.edulearn.assessment.service.impl;

import com.edulearn.assessment.dto.*;
import com.edulearn.assessment.entity.Attempt;
import com.edulearn.assessment.entity.Question;
import com.edulearn.assessment.entity.Quiz;
import com.edulearn.assessment.event.NotificationEvent;
import com.edulearn.assessment.event.NotificationEventPublisher;
import com.edulearn.assessment.repository.AttemptRepository;
import com.edulearn.assessment.repository.QuestionRepository;
import com.edulearn.assessment.repository.QuizRepository;
import com.edulearn.assessment.service.AssessmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AttemptRepository attemptRepository;
    private final ObjectMapper objectMapper;
    private final NotificationEventPublisher notificationEventPublisher;

    @Override
    public QuizResponse createQuiz(QuizRequest request) {
        Quiz quiz = Quiz.builder()
                .courseId(request.getCourseId())
                .title(request.getTitle())
                .description(request.getDescription())
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .passingScore(request.getPassingScore())
                .maxAttempts(request.getMaxAttempts())
                .isPublished(false)
                .build();

        return mapToQuizResponse(quizRepository.save(quiz));
    }

    @Override
    public QuestionResponse addQuestion(UUID quizId, QuestionRequest request) {
        quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        Question question = Question.builder()
                .quizId(quizId)
                .text(request.getText())
                .type(request.getType())
                .optionA(request.getOptionA())
                .optionB(request.getOptionB())
                .optionC(request.getOptionC())
                .optionD(request.getOptionD())
                .correctAnswer(request.getCorrectAnswer())
                .marks(request.getMarks())
                .orderIndex(request.getOrderIndex())
                .build();

        return mapToQuestionResponse(questionRepository.save(question));
    }

    @Override
    public List<QuizResponse> getQuizzesByCourse(UUID courseId) {
        return quizRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapToQuizResponse)
                .toList();
    }

    @Override
    public QuizResponse getQuizById(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        return mapToQuizResponse(quiz);
    }

    @Override
    public AttemptResponse submitAttempt(UUID quizId, AttemptRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        long attemptCount = attemptRepository.countByStudentIdAndQuizId(request.getStudentId(), quizId);
        if (attemptCount >= quiz.getMaxAttempts()) {
            throw new RuntimeException("Maximum attempts exceeded");
        }

        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId);

        Map<UUID, String> submittedAnswers = request.getAnswers()
                .stream()
                .collect(Collectors.toMap(AnswerRequest::getQuestionId, AnswerRequest::getAnswer));

        double totalScore = 0.0;
        double maxScore = 0.0;

        for (Question question : questions) {
            maxScore += question.getMarks();

            String submitted = submittedAnswers.get(question.getQuestionId());
            if (submitted != null && submitted.trim().equalsIgnoreCase(question.getCorrectAnswer().trim())) {
                totalScore += question.getMarks();
            }
        }

        double requiredScore = (quiz.getPassingScore() / 100.0) * maxScore;
        boolean passed = totalScore >= requiredScore;

        String answersJson;
        try {
            answersJson = objectMapper.writeValueAsString(submittedAnswers);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize answers");
        }

        LocalDateTime now = LocalDateTime.now();

        Attempt attempt = Attempt.builder()
                .quizId(quizId)
                .studentId(request.getStudentId())
                .score(totalScore)
                .passed(passed)
                .startedAt(now)
                .submittedAt(now)
                .answersJson(answersJson)
                .build();

        Attempt savedAttempt = attemptRepository.save(attempt);

        notificationEventPublisher.publish(NotificationEvent.builder()
                .eventType("QUIZ_SUBMITTED")
                .userId(request.getStudentId().toString())
                .title("Quiz Attempt Recorded")
                .message("Your quiz score: " + totalScore + ". " + (passed ? "Congratulations, you passed!" : "Keep trying!"))
                .build());

        return mapToAttemptResponse(savedAttempt);
    }

    @Override
    public List<AttemptResponse> getAttemptsByStudent(UUID studentId) {
        return attemptRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToAttemptResponse)
                .toList();
    }

    @Override
    public Double getBestScore(UUID studentId, UUID quizId) {
        return attemptRepository.findTopByStudentIdAndQuizIdOrderByScoreDesc(studentId, quizId)
                .map(Attempt::getScore)
                .orElse(0.0);
    }

    @Override
    public QuizResponse updateQuiz(UUID quizId, QuizRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        quiz.setCourseId(request.getCourseId());
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTimeLimitMinutes(request.getTimeLimitMinutes());
        quiz.setPassingScore(request.getPassingScore());
        quiz.setMaxAttempts(request.getMaxAttempts());

        return mapToQuizResponse(quizRepository.save(quiz));
    }

    @Override
    public void deleteQuiz(UUID quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new RuntimeException("Quiz not found");
        }

        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId);
        questionRepository.deleteAll(questions);
        quizRepository.deleteById(quizId);
    }

    @Override
    public QuizResponse publishQuiz(UUID quizId, Boolean published) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        quiz.setIsPublished(published);
        return mapToQuizResponse(quizRepository.save(quiz));
    }

    private QuizResponse mapToQuizResponse(Quiz quiz) {
        List<QuestionResponse> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(quiz.getQuizId())
                .stream()
                .map(this::mapToQuestionResponse)
                .toList();

        return QuizResponse.builder()
                .quizId(quiz.getQuizId())
                .courseId(quiz.getCourseId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .passingScore(quiz.getPassingScore())
                .maxAttempts(quiz.getMaxAttempts())
                .isPublished(quiz.getIsPublished())
                .questions(questions)
                .build();
    }

    private QuestionResponse mapToQuestionResponse(Question question) {
        return QuestionResponse.builder()
                .questionId(question.getQuestionId())
                .quizId(question.getQuizId())
                .text(question.getText())
                .type(question.getType())
                .optionA(question.getOptionA())
                .optionB(question.getOptionB())
                .optionC(question.getOptionC())
                .optionD(question.getOptionD())
                .marks(question.getMarks())
                .orderIndex(question.getOrderIndex())
                .build();
    }

    private AttemptResponse mapToAttemptResponse(Attempt attempt) {
        return AttemptResponse.builder()
                .attemptId(attempt.getAttemptId())
                .quizId(attempt.getQuizId())
                .studentId(attempt.getStudentId())
                .score(attempt.getScore())
                .passed(attempt.getPassed())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .build();
    }
}