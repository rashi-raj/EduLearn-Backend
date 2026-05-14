package com.edulearn.lesson.service;

import com.edulearn.lesson.dto.*;
import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.entity.Resource;
import com.edulearn.lesson.repository.LessonRepository;
import com.edulearn.lesson.repository.ResourceRepository;
import com.edulearn.lesson.service.impl.LessonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LessonServiceImpl Unit Tests")
class LessonServiceImplTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private LessonServiceImpl lessonService;

    private UUID courseId;
    private UUID lessonId;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
        lesson = Lesson.builder()
                .lessonId(lessonId)
                .courseId(courseId)
                .title("Test Lesson")
                .orderIndex(1)
                .build();
    }

    @Test
    void addLesson_shouldWork() {
        LessonRequest request = new LessonRequest();
        request.setTitle("Intro");
        request.setCourseId(courseId);

        when(lessonRepository.save(any())).thenReturn(lesson);

        LessonResponse response = lessonService.addLesson(request);

        assertNotNull(response);
        verify(lessonRepository).save(any());
    }

    @Test
    void getLessonsByCourse_shouldReturnSortedList() {
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId)).thenReturn(List.of(lesson));

        List<LessonResponse> responses = lessonService.getLessonsByCourse(courseId);

        assertEquals(1, responses.size());
        assertEquals(lessonId, responses.get(0).getLessonId());
    }

    @Test
    void getLessonById_shouldReturnLesson_whenExists() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        LessonResponse response = lessonService.getLessonById(lessonId);

        assertNotNull(response);
        assertEquals(lessonId, response.getLessonId());
    }

    @Test
    void getLessonById_shouldThrowException_whenNotFound() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> lessonService.getLessonById(lessonId));
    }

    @Test
    void updateLesson_shouldWork() {
        LessonRequest request = new LessonRequest();
        request.setTitle("Updated Title");

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(any())).thenReturn(lesson);

        LessonResponse response = lessonService.updateLesson(lessonId, request);

        assertNotNull(response);
        verify(lessonRepository).save(any());
    }

    @Test
    void deleteLesson_shouldWork() {
        when(lessonRepository.existsById(lessonId)).thenReturn(true);

        lessonService.deleteLesson(lessonId);

        verify(lessonRepository).deleteById(lessonId);
    }

    @Test
    void deleteLesson_shouldThrowException_whenNotFound() {
        when(lessonRepository.existsById(lessonId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> lessonService.deleteLesson(lessonId));
    }

    @Test
    void reorderLessons_shouldWork() {
        LessonOrderUpdateRequest request = new LessonOrderUpdateRequest();
        request.setLessonId(lessonId);
        request.setOrderIndex(5);

        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId)).thenReturn(List.of(lesson));
        when(lessonRepository.saveAll(any())).thenReturn(List.of(lesson));

        List<LessonResponse> responses = lessonService.reorderLessons(courseId, List.of(request));

        assertNotNull(responses);
        verify(lessonRepository).saveAll(any());
    }

    @Test
    void addResource_shouldWork() {
        ResourceRequest request = new ResourceRequest();
        request.setName("PDF");
        
        Resource resource = Resource.builder().resourceId(UUID.randomUUID()).lessonId(lessonId).name("PDF").build();

        when(lessonRepository.existsById(lessonId)).thenReturn(true);
        when(resourceRepository.save(any())).thenReturn(resource);

        ResourceResponse response = lessonService.addResource(lessonId, request);

        assertNotNull(response);
        assertEquals("PDF", response.getName());
    }

    @Test
    void addResource_shouldThrowException_whenLessonNotFound() {
        when(lessonRepository.existsById(lessonId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> lessonService.addResource(lessonId, new ResourceRequest()));
    }

    @Test
    void removeResource_shouldWork() {
        UUID resourceId = UUID.randomUUID();
        when(resourceRepository.existsById(resourceId)).thenReturn(true);

        lessonService.removeResource(resourceId);

        verify(resourceRepository).deleteById(resourceId);
    }

    @Test
    void removeResource_shouldThrowException_whenNotFound() {
        UUID resourceId = UUID.randomUUID();
        when(resourceRepository.existsById(resourceId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> lessonService.removeResource(resourceId));
    }

    @Test
    void getPreviewLessons_shouldReturnLimitedList() {
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId)).thenReturn(List.of(lesson));

        List<LessonResponse> responses = lessonService.getPreviewLessons(courseId);

        assertTrue(responses.size() <= 3);
    }
}