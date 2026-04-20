package com.edulearn.course.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CourseRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000)
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 100)
    private String category;

    @NotBlank(message = "Level is required")
    @Size(max = 50)
    private String level;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    private BigDecimal price;

    @NotNull(message = "Instructor ID is required")
    private UUID instructorId;

    @Size(max = 500)
    private String thumbnailUrl;

    @PositiveOrZero(message = "Total duration must be non-negative")
    private Integer totalDuration;

    @NotBlank(message = "Language is required")
    @Size(max = 50)
    private String language;

    @NotNull(message = "Course validity is required")
    @Min(value = 1, message = "Validity must be at least 1 month")
    @Max(value = 60, message = "Validity cannot be more than 60 months")
    private Integer validityInMonths;
}