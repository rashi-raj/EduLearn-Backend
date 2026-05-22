package com.edulearn.course.repository;

import com.edulearn.course.entity.Course;
import com.edulearn.course.entity.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    List<Course> findByCategoryIgnoreCase(String category);

    List<Course> findByInstructorId(UUID instructorId);

    List<Course> findByLevelIgnoreCase(String level);

    List<Course> findByIsPublished(Boolean isPublished);

    List<Course> findByPriceLessThanEqual(BigDecimal price);

    List<Course> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);

    List<Course> findByStatus(CourseStatus status);

    List<Course> findByIsPublishedTrueAndStatus(CourseStatus status);
}