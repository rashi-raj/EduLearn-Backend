package com.edulearn.course.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublishCourseRequest {

    @NotNull(message = "Publish status is required")
    private Boolean published;
}