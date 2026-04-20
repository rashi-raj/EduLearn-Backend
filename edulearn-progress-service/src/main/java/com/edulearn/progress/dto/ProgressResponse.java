package com.edulearn.progress.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProgressResponse {
    private UUID studentId;
    private UUID courseId;
    private int completedLessons;
    private int totalLessons;
    private double progressPercent;
    private boolean certificateEligible;
}