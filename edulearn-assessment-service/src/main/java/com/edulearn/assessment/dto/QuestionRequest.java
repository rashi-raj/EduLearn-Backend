package com.edulearn.assessment.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionRequest {

    @NotBlank
    @Size(max = 2000)
    private String text;

    @NotBlank
    @Size(max = 50)
    private String type;

    @Size(max = 500)
    private String optionA;

    @Size(max = 500)
    private String optionB;

    @Size(max = 500)
    private String optionC;

    @Size(max = 500)
    private String optionD;

    @NotBlank
    @Size(max = 100)
    private String correctAnswer;

    @NotNull
    @Positive
    private Integer marks;

    @NotNull
    @PositiveOrZero
    private Integer orderIndex;
}