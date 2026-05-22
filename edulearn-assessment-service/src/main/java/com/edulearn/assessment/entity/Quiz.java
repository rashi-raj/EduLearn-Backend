package com.edulearn.assessment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID quizId;

    @Column(nullable = false)
    private UUID courseId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Integer timeLimitMinutes;

    @Column(nullable = false)
    private Double passingScore;

    @Column(nullable = false)
    private Integer maxAttempts;

    @Column(nullable = false)
    private Boolean isPublished;
}