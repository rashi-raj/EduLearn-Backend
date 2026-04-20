package com.edulearn.lesson.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotBlank
    @Size(max = 1000)
    private String fileUrl;

    @NotBlank
    @Size(max = 50)
    private String fileType;

    @PositiveOrZero
    private Integer sizeKb;
}