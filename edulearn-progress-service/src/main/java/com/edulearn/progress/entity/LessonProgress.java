package com.edulearn.progress.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lesson_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID lessonProgressId;

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private UUID courseId;

    @Column(nullable = false)
    private UUID lessonId;

    @Column(nullable = false)
    private Boolean completed;

    @Column
    private LocalDateTime completedAt;
}