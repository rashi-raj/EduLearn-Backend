package com.edulearn.lesson.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class LessonResponse {

    private UUID lessonId;
    private UUID courseId;
    private String title;
    private String contentType;
    private String contentUrl;
    private Integer durationMinutes;
    private Integer orderIndex;
    private String description;
    private Boolean isPreview;
    private List<ResourceResponse> resources;
}