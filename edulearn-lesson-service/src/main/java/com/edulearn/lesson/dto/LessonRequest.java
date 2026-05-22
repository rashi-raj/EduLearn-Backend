package com.edulearn.lesson.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LessonRequest {

    @NotNull
    private UUID courseId;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 50)
    private String contentType;

    @NotBlank
    @Size(max = 1000)
    private String contentUrl;

    @PositiveOrZero
    private Integer durationMinutes;

    @NotNull
    @PositiveOrZero
    private Integer orderIndex;

    @Size(max = 2000)
    private String description;

    @NotNull
    private Boolean isPreview;
}