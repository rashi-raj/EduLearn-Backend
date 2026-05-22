package com.edulearn.enrollment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EnrollmentRequest {

    @NotNull
    private UUID studentId;

    @NotNull
    private UUID courseId;

    private String studentName;
    private String studentEmail;
}