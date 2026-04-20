package com.edulearn.progress.repository;

import com.edulearn.progress.entity.CompletedLesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompletedLessonRepository extends JpaRepository<CompletedLesson, UUID> {

    boolean existsByStudentIdAndCourseIdAndLessonId(UUID studentId, UUID courseId, UUID lessonId);

    long countByStudentIdAndCourseId(UUID studentId, UUID courseId);

    List<CompletedLesson> findByStudentIdAndCourseId(UUID studentId, UUID courseId);
}