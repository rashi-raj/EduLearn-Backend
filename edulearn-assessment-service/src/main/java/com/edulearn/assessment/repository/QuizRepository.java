package com.edulearn.assessment.repository;

import com.edulearn.assessment.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {

    List<Quiz> findByCourseId(UUID courseId);

    List<Quiz> findByIsPublished(Boolean isPublished);

    long countByCourseId(UUID courseId);
}