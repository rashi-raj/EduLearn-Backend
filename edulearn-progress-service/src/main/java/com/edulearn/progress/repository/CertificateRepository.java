package com.edulearn.progress.repository;

import com.edulearn.progress.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {

    Optional<Certificate> findByStudentIdAndCourseId(UUID studentId, UUID courseId);
}