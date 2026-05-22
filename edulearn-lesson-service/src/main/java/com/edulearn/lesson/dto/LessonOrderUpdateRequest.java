package com.edulearn.lesson.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LessonOrderUpdateRequest {

    @NotNull
    private UUID lessonId;

    @NotNull
    private Integer orderIndex;
}