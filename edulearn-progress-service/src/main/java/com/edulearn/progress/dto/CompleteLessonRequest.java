package com.edulearn.progress.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CompleteLessonRequest {

    @NotNull
    private UUID studentId;

    @NotNull
    private UUID courseId;

    @NotNull
    private UUID lessonId;
}