package com.edulearn.assessment.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class QuizResponse {

    private UUID quizId;
    private UUID courseId;
    private String title;
    private String description;
    private Integer timeLimitMinutes;
    private Double passingScore;
    private Integer maxAttempts;
    private Boolean isPublished;
    private List<QuestionResponse> questions;
}