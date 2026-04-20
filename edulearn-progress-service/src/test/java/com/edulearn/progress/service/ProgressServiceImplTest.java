package com.edulearn.progress.service;

import com.edulearn.progress.dto.ProgressResponse;
import com.edulearn.progress.entity.LessonProgress;
import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.repository.CertificateRepository;
import com.edulearn.progress.repository.LessonProgressRepository;
import com.edulearn.progress.repository.ProgressRepository;
import com.edulearn.progress.service.impl.ProgressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
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

    @InjectMocks
    private ProgressServiceImpl progressService;

    private UUID studentId;
    private UUID courseId;
    private UUID lessonId;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
    }

    @Test
    void completeLesson_shouldSaveProgress_whenNotAlreadyCompleted() {
        when(lessonProgressRepository.existsByStudentIdAndLessonId(studentId, lessonId))
                .thenReturn(false);
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lessonProgressRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Collections.emptyList());
        when(progressRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.empty());
        when(progressRepository.save(any(Progress.class))).thenAnswer(inv -> inv.getArgument(0));

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

        when(progressRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.of(progress));
        when(lessonProgressRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Collections.nCopies(5, new LessonProgress()));
        when(progressRepository.save(any(Progress.class))).thenAnswer(inv -> inv.getArgument(0));

        ProgressResponse response = progressService.getProgress(studentId, courseId);

        assertNotNull(response);
        assertEquals(studentId, response.getStudentId());
        assertEquals(courseId, response.getCourseId());
    }
}
