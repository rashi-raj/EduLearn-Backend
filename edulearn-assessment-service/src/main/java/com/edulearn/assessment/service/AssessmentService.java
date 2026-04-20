package com.edulearn.assessment.service;

import com.edulearn.assessment.dto.*;

import java.util.List;
import java.util.UUID;

public interface AssessmentService {

    QuizResponse createQuiz(QuizRequest request);

    QuestionResponse addQuestion(UUID quizId, QuestionRequest request);

    List<QuizResponse> getQuizzesByCourse(UUID courseId);

    QuizResponse getQuizById(UUID quizId);

    AttemptResponse submitAttempt(UUID quizId, AttemptRequest request);

    List<AttemptResponse> getAttemptsByStudent(UUID studentId);

    Double getBestScore(UUID studentId, UUID quizId);

    QuizResponse updateQuiz(UUID quizId, QuizRequest request);

    void deleteQuiz(UUID quizId);

    QuizResponse publishQuiz(UUID quizId, Boolean published);
}