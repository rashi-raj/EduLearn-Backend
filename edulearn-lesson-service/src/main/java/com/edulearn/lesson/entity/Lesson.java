package com.edulearn.lesson.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID lessonId;

    @Column(nullable = false)
    private UUID courseId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 50)
    private String contentType;

    @Column(nullable = false, length = 1000)
    private String contentUrl;

    @Column
    private Integer durationMinutes;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Boolean isPreview;
}