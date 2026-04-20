package com.edulearn.course.controller;

import com.edulearn.course.dto.CourseResponse;
import com.edulearn.course.service.CourseService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(courseController).build();
    }

    @Test
    void createCourse_shouldReturnCreated() throws Exception {
        CourseResponse response = CourseResponse.builder()
                .courseId(UUID.randomUUID())
                .title("Spring Boot")
                .build();

        when(courseService.createCourse(any())).thenReturn(response);

        String requestBody = """
                {
                  "title": "Spring Boot",
                  "description": "Learn Spring Boot",
                  "category": "Backend",
                  "level": "Beginner",
                  "price": 999.0,
                  "instructorId": "123e4567-e89b-12d3-a456-426614174000",
                  "thumbnailUrl": "thumb.jpg",
                  "totalDuration": 10,
                  "language": "English",
                  "validityInMonths": 6
                }
                """;

        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void getCourseById_shouldReturnOk() throws Exception {
        UUID courseId = UUID.randomUUID();

        CourseResponse response = CourseResponse.builder()
                .courseId(courseId)
                .title("Java")
                .build();

        when(courseService.getCourseById(courseId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/courses/{courseId}", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java"));
    }
}