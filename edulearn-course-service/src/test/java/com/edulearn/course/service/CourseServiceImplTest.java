package com.edulearn.course.service;

import com.edulearn.course.dto.CourseRequest;
import com.edulearn.course.dto.CourseResponse;
import com.edulearn.course.entity.Course;
import com.edulearn.course.entity.CourseStatus;
import com.edulearn.course.event.NotificationEventPublisher;
import com.edulearn.course.repository.CourseRepository;
import com.edulearn.course.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.DisplayName;
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

    @Mock
    private NotificationEventPublisher notificationEventPublisher;

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
    @Test
    void getCoursesByCategory_shouldReturnList() {
        when(courseRepository.findByCategoryIgnoreCase("Backend")).thenReturn(List.of(Course.builder().status(CourseStatus.DRAFT).build()));
        List<CourseResponse> response = courseService.getCoursesByCategory("Backend");
        assertEquals(1, response.size());
    }

    @Test
    void getCoursesByInstructor_shouldReturnList() {
        UUID instructorId = UUID.randomUUID();
        when(courseRepository.findByInstructorId(instructorId)).thenReturn(List.of(Course.builder().status(CourseStatus.DRAFT).build()));
        List<CourseResponse> response = courseService.getCoursesByInstructor(instructorId);
        assertEquals(1, response.size());
    }

    @Test
    void searchCourses_shouldReturnList() {
        when(courseRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("Spring", "Spring"))
                .thenReturn(List.of(Course.builder().status(CourseStatus.DRAFT).build()));
        List<CourseResponse> response = courseService.searchCourses("Spring");
        assertEquals(1, response.size());
    }

    @Test
    void updateCourse_shouldUpdateSuccessfully() {
        UUID courseId = UUID.randomUUID();
        Course course = Course.builder().courseId(courseId).status(CourseStatus.DRAFT).build();
        CourseRequest request = new CourseRequest();
        request.setTitle("Updated Title");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        CourseResponse response = courseService.updateCourse(courseId, request);

        assertEquals("Updated Title", response.getTitle());
    }

    @Test
    void updateCourse_rejectedCourse_shouldMoveToDraft() {
        UUID courseId = UUID.randomUUID();
        Course course = Course.builder().courseId(courseId).status(CourseStatus.REJECTED).build();
        CourseRequest request = new CourseRequest();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        CourseResponse response = courseService.updateCourse(courseId, request);

        assertEquals("DRAFT", response.getStatus());
        assertFalse(response.getIsPublished());
    }

    @Test
    void deleteCourse_shouldDeleteSuccessfully() {
        UUID courseId = UUID.randomUUID();
        when(courseRepository.existsById(courseId)).thenReturn(true);
        courseService.deleteCourse(courseId);
        verify(courseRepository).deleteById(courseId);
    }

    @Test
    void deleteCourse_shouldThrowExceptionWhenNotFound() {
        UUID courseId = UUID.randomUUID();
        when(courseRepository.existsById(courseId)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> courseService.deleteCourse(courseId));
    }

    @Test
    void publishCourse_toPublished_shouldWork() {
        UUID courseId = UUID.randomUUID();
        Course course = Course.builder().courseId(courseId).build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        CourseResponse response = courseService.publishCourse(courseId, true);

        assertTrue(response.getIsPublished());
        assertEquals("PUBLISHED", response.getStatus());
    }

    @Test
    void publishCourse_toDraft_shouldWork() {
        UUID courseId = UUID.randomUUID();
        Course course = Course.builder().courseId(courseId).build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        CourseResponse response = courseService.publishCourse(courseId, false);

        assertFalse(response.getIsPublished());
        assertEquals("DRAFT", response.getStatus());
    }

    @Test
    void submitCourseForApproval_shouldWork() {
        UUID courseId = UUID.randomUUID();
        Course course = Course.builder().courseId(courseId).status(CourseStatus.DRAFT).isPublished(false).build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        CourseResponse response = courseService.submitCourseForApproval(courseId);

        assertEquals("PENDING_APPROVAL", response.getStatus());
    }

    @Test
    void submitCourseForApproval_alreadyPublished_shouldThrow() {
        UUID courseId = UUID.randomUUID();
        Course course = Course.builder().courseId(courseId).isPublished(true).build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        assertThrows(RuntimeException.class, () -> courseService.submitCourseForApproval(courseId));
    }

    @Test
    void submitCourseForApproval_alreadyPending_shouldThrow() {
        UUID courseId = UUID.randomUUID();
        Course course = Course.builder().courseId(courseId).status(CourseStatus.PENDING_APPROVAL).isPublished(false).build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        assertThrows(RuntimeException.class, () -> courseService.submitCourseForApproval(courseId));
    }

    @Test
    void getCoursesByStatus_shouldWork() {
        when(courseRepository.findByStatus(CourseStatus.PUBLISHED)).thenReturn(List.of(Course.builder().status(CourseStatus.PUBLISHED).build()));
        List<CourseResponse> response = courseService.getCoursesByStatus("PUBLISHED");
        assertEquals(1, response.size());
    }

    @Test
    void getCoursesByStatus_invalidStatus_shouldThrow() {
        assertThrows(RuntimeException.class, () -> courseService.getCoursesByStatus("INVALID"));
    }

    @Test
    void reviewCourse_approve_shouldWork() {
        UUID courseId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();
        Course course = Course.builder().courseId(courseId).instructorId(instructorId).title("Test").build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.saveAndFlush(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        CourseResponse response = courseService.reviewCourse(courseId, "APPROVE");

        assertEquals("PUBLISHED", response.getStatus());
        assertTrue(response.getIsPublished());
        verify(notificationEventPublisher).publish(any());
    }

    @Test
    void reviewCourse_reject_shouldWork() {
        UUID courseId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();
        Course course = Course.builder().courseId(courseId).instructorId(instructorId).title("Test").build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.saveAndFlush(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        CourseResponse response = courseService.reviewCourse(courseId, "REJECT");

        assertEquals("REJECTED", response.getStatus());
        assertFalse(response.getIsPublished());
        verify(notificationEventPublisher).publish(any());
    }

    @Test
    void reviewCourse_kafkaFailure_shouldStillReturnResponse() {
        UUID courseId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();
        Course course = Course.builder().courseId(courseId).instructorId(instructorId).title("Test").build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.saveAndFlush(any(Course.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("Kafka down")).when(notificationEventPublisher).publish(any());

        CourseResponse response = courseService.reviewCourse(courseId, "APPROVE");

        assertNotNull(response);
        verify(notificationEventPublisher).publish(any());
    }

    @Test
    void getPublishedCourses_shouldWork() {
        when(courseRepository.findByIsPublishedTrueAndStatus(CourseStatus.PUBLISHED)).thenReturn(List.of(Course.builder().status(CourseStatus.PUBLISHED).build()));
        List<CourseResponse> response = courseService.getPublishedCourses();
        assertEquals(1, response.size());
    }
}