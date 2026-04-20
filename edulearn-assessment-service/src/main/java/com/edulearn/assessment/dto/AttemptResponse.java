package com.edulearn.assessment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AttemptResponse {

    private UUID attemptId;
    private UUID quizId;
    private UUID studentId;
    private Double score;
    private Boolean passed;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
}