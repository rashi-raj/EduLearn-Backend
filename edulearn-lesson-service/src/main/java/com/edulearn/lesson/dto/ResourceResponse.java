package com.edulearn.lesson.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponse {

    private UUID resourceId;
    private UUID lessonId;
    private String name;
    private String fileUrl;
    private String fileType;
    private Integer sizeKb;
}