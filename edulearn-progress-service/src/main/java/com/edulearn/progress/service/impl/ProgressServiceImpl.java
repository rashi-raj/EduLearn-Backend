package com.edulearn.progress.service.impl;

import com.edulearn.progress.dto.CertificateResponse;
import com.edulearn.progress.dto.ProgressResponse;
import com.edulearn.progress.entity.Certificate;
import com.edulearn.progress.entity.CompletedLesson;
import com.edulearn.progress.entity.LessonProgress;
import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.event.NotificationEvent;
import com.edulearn.progress.event.NotificationEventPublisher;
import com.edulearn.progress.repository.CertificateRepository;
import com.edulearn.progress.repository.CompletedLessonRepository;
import com.edulearn.progress.repository.LessonProgressRepository;
import com.edulearn.progress.repository.ProgressRepository;
import com.edulearn.progress.service.ProgressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ProgressServiceImpl implements ProgressService {

    private final ProgressRepository progressRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CompletedLessonRepository completedLessonRepository;
    private final CertificateRepository certificateRepository;
    private final RestTemplate restTemplate;
    private final NotificationEventPublisher notificationEventPublisher;

    public ProgressServiceImpl(
            ProgressRepository progressRepository,
            LessonProgressRepository lessonProgressRepository,
            CompletedLessonRepository completedLessonRepository,
            CertificateRepository certificateRepository,
            RestTemplate restTemplate,
            NotificationEventPublisher notificationEventPublisher
    ) {
        this.progressRepository = progressRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.completedLessonRepository = completedLessonRepository;
        this.certificateRepository = certificateRepository;
        this.restTemplate = restTemplate;
        this.notificationEventPublisher = notificationEventPublisher;
    }

    @Override
    public void completeLesson(UUID studentId, UUID courseId, UUID lessonId) {
        log.info("Processing completeLesson for studentId={}, courseId={}, lessonId={}", studentId, courseId, lessonId);
        
        boolean existsInLP = lessonProgressRepository.existsByStudentIdAndLessonId(studentId, lessonId);
        
        if (!existsInLP) {
            LessonProgress lessonProgress = LessonProgress.builder()
                    .studentId(studentId)
                    .courseId(courseId)
                    .lessonId(lessonId)
                    .completed(true)
                    .completedAt(LocalDateTime.now())
                    .build();
            lessonProgressRepository.save(lessonProgress);
            log.debug("Saved to lesson_progress table");
        }

        // Also save to completed_lessons table for backward compatibility/auditing as requested
        boolean existsInCL = !completedLessonRepository.findByStudentIdAndCourseId(studentId, courseId)
                .stream().filter(cl -> cl.getLessonId().equals(lessonId)).toList().isEmpty();
        
        if (!existsInCL) {
            CompletedLesson completedLesson = CompletedLesson.builder()
                    .studentId(studentId)
                    .courseId(courseId)
                    .lessonId(lessonId)
                    .completedAt(LocalDateTime.now())
                    .build();
            completedLessonRepository.save(completedLesson);
            log.debug("Saved to completed_lessons table");
        }

        updateAndSaveProgress(studentId, courseId);
    }

    @Override
    public ProgressResponse getProgress(UUID studentId, UUID courseId) {
        Progress progress = progressRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElseGet(() -> {
                    int initialTotal = fetchTotalLessons(courseId);
                    Progress newProgress = Progress.builder()
                            .studentId(studentId)
                            .courseId(courseId)
                            .completedLessons(0)
                            .totalLessons(Math.max(0, initialTotal)) // Never store -1
                            .progressPercent(0.0)
                            .completed(false)
                            .build();

                    return progressRepository.save(newProgress);
                });

        int latestTotalLessons = fetchTotalLessons(courseId);

        List<LessonProgress> lessonProgressList =
                lessonProgressRepository.findByStudentIdAndCourseId(studentId, courseId);

        int completedLessons = lessonProgressList.size();

        // Only update total lessons if we actually got a valid number from the service
        if (latestTotalLessons > 0) {
            progress.setTotalLessons(latestTotalLessons);
        } else {
            latestTotalLessons = progress.getTotalLessons(); // Fallback to last known
            // If still 0 or negative (first-time or corrupted), use completedLessons as floor
            if (latestTotalLessons <= 0 && completedLessons > 0) {
                latestTotalLessons = completedLessons;
                progress.setTotalLessons(completedLessons);
                log.warn("totalLessons was <=0, using completedLessons={} as floor for studentId={}, courseId={}",
                        completedLessons, studentId, courseId);
            }
        }

        boolean lessonCompleted = latestTotalLessons > 0 && completedLessons >= latestTotalLessons;
        boolean enrollmentCompleted = checkEnrollmentCompletion(studentId, courseId);
        boolean completed = lessonCompleted || enrollmentCompleted;
        
        progress.setCompletedLessons(completedLessons);
        double calculatedProgress = enrollmentCompleted ? 100.0 : calculateProgressPercent(completedLessons, latestTotalLessons);
        
        // Prevent regression: if stored progress was higher and service is currently down, keep the higher value
        if (latestTotalLessons <= 0 && progress.getProgressPercent() > calculatedProgress) {
            calculatedProgress = progress.getProgressPercent();
            log.info("Keeping stored progress {}% as latest total lessons is unavailable", calculatedProgress);
        }
        
        progress.setProgressPercent(calculatedProgress);
        
        // Final completion check
        if (calculatedProgress >= 100.0) {
            completed = true;
        }
        
        progress.setCompleted(completed);

        if (completed) {
            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(LocalDateTime.now());
            }
            generateCertificate(studentId, courseId);
        }

        progressRepository.save(progress);
        
        // Sync with enrollment service
        syncProgressWithEnrollment(studentId, courseId, progress.getProgressPercent());

        java.util.Set<UUID> completedIds = lessonProgressList
                .stream()
                .filter(lp -> Boolean.TRUE.equals(lp.getCompleted()))
                .map(LessonProgress::getLessonId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        return ProgressResponse.builder()
                .studentId(progress.getStudentId())
                .courseId(progress.getCourseId())
                .progressPercent(progress.getProgressPercent())
                .completedLessons(progress.getCompletedLessons())
                .totalLessons(progress.getTotalLessons())
                .completedLessonIds(completedIds)
                .certificateEligible(completed)
                .build();
    }

    @Override
    public CertificateResponse getCertificate(UUID studentId, UUID courseId) {
        ProgressResponse p = getProgress(studentId, courseId);

        Optional<Certificate> certOpt = certificateRepository.findByStudentIdAndCourseId(studentId, courseId);
        
        if (certOpt.isEmpty()) {
            if (p.isCertificateEligible()) {
                log.info("Certificate missing for eligible student {} in course {}. Generating now...", studentId, courseId);
                generateCertificate(studentId, courseId);
                certOpt = certificateRepository.findByStudentIdAndCourseId(studentId, courseId);
            }
            
            if (certOpt.isEmpty()) {
                throw new RuntimeException("Certificate not found and student is not yet eligible.");
            }
        }

        Certificate certificate = certOpt.get();

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
        if (totalLessons > 0) {
            progress.setTotalLessons(totalLessons);
        }
        progress.setProgressPercent(progressPercent);
        progress.setCompleted(completed);

        if (completed) {
            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(LocalDateTime.now());
            }
            generateCertificate(studentId, courseId);
        }

        progressRepository.save(progress);
        syncProgressWithEnrollment(studentId, courseId, progressPercent);
    }

    private double calculateProgressPercent(int completedLessons, int totalLessons) {
        if (totalLessons <= 0) {
            return completedLessons > 0 ? 1.0 : 0.0; // Show at least 1% if some lessons are done
        }
        double percent = (completedLessons * 100.0) / totalLessons;
        return Math.min(100.0, Math.round(percent));
    }

    private int fetchTotalLessons(UUID courseId) {
        try {
            String url = "http://EDULEARN-LESSON-SERVICE/api/v1/lessons/course/" + courseId;

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    }
            );

            if (response == null || response.getBody() == null) {
                return -1;
            }

            return response.getBody().size();

        } catch (Exception e) {
            System.err.println("!!! PROGRESS SERVICE ERROR: Failed to fetch total lessons from Lesson Service: " + e.getMessage());
            return -1;
        }
    }

    private boolean checkEnrollmentCompletion(UUID studentId, UUID courseId) {
        try {
            String url = "http://EDULEARN-ENROLLMENT-SERVICE/api/v1/enrollments/student/" + studentId;

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    }
            );

            if (response == null || response.getBody() == null) {
                return false;
            }

            for (Map<String, Object> enrollment : response.getBody()) {
                if (courseId.toString().equals(enrollment.get("courseId"))) {
                    Object status = enrollment.get("status");
                    Object progress = enrollment.get("progressPercent");

                    if ("COMPLETED".equals(status)) {
                        return true;
                    }

                    if (progress instanceof Number && ((Number) progress).doubleValue() >= 100.0) {
                        return true;
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

        NotificationEvent event = NotificationEvent.builder()
                .eventType("CERTIFICATION")
                .userId(studentId.toString())
                .title("Certificate Earned! 🎓")
                .message("Congratulations! You have successfully earned your certificate for courseId: " + courseId)
                .build();

        notificationEventPublisher.publish(event);
    }

    private void syncProgressWithEnrollment(UUID studentId, UUID courseId, double progressPercent) {
        try {
            String progressUrl = String.format(
                "http://EDULEARN-ENROLLMENT-SERVICE/api/v1/enrollments/progress?studentId=%s&courseId=%s",
                studentId, courseId);

            Map<String, Double> body = Map.of("progressPercent", progressPercent);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<Map<String, Double>> entity =
                new org.springframework.http.HttpEntity<>(body, headers);

            restTemplate.exchange(progressUrl, HttpMethod.PATCH, entity, Void.class);
            log.info("Synced progress {}% to enrollment service for studentId={}, courseId={}", progressPercent, studentId, courseId);

            // If 100%, explicitly mark enrollment as COMPLETED via /complete endpoint
            if (progressPercent >= 100.0) {
                String completeUrl = String.format(
                    "http://EDULEARN-ENROLLMENT-SERVICE/api/v1/enrollments/complete?studentId=%s&courseId=%s",
                    studentId, courseId);
                restTemplate.exchange(completeUrl, HttpMethod.PATCH,
                    new org.springframework.http.HttpEntity<>(null, headers), Void.class);
                log.info("Marked enrollment as COMPLETED for studentId={}, courseId={}", studentId, courseId);
            }
        } catch (Exception e) {
            log.error("Failed to sync progress to enrollment service: {} - Cause: {}", e.getMessage(),
                e.getCause() != null ? e.getCause().getMessage() : "unknown");
        }
    }
}