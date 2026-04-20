package com.edulearn.course.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CourseResponse {

    private UUID courseId;
    private String title;
    private String description;
    private String category;
    private String level;
    private BigDecimal price;
    private UUID instructorId;
    private String thumbnailUrl;
    private Integer totalDuration;
    private Boolean isPublished;
    private String status;
    private String language;
    private Integer validityInMonths;
    private LocalDateTime createdAt;
}