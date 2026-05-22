package com.edulearn.course.controller;

import com.edulearn.course.dto.CourseRequest;
import com.edulearn.course.dto.CourseResponse;
import com.edulearn.course.dto.PublishCourseRequest;
import com.edulearn.course.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@DisplayName("CourseController Unit Tests")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

    private CourseRequest createValidRequest() {
        CourseRequest request = new CourseRequest();
        request.setTitle("Valid Title");
        request.setDescription("Valid Description");
        request.setCategory("Backend");
        request.setLevel("Beginner");
        request.setPrice(BigDecimal.valueOf(100));
        request.setInstructorId(UUID.randomUUID());
        request.setLanguage("English");
        request.setValidityInMonths(6);
        return request;
    }

    @Test
    @DisplayName("POST /api/v1/courses: creates a course")
    void createCourse_shouldReturnCreated() throws Exception {
        CourseRequest request = createValidRequest();
        
        CourseResponse response = CourseResponse.builder().title("Valid Title").build();
        when(courseService.createCourse(any(CourseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Valid Title"));
    }

    @Test
    @DisplayName("GET /api/v1/courses: returns all courses")
    void getAllCourses_shouldReturnList() throws Exception {
        when(courseService.getAllCourses()).thenReturn(List.of(new CourseResponse()));

        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/courses/{id}: returns a course")
    void getCourseById_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(courseService.getCourseById(id)).thenReturn(new CourseResponse());

        mockMvc.perform(get("/api/v1/courses/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/courses/category/{category}: returns courses by category")
    void getCoursesByCategory_shouldReturnList() throws Exception {
        when(courseService.getCoursesByCategory("Backend")).thenReturn(List.of(new CourseResponse()));

        mockMvc.perform(get("/api/v1/courses/category/Backend"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/courses/instructor/{instructorId}: returns courses by instructor")
    void getCoursesByInstructor_shouldReturnList() throws Exception {
        UUID id = UUID.randomUUID();
        when(courseService.getCoursesByInstructor(id)).thenReturn(List.of(new CourseResponse()));

        mockMvc.perform(get("/api/v1/courses/instructor/{instructorId}", id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/courses/search: returns search results")
    void searchCourses_shouldReturnList() throws Exception {
        when(courseService.searchCourses("java")).thenReturn(List.of(new CourseResponse()));

        mockMvc.perform(get("/api/v1/courses/search").param("keyword", "java"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/courses/{id}: updates a course")
    void updateCourse_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        CourseRequest request = createValidRequest();
        when(courseService.updateCourse(eq(id), any(CourseRequest.class))).thenReturn(new CourseResponse());

        mockMvc.perform(put("/api/v1/courses/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/courses/{id}: deletes a course")
    void deleteCourse_shouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/courses/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/v1/courses/{id}/publish: publishes a course")
    void publishCourse_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        PublishCourseRequest request = new PublishCourseRequest();
        request.setPublished(true);
        when(courseService.publishCourse(id, true)).thenReturn(new CourseResponse());

        mockMvc.perform(patch("/api/v1/courses/{id}/publish", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/v1/courses/{id}/submit-for-approval: submits for approval")
    void submitCourseForApproval_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(courseService.submitCourseForApproval(id)).thenReturn(new CourseResponse());

        mockMvc.perform(patch("/api/v1/courses/{id}/submit-for-approval", id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/courses/status/{status}: returns courses by status")
    void getCoursesByStatus_shouldReturnList() throws Exception {
        when(courseService.getCoursesByStatus("PUBLISHED")).thenReturn(List.of(new CourseResponse()));

        mockMvc.perform(get("/api/v1/courses/status/PUBLISHED"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/courses/{id}/approve: approves a course")
    void approveCourse_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(courseService.reviewCourse(id, "APPROVE")).thenReturn(new CourseResponse());

        mockMvc.perform(put("/api/v1/courses/{id}/approve", id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/courses/{id}/reject: rejects a course")
    void rejectCourse_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(courseService.reviewCourse(id, "REJECT")).thenReturn(new CourseResponse());

        mockMvc.perform(put("/api/v1/courses/{id}/reject", id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/courses/published: returns published courses")
    void getPublishedCourses_shouldReturnList() throws Exception {
        when(courseService.getPublishedCourses()).thenReturn(List.of(new CourseResponse()));

        mockMvc.perform(get("/api/v1/courses/published"))
                .andExpect(status().isOk());
    }
}