package com.edulearn.lesson.repository;

import com.edulearn.lesson.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByCourseId(UUID courseId);

    List<Lesson> findByCourseIdOrderByOrderIndexAsc(UUID courseId);

    List<Lesson> findByContentTypeIgnoreCase(String contentType);

    List<Lesson> findByCourseIdAndIsPreviewTrueOrderByOrderIndexAsc(UUID courseId);

    long countByCourseId(UUID courseId);
}