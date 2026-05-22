package com.edulearn.assessment.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class QuizRequest {

    @NotNull
    private UUID courseId;

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String description;

    @NotNull
    @Positive
    private Integer timeLimitMinutes;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private Double passingScore;

    @NotNull
    @Positive
    private Integer maxAttempts;
}