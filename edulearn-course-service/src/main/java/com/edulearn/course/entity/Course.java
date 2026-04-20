package com.edulearn.course.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID courseId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 50)
    private String level;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private UUID instructorId;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column
    private Integer totalDuration;

    @Column(nullable = false)
    private Boolean isPublished;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CourseStatus status;

    @Column(nullable = false, length = 50)
    private String language;

    @Column(nullable = false)
    private Integer validityInMonths;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}