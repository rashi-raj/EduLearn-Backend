package com.edulearn.assessment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID questionId;

    @Column(nullable = false)
    private UUID quizId;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(nullable = false, length = 50)
    private String type; // MCQ / TRUE_FALSE

    @Column(length = 500)
    private String optionA;

    @Column(length = 500)
    private String optionB;

    @Column(length = 500)
    private String optionC;

    @Column(length = 500)
    private String optionD;

    @Column(nullable = false, length = 100)
    private String correctAnswer;

    @Column(nullable = false)
    private Integer marks;

    @Column(nullable = false)
    private Integer orderIndex;
}