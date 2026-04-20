package com.edulearn.lesson.controller;

import com.edulearn.lesson.dto.LessonResponse;
import com.edulearn.lesson.service.LessonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LessonControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LessonService lessonService;

    @InjectMocks
    private LessonController lessonController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(lessonController).build();
    }

    @Test
    void addLesson_shouldReturnCreated() throws Exception {
        LessonResponse response = LessonResponse.builder()
                .lessonId(UUID.randomUUID())
                .title("Intro")
                .description("Introduction lesson")
                .courseId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .orderIndex(1)
                .build();

        when(lessonService.addLesson(any())).thenReturn(response);

        String requestBody = """
                {
                  "courseId": "123e4567-e89b-12d3-a456-426614174000",
                  "title": "Intro",
                  "contentType": "VIDEO",
                  "contentUrl": "https://example.com/video-1",
                  "durationMinutes": 10,
                  "orderIndex": 1,
                  "description": "Introduction lesson",
                  "isPreview": true
                }
                """;

        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }
}