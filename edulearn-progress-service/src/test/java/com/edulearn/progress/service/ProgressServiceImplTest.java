package com.edulearn.progress.service;

import com.edulearn.progress.dto.ProgressResponse;
import com.edulearn.progress.entity.LessonProgress;
import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.event.NotificationEventPublisher;
import com.edulearn.progress.repository.CertificateRepository;
import com.edulearn.progress.repository.LessonProgressRepository;
import com.edulearn.progress.repository.ProgressRepository;
import com.edulearn.progress.service.impl.ProgressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceImplTest {

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private LessonProgressRepository lessonProgressRepository;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    private ProgressServiceImpl progressService;

    private UUID studentId;
    private UUID courseId;
    private UUID lessonId;

    @BeforeEach
    void setUp() {
        progressService = new ProgressServiceImpl(
                progressRepository,
                lessonProgressRepository,
                null, certificateRepository,
                restTemplate,
                notificationEventPublisher
        );

        studentId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
    }

    @Test
    void completeLesson_shouldSaveProgress_whenNotAlreadyCompleted() {
        when(lessonProgressRepository.existsByStudentIdAndLessonId(studentId, lessonId))
                .thenReturn(false);

        when(lessonProgressRepository.save(any(LessonProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(lessonProgressRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(List.of(buildCompletedLessonProgress()));

        when(progressRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.empty());

        when(progressRepository.save(any(Progress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        progressService.completeLesson(studentId, courseId, lessonId);

        verify(lessonProgressRepository).save(any(LessonProgress.class));
        verify(progressRepository).save(any(Progress.class));
    }

    @Test
    void completeLesson_shouldSkip_whenAlreadyCompleted() {
        when(lessonProgressRepository.existsByStudentIdAndLessonId(studentId, lessonId))
                .thenReturn(true);

        progressService.completeLesson(studentId, courseId, lessonId);

        verify(lessonProgressRepository, never()).save(any(LessonProgress.class));
        verify(progressRepository, never()).save(any(Progress.class));
    }

    @Test
    void getProgress_shouldReturnExistingProgress() {
        Progress progress = Progress.builder()
                .progressId(UUID.randomUUID())
                .studentId(studentId)
                .courseId(courseId)
                .completedLessons(5)
                .totalLessons(10)
                .progressPercent(50.0)
                .completed(false)
                .build();

        List<LessonProgress> completedLessons = List.of(
                buildCompletedLessonProgress(),
                buildCompletedLessonProgress(),
                buildCompletedLessonProgress(),
                buildCompletedLessonProgress(),
                buildCompletedLessonProgress()
        );

        when(progressRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.of(progress));

        when(lessonProgressRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(completedLessons);

        when(progressRepository.save(any(Progress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProgressResponse response = progressService.getProgress(studentId, courseId);

        assertNotNull(response);
        assertEquals(studentId, response.getStudentId());
        assertEquals(courseId, response.getCourseId());
        assertEquals(5, response.getCompletedLessonIds().size());

        verify(progressRepository).save(any(Progress.class));
    }

    private LessonProgress buildCompletedLessonProgress() {
        return LessonProgress.builder()
                .lessonProgressId(UUID.randomUUID())
                .studentId(studentId)
                .courseId(courseId)
                .lessonId(UUID.randomUUID())
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();
    }
}