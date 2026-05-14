package com.edulearn.lesson.controller;

import com.edulearn.lesson.dto.*;
import com.edulearn.lesson.service.LessonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LessonController.class)
@DisplayName("LessonController Unit Tests")
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LessonService lessonService;

    @Autowired
    private ObjectMapper objectMapper;

    private LessonRequest createValidRequest() {
        LessonRequest request = new LessonRequest();
        request.setCourseId(UUID.randomUUID());
        request.setTitle("Intro");
        request.setContentType("VIDEO");
        request.setContentUrl("http://url.com");
        request.setOrderIndex(1);
        request.setIsPreview(true);
        return request;
    }

    @Test
    @DisplayName("POST /api/v1/lessons: creates a lesson")
    void addLesson_shouldReturnCreated() throws Exception {
        LessonRequest request = createValidRequest();
        LessonResponse response = LessonResponse.builder().lessonId(UUID.randomUUID()).title("Intro").build();

        when(lessonService.addLesson(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Intro"));
    }

    @Test
    @DisplayName("GET /api/v1/lessons/course/{id}: returns lessons")
    void getLessonsByCourse_shouldReturnList() throws Exception {
        UUID courseId = UUID.randomUUID();
        when(lessonService.getLessonsByCourse(courseId)).thenReturn(List.of(new LessonResponse()));

        mockMvc.perform(get("/api/v1/lessons/course/{courseId}", courseId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/lessons/{id}: returns lesson")
    void getLessonById_shouldReturnOk() throws Exception {
        UUID lessonId = UUID.randomUUID();
        when(lessonService.getLessonById(lessonId)).thenReturn(new LessonResponse());

        mockMvc.perform(get("/api/v1/lessons/{lessonId}", lessonId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/lessons/{id}: updates lesson")
    void updateLesson_shouldReturnOk() throws Exception {
        UUID lessonId = UUID.randomUUID();
        LessonRequest request = createValidRequest();
        when(lessonService.updateLesson(eq(lessonId), any())).thenReturn(new LessonResponse());

        mockMvc.perform(put("/api/v1/lessons/{lessonId}", lessonId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/lessons/{id}: deletes lesson")
    void deleteLesson_shouldReturnNoContent() throws Exception {
        UUID lessonId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/lessons/{lessonId}", lessonId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/v1/lessons/course/{id}/reorder: reorders lessons")
    void reorderLessons_shouldReturnOk() throws Exception {
        UUID courseId = UUID.randomUUID();
        LessonOrderUpdateRequest update = new LessonOrderUpdateRequest();
        update.setLessonId(UUID.randomUUID());
        update.setOrderIndex(2);

        when(lessonService.reorderLessons(eq(courseId), any())).thenReturn(List.of());

        mockMvc.perform(patch("/api/v1/lessons/course/{courseId}/reorder", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(update))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/lessons/{id}/resources: adds resource")
    void addResource_shouldReturnCreated() throws Exception {
        UUID lessonId = UUID.randomUUID();
        ResourceRequest request = new ResourceRequest();
        request.setName("PDF");
        request.setFileType("DOC");
        request.setFileUrl("http://file.com");

        when(lessonService.addResource(eq(lessonId), any())).thenReturn(new ResourceResponse());

        mockMvc.perform(post("/api/v1/lessons/{lessonId}/resources", lessonId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("DELETE /api/v1/lessons/resources/{id}: removes resource")
    void removeResource_shouldReturnNoContent() throws Exception {
        UUID resourceId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/lessons/resources/{resourceId}", resourceId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/lessons/course/{id}/preview: returns preview lessons")
    void getPreviewLessons_shouldReturnList() throws Exception {
        UUID courseId = UUID.randomUUID();
        when(lessonService.getPreviewLessons(courseId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/lessons/course/{courseId}/preview", courseId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/lessons/ping: health check")
    void ping_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("Lesson service is working"));
    }
}