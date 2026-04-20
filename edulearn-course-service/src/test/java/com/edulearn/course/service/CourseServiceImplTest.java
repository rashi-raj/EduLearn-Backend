package com.edulearn.course.service;

import com.edulearn.course.dto.CourseRequest;
import com.edulearn.course.dto.CourseResponse;
import com.edulearn.course.entity.Course;
import com.edulearn.course.entity.CourseStatus;
import com.edulearn.course.repository.CourseRepository;
import com.edulearn.course.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Test
    void createCourse_shouldSaveCourseSuccessfully() {
        CourseRequest request = new CourseRequest();
        request.setTitle("Spring Boot");
        request.setDescription("Learn Spring Boot");
        request.setCategory("Backend");
        request.setLevel("Beginner");
        request.setPrice(BigDecimal.valueOf(999.0));
        request.setInstructorId(UUID.randomUUID());
        request.setThumbnailUrl("thumb.jpg");
        request.setTotalDuration(10);
        request.setLanguage("English");
        request.setValidityInMonths(6);

        Course savedCourse = Course.builder()
                .courseId(UUID.randomUUID())
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .level(request.getLevel())
                .price(request.getPrice())
                .instructorId(request.getInstructorId())
                .thumbnailUrl(request.getThumbnailUrl())
                .totalDuration(request.getTotalDuration())
                .language(request.getLanguage())
                .validityInMonths(request.getValidityInMonths())
                .status(CourseStatus.DRAFT)
                .isPublished(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);

        CourseResponse response = courseService.createCourse(request);

        assertNotNull(response);
        assertEquals("Spring Boot", response.getTitle());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void getCourseById_shouldReturnCourse() {
        UUID courseId = UUID.randomUUID();

        Course course = Course.builder()
                .courseId(courseId)
                .title("Java")
                .description("Java Course")
                .category("Programming")
                .level("Intermediate")
                .price(BigDecimal.valueOf(499.0))
                .instructorId(UUID.randomUUID())
                .thumbnailUrl("java.jpg")
                .totalDuration(8)
                .language("English")
                .validityInMonths(12)
                .status(CourseStatus.DRAFT)
                .isPublished(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        CourseResponse response = courseService.getCourseById(courseId);

        assertNotNull(response);
        assertEquals(courseId, response.getCourseId());
        assertEquals("Java", response.getTitle());
    }

    @Test
    void getCourseById_shouldThrowExceptionWhenNotFound() {
        UUID courseId = UUID.randomUUID();

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.getCourseById(courseId));

        assertEquals("Course not found", ex.getMessage());
    }

    @Test
    void getAllCourses_shouldReturnList() {
        Course course = Course.builder()
                .courseId(UUID.randomUUID())
                .title("DSA")
                .description("DSA Course")
                .category("Programming")
                .level("Advanced")
                .price(BigDecimal.valueOf(799.0))
                .instructorId(UUID.randomUUID())
                .thumbnailUrl("dsa.jpg")
                .totalDuration(15)
                .language("English")
                .validityInMonths(12)
                .status(CourseStatus.PUBLISHED)
                .isPublished(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(courseRepository.findAll()).thenReturn(List.of(course));

        List<CourseResponse> response = courseService.getAllCourses();

        assertEquals(1, response.size());
        assertEquals("DSA", response.get(0).getTitle());
    }
}