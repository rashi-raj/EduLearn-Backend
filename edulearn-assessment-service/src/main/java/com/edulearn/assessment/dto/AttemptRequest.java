package com.edulearn.assessment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AttemptRequest {

    @NotNull
    private UUID studentId;

    @NotNull
    private List<AnswerRequest> answers;
}