package com.edulearn.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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