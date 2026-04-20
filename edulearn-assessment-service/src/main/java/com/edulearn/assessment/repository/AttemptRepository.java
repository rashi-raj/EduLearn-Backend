package com.edulearn.assessment.repository;

import com.edulearn.assessment.entity.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttemptRepository extends JpaRepository<Attempt, UUID> {

    List<Attempt> findByStudentId(UUID studentId);

    List<Attempt> findByQuizId(UUID quizId);

    List<Attempt> findByStudentIdAndQuizId(UUID studentId, UUID quizId);

    long countByStudentIdAndQuizId(UUID studentId, UUID quizId);

    Optional<Attempt> findTopByStudentIdAndQuizIdOrderByScoreDesc(UUID studentId, UUID quizId);
}