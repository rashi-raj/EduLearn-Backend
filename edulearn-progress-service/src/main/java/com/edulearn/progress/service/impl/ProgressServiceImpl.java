package com.edulearn.progress.service.impl;

import com.edulearn.progress.dto.CertificateResponse;
import com.edulearn.progress.dto.ProgressResponse;
import com.edulearn.progress.entity.Certificate;
import com.edulearn.progress.entity.LessonProgress;
import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.repository.CertificateRepository;
import com.edulearn.progress.repository.LessonProgressRepository;
import com.edulearn.progress.repository.ProgressRepository;
import com.edulearn.progress.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final ProgressRepository progressRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CertificateRepository certificateRepository;
    private final RestTemplate restTemplate;

    @Override
    public void completeLesson(UUID studentId, UUID courseId, UUID lessonId) {

        // prevent duplicate completion for same student + course + lesson
        if (lessonProgressRepository.existsByStudentIdAndLessonId(studentId, lessonId)) {
            return;
        }

        LessonProgress lessonProgress = LessonProgress.builder()
                .studentId(studentId)
                .courseId(courseId)
                .lessonId(lessonId)
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();

        lessonProgressRepository.save(lessonProgress);

        updateAndSaveProgress(studentId, courseId);
    }

    @Override
    public ProgressResponse getProgress(UUID studentId, UUID courseId) {

        Progress progress = progressRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElseGet(() -> {
                    Progress newProgress = Progress.builder()
                            .studentId(studentId)
                            .courseId(courseId)
                            .completedLessons(0)
                            .totalLessons(fetchTotalLessons(courseId))
                            .progressPercent(0.0)
                            .completed(false)
                            .build();

                    return progressRepository.save(newProgress);
                });

        // keep total lessons synced with lesson-service
        int latestTotalLessons = fetchTotalLessons(courseId);
        int completedLessons = (int) lessonProgressRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .size();

        double progressPercent = calculateProgressPercent(completedLessons, latestTotalLessons);
        boolean lessonCompleted = (latestTotalLessons > 0 && completedLessons >= latestTotalLessons) || (latestTotalLessons == 0);
        boolean enrollmentCompleted = checkEnrollmentCompletion(studentId, courseId);
        
        boolean completed = lessonCompleted || enrollmentCompleted;

        progress.setCompletedLessons(completedLessons);
        progress.setTotalLessons(latestTotalLessons);
        progress.setProgressPercent(enrollmentCompleted ? 100.0 : progressPercent);
        progress.setCompleted(completed);

        if (completed) {
            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(LocalDateTime.now());
            }
            generateCertificate(studentId, courseId);
        }

        progressRepository.save(progress);

        return ProgressResponse.builder()
                .studentId(progress.getStudentId())
                .courseId(progress.getCourseId())
                .progressPercent(progress.getProgressPercent())
                .completedLessons(progress.getCompletedLessons())
                .totalLessons(progress.getTotalLessons())
                .build();
    }

    @Override
    public CertificateResponse getCertificate(UUID studentId, UUID courseId) {

        // Auto-sync progress to ensure certificate is generated if completed
        getProgress(studentId, courseId);

        Certificate certificate = certificateRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        return CertificateResponse.builder()
                .certificateId(certificate.getCertificateId())
                .studentId(certificate.getStudentId())
                .courseId(certificate.getCourseId())
                .issuedAt(certificate.getIssuedAt())
                .certificateUrl(certificate.getCertificateUrl())
                .build();
    }

    private void updateAndSaveProgress(UUID studentId, UUID courseId) {

        List<LessonProgress> lessons =
                lessonProgressRepository.findByStudentIdAndCourseId(studentId, courseId);

        int completedLessons = lessons.size();
        int totalLessons = fetchTotalLessons(courseId);

        double progressPercent = calculateProgressPercent(completedLessons, totalLessons);
        boolean completed = totalLessons > 0 && completedLessons >= totalLessons;

        Progress progress = progressRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElse(
                        Progress.builder()
                                .studentId(studentId)
                                .courseId(courseId)
                                .completedLessons(0)
                                .totalLessons(0)
                                .progressPercent(0.0)
                                .completed(false)
                                .build()
                );

        progress.setCompletedLessons(completedLessons);
        progress.setTotalLessons(totalLessons);
        progress.setProgressPercent(progressPercent);
        progress.setCompleted(completed);

        if (completed) {
            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(LocalDateTime.now());
            }
            generateCertificate(studentId, courseId);
        }

        progressRepository.save(progress);
    }

    private double calculateProgressPercent(int completedLessons, int totalLessons) {
        if (totalLessons <= 0) {
            return 0.0;
        }
        return Math.round((completedLessons * 100.0) / totalLessons);
    }

    private int fetchTotalLessons(UUID courseId) {
        try {
            String url = "http://EDULEARN-LESSON-SERVICE/api/v1/lessons/course/" + courseId;

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> lessons = response.getBody();
            return lessons != null ? lessons.size() : 0;

        } catch (Exception e) {
            System.err.println("Failed to fetch total lessons: " + e.getMessage());
            return 0;
        }
    }

    private boolean checkEnrollmentCompletion(UUID studentId, UUID courseId) {
        try {
            String url = "http://EDULEARN-ENROLLMENT-SERVICE/api/v1/enrollments/student/" + studentId;
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> enrollments = response.getBody();
            if (enrollments != null) {
                for (Map<String, Object> e : enrollments) {
                    if (courseId.toString().equals(e.get("courseId"))) {
                        Object status = e.get("status");
                        Object progress = e.get("progressPercent");
                        if ("COMPLETED".equals(status)) return true;
                        if (progress instanceof Number && ((Number) progress).doubleValue() >= 100.0) return true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch enrollment completion status: " + e.getMessage());
        }
        return false;
    }

    private void generateCertificate(UUID studentId, UUID courseId) {

        Optional<Certificate> existingCertificate =
                certificateRepository.findByStudentIdAndCourseId(studentId, courseId);

        if (existingCertificate.isPresent()) {
            return;
        }

        Certificate certificate = Certificate.builder()
                .studentId(studentId)
                .courseId(courseId)
                .issuedAt(LocalDateTime.now())
                .certificateUrl("/certificate/view/" + studentId + "/" + courseId)
                .build();

        certificateRepository.save(certificate);
    }
}