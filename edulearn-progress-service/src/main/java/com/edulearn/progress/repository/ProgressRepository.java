package com.edulearn.progress.repository;

import com.edulearn.progress.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProgressRepository extends JpaRepository<Progress, UUID> {

    Optional<Progress> findByStudentIdAndCourseId(UUID studentId, UUID courseId);
}