package com.edulearn.assessment.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class QuestionResponse {

    private UUID questionId;
    private UUID quizId;
    private String text;
    private String type;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private Integer marks;
    private Integer orderIndex;
}