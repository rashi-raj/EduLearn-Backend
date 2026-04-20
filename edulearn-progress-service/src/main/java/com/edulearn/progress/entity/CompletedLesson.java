package com.edulearn.progress.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "completed_lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletedLesson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID studentId;
    private UUID courseId;
    private UUID lessonId;
    private LocalDateTime completedAt;
}