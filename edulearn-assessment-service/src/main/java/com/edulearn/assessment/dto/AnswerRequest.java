package com.edulearn.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AnswerRequest {

    @NotNull
    private UUID questionId;

    @NotBlank
    private String answer;
}