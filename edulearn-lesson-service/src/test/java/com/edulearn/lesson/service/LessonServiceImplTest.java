package com.edulearn.lesson.service;

import com.edulearn.lesson.dto.LessonRequest;
import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.repository.LessonRepository;
import com.edulearn.lesson.repository.ResourceRepository;
import com.edulearn.lesson.service.impl.LessonServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceImplTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private LessonServiceImpl lessonService;

    @Test
    void createLesson_shouldWork() {
        LessonRequest request = new LessonRequest();
        request.setTitle("Intro");
        request.setCourseId(UUID.randomUUID());

        Lesson lesson = Lesson.builder()
                .lessonId(UUID.randomUUID())
                .title("Intro")
                .build();

        when(lessonRepository.save(any())).thenReturn(lesson);

        var response = lessonService.addLesson(request);

        assertNotNull(response);
        verify(lessonRepository, times(1)).save(any());
    }
}