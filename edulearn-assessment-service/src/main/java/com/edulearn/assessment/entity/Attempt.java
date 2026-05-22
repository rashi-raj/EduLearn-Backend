package com.edulearn.assessment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID attemptId;

    @Column(nullable = false)
    private UUID quizId;

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private Double score;

    @Column(nullable = false)
    private Boolean passed;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column(length = 5000)
    private String answersJson;
}