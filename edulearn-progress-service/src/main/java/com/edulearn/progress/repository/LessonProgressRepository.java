package com.edulearn.progress.repository;

import com.edulearn.progress.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {

    List<LessonProgress> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    boolean existsByStudentIdAndLessonId(UUID studentId, UUID lessonId);
}