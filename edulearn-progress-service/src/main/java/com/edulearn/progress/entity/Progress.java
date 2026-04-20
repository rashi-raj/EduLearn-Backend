package com.edulearn.progress.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID progressId;

    private UUID studentId;

    private UUID courseId;

    @Builder.Default
    private Integer completedLessons = 0;

    @Builder.Default
    private Integer totalLessons = 0;

    @Builder.Default
    private Double progressPercent = 0.0;

    @Builder.Default
    private Boolean completed = false;

    private LocalDateTime completedAt;
}