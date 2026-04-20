package com.edulearn.course.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseApprovalActionRequest {

    @NotBlank(message = "Action is required")
    private String action; // APPROVE or REJECT
}