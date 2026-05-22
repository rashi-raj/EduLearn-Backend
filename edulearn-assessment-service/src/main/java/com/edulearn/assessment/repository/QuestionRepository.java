package com.edulearn.assessment.repository;

import com.edulearn.assessment.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByQuizIdOrderByOrderIndexAsc(UUID quizId);
}