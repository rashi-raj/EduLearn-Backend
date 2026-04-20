package com.edulearn.lesson.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ResourceResponse {

    private UUID resourceId;
    private UUID lessonId;
    private String name;
    private String fileUrl;
    private String fileType;
    private Integer sizeKb;
}